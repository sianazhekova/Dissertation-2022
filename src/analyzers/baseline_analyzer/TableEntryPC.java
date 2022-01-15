package analyzers.baseline_analyzer;

// This will be for storing entries in PointTables

import java.math.BigInteger;

public class TableEntryPC {
    private PCPair pairPC;
    private long tripCount;  // 2 ways of implementing this - either via Time module to record timestamps or count number of non-START/END events
    private int numOccurrence;

    public BigInteger getAddressPC() {
        return pairPC.getPC();
    }

    public long getTripCount() {
        return tripCount;
    }

    public int getNumOccurrence() {
        return numOccurrence;
    }

    public MemoryAccess getMemAccessType() {
        return pairPC.getMemAccessType();
    }

    public void setTripCount(long trips) {
        this.tripCount = trips;
    }

    public void setNumOccurrence(int freqCount) {
        this.numOccurrence = freqCount;
    }

    public TableEntryPC(BigInteger PCAddress, long numTrips, MemoryAccess readOrWrite, int freqCount) {
        this.pairPC = new PCPair(PCAddress, readOrWrite);
        this.tripCount = numTrips;
        this.numOccurrence = freqCount;
    }

    public TableEntryPC() {
        this.pairPC = new PCPair();
        this.tripCount = -1;
        this.numOccurrence = -1;
    }
}
