package analyzers.readers;

/* This class represents a data structure that stores the type of memory access,
    the address referenced, the size of memory access (in Bytes), and the PC address
* */

import java.math.BigInteger;

public class MemBufferBlock {
    private EventType event;
    private BigInteger addressRef;
    private BigInteger sizeOfAccess;
    private BigInteger addressPC;

    public MemBufferBlock(EventType memAccess, BigInteger refAddr, BigInteger size, BigInteger pcAddr) {
        event = memAccess;
        addressRef = refAddr;
        sizeOfAccess = size;
        addressPC = pcAddr;
    }

    public MemBufferBlock(EventType memAccess, BigInteger pcAddr) {
        event = memAccess;
        addressPC = pcAddr;
    }

    public MemBufferBlock() {
        event = EventType.INVALID;
        addressRef = BigInteger.valueOf(-1);
        sizeOfAccess = BigInteger.valueOf(-1);
        addressPC = BigInteger.valueOf(-1);
    }

    public boolean isALoopStart() { return event == EventType.START; }

    public boolean isALoopEnd() { return event == EventType.END; }

    public boolean isALoad() { return event == EventType.LOAD; }

    public boolean isAStore() { return event == EventType.STORE; }

    public EventType getEvent() {
        return event;
    }

    public BigInteger getAddressPC() {
        return addressPC;
    }

    public BigInteger getAddressRef() {
        return addressRef;
    }

    public BigInteger getSizeOfAccess() {
        return sizeOfAccess;
    }
}
