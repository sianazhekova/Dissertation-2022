package analyzers.sd3_analyzer;

import analyzers.baseline_analyzer.IntervalType;
import analyzers.baseline_analyzer.PCPair;
import analyzers.readers.InstructionsFileReader;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Stride implements IntervalType {

    private BigInteger low;
    private BigInteger high;
    private BigInteger strideDistance;
    private BigInteger sizeOfAccess;
    private BigInteger totalNumAccesses;
    private PCPair PCAndReadWrite;

    public Stride() {
        low = BigInteger.valueOf(-1);
        high = BigInteger.valueOf(-1);

        strideDistance = BigInteger.valueOf(0);
        sizeOfAccess = BigInteger.valueOf(0);

        totalNumAccesses = BigInteger.valueOf(0);
        PCAndReadWrite = new PCPair();
    }

    public Stride(BigInteger newLow, @NotNull BigInteger newStrideDistance, BigInteger newSizeOfAccess, BigInteger newTotalNumAccesses, PCPair newPCPair) {
        /* ASSERT the stride distance is not zero */
        assert(!newStrideDistance.equals(BigInteger.ZERO));

        /* If the stride distance is negative, then we need to set the address of the lowest point (low) in the following way, and invert the stride distance afterwards: */
        if (newStrideDistance.signum() == -1) {
            high = newLow;
            newLow = newLow.add((newSizeOfAccess.subtract(BigInteger.ONE)).multiply(newStrideDistance));
            newStrideDistance = newStrideDistance.abs();
        }

        strideDistance = newStrideDistance;
        sizeOfAccess = newSizeOfAccess;

        totalNumAccesses = newTotalNumAccesses;

        PCAndReadWrite = newPCPair;

        low = newLow;
        BigInteger i = sizeOfAccess.subtract(BigInteger.ONE);
        high = low.add(i.multiply(strideDistance));
    }

    public Stride(BigInteger inLow, BigInteger inHigh, BigInteger newStrideDist, BigInteger newSizeOfAccess, BigInteger newTotalNumAccesses, PCPair newPCPair) {
        low = inLow;
        high = inHigh;
        strideDistance = newStrideDist;
        sizeOfAccess = newSizeOfAccess;
        BigInteger diff = high.subtract(low);
        sizeOfAccess = (diff.divide(strideDistance)).add(BigInteger.ONE);

        totalNumAccesses = newTotalNumAccesses;
        PCAndReadWrite = newPCPair;
    }

    public boolean addressWithinBlock(@NotNull BigInteger memAddress) {
        boolean isIn = !(memAddress.compareTo(this.low) == -1 || memAddress.compareTo(this.getHigh()) == 1);

        return isIn;
    }

    /* This is only an approximation - may not always be true with irregular strides such as [10, 14, 18, 14, 18, 22, 18, 22, 26] */
    public boolean containsAddressInStride(@NotNull BigInteger memAddress) {
        BigInteger startOffset = memAddress.subtract(this.low);
        return addressWithinBlock(memAddress) && startOffset.mod(this.strideDistance).equals(BigInteger.ZERO);
    }

    public void expandStride(@NotNull BigInteger newAddress) {

        if (newAddress.compareTo(getLow()) == -1) {
            // memory address to be added is less than the lowest address in the stride
            low = newAddress;
            updateSizeOfAccess();

        } else if (newAddress.compareTo(getHigh()) == 1) {
            // memory address to be added is greater than the highest address in the stride
            high = newAddress;
            updateHighAddress();
        }
        incrementNumAccesses();
    }

    public void deflateStride(@NotNull BigInteger removeAddress) {
        if (removeAddress.equals(getStartAddress())) {
            this.low = getStartAddress().add(strideDistance);
            assert(low.compareTo(high) != 1);

            decrementNumAccesses();
            updateSizeOfAccess();
        } else if (removeAddress.equals(getEndAddress())) {
            this.high = getEndAddress().subtract(strideDistance);
            assert(high.compareTo(low) != -1);

            decrementNumAccesses();
            updateSizeOfAccess();
        } else {
            decrementNumAccesses();
        }
    }

    public void mergeWithStride(@NotNull Stride otherStride) {
        this.setLow(this.getStartAddress().min(otherStride.getStartAddress()));
        this.setHigh(this.getEndAddress().max(otherStride.getEndAddress()));

        this.setNumAccesses(this.getTotalNumAccesses() + (otherStride.getTotalNumAccesses()));
        this.setSizeOfAccess((this.getHigh().subtract(this.getLow()).add(BigInteger.ONE)).divide(this.strideDistance));   // Leave it like that for now

    }

    public List<BigInteger> getCrossSection(@NotNull Stride otherStride) {
        // Approximation of stride cross-section
        ArrayList<BigInteger> list = new ArrayList<>();

        BigInteger otherStart = otherStride.getStartAddress();
        BigInteger otherEnd = otherStride.getEndAddress();

        BigInteger newStart = this.low.max(otherStart);

        if (!newStart.equals(this.low) && !containsAddressInStride(newStart) ) {
            newStart  = this.low.add( ((newStart.mod(strideDistance)).subtract(BigInteger.ONE)).multiply(strideDistance) );
            newStart = this.low.max(newStart);
        }

        BigInteger newEnd = this.high.min(otherEnd);

        if (!newEnd.equals(this.high) && !containsAddressInStride(newEnd)) {
            newEnd = this.low.add( ((newStart.mod(strideDistance)).subtract(BigInteger.ONE)).multiply(strideDistance) );
            newEnd = this.high.min(newEnd);
        }
        list.add(0, newStart);
        list.add(1, newEnd);

        return list;
    }

    public BigInteger returnNearestAddress(BigInteger inAddress) {
        assert(addressWithinBlock(inAddress));
        BigInteger returnAddress;

        BigInteger remainder = inAddress.mod(strideDistance);
        returnAddress = this.low.add(remainder.multiply(strideDistance));

        return returnAddress;
    }

    public String getStringStrideState() {
        return " | Lowest Address: 0x" + InstructionsFileReader.toHexString(this.low) + " | " +
                " | Highest Address: 0x" + InstructionsFileReader.toHexString(this.high) + " | " +
                " | Stride Distance: " + this.strideDistance.toString(10) + " | " +
                " | Total Number of Accesses: " + this.totalNumAccesses.longValue() + " | " +
                " | Length of Stride : " + this.sizeOfAccess + " | " +
                PCAndReadWrite.getPCPairString();
    }

    public String getTestStringStrideState() {
        return " | Lowest Address: " + this.low + " | " +
                " | Highest Address: " + this.high + " | " +
                " | Stride Distance: " + this.strideDistance.toString(10) + " | " +
                " | Total Number of Accesses: " + this.totalNumAccesses.longValue() + " | " +
                " | Length of Stride : " + this.sizeOfAccess + " | " +
                PCAndReadWrite.getPCPairString();
    }

    public void printStrideState() {
        System.out.println(getStringStrideState());
    }

    public void printTestStrideState() { System.out.println(getTestStringStrideState());}

    public void updateHighAddress(BigInteger newEnd) {
        high = newEnd;
    }

    public void updateHighAddress() { // high = max(high, low + i * stride)
        BigInteger i = sizeOfAccess.subtract(BigInteger.ONE);
        high = high.max(low.add(i.multiply(strideDistance)));
    }

    public void addNewSizeOfAccess(BigInteger addSize) {
        sizeOfAccess = sizeOfAccess.add(addSize);
    }

    public void updateSizeOfAccess() {
        BigInteger diff  = this.getEndAddress().subtract(this.getStartAddress());
        sizeOfAccess = diff.divide(this.strideDistance).add(BigInteger.ONE);
    }

    public void incrementSizeOfAccess() {
        addNewSizeOfAccess(BigInteger.ONE);
    }

    public void decrementSizeOfAccesses() {
        sizeOfAccess = sizeOfAccess.subtract(BigInteger.ONE);
    }

    public void incrementNumAccesses() {
       totalNumAccesses = totalNumAccesses.add(BigInteger.ONE);
    }

    public void decrementNumAccesses() {
        totalNumAccesses = totalNumAccesses.subtract(BigInteger.ONE);
    }

    public void addNumAccesses(long addAccesses) {
       totalNumAccesses = totalNumAccesses.add(BigInteger.valueOf(addAccesses));
    }

    public BigInteger getSecondToLastAddress() {
        return high.subtract(strideDistance);
    }

    public BigInteger getLow() {
        return low;
    }

    @Override
    public BigInteger getStartAddress() {
        return getLow();
    }

    @Override
    public BigInteger getEndAddress() {
        return getHigh();
    }

    public void setLow(BigInteger newLow) {
        this.low = newLow;
    }

    public BigInteger getHigh() {
        return high;
    }

    public void setHigh(BigInteger newHigh) {
        this.high = newHigh;
    }

    public BigInteger getStrideDistance() {
        return strideDistance;
    }

    public BigInteger getSizeOfAccess() {
        return sizeOfAccess;
    }

    public void setSizeOfAccess(BigInteger sizeOfAccess) {
        this.sizeOfAccess = sizeOfAccess;
    }

    public long getTotalNumAccesses() {
        return totalNumAccesses.longValue();
    }

    public long getNumDistinctAddr() {
        return sizeOfAccess.longValue();
    }

    public void setNumAccesses(long numAccesses) {
        this.totalNumAccesses = BigInteger.valueOf(numAccesses);
    }

    public void incrementNumAccesses(long addNumAccesses) {
        this.totalNumAccesses = this.totalNumAccesses.add(BigInteger.valueOf(addNumAccesses));
    }

    public PCPair getPCAndReadWrite() {
        return PCAndReadWrite;
    }

    public void setPCAndReadWrite(PCPair newPCPair) { this.PCAndReadWrite = newPCPair; }

    @Override
    public IntervalType copy() {
        // TODO
        Stride strideToCopy = new Stride(low, high, strideDistance, sizeOfAccess, totalNumAccesses, PCAndReadWrite);

        return strideToCopy;
    }

}
