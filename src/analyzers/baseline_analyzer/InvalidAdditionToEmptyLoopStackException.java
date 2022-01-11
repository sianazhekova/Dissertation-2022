package analyzers.baseline_analyzer;

public class InvalidAdditionToEmptyLoopStackException extends Exception {

    private static final long serialVersionID = 7_718_828_512_143_293_558L;
    private long tripCount;
    private long accessAddress;
    private MemoryAccess accessType;

    public InvalidAdditionToEmptyLoopStackException(long numTrips, long accessedAddr, MemoryAccess readOrWrite) {
        super();
        this.tripCount = numTrips;
        this.accessAddress = accessedAddr;
        this.accessType = readOrWrite;

    }

    public InvalidAdditionToEmptyLoopStackException(String message, long numTrips, long accessedAddr, MemoryAccess readOrWrite) {
        super(message);
        this.tripCount = numTrips;
        this.accessAddress = accessedAddr;
        this.accessType = readOrWrite;
    }

    public InvalidAdditionToEmptyLoopStackException(String message, Throwable cause, long numTrips, long accessedAddr, MemoryAccess readOrWrite) {
        super(message, cause);
        this.tripCount = numTrips;
        this.accessAddress = accessedAddr;
        this.accessType = readOrWrite;
    }

    public InvalidAdditionToEmptyLoopStackException(Throwable cause, long numTrips, long accessedAddr, MemoryAccess readOrWrite) {
        super(cause);
        this.tripCount = numTrips;
        this.accessAddress = accessedAddr;
        this.accessType = readOrWrite;
    }

    public long getTripCount() {
        return tripCount;
    }

    public long getAccessAddress() {
        return accessAddress;
    }

    public MemoryAccess getAccessType() {
        return accessType;
    }
}
