package analyzers.baseline_analyzer;

import org.jetbrains.annotations.NotNull;

public class PCPair implements Comparable<PCPair>{
    protected long PC;
    protected MemoryAccess readOrWrite;

    public PCPair() {
        PC = -1;
        readOrWrite = MemoryAccess.INVALID;
    }

    public PCPair(long PCAddress, MemoryAccess memoryAccess) {
        PC = PCAddress;
        readOrWrite = memoryAccess;
    }

    public long getPC() {
        return PC;
    }

    public MemoryAccess getMemAccessType() {
        return readOrWrite;
    }

    @Override
    public int compareTo(@NotNull PCPair otherPair) {
        int comparison = Long.compare(this.PC, otherPair.getPC());

        if (comparison != 0)
            return comparison;

        return Integer.compare(readOrWrite.getIntID(), otherPair.getMemAccessType().getIntID());
    }

    public boolean equalTo(@NotNull PCPair otherPair) {
        return this.PC == otherPair.getPC() && this.readOrWrite.getIntID() == otherPair.getMemAccessType().getIntID();
    }

}
