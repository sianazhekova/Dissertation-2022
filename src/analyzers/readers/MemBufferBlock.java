package analyzers.readers;

/* This class represents a data structure that stores the type of memory access,
    the address referenced, the size of memory access (in Bytes), and the PC address
* */

public class MemBufferBlock {
    private EventType event;
    private long addressRef;
    private long sizeOfAccess;
    private long addressPC;

    public MemBufferBlock(EventType memAccess, long refAddr, long size, long pcAddr) {
        event = memAccess;
        addressRef = refAddr;
        sizeOfAccess = size;
        addressPC = pcAddr;
    }

    public MemBufferBlock(EventType memAccess, long pcAddr) {
        event = memAccess;
        addressPC = pcAddr;
    }

    public MemBufferBlock() {
        event = EventType.INVALID;
        addressRef = -1;
        sizeOfAccess = -1;
        addressPC = -1;
    }

    public boolean isALoopStart() { return event == EventType.START; }

    public boolean isALoopEnd() { return event == EventType.END; }

    public boolean isALoad() { return event == EventType.LOAD; }

    public boolean isAStore() { return event == EventType.STORE; }

    public EventType getEvent() {
        return event;
    }

    public long getAddressPC() {
        return addressPC;
    }

    public long getAddressRef() {
        return addressRef;
    }

    public long getSizeOfAccess() {
        return sizeOfAccess;
    }
}
