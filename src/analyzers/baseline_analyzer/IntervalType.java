package analyzers.baseline_analyzer;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public interface IntervalType extends Comparable<IntervalType> {

    /**
     * TO-DO : FIX THIS for CLOSED INTERVALS
     * */

    /* An Interface denoting the common superclass for the Stride and PointPC classes that groups them together.
       Adapted from: https://stackoverflow.com/questions/16033711/java-iterating-over-every-two-elements-in-a-list  */

    default boolean isAdjacent(@NotNull IntervalType another) {
        return getStartAddress().equals(another.getEndAddress()) || getEndAddress().equals(another.getStartAddress());
    }

    default boolean hasOverlap(@NotNull IntervalType another){
        return getEndAddress().compareTo(another.getStartAddress()) == 1 && another.getEndAddress().compareTo(getStartAddress()) == 1;
    }

    /* Obtaining the length of the interval */
    default BigInteger getLength() {
        return getEndAddress().subtract(getStartAddress()).add(BigInteger.ONE);
    }

    default int compareTo(@NotNull IntervalType anotherInterval) {
        BigInteger thisStart = getStartAddress();
        BigInteger otherStart = anotherInterval.getStartAddress();

        BigInteger thisEnd = getEndAddress();
        BigInteger otherEnd = anotherInterval.getEndAddress();

        if ( thisStart.compareTo(otherStart) == 1) {
            return 1;
        } else if ( thisStart.compareTo(otherStart) == -1 ) {
            return -1;
        } else if ( thisEnd.compareTo(otherEnd) == 1 ) {
            return 1;
        } else if ( thisEnd.compareTo(otherEnd) == -1 ) {
            return -1;
        } else {
            return 0;
        }
    }

    /* Obtaining the start address of the interval */
    BigInteger getStartAddress();

    /* Obtaining the end address of the interval */
    BigInteger getEndAddress();

}