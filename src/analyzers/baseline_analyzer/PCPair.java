package analyzers.baseline_analyzer;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public class PCPair implements Comparable<PCPair>{
    protected BigInteger PC;
    protected MemoryAccess readOrWrite;

    public PCPair() {
        PC = BigInteger.valueOf(-1);
        readOrWrite = MemoryAccess.INVALID;
    }

    public PCPair(BigInteger PCAddress, MemoryAccess memoryAccess) {
        PC = PCAddress;
        readOrWrite = memoryAccess;
    }

    public BigInteger getPC() {
        return PC;
    }

    public MemoryAccess getMemAccessType() {
        return readOrWrite;
    }

    @Override
    public int compareTo(@NotNull PCPair otherPair) {
        int comparison = this.PC.compareTo(otherPair.getPC());

        if (comparison != 0)
            return comparison;

        return Integer.compare(readOrWrite.getIntID(), otherPair.getMemAccessType().getIntID());
    }

    public boolean equalTo(@NotNull PCPair otherPair) {
        return this.PC.equals(otherPair.getPC()) && this.readOrWrite.getIntID() == otherPair.getMemAccessType().getIntID();
    }

}
