package analyzers.sd3_analyzer;

import analyzers.baseline_analyzer.IntervalType;
import analyzers.baseline_analyzer.PointPC;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class DynamicGCD {

    private BigInteger low1;
    private BigInteger low2;

    private BigInteger high1;
    private BigInteger high2;

    private IntervalType firstInterval;
    private IntervalType secondInterval;

    public DynamicGCD(IntervalType interval1, IntervalType interval2) throws InvalidIntervalTypeException {
        if (!((interval1 instanceof Stride) || (interval2 instanceof Stride) || (interval1 instanceof PointPC) || (interval2 instanceof PointPC)))
            throw new InvalidIntervalTypeException("The provided intervals are not in a valid format.");

        if (interval1 instanceof Stride) {
            low1 = ((Stride) interval1).getLow();
            high1 = ((Stride) interval1).getHigh();
        } else {
            low1 = ((PointPC)interval1).getRefStartAddress();
            high1 = ((PointPC)interval1).getEndAddress();
        }

        if (interval2 instanceof Stride) {
            low2 = ((Stride) interval2).getLow();
            high2 = ((Stride) interval2).getHigh();
        } else {
            low2 = ((PointPC)interval2).getRefStartAddress();
            high2 = ((PointPC)interval2).getEndAddress();
        }

        firstInterval = interval1;
        secondInterval = interval2;
    }

    public boolean intervalsOverlap() {
        if ((firstInterval instanceof PointPC && secondInterval instanceof Stride) || (firstInterval instanceof Stride && secondInterval instanceof PointPC)
        ||  (firstInterval instanceof PointPC && secondInterval instanceof PointPC)) {
            BigInteger overlapLength = getTotalOverlapLength();
            return (overlapLength.compareTo(BigInteger.ZERO) == 1);
        } else {
            // The 2 intervals are of type Stride -> need to use the interval tree overlap test
            //TODO
            return true;
        }
    }

    public BigInteger getTotalOverlapLength() {
        BigInteger interval1Length = high1.subtract(low1);
        BigInteger interval2Length = high2.subtract(low2);
        BigInteger minIntervalLength = interval1Length.min(interval2Length);

        BigInteger crossLength12 = high2.subtract(low1);
        BigInteger crossLength21 = high1.subtract(low2);
        BigInteger minCrossLength = crossLength12.min(crossLength21);

        int comparison = minIntervalLength.compareTo(minCrossLength);

        if (comparison == -1 || comparison == 0) {
            return minIntervalLength;
        } else if (minCrossLength.compareTo(BigInteger.ZERO) == 1) {
            return minCrossLength;
        } else {
            return BigInteger.ZERO;
        }
    }

    /* The Extended Euclidean algorithm that aims to find the smallest solution pair (x, y) to the Diophantine equation ax + by = gcd(a, b)
       There are two versions - the standard extended-Euclid method and the optimization (which should only be used for inputs that are less than half the maximal size,
       otherwise an overflow error may occur when computing old_s * a.
       Code has been adapted from:  http://en.wikipedia.org/wiki/Extended_Euclidean_algorithm
    */
    public static @NotNull List<BigInteger> extendedEuclid(BigInteger a, BigInteger b) {
        a = a.abs();
        b = b.abs();

        List<BigInteger> solution = new ArrayList<>();

        BigInteger s = BigInteger.ZERO;
        BigInteger old_s = BigInteger.ONE;

        BigInteger t = BigInteger.ONE;
        BigInteger old_t = BigInteger.ZERO;

        BigInteger r = b;
        BigInteger old_r = a;

        BigInteger temp;
        BigInteger quotient;
        while (!r.equals(BigInteger.ZERO)) {
            quotient = old_r.divide(r);
            temp = r; r = old_r.subtract(quotient.multiply(r)); old_r = temp;
            temp = t; t = old_t.subtract(quotient.multiply(t)); old_t = temp;
            temp = s; s = old_s.subtract(quotient.multiply(s)); old_s = temp;
        }
        /* The Bezout's coefficients: (old_s, old_t) */
        solution.add(old_s); solution.add(old_t); solution.add(old_r);

        /* The greatest common divisor: old_r
           The quotients by the gcd: (t, s) */

        return solution;
    }

    // Runtime optimisation
    public static @NotNull List<BigInteger> extendedEuclidOptimization(BigInteger a, BigInteger b) {
        a = a.abs();
        b = b.abs();

        List<BigInteger> solution = new ArrayList<>();
        BigInteger s = BigInteger.ZERO;
        BigInteger old_s = BigInteger.ONE;

        BigInteger r = b;
        BigInteger old_r = a;

        BigInteger quotient;
        BigInteger temp;
        while (!r.equals(BigInteger.ZERO)) {
            quotient = old_r.divide(r);
            temp = old_r; old_r = r; r = temp.subtract(quotient.multiply(r));
            temp = old_s; old_s = s; s = temp.subtract(quotient.multiply(s));
        }

        BigInteger bezout_t;
        if (!b.equals(BigInteger.ZERO)) {
            bezout_t = (old_r.subtract(old_s.multiply(a))).divide(b);

        } else {
            bezout_t = BigInteger.ZERO;
        }

        /* The greatest common divisor is: old_r */

        /* The Bezout coefficients are: (old_s, bezout_t)  */
        solution.add(old_s); solution.add(bezout_t); solution.add(old_r);

        return solution;
    }

    public float dynamicGCD() {
        // Require low1 <= low2, otherwise swap the strides and the appropriate state
        if (this.low2.compareTo(this.low1) == -1) {
            swapState();
        }

        //BigInteger length = getTotalOverlapLength();
        BigInteger high = high1.min(high2);
        BigInteger length = high.subtract(low2).add(BigInteger.ONE);

        BigInteger strideDistance1 = ((Stride)firstInterval).getStrideDistance();
        BigInteger strideDistance2 = ((Stride)secondInterval).getStrideDistance();
        System.out.println("The first stride distance is " + strideDistance1 + " and the second stride distance is " + strideDistance2);

        // Use StrideIterator to obtain totalStrideAccesses, distinctStrideAccesses
        BigInteger totalStrideAccesses = BigInteger.valueOf(((Stride) firstInterval).getTotalNumAccesses());
        BigInteger distinctStrideAccesses = BigInteger.valueOf(((Stride) firstInterval).getNumDistinctAddr());
        float averageOccurrenceCount = totalStrideAccesses.floatValue()/distinctStrideAccesses.floatValue();

        System.out.println("The total number of accesses (in lower stride 1) is " + totalStrideAccesses +
                " and the total number of distinct accesses (in lower stride 1) is " + distinctStrideAccesses
        );

        System.out.println("The average occurrence count is " + averageOccurrenceCount);

        if (low1.equals(low2) && strideDistance1.equals(strideDistance2)) {
            return ((Stride) firstInterval).getSizeOfAccess().max(BigInteger.ZERO).floatValue() * averageOccurrenceCount;

        }

        BigInteger delta = strideDistance1.subtract( (low2.subtract(low1)).mod(strideDistance1) ).mod(strideDistance1);

        System.out.println("delta is " + delta);

        BigInteger GCD = strideDistance1.gcd(strideDistance2);

        System.out.println("GCD is " + GCD);


        if ( delta.equals(BigInteger.ZERO) || !(GCD.mod(delta)).equals(BigInteger.ZERO)) {
            return 0;
        }

        BigInteger x;
        BigInteger y;
        // A call to the EXTENDED-EUCLID algorithm to obtain the smallest possible solution pair: (x, y)
        List<BigInteger> bezoutCoefficients = extendedEuclid(strideDistance1, strideDistance2);
        x = bezoutCoefficients.get(0);
        y = bezoutCoefficients.get(1);

        System.out.println("Bezout coefficient y is  " + y);

        BigInteger LCM = lcm(strideDistance1, strideDistance2);

        System.out.println("LCM is " + LCM);

        BigInteger offset  = ((strideDistance2.multiply(y).multiply(delta).divide(GCD)).add(LCM)).mod(LCM);

        System.out.println("The offset is " + offset);

        BigInteger result = (length.subtract(offset.add(BigInteger.ONE)).add(LCM)).divide(LCM);

        System.out.println("The result is " + result);

        return result.max(BigInteger.ZERO).floatValue() * averageOccurrenceCount;
    }

    // Utility Function
    public static @NotNull BigInteger lcm(@NotNull BigInteger bigInt1, BigInteger bigInt2) {
        return bigInt1.multiply(bigInt2).divide(bigInt1.gcd(bigInt2));
    }

    public void swapState() {

        BigInteger tempLow1 = this.low1;
        this.low1 = this.low2;
        this.low2 = tempLow1;

        BigInteger tempHigh1 = this.high1;
        this.high1 = this.high2;
        this.high2 = tempHigh1;

        IntervalType tempInterval = firstInterval;
        firstInterval = secondInterval;
        secondInterval = tempInterval;
    }

    public BigInteger getLow1() {
        return low1;
    }

    public BigInteger getLow2() {
        return low2;
    }

    public BigInteger getHigh1() {
        return high1;
    }

    public BigInteger getHigh2() {
        return high2;
    }

    public IntervalType getFirstStride() {
        return firstInterval;
    }

    public IntervalType getSecondStride() {
        return secondInterval;
    }
}
