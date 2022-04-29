package analyzers.sd3_analyzer;

import analyzers.baseline_analyzer.MemoryAccess;
import analyzers.baseline_analyzer.PointPC;
import analyzers.baseline_analyzer.Table;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;

public class StrideTable implements Table, Cloneable {

    // have the table be keyed by the PCPairs

    public Map<BigInteger, IntervalTree> table;

    public StrideTable() { this.table = new LinkedHashMap<>(); }

    public StrideTable(Map<BigInteger, IntervalTree> input) { this.table = input; }

    public void addNewMemoryAccess(@NotNull PointPC pcPoint, Stride strideLoc) {
        BigInteger newPC = pcPoint.getPCPair().getPC();
        MemoryAccess memAccess = pcPoint.getPCPair().getMemAccessType();
        BigInteger newRefAddress = pcPoint.getRefStartAddress();

        IntervalTree intTreeInsert = this.table.getOrDefault(newPC, new IntervalTree());


    }

    public void insertPair(BigInteger PCKey, Stride strideToInsert) {


    }

    public void insertAddress(BigInteger PCKey, BigInteger refAddress,  Stride strideToInsert) {


    }

    public void updateStridesAfterKill(BigInteger killedBit) {

        for (BigInteger PCKey : this.table.keySet()) {
            IntervalTree intTree = returnIntervalTreeForKey(PCKey);
            if (intTree.getRoot().isNil()) {
                continue;
            }
            intTree.killAddress(killedBit);
        }
    }

    public void sanitiseTable() {
        for (BigInteger PCKey : this.table.keySet()) {
            IntervalTree intTree = returnIntervalTreeForKey(PCKey);
            if (intTree.getRoot().isNil()) {
                deletePC(PCKey);
            }
        }
    }

    public void mergeHistoryIntoPending(@NotNull StrideTable otherTable) {
        // otherTable is the History table
        for (BigInteger otherPCKey : otherTable.getKeys()) {
            IntervalTree cloneIntervalTree = otherTable.table.get(otherPCKey).copyTree();
            this.table.put(otherPCKey, cloneIntervalTree);
        }
        otherTable.clear();
    }

    public void mergePendingIntoHistory(@NotNull StrideTable otherTable) {
        // otherTable is the Pending table
        for (BigInteger otherPCKey : otherTable.getKeys()) {
            if (this.table.containsKey(otherPCKey)) {
                // TODO: Fix this with mergeWithHelper()
                this.table.get(otherPCKey).mergeWith(otherTable.table.get(otherPCKey));

            } else {
                this.table.put(otherPCKey, otherTable.table.get(otherPCKey));
            }
        }
        // TODO : Implement PC Cache for likelihood of merging
    }

    public void clear() { table.clear(); }

    public boolean clearEntry(BigInteger PCKey) {
        if (table.containsKey(PCKey)) {
            table.remove(PCKey);
            return true;
        }
        return false;
    }

    public IntervalTree returnIntervalTreeForKey(BigInteger PCKey) {
        if (this.table.containsKey(PCKey)) {
            return this.table.get(PCKey); //.copyTree()
        } else {
            return new IntervalTree();  // returning the T.nil sentinel node
        }
    }

    public boolean containsAccessType(BigInteger keyRefAddress, MemoryAccess accessType) {

        for (BigInteger PCKey : this.table.keySet() ) {
            IntervalTree tree = table.get(PCKey);
            if (tree.getRoot().isNil()) {
                continue;
            }
            if (tree.containsAccessType(keyRefAddress, accessType)) {
                return true;
            }
        }

        return false;
    }

    public boolean isStrideTableEmpty() { return table.isEmpty(); }

    @Override
    public StrideTable clone() {
        // need interval tree clone
        Map<BigInteger, IntervalTree> newStrideTableCopy = new TreeMap<>();

        for (BigInteger currentKey : this.table.keySet()) {
            IntervalTree copyTree = this.table.get(currentKey).copyTree();
            newStrideTableCopy.put(currentKey, copyTree);
        }
        StrideTable newStrideTable = new StrideTable(newStrideTableCopy);

        return newStrideTable;
    }

    public boolean deletePC(BigInteger PCKey) {
        if (this.table.containsKey(PCKey)) {
            this.table.remove(PCKey);

            return true;
        }
        return false;
    }

    public IntervalTree getTableEntry(BigInteger keyRefAddr) {
        return this.table.getOrDefault(keyRefAddr, new IntervalTree());
    }

    public boolean containsKey(BigInteger PCKey) { return this.table.containsKey(PCKey); }

    public Set<BigInteger> getKeys() {
        return this.table.keySet();
    }

    public IntervalTree getIntervalTree(BigInteger PCKey) {
        return this.table.get(PCKey);
    }

}
