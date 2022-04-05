package analyzers.sd3_analyzer.unit_tests;

import analyzers.baseline_analyzer.IntervalType;
import analyzers.baseline_analyzer.MemoryAccess;
import analyzers.baseline_analyzer.PCPair;
import analyzers.baseline_analyzer.PointPC;
import analyzers.sd3_analyzer.DynamicGCD;
import analyzers.sd3_analyzer.InvalidIntervalTypeException;
import analyzers.sd3_analyzer.Stride;
import org.junit.jupiter.api.*;
import utils.IterableUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class DynamicGCDTest {

    private final Logger logger = Logger.getLogger(DynamicGCDTest.class.getName());
    private List<DynamicGCD> gcdTestList;
    private List<BigInteger> ints1;
    private List<BigInteger> ints2;
    private List<BigInteger> ints3;
    private long[][] longDivs = new long[][]{ {1, 10}, {2, 5}, {7, 21}, {1000, 100}, {9, 9}, {123456789, 9}, {0, 5}, {8, 0}, {7, 6}, {-2, -6}, {-3, 30} };
    private List<IntervalType> intervals;

    @BeforeEach
    public void setUp() {
        gcdTestList = new ArrayList<>();
        ints1 = LongStream.iterate(1000, n -> n + 1).limit(20).mapToObj(i -> BigInteger.valueOf(i)).collect(Collectors.toList());
        ints2 = LongStream.iterate(3050, n -> n + 1).limit(20).mapToObj(i -> BigInteger.valueOf(i)).collect(Collectors.toList());
        ints3 = LongStream.iterate(0, n -> n + 1).limit(20).mapToObj(i -> BigInteger.valueOf(i)).collect(Collectors.toList());
        intervals = new ArrayList<>();
        intervals.add(new PointPC(BigInteger.valueOf(12345), BigInteger.valueOf(100), MemoryAccess.WRITE, BigInteger.valueOf(111)));
        intervals.add(new PointPC(BigInteger.valueOf(1000), BigInteger.valueOf(100), MemoryAccess.WRITE, BigInteger.valueOf(111)));
        intervals.add(new PointPC(BigInteger.valueOf(12300), BigInteger.valueOf(100), MemoryAccess.WRITE, BigInteger.valueOf(111)));
        intervals.add(new PointPC(BigInteger.valueOf(12445), BigInteger.valueOf(1), MemoryAccess.WRITE, BigInteger.valueOf(111)));
    }

    @Test
    public void testDynamicIntervalOverlap() {
        /* TODO */
    }

    @Test
    public void testGetTotalOverlapLength() throws InvalidIntervalTypeException {
        logger.info("Testing the general length of overlaps");
        DynamicGCD testInterval = new DynamicGCD(intervals.get(0), intervals.get(2));
        DynamicGCD testInterval2 = new DynamicGCD(intervals.get(0), intervals.get(3));
        DynamicGCD testInterval3 = new DynamicGCD(intervals.get(0), intervals.get(1));

        BigInteger overlapLen1 = testInterval.getTotalOverlapLength();
        BigInteger overlapLen2 = testInterval2.getTotalOverlapLength();
        BigInteger overlapLen3 = testInterval3.getTotalOverlapLength();

        //Assertions.assertEquals(overlapLen1, );
        //Assertions.assertEquals();
        //Assertions.assertEquals();

    }

    @TestFactory
    public Stream<DynamicTest> testExtendedEuclid() {
        logger.info("Testing the Extended Euclid (Standard) algorithm");

        return Arrays.stream(longDivs).map(entry -> {
            BigInteger num1 = BigInteger.valueOf(entry[0]);
            BigInteger num2 = BigInteger.valueOf(entry[1]);
            List<BigInteger> solutionList = DynamicGCD.extendedEuclid(num1, num2);
            BigInteger gcd = solutionList.get(2);
            return dynamicTest(num1 + " and " + num2
                    + " have a GCD of " +  gcd
                    +  " and coefficients (t, s) " + solutionList.get(0) + " , " + solutionList.get(1), () -> {
                assertEquals(gcd, num1.gcd(num2));
            });
        });
    }

    @TestFactory
    public Stream<DynamicTest> testExtendedEuclidOptimization() {
        logger.info("Testing the Extended Euclid (Optimised) algorithm");

        return Arrays.stream(longDivs).map(entry -> {
            BigInteger num1 = BigInteger.valueOf(entry[0]);
            BigInteger num2 = BigInteger.valueOf(entry[1]);
            List<BigInteger> solutionList = DynamicGCD.extendedEuclidOptimization(num1, num2);
            BigInteger gcd = solutionList.get(2);
            return dynamicTest(num1 + " and " + num2
                    + " have a GCD of " +  gcd
                    +  " and coefficients (t, s) " + solutionList.get(0) + " , " + solutionList.get(1), () -> {
                assertEquals(gcd, num1.gcd(num2));
            });
        });
    }

    @Test
    public void testDynamicGCD() {
        /* For now, it will only be tested on strides */
        logger.info("Testing the Dynamic GCD algorithm");

        List<Stride> strideList = List.of(new Stride(BigInteger.valueOf(20), BigInteger.valueOf(2), BigInteger.valueOf(6), BigInteger.valueOf(6), new PCPair(BigInteger.valueOf(123), MemoryAccess.WRITE)),
                new Stride(BigInteger.valueOf(21), BigInteger.valueOf(3), BigInteger.valueOf(6), BigInteger.valueOf(6), new PCPair(BigInteger.valueOf(123), MemoryAccess.WRITE)),
                new Stride(BigInteger.valueOf(20), BigInteger.valueOf(2), BigInteger.valueOf(6), BigInteger.valueOf(10), new PCPair(BigInteger.valueOf(123), MemoryAccess.WRITE)),
                new Stride(BigInteger.valueOf(1000), BigInteger.valueOf(5), BigInteger.valueOf(10), BigInteger.valueOf(10), new PCPair(BigInteger.valueOf(123), MemoryAccess.WRITE)),
                new Stride(BigInteger.valueOf(1001), BigInteger.valueOf(4), BigInteger.valueOf(20), BigInteger.valueOf(20), new PCPair(BigInteger.valueOf(123), MemoryAccess.WRITE)),
                new Stride(BigInteger.valueOf(1000), BigInteger.valueOf(5), BigInteger.valueOf(10), BigInteger.valueOf(40), new PCPair(BigInteger.valueOf(123), MemoryAccess.WRITE)),
                new Stride(BigInteger.valueOf(1000), BigInteger.valueOf(5), BigInteger.valueOf(10), BigInteger.valueOf(40), new PCPair(BigInteger.valueOf(123), MemoryAccess.WRITE)),
                new Stride(BigInteger.valueOf(980), BigInteger.valueOf(20), BigInteger.valueOf(6), BigInteger.valueOf(5), new PCPair(BigInteger.valueOf(123), MemoryAccess.WRITE))
        );

        BiFunction<Stride, Stride, Float> funcGCD = (s1, s2) -> {
            try {
                return new DynamicGCD(s1, s2).dynamicGCD();
            } catch (InvalidIntervalTypeException e) {
                e.printStackTrace();
            }
            return null;
        };

        IterableUtils.iterateTuples(strideList, (s1, s2) -> {
            float numOverlaps = funcGCD.apply(s1, s2);
            System.out.println("For strides " + s1.getTestStringStrideState() +
                    " and " + s2.getTestStringStrideState() +
                    ", the number of overlaps obtained via the Dynamic-GCD method is "
                    + numOverlaps
            );
        });
    }

    @TestFactory
    public Stream<DynamicTest> testLCM() {
        logger.info("Testing the helper function for obtaining the Least Common Multiple");

        return Arrays.stream(longDivs).map(entry -> {
            BigInteger num1 = BigInteger.valueOf(entry[0]);
            BigInteger num2 = BigInteger.valueOf(entry[1]);
            BigInteger lcm = DynamicGCD.lcm(num1, num2);

            return dynamicTest(num1 + " and " + num2
                    + " have a LCM of " +  lcm, () -> {
                assertTrue(lcm.abs().compareTo(BigInteger.ZERO) >= 0);
            });
        });

    }
}
