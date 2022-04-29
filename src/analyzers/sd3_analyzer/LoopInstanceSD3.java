package analyzers.sd3_analyzer;

import analyzers.baseline_analyzer.*;
import analyzers.readers.InstructionsFileReader;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.math.BigInteger;
import java.util.*;

public class LoopInstanceSD3 extends LoopInstance {

    protected StrideTable pendingStrideTable;
    protected StrideTable historyStrideTable;

    protected Set<Stride> killedStrides;


    public LoopInstanceSD3(BigInteger newLoopID) {
        super(newLoopID);

        pendingStrideTable = new StrideTable();
        historyStrideTable = new StrideTable();
        killedStrides = new HashSet<>();
    }

    public StrideTable getHistoryStrideTable() { return historyStrideTable; }

    public StrideTable getPendingStrideTable() { return pendingStrideTable; }

    public void addNewMemoryAccess(@NotNull PointPC pcPoint, long tripCount, StrideDetection strideDetector) {
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
        boolean isStrideInitially = strideDetector.isAStride();
        Stride strideToInsert = null;

        if (isStrideInitially) {
            strideToInsert = strideDetector.obtainStrideForSearch();
        }

        strideDetector.updateFSMState(memAddress);
        boolean isStrideAfter = strideDetector.isAStride();

        if (!isStrideAfter) {
            // It is a POINT, so ADD it to the Pending Point Table of the Loop Instance
            pendingPointTable.addNewEntryForAddress(memAddress, PCAddress, accessMode, tripCount);

        } else {
            // It is part of a STRIDE, so ADD it to the Pending Stride Table of the Loop Instance

            // Case 1: This memory reference address establishes the stride for the first time
            if (!isStrideInitially) {
                BigInteger strideDist = strideDetector.obtainLearnedStrideDist();
                strideToInsert = new Stride(memAddress, memAddress, strideDist, BigInteger.ONE, BigInteger.ONE, new PCPair(PCAddress, accessMode));
                this.pendingStrideTable.insertPair(PCAddress, strideToInsert);


            } else { // Case 2: This memory reference address is part of an already existing stride
                strideToInsert.setPCAndReadWrite(new PCPair(PCAddress, accessMode));
                this.pendingStrideTable.insertAddress(PCAddress, memAddress, strideToInsert);

            }

        }

        // TODO : Work with killed strides as well (!!!)
        // If the memory access is a Write (so a store) and there have been no Reads for that memory address in the current loop iteration, then it is a killed bit
        if (accessMode == MemoryAccess.WRITE && !pendingPointTable.containsAccessType(memAddress, MemoryAccess.READ)
                && !pendingStrideTable.containsAccessType(memAddress, MemoryAccess.READ)) {
            killedBits.add(memAddress);
        }

    }

    @Override
    public boolean isKilled(@NotNull PointPC memAccessPoint) {
        return isKilled(memAccessPoint.getRefStartAddress());
    }

    @Override
    public boolean isKilled(BigInteger memAccessAddr) {
        boolean inStride = false;
        for (Stride strKey : killedStrides) {
            if (strKey.containsAddressInStride(memAccessAddr)) {
                inStride = true;
                break;
            }
        }
        return killedBits.contains(memAccessAddr) || inStride;
    }

    public boolean isKilledStridesSetEmoty() { return killedStrides.isEmpty(); }

    public boolean isKilledPendingStrideTable() { return pendingStrideTable.isStrideTableEmpty(); }

    public boolean isKilledHistoryStrideTable() { return historyStrideTable.isStrideTableEmpty(); }

    @Override
    public void mergePendingIntoHistory() {
        // At a loop iteration end, merge the Pending Point Table into the History Point Table
        historyPointTable.mergeWith(pendingPointTable);
        pendingPointTable.clear();

        historyStrideTable.mergePendingIntoHistory(pendingStrideTable);
        pendingStrideTable.clear();

        killedBits.clear();
        killedStrides.clear();
    }

    @Override
    public void mergeHistoryIntoPending(LoopInstance innerLoop) throws NullLoopInstanceException {
        if (innerLoop == null) throw new NullLoopInstanceException("A null loop instance object has been passed onto loop with loop ID = " +
                this.loopID + "in its iteration number" + this.numLoopIterations);

        // Iterate through the memory address keys of the History Table of the inner loop, and the table list entries of those address keys that do not fall into
        // the current set of killed bits of the current (outer) loop instance are merged into the current loop instance's Pending Table

        PointTable innerHistoryPointTable = innerLoop.getHistoryPointTable(); // a reference to the History Table of the inner loop
        StrideTable otherHistoryStrideTable = ((LoopInstanceSD3)innerLoop).getHistoryStrideTable();

        Set<BigInteger> pendingWrites = new HashSet<>();

        // Iterate through the memory point accesses

        for (BigInteger keyAddr : innerHistoryPointTable.getKeySet()) {
            if (!isKilled(keyAddr)) {
                if (!innerHistoryPointTable.containsAccessType(keyAddr, MemoryAccess.READ) && !this.pendingPointTable.containsAccessType(keyAddr, MemoryAccess.READ)
                        && !otherHistoryStrideTable.containsAccessType(keyAddr, MemoryAccess.READ)
                        && innerHistoryPointTable.containsAccessType(keyAddr, MemoryAccess.WRITE) ) {
                    killedBits.add(keyAddr);
                }
                List<TableEntryPC> tempList = innerHistoryPointTable.getTableEntry(keyAddr);
                this.pendingPointTable.mergeAddressLine(keyAddr, tempList);
            }
        }

        // Access the stride table and update it (remove the killed bits)

        for (BigInteger killedBit : killedBits) {
            otherHistoryStrideTable.updateStridesAfterKill(killedBit);
        }

        // Merge the updated history table

        this.pendingStrideTable.mergeHistoryIntoPending(otherHistoryStrideTable);

        this.killedBits.addAll(pendingWrites);
        this.numInnerLoops++;
    }

    @Override
    public void loopIterationEnd() {
        recordPointPointConflicts();
        recordPointHistoryConflicts();
        recordHistoryHistoryConflicts();

        this.mergePendingIntoHistory();
        this.numLoopIterations++;
    }

    public void recordPointHistoryConflicts() {


    }

    public void recordHistoryHistoryConflicts() {


    }

    public void printContentsTable(@NotNull StrideTable strideTable) {
        if (strideTable.isStrideTableEmpty()) {
            System.out.println("The current stride table is empty");
        }

        for (BigInteger keyPCAddress : strideTable.getKeys()) {
            System.out.println("The key is 0x" + InstructionsFileReader.toHexString(keyPCAddress));
            Assertions.assertTrue(strideTable.containsKey(keyPCAddress));

            Assertions.assertTrue( strideTable.getTableEntry(keyPCAddress).getRoot().isNil() );
            System.out.println("The interval tree of strides is : ");
            IntervalTree intTree = strideTable.getIntervalTree(keyPCAddress);
            intTree.printTree();

            System.out.println("\n");
        }
    }

    public String getStringOfKilledStrides() {
        StringBuilder sb = new StringBuilder();

        for (Stride killedStride : killedStrides) {
            sb.append(killedStride.getStringStrideState());
            sb.append("\n");
        }
        return sb.toString();
    }

    public void printKilledStrides() {
        System.out.println(getStringOfKilledStrides());
    }

    public void printHistoryStrideTable() { printContentsTable(historyStrideTable); }

    public void printPendingStrideTable() { printContentsTable(pendingStrideTable); }

}
