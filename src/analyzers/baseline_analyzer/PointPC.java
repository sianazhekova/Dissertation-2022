package analyzers.baseline_analyzer;

// Maps to Point

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public class PointPC implements IntervalType {
    public BigInteger refStartAddress;
    public BigInteger endAddress;

    public PCPair pcPair;  // TO-DO Maybe: Encapsulate that in a PC-Interface, so PointPC implements it

    public PointPC() {
        refStartAddress = BigInteger.valueOf(-1);
        endAddress = BigInteger.valueOf(-1);
        pcPair = new PCPair();
    }

    public PointPC(BigInteger refAddress, BigInteger sizeOfAccess, PCPair PCAddress) {
        refStartAddress = refAddress;
        endAddress = refStartAddress.add(sizeOfAccess);
        pcPair = PCAddress;
    }

    public PointPC(@NotNull BigInteger refMemAddress, BigInteger sizeOfAccess, MemoryAccess readOrWrite, BigInteger PCAddress) {
        refStartAddress = refMemAddress;
        endAddress = refMemAddress.add(sizeOfAccess);
        pcPair = new PCPair(PCAddress, readOrWrite);
    }

    public BigInteger getRefStartAddress() {
        return refStartAddress;
    }

    public BigInteger getStartAddress() { return getRefStartAddress(); }

    public BigInteger getEndAddress() { return endAddress; }

    public PCPair getPCPair() {
        return pcPair;
    }

    @Override
    public boolean isAdjacent(IntervalType another) {
        return false;
    }

    @Override
    public boolean hasOverlap(IntervalType another) {
        return false;
    }
}
