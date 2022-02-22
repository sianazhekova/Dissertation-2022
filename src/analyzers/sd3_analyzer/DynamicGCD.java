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
        ||  (firstInterval instanceof PointPC && secondInterval instanceof PointPC) ) {
            BigInteger overlapLength = getTotalOverlapLength();
            return (overlapLength.equals(BigInteger.ZERO));
        } else {
            // The 2 intervals are of type Stride
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
    public List<BigInteger> extendedEuclid(BigInteger a, BigInteger b) {
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
        solution.add(old_s); solution.add(old_t);

        /* The greatest common divisor: old_r
           The quotients by the gcd: (t, s) */

        return solution;
    }

    public List<BigInteger> extendedEuclidOptimization(BigInteger a, BigInteger b) {
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
        solution.add(old_s); solution.add(bezout_t);

        return solution;
    }

    public long dynamicGCD() {
        // Require low1 <= low2. otherwise swap the strides

        BigInteger low = low1.min(low2);
        BigInteger high = high1.min(high2);
        BigInteger length = getTotalOverlapLength();

        BigInteger strideDistance1 = ((Stride)firstInterval).getStrideDistance();
        BigInteger strideDistance2 = ((Stride)secondInterval).getStrideDistance();

        BigInteger delta = strideDistance1.subtract((low.subtract(low1)).mod(strideDistance1)).mod(strideDistance1);
        BigInteger GCD = strideDistance1.gcd(strideDistance2);
        if ( !(GCD.mod(delta)).equals(BigInteger.ZERO)) {
            return 0;
        }

        BigInteger x;
        BigInteger y;
        // A call to the EXTENDED-EUCLID algorithm to obtain the smallest possible solution pair: (x, y)
        List<BigInteger> bezoutCoefficients = extendedEuclid(strideDistance1, strideDistance2);
        x = bezoutCoefficients.get(0);
        y = bezoutCoefficients.get(1);

        BigInteger LCM = lcm(strideDistance1, strideDistance2);
        BigInteger offset  = ((strideDistance2.multiply(y).multiply(delta).divide(GCD)).add(LCM)).mod(LCM);
        BigInteger result = (length.subtract(offset.add(BigInteger.ONE)).add(LCM)).divide(LCM);

        // Use StrideIterator to obtain totalStrideAccesses, distinctStrideAccesses
        BigInteger totalStrideAccesses = BigInteger.ONE;
        BigInteger distinctStrideAccesses = BigInteger.ONE;

        BigInteger averageOccurrenceCount = totalStrideAccesses.divide(distinctStrideAccesses);

        return result.max(BigInteger.ZERO).multiply(averageOccurrenceCount).longValue();

    }

    // Utility Function
    public static @NotNull BigInteger lcm(@NotNull BigInteger bigInt1, BigInteger bigInt2) {
        return bigInt1.multiply(bigInt2).divide(bigInt1.gcd(bigInt2));
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
