package analyzers.baseline_analyzer;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;

public class LoopInstance {
    protected PointTable pendingPointTable;
    protected PointTable historyPointTable;

    protected Set<BigInteger> killedBits;

    public Map<DataDependence, LoopInstanceLevelSummary> summaryDependencies;

    protected long numInnerLoops;
    protected long numLoopIterations;
    protected BigInteger loopID;


    public LoopInstance(BigInteger newLoopID) {
        this.loopID = newLoopID;
        this.numLoopIterations = 0;
        this.numInnerLoops = 0;

        pendingPointTable = new PointTable();
        historyPointTable = new PointTable();
        killedBits = new HashSet<>();

        summaryDependencies = new HashMap<>();
        summaryDependencies.put(DataDependence.RW, new LoopInstanceLevelSummary(newLoopID));
        summaryDependencies.put(DataDependence.WR, new LoopInstanceLevelSummary(newLoopID));
        summaryDependencies.put(DataDependence.WW, new LoopInstanceLevelSummary(newLoopID));
    }

    public BigInteger getLoopID() {
        return loopID;
    }

    public long getNumInnerLoops() {
        return numInnerLoops;
    }

    public long getNumLoopIterations() {
        return numLoopIterations;
    }

    public Map<DataDependence, LoopInstanceLevelSummary> getSummaryLoopInstanceDependencies() {
        return summaryDependencies;
    }

    public PointTable getHistoryPointTable() {
        return historyPointTable; //.clone();
    }

    public PointTable getPendingPointTable() {
        return pendingPointTable;  //.clone();
    }

    public void addNewMemoryAccess(@NotNull PointPC pcPoint, long tripCount) {  // Block block, pc, ...detector
        BigInteger memAddress = pcPoint.getRefStartAddress();
        BigInteger PCAddress = pcPoint.getPCPair().getPC();
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

    public boolean isKilled(BigInteger memAccessRefAddr) {
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

        // TODO : Change that so that it does not need an external reference to the other loop's History Table
        PointTable innerHistoryTable = innerLoop.getHistoryPointTable(); // a reference to the History Table of the inner loop
        Set<BigInteger> pendingWrites = new HashSet<>();
        for (BigInteger keyAddr : innerHistoryTable.getKeySet()) {
            if (!isKilled(keyAddr)) {
                if (!innerHistoryTable.containsAccessType(keyAddr, MemoryAccess.READ) && !this.pendingPointTable.containsAccessType(keyAddr, MemoryAccess.READ)) {
                    killedBits.add(keyAddr);
                }
                List<TableEntryPC> tempList = innerHistoryTable.getTableEntry(keyAddr);
                this.pendingPointTable.mergeAddressLine(keyAddr, tempList);
            }
        }
        this.killedBits.addAll(pendingWrites);
        this.numInnerLoops++;
    }

    public void loopIterationEnd() {
        recordLoopDataConflicts();
        mergePendingIntoHistory();
        this.numLoopIterations++;
    }

    public void recordLoopDataConflicts() {
        if (pendingPointTable.getKeySet().size() == 0) return;

        Map<DataDependence, LinkedHashSet<PairwiseConflictLevelSummary>> mapInstructions = new HashMap<>();
        mapInstructions.put(DataDependence.RW, new LinkedHashSet<>());
        mapInstructions.put(DataDependence.WW, new LinkedHashSet<>());
        mapInstructions.put(DataDependence.WR, new LinkedHashSet<>());

        for (BigInteger keyAddress : pendingPointTable.getKeySet()) {
            List<TableEntryPC> currListOfEntries = pendingPointTable.getTableEntry(keyAddress);
            if (currListOfEntries.size() > 0) {
                DataDependence conflictType;

                TableEntryPC pendingHead = currListOfEntries.get(0);
                if (historyPointTable.containsKey(keyAddress)) {
                    List<TableEntryPC> historyList = historyPointTable.getTableEntry(keyAddress);
                    if (historyList.size() > 0) {
                        TableEntryPC historyTail = historyList.get(historyList.size() - 1);
                        conflictType = DataDependence.getDependence(historyTail.getMemAccessType(), pendingHead.getMemAccessType());
                        if (conflictType == DataDependence.DEPNONE) continue;
                        LinkedHashSet<PairwiseConflictLevelSummary> set = mapInstructions.get(conflictType);
                        set.add(new PairwiseConflictLevelSummary(keyAddress,
                                                            new PCPair(historyTail.getAddressPC(), historyTail.getMemAccessType()),
                                                            new PCPair(pendingHead.getAddressPC(), pendingHead.getMemAccessType()),
                                                            1,
                                                            historyTail.getTripCount(),
                                                            pendingHead.getTripCount()
                        ));
                    }
                }
                // Iterate through the rest of the TableEntryPC
                int count = 0;
                ListIterator<TableEntryPC> listIterator = currListOfEntries.listIterator();
                TableEntryPC currEntry = listIterator.next();   // First Entry
                DataDependence currConflict;
                TableEntryPC nextEntry;
                while (listIterator.hasNext()) {
                    if (count++ == currListOfEntries.size() - 1) continue;
                    nextEntry = listIterator.next();
                    currConflict = DataDependence.getDependence(currEntry.getMemAccessType(), nextEntry.getMemAccessType());
                    if (currConflict != DataDependence.DEPNONE) {
                        LinkedHashSet<PairwiseConflictLevelSummary> set = mapInstructions.get(currConflict);
                        set.add(new PairwiseConflictLevelSummary(keyAddress,
                                new PCPair(currEntry.getAddressPC(), currEntry.getMemAccessType()),
                                new PCPair(nextEntry.getAddressPC(), nextEntry.getMemAccessType()),
                                1,
                                currEntry.getTripCount(),
                                nextEntry.getTripCount()
                        ));
                    }
                    currEntry = nextEntry;
                }
            }
        }
        // Aggregate the Conflict Instruction Lists into LoopInstanceLevelSummary

        for (DataDependence keyConflictType : mapInstructions.keySet()) {
            LinkedHashSet<PairwiseConflictLevelSummary> currInstrList = mapInstructions.get(keyConflictType);
            if (currInstrList.size() > 0) {
                LoopInstanceLevelSummary instanceSummary = summaryDependencies.get(keyConflictType);
                instanceSummary.addLoopIterationConflicts(currInstrList.size(), currInstrList);
            }
        }
    }

    Map<DataDependence, LoopInstanceLevelSummary> loopTermination() {
        System.out.println(String.format("Loop Instance %d has terminated", loopID));
        return summaryDependencies;
    }
}
