package analyzers.sd3_analyzer;

import analyzers.baseline_analyzer.IntervalType;
import analyzers.baseline_analyzer.PCPair;

import java.math.BigInteger;

public class Stride implements IntervalType {

    private BigInteger low;
    private BigInteger high;
    private BigInteger strideDistance;
    private BigInteger sizeOfAccess;
    private long totalNumAccesses;
    private PCPair PCAndReadWrite;

    public Stride() {
        low = BigInteger.valueOf(-1);
        high = BigInteger.valueOf(-1);
        strideDistance = BigInteger.valueOf(0);
        sizeOfAccess = BigInteger.valueOf(0);
        totalNumAccesses = -1;
        PCAndReadWrite = new PCPair();
    }

    public Stride(BigInteger newLow, BigInteger newHigh, BigInteger newStrideDistance, BigInteger newSizeOfAccess, Long newTotalNumAccesses, PCPair newPCPair) {
        low = newLow;
        high = newHigh;
        strideDistance = newStrideDistance;
        sizeOfAccess = newSizeOfAccess;
        totalNumAccesses = newTotalNumAccesses;
        PCAndReadWrite = newPCPair;
    }

    public BigInteger getLow() {
        return low;
    }

    public void setLow(BigInteger low) {
        this.low = low;
    }

    public BigInteger getHigh() {
        return high;
    }

    public void setHigh(BigInteger high) {
        this.high = high;
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
        return totalNumAccesses;
    }

    public void setNumAccesses(long numAccesses) {
        this.totalNumAccesses = numAccesses;
    }

    public void incrementNumAccesses(long addNumAccesses) {
        this.totalNumAccesses += addNumAccesses;
    }

    public PCPair getPCAndReadWrite() {
        return PCAndReadWrite;
    }
}
