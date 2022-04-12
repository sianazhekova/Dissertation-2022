package analyzers.baseline_analyzer;

import analyzers.readers.InstructionsFileReader;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

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
                    + InstructionsFileReader.toHexString(loopID) + ", for a memory access with an address, "
                    + InstructionsFileReader.toHexString( memAddress) + ", at a PC address = "
                    + InstructionsFileReader.toHexString(PCAddress) + ", for a trip count of "
                    + tripCount
            );
            return;
        }

        MemoryAccess accessMode = pcPoint.getPCPair().getMemAccessType();

        //  if (!strideDetectors.containsKey(pcPoint.getPCAddress() )) {
        //      strideDetectors.
        //
        //  else {
        //      strideDetectors.updateFSMGeneral( pcPoint.getPCAddress() );
        //  }

        // Record the new memory access entry by adding it to the Pending Point Table of the current loop iteration
        pendingPointTable.addNewEntryForAddress(memAddress, PCAddress, accessMode, tripCount);

        // }

        // If the memory access is a Write (so a store) and there have been no Reads for that memory address in the current loop iteration, then it is a killed bit
        if (accessMode == MemoryAccess.WRITE && !pendingPointTable.containsAccessType(memAddress, MemoryAccess.READ)) {
            killedBits.add(memAddress);
        }
    }

    public boolean isKilled(@NotNull PointPC memAccessPoint) {
        return killedBits.contains(memAccessPoint.getRefStartAddress());
    }

    public boolean isKilled(BigInteger memAccessRefAddr) {
        return killedBits.contains(memAccessRefAddr);
    }

    public boolean isKilledBitsSetEmpty() { return killedBits.isEmpty(); }

    public boolean isPendingPointTableEmpty() { return pendingPointTable.isPointTableEmpty(); }

    public boolean isHistoryPointTableEmpty() { return historyPointTable.isPointTableEmpty(); }

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
                if (!innerHistoryTable.containsAccessType(keyAddr, MemoryAccess.READ) && !this.pendingPointTable.containsAccessType(keyAddr, MemoryAccess.READ)
                        && innerHistoryTable.containsAccessType(keyAddr, MemoryAccess.WRITE) /* && this.pendingPointTable.containsAccessType(keyAddr, MemoryAccess.WRITE) */ ) {
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
                Iterator<TableEntryPC> listIterator = currListOfEntries.iterator();
                TableEntryPC currEntry = listIterator.next();   // First Entry
                DataDependence currConflict;
                TableEntryPC nextEntry;
                System.out.println("The first entry to merge in pending table is for PC"
                        + InstructionsFileReader.toHexString(currEntry.getAddressPC())
                        + " for approx mem ref address of "
                        + InstructionsFileReader.toHexString(keyAddress)
                );
                while (listIterator.hasNext()) {
                    if (count++ == currListOfEntries.size() - 1) continue;
                    nextEntry = listIterator.next();
                    currConflict = DataDependence.getDependence(currEntry.getMemAccessType(), nextEntry.getMemAccessType());
                    if (currConflict != DataDependence.DEPNONE) {
                        LinkedHashSet<PairwiseConflictLevelSummary> set = mapInstructions.get(currConflict);
                        System.out.println("The next entry to merge in pending table is for PC"
                                + InstructionsFileReader.toHexString(nextEntry.getAddressPC())
                                + " for approx mem ref address of "
                                + InstructionsFileReader.toHexString(keyAddress)
                        );
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
            LoopInstanceLevelSummary instanceSummary = summaryDependencies.get(keyConflictType);
            if (currInstrList.size() > 0) {

                long aggregateFreqCount = currInstrList.stream()
                        .map(conflictSummary -> conflictSummary.getFrequencyCount())
                        .mapToLong(n -> n)
                        .sum();

                instanceSummary.addLoopIterationConflicts(aggregateFreqCount, currInstrList);
            } else {
                instanceSummary = summaryDependencies.get(keyConflictType);
                instanceSummary.incrementIterationState(0);
            }
        }
    }

    public String getOutputInstanceStatistics() {
        Assertions.assertTrue(summaryDependencies.size() > 0);
        StringBuilder sb = new StringBuilder();
        DataDependence[] dependencies = DataDependence.getDependenceTypes();

        for (DataDependence depType : dependencies) {
            sb.append(depType.name() + " : \n".indent(2));
            sb.append(summaryDependencies.get(depType).printToString());
        }
        sb.append("\n");

        return sb.toString();
    }

    // A helper util function printing the contents of the point table
    public void printContentsTable(@NotNull PointTable pointTable) {
        if (pointTable.isPointTableEmpty()) {
            System.out.println("The current point table is empty");
        }

        for (BigInteger keyRefAddress : pointTable.getKeySet()) {
            System.out.println("The key is 0x" + InstructionsFileReader.toHexString(keyRefAddress));
            Assertions.assertTrue(pointTable.containsKey(keyRefAddress));
            Assertions.assertTrue(pointTable.getTableEntry(keyRefAddress).size() != 0);
            ListIterator<TableEntryPC> iteratorsEntries = pointTable.getTableEntry(keyRefAddress).listIterator();
            while (iteratorsEntries.hasNext()) {
                TableEntryPC tableEntry = iteratorsEntries.next();
                System.out.println("The Table Entry has a PC address of 0x" + InstructionsFileReader.toHexString(tableEntry.getAddressPC())
                        + ", and has a trip count of " + tableEntry.getTripCount()
                        + ", and has an access type of " + MemoryAccess.getStringMemAccess(tableEntry.getMemAccessType())
                        + ", and a frequency of " + tableEntry.getNumOccurrence()
                );
            }
            System.out.println("\n");
        }
    }

    public String getStringOfKilledBits() {
        StringBuilder sb =  new StringBuilder("Printing the killed bits :\n");
        if (this.killedBits.size() == 0) sb.append("{}");
        for (BigInteger killedBit : this.killedBits) {
            sb.append(" { " + InstructionsFileReader.toHexString(killedBit) + " } ");
        }
        sb.append("\n");
        return sb.toString();
    }

    public void printKilledBits() {
        String killedBitsString = getStringOfKilledBits();
        System.out.println(killedBitsString);
    }

    public void printPendingPointTable() {
        printContentsTable(this.pendingPointTable);
    }

    public void printHistoryPointTable() {
        printContentsTable(this.historyPointTable);
    }

    Map<DataDependence, LoopInstanceLevelSummary> loopTermination() {
        System.out.println(String.format("Loop Instance %d has terminated", loopID));
        return summaryDependencies;
    }
}
