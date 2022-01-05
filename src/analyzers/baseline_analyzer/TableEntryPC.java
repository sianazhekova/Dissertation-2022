package analyzers.baseline_analyzer;

// This will be for storing entries in PointTables

public class TableEntryPC {
    //private PCPair pairPC;
    private PointPC point;
    private long tripCount;  // 2 ways of implementing this - either via Time module to record timestamps or count number of non-START/END events
    private int numOccurrence;

    public long getAddressPC() {
        return point.getPCPair().getPC();
    }

    public long getTripCount() {
        return tripCount;
    }

    public int getNumOccurrence() {
        return numOccurrence;
    }

    public MemoryAccess getMemAccessType() {
        return point.getPCPair().getMemAccessType();
    }

    public void setTripCount(long trips) {
        this.tripCount = trips;
    }

    public void setNumOccurrence(int freqCount) {
        this.numOccurrence = freqCount;
    }

    public TableEntryPC(long PCAddress, long numTrips, MemoryAccess readOrWrite, int freqCount) {
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
