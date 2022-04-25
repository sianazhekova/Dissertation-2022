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

    public void addNewMemoryAccess(PointPC pcPoint, long tripCount) {


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

    public Set<BigInteger> getKeys() {
        return this.table.keySet();
    }

    public IntervalTree getIntervalTree(BigInteger PCKey) {
        return this.table.get(PCKey);
    }

}
