package analyzers.baseline_analyzer;
import java.util.*;

/*
    This class will be storing the type of instruction-level dependence that has occurred for a referenced memory address (approximately),
    the Program Counters (PC addresses) of the two instructions causing that conflict,
    the number of occurrences of that dependence at the time it had been last reported within the scope of a given loop instance iteration,
    the trip count of the most recent memory access causing that dependence.
*/

import org.jetbrains.annotations.NotNull;

public class InstructionLevelConflict {

    private Set<Long> approxMemAddressSet = new HashSet<>();
    DataDependence typeOfConflict;
    private PCPair prevInstruction;
    private PCPair nextInstruction;
    private long frequencyCount;
    private long beginTripCount;
    private long endTripCount;

    public InstructionLevelConflict() {
        typeOfConflict = DataDependence.DEPNONE;
    }

    public InstructionLevelConflict(long refAddress, @NotNull PCPair prevInstr, @NotNull PCPair nextInstr, long freqCount, long beginTime, long endTime) {
        approxMemAddressSet.add(refAddress);
        typeOfConflict = DataDependence.getDependence(prevInstr.getMemAccessType(), nextInstr.getMemAccessType());
        prevInstruction = prevInstr;
        nextInstruction = nextInstr;
        frequencyCount = freqCount;
        beginTripCount = beginTime;
        endTripCount = endTime;
    }

    public String convertAddressSetToString(@NotNull Set<Long> setOfAddresses) {
        StringBuilder sb = new StringBuilder();
        for (Long address : setOfAddresses) {
            sb.append("{" + address + "}");
        }
        return sb.toString();
    }

    public boolean isAdditive(@NotNull InstructionLevelConflict anotherConflict) {
        return (prevInstruction.equalTo(anotherConflict.getPrevInstruction()) && nextInstruction.equalTo(anotherConflict.getNextInstruction())
                && typeOfConflict.equals(anotherConflict.getTypeOfConflict()));
    }

    public void addCountsFrom(InstructionLevelConflict anotherConflict) {
        assert(isAdditive(anotherConflict));
        frequencyCount += anotherConflict.getFrequencyCount();
        approxMemAddressSet.addAll(anotherConflict.getApproxMemAddressSet());
        setEndTripCount(anotherConflict.getEndTripCount());
    }

    public Set<Long> getApproxMemAddressSet() {
        return approxMemAddressSet;
    }

    public PCPair getPrevInstruction() {
        return prevInstruction;
    }

    public PCPair getNextInstruction() {
        return nextInstruction;
    }

    public DataDependence getTypeOfConflict() {
        return typeOfConflict;
    }

    public long getFrequencyCount() {
        return frequencyCount;
    }

    public long getBeginTripCount() {
        return beginTripCount;
    }

    public long getEndTripCount() {
        return endTripCount;
    }

    public void setPrevInstruction(PCPair newBeginInstr) {
        prevInstruction = newBeginInstr;
    }

    public void setNextInstruction(PCPair newLastInstr) {
        nextInstruction = newLastInstr;

    }
    public void setEndTripCount(long newEndTime) {
        endTripCount = newEndTime;
    }

    public void updateFrequencyCount(long changeCount) {
        frequencyCount += changeCount;
    }

    public void setFrequencyCount(long newFreqCount) {
        frequencyCount = newFreqCount;
    }

    public String printToString() {
        return String.format("| RefAddr: %d |\n | ConflictType: %s |\n | PCFirst: %s |\n | PCLast: %s |\n | FreqCount: %d |\n | StartTripCount: %d |\n | LastTripCount : %d |\n",
                convertAddressSetToString(approxMemAddressSet),
                DataDependence.getStringDepType(typeOfConflict),
                prevInstruction.getPC(),
                nextInstruction.getPC(),
                frequencyCount,
                beginTripCount,
                endTripCount
        );
    }
}
