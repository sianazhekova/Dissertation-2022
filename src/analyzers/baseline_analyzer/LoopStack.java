package analyzers.baseline_analyzer;

import analyzers.baseline_analyzer.unit_tests.PairwiseConflictLevelSummaryTest;
import analyzers.readers.EventType;
import analyzers.readers.InstructionsFileReader;
import analyzers.readers.MemBufferBlock;
import java.lang.*;

import analyzers.readers.MemoryTracer;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;

public class LoopStack {

    protected Deque<LoopInstance> stack;
    public Map<BigInteger, Map<DataDependence, LoopLevelSummary>> loopDependencies;
    public Set<BigInteger> uniqueLoopIDsCache;
    private boolean throwStackExceptions;

    private Date date;
    private long initialTime;

    private final Logger logger = Logger.getLogger(LoopStack.class.getName());
    public MemoryTracer memoryTracer;


    public LoopStack(boolean withExceptions) {
        stack = new ArrayDeque<>();
        loopDependencies = new HashMap<>();
        uniqueLoopIDsCache = new HashSet<>();
        throwStackExceptions = withExceptions;
        memoryTracer = new MemoryTracer();

        date = new Date();
        initialTime = date.getTime();

        memoryTracer.printMemoryStatistics("At initialisation of Loop Stack");
    }

    public LoopStack() {
        stack = new ArrayDeque<>();
        loopDependencies = new HashMap<>();
        uniqueLoopIDsCache = new HashSet<>();
        throwStackExceptions = false;
        memoryTracer = new MemoryTracer();

        date = new Date();
        initialTime = date.getTime();
    }

    // TODO: Maybe add sanity-checks for input count of loop starts and loop ends
    public void encounterNewAccess(@NotNull List<MemBufferBlock> bufferBlockPair, long tripCount) throws InvalidAdditionToEmptyLoopStackException, NullLoopInstanceException {
        assert(bufferBlockPair.size() == 2 || bufferBlockPair.size() == 1);

        MemBufferBlock firstBufferBlock = bufferBlockPair.get(0);
        EventType event = firstBufferBlock.getEvent();
        System.out.println("The mem address to be added has a trip count of " + tripCount +  " and corresponds to the address " + InstructionsFileReader.toHexString(firstBufferBlock.getAddressPC()));
        // This is the last memory buffer containing the instruction metadata about the memory access, in our selected trace file
        if (bufferBlockPair.size() == 1 && event != EventType.END) return;

        if (event == EventType.START) {
            // Newly encountered access is a start of a loop - either the first iteration of a new loop instance,
            // or the i-th iteration of the loop instance at the top of the loop stack
            BigInteger loopPCAddress = firstBufferBlock.getAddressPC();
            if (!isSeen(loopPCAddress)) {
                // Loop PC address has never been encountered before, or if it has, it has terminated and is not directly succeeding the previous iteration
                startOfLoop(loopPCAddress);
            }
        } else if (event == EventType.END) {
            if (bufferBlockPair.size() == 2) {
                BigInteger topLoopID = stack.peek().getLoopID();

                MemBufferBlock secondBufferBlock = bufferBlockPair.get(1);
                EventType eventAfter = secondBufferBlock.getEvent();
                BigInteger loopSuccessorID = secondBufferBlock.getAddressPC();

                if (eventAfter == EventType.START && isSeen(loopSuccessorID)) {  // isSeen(loopSuccessorID)
                    // This means the current loop has another iteration at least so only the loop-iteration-end stage should be invoked
                    endOfLoopIteration(topLoopID);
                    return;
                }
                // The END signal at this code point signifies the end of the iteration of the top-most (current) loop instance, as well as its termination
                endOfLoopIteration(topLoopID);
                System.out.println("Loop is terminating!!!");
                loopTermination(topLoopID);

            } else {
                assert(bufferBlockPair.size() == 1);
                assert(!isStackEmpty());

                while (!stack.isEmpty()) {
                    LoopInstance topLoop = stack.peek();
                    BigInteger topLoopID = topLoop.getLoopID();
                    endOfLoopIteration(topLoopID);
                    loopTermination(topLoopID);
                }
                return;
            }
        } else if (event == EventType.LOAD || event == EventType.STORE) {
            BigInteger newPCAddress = firstBufferBlock.getAddressPC();
            BigInteger newApproxRefAddress = firstBufferBlock.getAddressRef();
            BigInteger newAccessSize = firstBufferBlock.getSizeOfAccess();
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

    public void addNewAccess(BigInteger refStartAddress, BigInteger sizeOfAccess, MemoryAccess readOrWrite, BigInteger PCAddress, long numTrips) {
        PointPC newPoint = new PointPC(refStartAddress, sizeOfAccess, readOrWrite, PCAddress);
        LoopInstance topLoopInstance = stack.peek();
        // Record memory space here
        memoryTracer.updateMemoryParams();
        memoryTracer.printMemoryStatistics("Before addition of a new memory access");
        topLoopInstance.addNewMemoryAccess(newPoint, numTrips);
        // Record memory space here
        memoryTracer.updateMemoryParams();
        memoryTracer.printMemoryStatistics("After addition of  a new memory access");
    }

    public void startOfLoop(BigInteger newLoopID) {
        stack.push(new LoopInstance(newLoopID));
        Map<DataDependence, LoopLevelSummary> depMapSummary = loopDependencies.getOrDefault(newLoopID, new HashMap<>());

        if (encounterNewLoopID(newLoopID)) {
            DataDependence[] tempList = new DataDependence[]{ DataDependence.RW, DataDependence.WR, DataDependence.WW };
            for (DataDependence depKey : tempList) {
                if (!depMapSummary.containsKey(depKey)) {
                    depMapSummary.put(depKey, new LoopLevelSummary());
                }
            }
            loopDependencies.put(newLoopID,depMapSummary);
        }
    }

    public void endOfLoopIteration(BigInteger newLoopID) {
        if (stack.isEmpty() || !stack.peek().getLoopID().equals(newLoopID)) {
            // TODO: Convert that into a logging statement, using Java loggers
            System.out.println("Invalid end of loop iteration with a mismatch of new loop ID = " + newLoopID + " and " + (stack.isEmpty() ? "Empty Stack" : stack.peek().getLoopID()));
            return;
        }
        logger.info("End of loop iteration");
        // Record Memory Space here
        memoryTracer.updateMemoryParams();
        memoryTracer.printMemoryStatistics("Before end of loop iteration");
        stack.peek().loopIterationEnd();
        // Record allocated memory space here
        memoryTracer.updateMemoryParams();
        memoryTracer.printMemoryStatistics("After end of loop iteration");

        logger.info("PRINTING THE HISTORY POINT TABLE.");
        stack.peek().printHistoryPointTable();
        logger.info("PRINTING THE PENDING POINT TABLE.");
        stack.peek().printPendingPointTable();
    }

    public void loopTermination(BigInteger currLoopID) throws NullLoopInstanceException {
        if (stack.isEmpty() || stack.peek().getLoopID() != currLoopID) {
            // TODO: Convert that into a logging statement, using Java loggers
            System.out.println("Invalid end of loop iteration with a mismatch of new loop ID = " + InstructionsFileReader.toHexString(currLoopID) + " and " + (stack.isEmpty() ? "Empty Stack" : InstructionsFileReader.toHexString(stack.peek().getLoopID())));
            return;
        }
        System.out.println();
        // Record Memory Space here
        memoryTracer.updateMemoryParams();
        memoryTracer.printMemoryStatistics("Before loop termination");
        LoopInstance topLoop = stack.pop();
        if (stack.size() >= 1) {
            LoopInstance secondFromTop = stack.peek();
            // Record Memory Space here
            memoryTracer.updateMemoryParams();
            memoryTracer.printMemoryStatistics("Before merge of history table/s into pending table/s");
            secondFromTop.mergeHistoryIntoPending(topLoop);
            // Record Memory Space here
            memoryTracer.updateMemoryParams();
            memoryTracer.printMemoryStatistics("After merge of history table/s into pending table/s");
        }
        Map<DataDependence, LoopInstanceLevelSummary> topLoopConflicts = topLoop.getSummaryLoopInstanceDependencies();
        summariseLoopsInStack(currLoopID, topLoopConflicts);
        deleteLoopIDCache(currLoopID);
        // Record Memory Space here
        memoryTracer.updateMemoryParams();
        memoryTracer.printMemoryStatistics("After loop termination");
    }

    public void summariseLoopsInStack(BigInteger currLoopInstance, @NotNull Map<DataDependence, LoopInstanceLevelSummary> loopInstanceConflicts) {
        Map<DataDependence, LoopLevelSummary> currDepMap = this.loopDependencies.get(currLoopInstance);
        for (DataDependence conflictKey : currDepMap.keySet()) {
            LoopInstanceLevelSummary instanceSummary = loopInstanceConflicts.get(conflictKey);
            LoopLevelSummary thisLoopSummary = currDepMap.getOrDefault(conflictKey, new LoopLevelSummary());
            thisLoopSummary.addLoopInstanceConflicts(instanceSummary);
        }
    }

    public String getOutputTotalStatistics() {
        if (loopDependencies.size() == 0) return new String("No Loops recorded");
        StringBuilder sb = new StringBuilder();
        for (BigInteger loopID : loopDependencies.keySet()) {
            sb.append(InstructionsFileReader.toHexString(loopID) + " : \n");
            DataDependence[] dependencies = DataDependence.getDependenceTypes();

            for (DataDependence depType : dependencies) {
                sb.append(depType.name() + " : \n".indent(2));
                sb.append(loopDependencies.get(loopID).get(depType).printToString());
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public Deque<LoopInstance> getRefToStack() {
        // Ideally, we would want to return a reference to the cloned stack member, but this would incur extra memory at runtime
        // that may impact our measurements of the runtime memory of the loop instance tables
        return stack;
    }

    public boolean isStackEmpty() {
        return stack.isEmpty();
    }

    public Map<BigInteger, Map<DataDependence, LoopLevelSummary>> getLoopDependencies() {
        return loopDependencies;
    }

    public boolean isSeen(BigInteger loopIDAddress) {
        return uniqueLoopIDsCache.contains(loopIDAddress);
    }

    public boolean encounterNewLoopID(BigInteger loopIDAddress) {
        return uniqueLoopIDsCache.add(loopIDAddress);
    }

    public boolean deleteLoopIDCache(BigInteger loopIDAddress) {
        if (uniqueLoopIDsCache.contains(loopIDAddress)) {
            uniqueLoopIDsCache.remove(loopIDAddress);
            return true;
        }
        return false;
    }
}
