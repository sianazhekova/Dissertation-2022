package analyzers.baseline_analyzer;

import analyzers.readers.EventType;
import analyzers.readers.MemBufferBlock;
import jdk.jfr.Event;
import org.jetbrains.annotations.NotNull;

import javax.xml.crypto.Data;
import java.util.*;

public class LoopStack {

    protected Deque<LoopInstance> stack;
    public Map<Long, Map<DataDependence, LoopLevelSummary>> loopDependencies;
    public Set<Long> uniqueLoopIDsCache;
    private boolean throwStackExceptions;

    public LoopStack(boolean withExceptions) {
        stack = new ArrayDeque<>();
        loopDependencies = new HashMap<>();
        uniqueLoopIDsCache = new HashSet<>();
        throwStackExceptions = withExceptions;
    }

    public LoopStack() {
        stack = new ArrayDeque<>();
        loopDependencies = new HashMap<>();
        uniqueLoopIDsCache = new HashSet<>();
        throwStackExceptions = false;
    }

    // TODO: Maybe add sanity-checks for input count of loop starts and loop ends
    public void encounterNewAccess(@NotNull List<MemBufferBlock> bufferBlockPair, long tripCount) throws InvalidAdditionToEmptyLoopStackException, NullLoopInstanceException {
        assert(bufferBlockPair.size() == 2 || bufferBlockPair.size() == 1);
        MemBufferBlock firstBufferBlock = bufferBlockPair.get(0);

        EventType event = firstBufferBlock.getEvent();
        // This is the last memory buffer containing the instruction metadata about the memory access, in our selected trace file
        if (bufferBlockPair.size() == 1 && event != EventType.END) return;

        if (event == EventType.START) {
            // Newly encountered access is a start of a loop - either the first iteration of a new loop instance,
            // or the i-th iteration of the loop instance at the top of the loop stack
            long loopPCAddress = firstBufferBlock.getAddressPC();
            if (!isSeen(loopPCAddress)) {
                // Loop PC address has never been encountered before, or if it has, it has terminated and is not directly succeeding the previous iteration
                startOfLoop(loopPCAddress);
            }
        } else if (event == EventType.END) {
            if (bufferBlockPair.size() == 2) {
                long topLoopID = stack.peek().getLoopID();

                MemBufferBlock secondBufferBlock = bufferBlockPair.get(1);
                EventType eventAfter = secondBufferBlock.getEvent();
                long loopSuccessorID = secondBufferBlock.getAddressPC();

                if (eventAfter == EventType.START && topLoopID == loopSuccessorID) {  // isSeen(loopSuccessorID)
                    // This means the current loop has another iteration at least so only the loop-iteration-end stage should be invoked
                    endOfLoopIteration(topLoopID);
                    return;
                }
                // The END signal at this code point signifies the end of the iteration of the top-most (current) loop instance, as well as its termination
                endOfLoopIteration(topLoopID);
                loopTermination(topLoopID);

            } else {
                assert(bufferBlockPair.size() == 1);
                assert(!isStackEmpty());

                while (!stack.isEmpty()) {
                    LoopInstance topLoop = stack.peek();
                    long topLoopID = topLoop.getLoopID();
                    endOfLoopIteration(topLoopID);
                    loopTermination(topLoopID);
                }
                return;
            }
        } else if (event == EventType.LOAD || event == EventType.STORE) {
            long newPCAddress = firstBufferBlock.getAddressPC();
            long newApproxRefAddress = firstBufferBlock.getAddressRef();
            long newAccessSize = firstBufferBlock.getSizeOfAccess();
            MemoryAccess newLoadOrStore = firstBufferBlock.isAStore() ? MemoryAccess.WRITE : MemoryAccess.READ;
            if (stack.size() == 0) {
                if (throwStackExceptions) {
                    throw new InvalidAdditionToEmptyLoopStackException(String.format("Tried to add a load/store at trip count %d, at an approximate memory address %d that is a %s",
                            tripCount,
                            newApproxRefAddress,
                            newLoadOrStore.name()),
                            tripCount,
                            newApproxRefAddress,
                            newLoadOrStore);
                }
                // Memory accesses that are not strictly enclosed in a loop will not be taken into account
                return;
            }
            addNewAccess(newApproxRefAddress, newAccessSize, newLoadOrStore, newPCAddress, tripCount);

        } else {
            System.out.println(String.format("An invalid address has been encountered at trip count %d\n", tripCount));
        }
    }

    public void addNewAccess(long refStartAddress, long sizeOfAccess, MemoryAccess readOrWrite, long PCAddress, long numTrips) {
        PointPC newPoint = new PointPC(refStartAddress, sizeOfAccess, readOrWrite, PCAddress);
        LoopInstance topLoopInstance = stack.peek();
        // Record memory space here
        topLoopInstance.addNewMemoryAccess(newPoint, numTrips);
        // Record memory space here
    }

    public void startOfLoop(long newLoopID) {
        stack.push(new LoopInstance(newLoopID));
        encounterNewLoopID(newLoopID);
    }

    public void endOfLoopIteration(long newLoopID) {
        if (stack.isEmpty() || stack.peek().getLoopID() != newLoopID) {
            // TODO: Convert that into a logging statement, using Java loggers
            System.out.println("Invalid end of loop iteration with a mismatch of new loop ID = " + newLoopID + " and " + (stack.isEmpty() ? "Empty Stack" : stack.peek().getLoopID()));
            return;
        }
        // Record Memory Space here
        stack.peek().loopIterationEnd();
        // Record allocated memory space here
    }

    public void loopTermination(long currLoopID) throws NullLoopInstanceException {
        if (stack.isEmpty() || stack.peek().getLoopID() != currLoopID) {
            // TODO: Convert that into a logging statement, using Java loggers
            System.out.println("Invalid end of loop iteration with a mismatch of new loop ID = " + currLoopID + " and " + (stack.isEmpty() ? "Empty Stack" : stack.peek().getLoopID()));
            return;
        }
        // Record Memory Space here
        LoopInstance topLoop = stack.pop();
        if (stack.size() >= 1) {
            LoopInstance secondFromTop = stack.peek();
            // Record Memory Space here
            secondFromTop.mergeHistoryIntoPending(topLoop);
            // Record Memory Space here
        }
        Map<DataDependence, LoopInstanceLevelSummary> topLoopConflicts = topLoop.getSummaryLoopInstanceDependencies();
        summariseLoopsInStack(currLoopID, topLoopConflicts);
        deleteLoopIDCache(currLoopID);
        // Record Memory Space here
    }

    public void summariseLoopsInStack(long currLoopInstance, @NotNull Map<DataDependence, LoopInstanceLevelSummary> loopInstanceConflicts) {
        Map<DataDependence, LoopLevelSummary> currDepMap = this.loopDependencies.get(currLoopInstance);
        for (DataDependence conflictKey : currDepMap.keySet()) {
            LoopInstanceLevelSummary instanceSummary = loopInstanceConflicts.get(conflictKey);
            LoopLevelSummary thisLoopSummary = currDepMap.getOrDefault(conflictKey, new LoopLevelSummary());
            thisLoopSummary.addLoopInstanceConflicts(instanceSummary);
        }
    }

    // TODO: Implement this after passing the basic unit tests!
    public String getOutputTotalStatistics() {
        return new String();
    }

    public Deque<LoopInstance> getRefToStack() {
        return stack; // Ideally, we would want to return a reference to the cloned stack member, but this would incur extra memory at runtime that may impact our measurements of the runtime memory of the loop instance tables
    }

    public boolean isStackEmpty() {
        return stack.isEmpty();
    }

    public Map<Long, Map<DataDependence, LoopLevelSummary>> getLoopDependencies() {
        return loopDependencies;
    }

    public boolean isSeen(long loopIDAddress) {
        return uniqueLoopIDsCache.contains(loopIDAddress);
    }

    public boolean encounterNewLoopID(long loopIDAddress) {
        return uniqueLoopIDsCache.add(loopIDAddress);
    }

    public boolean deleteLoopIDCache(long loopIDAddress) {
        if (uniqueLoopIDsCache.contains(loopIDAddress)) {
            uniqueLoopIDsCache.remove(loopIDAddress);
            return true;
        }
        return false;
    }
}
