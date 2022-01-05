package analyzers.baseline_analyzer;

// Maps to Point

public class PointPC {
    public long refStartAddress;
    public long endAddress;

    public PCPair pcPair;  // TO-DO Maybe: Encapsulate that in a PC-Interface, so PointPC implements it

    public PointPC() {
        refStartAddress = -1;
        endAddress = -1;
        pcPair = new PCPair();
    }

    public PointPC(long refAddress, long sizeOfAccess, PCPair PCAddress) {
        refStartAddress = refAddress;
        endAddress = refStartAddress + sizeOfAccess;
        pcPair = PCAddress;
    }

    public PointPC(long refMemAddress, long sizeOfAccess, MemoryAccess readOrWrite, long PCAddress) {
        refStartAddress = refMemAddress;
        endAddress = refMemAddress + sizeOfAccess;
        pcPair = new PCPair(PCAddress, readOrWrite);
    }

    public long getRefStartAddress() {
        return refStartAddress;
    }

    public PCPair getPCPair() {
        return pcPair;
    }
}
