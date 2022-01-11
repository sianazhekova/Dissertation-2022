package analyzers.baseline_analyzer;

import analyzers.readers.EventType;
import analyzers.readers.MemBufferBlock;
import org.jetbrains.annotations.NotNull;

import javax.xml.crypto.Data;
import java.util.*;

public class LoopStack {

    protected Deque<LoopInstance> stack;
    public Map<Long, Map<DataDependence, LoopLevelSummary>> loopDependencies;
    public Set<Long> uniqueLoopIDs;

    public LoopStack() {
        stack = new ArrayDeque<>();
        loopDependencies = new HashMap<>();
        uniqueLoopIDs = new HashSet<>();
    }

    public void encounterNewAccess(@NotNull MemBufferBlock bufferBlock, long tripCount) {
        EventType event = bufferBlock.getEvent();
        //if ()

    }

    public void addMemoryAddress(long refStartAddress, long sizeOfAccess, MemoryAccess readOrWrite, long PCAddress, long numTrips) throws InvalidAdditionToEmptyLoopStackException {
        if (stack.size() == 0) {
            throw new InvalidAdditionToEmptyLoopStackException(String.format("Tried to add a load/store at trip count %d, at an approximate memory address %d that is a %s",
                                                                                                    numTrips,
                                                                                                    refStartAddress,
                                                                                                    readOrWrite.name()),
                                                                                                    numTrips,
                                                                                                    refStartAddress,
                                                                                                    readOrWrite);
        }
        PointPC newPoint = new PointPC(refStartAddress, sizeOfAccess, readOrWrite, PCAddress);
        LoopInstance topLoopInstance = stack.peek();
        // Record memory space here
        topLoopInstance.addNewMemoryAccess(newPoint, numTrips);
        // Record memory space here
    }

    public void startOfLoop(long newLoopID) {
        stack.push(new LoopInstance(newLoopID));
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

    public void loopTerminate(long newLoopID) throws NullLoopInstanceException {
        if (stack.isEmpty() || stack.peek().getLoopID() != newLoopID) {
            // TODO: Convert that into a logging statement, using Java loggers
            System.out.println("Invalid end of loop iteration with a mismatch of new loop ID = " + newLoopID + " and " + (stack.isEmpty() ? "Empty Stack" : stack.peek().getLoopID()));
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
        summariseLoopsInStack(newLoopID, topLoopConflicts);
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

    public String printConflictsToString() {
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
        return uniqueLoopIDs.contains(loopIDAddress);
    }

    public boolean encounterNewLoopID(long loopIDAddress) {
        return uniqueLoopIDs.add(loopIDAddress);
    }

    public boolean deleteLoopID(long loopIDAddress) {
        if (uniqueLoopIDs.contains(loopIDAddress)) {
            uniqueLoopIDs.remove(loopIDAddress);
            return true;
        }
        return false;
    }
}
