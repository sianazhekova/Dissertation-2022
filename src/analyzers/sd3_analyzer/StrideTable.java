package analyzers.sd3_analyzer;

import analyzers.baseline_analyzer.MemoryAccess;
import analyzers.baseline_analyzer.PointPC;
import analyzers.baseline_analyzer.Table;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class StrideTable implements Table, Cloneable {

    // have the table be keyed by the PCPairs

    public Map<BigInteger, IntervalTree> table;

    public StrideTable() { this.table = new HashMap<>(); }

    public StrideTable(Map<BigInteger, IntervalTree> input) { this.table = input; }

    public void addNewMemoryAccess(PointPC pcPoint, long tripCount) {


    }

    public void mergeHistoryIntoPending(StrideTable otherTable) {
        // otherTable is the Pending table


    }

    public void mergePendingIntoHistory(StrideTable otherTable) {



    }

    public void clear() { table.clear(); }

    public boolean containsAccessType(BigInteger keyRefAddress, MemoryAccess accessType) {



        return false;
    }

    public boolean isStrideTableEmpty() { return table.isEmpty(); }

    public StrideTable clone() {
        // need interval tree clone


        return null;
    }

    public boolean deletePC() {



        return false;
    }




}
