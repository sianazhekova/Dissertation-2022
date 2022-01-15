package analyzers.baseline_analyzer;

// Maps to Point

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public class PointPC {
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

    public PCPair getPCPair() {
        return pcPair;
    }
}
