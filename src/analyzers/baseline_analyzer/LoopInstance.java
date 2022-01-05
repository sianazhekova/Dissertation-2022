package analyzers.baseline_analyzer;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LoopInstance {
    protected PointTable pendingPointTable;
    protected PointTable historyPointTable;

    protected Set<Long> killedBits;

    public LinkedHashMap<String, DataDependence> dependencies;
    public LoopLevelSummary currLoopDependencies;  // TODO: information about loop dependencies

    protected long numInnerLoops;
    protected long numLoopIterations;
    protected long loopID;

    public void LoopInstance(long loopID) {
        this.loopID = loopID;
        this.numLoopIterations = 0;
        this.numInnerLoops = 0;
    }

    public long getLoopID() {
        return loopID;
    }

    public long getNumInnerLoops() {
        return numInnerLoops;
    }

    public long getNumLoopIterations() {
        return numLoopIterations;
    }

    public LoopLevelSummary getCurrLoopDependencies() {
        return currLoopDependencies;
    }

    public PointTable getHistoryPointTable() {
        return historyPointTable; //.clone();
    }

    public PointTable getPendingPointTable() {
        return pendingPointTable;  //.clone();
    }

    public void addMemAccess(@NotNull PointPC pcPoint, long tripCount) {  // Block block, pc, ...detector
        long memAddress = pcPoint.getRefStartAddress();
        long PCAddress = pcPoint.getPCPair().getPC();
        if (isKilled(pcPoint)) {
            // Report a loop independent dependence
            // TODO: Encapsulate tracking of loop-independent dependencies in a class
            System.out.println("There is a loop-independent dependence for loop ID = "
                    + loopID + ", for a memory access with an address, "
                    + memAddress + ", at a PC address = "
                    + PCAddress + ", for a trip count of "
                    + tripCount
            );
            return;
        }

        MemoryAccess accessMode = pcPoint.getPCPair().getMemAccessType();
        // Store the memory access in the Pending Point Table of the current loop table
        pendingPointTable.addNewEntryForAddress(memAddress, PCAddress, accessMode, tripCount);

        // If the memory access is a Write (so a store) and there have been no Reads for that memory address in the current loop iteration, then it is a killed bit
        if (accessMode == MemoryAccess.WRITE && !pendingPointTable.containsAccessType(memAddress, MemoryAccess.READ)) {
            killedBits.add(memAddress);
        }

        // Record the new memory access entry by adding it to the Pending Point Table of the current loop iteration
        pendingPointTable.addNewEntryForAddress(memAddress, PCAddress, accessMode, tripCount);

    }

    public boolean isKilled(@NotNull PointPC memAccessPoint) {
        return killedBits.contains(memAccessPoint.getRefStartAddress());
    }

    public boolean isKilled(Long memAccessRefAddr) {
        return killedBits.contains(memAccessRefAddr);
    }

    public void mergePendingIntoHistory() {
        // At a loop iteration end, merge the Pending Point Table into the History Point Table
        historyPointTable.mergeWith(pendingPointTable);
        pendingPointTable.clear();
        killedBits.clear();
    }

    public void mergeHistoryIntoPending(LoopInstance innerLoop) throws NullLoopInstanceException {
        if (innerLoop == null) throw new NullLoopInstanceException("A null loop instance object has been passed onto loop with loop ID = " +
               this.loopID + "in its iteration number" + this.numLoopIterations);

        // Iterate through the memory address keys of the History Table of the inner loop, and the table list entries of those address keys that do not fall into
        // the current set of killed bits of the current (outer) loop instance are merged into the current loop instance's Pending Table
        PointTable innerHistoryTable = innerLoop.getHistoryPointTable(); // a reference to the History Table of the inner loop
        Set<Long> pendingWrites = new HashSet<>();
        for (Long keyAddr : innerHistoryTable.getKeySet()) {
            if (!isKilled(keyAddr)) {
                this.pendingPointTable.mergeAddressLine(keyAddr, innerHistoryTable.getTableEntry(keyAddr));
                this.pendingPointTable.accNewKilledBits(keyAddr, pendingWrites);
            }
        }
        this.killedBits.addAll(pendingWrites);
        this.numInnerLoops++;
    }

    public void loopIterationEnd() {
        handleLoopDependencies();
        mergePendingIntoHistory();
        this.numLoopIterations++;
    }

    public void handleLoopDependencies() {

    }

    LoopLevelSummary loopTerminate() {
        return currLoopDependencies;
    }

    /*  void handleConflicts(MemoryAccess pendingMode, MemoryAccess historyMode) */
}
