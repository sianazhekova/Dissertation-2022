package analyzers.sd3_analyzer.unit_tests;

import analyzers.baseline_analyzer.MemoryAccess;
import analyzers.baseline_analyzer.PCPair;
import analyzers.readers.InstructionsFileReader;
import analyzers.sd3_analyzer.Stride;
import org.junit.jupiter.api.*;

import javax.print.StreamPrintServiceFactory;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.LongStream;
import java.util.stream.Stream;


import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@DisplayName("Stride Test")
public class StrideTest {

    private final int TEST_LIMIT = 15;
    private Stride testStride1;

    private List<BigInteger> accessArray1;
    private List<BigInteger> addressesToAdd1;

    private List<BigInteger> accessArray2;
    private List<BigInteger> addressesToAdd2;

    private final Logger logger = Logger.getLogger(StrideTest.class.getName());

    @BeforeEach
    void setUp() {
        logger.info("Creating an empty stride");
        testStride1 = new Stride();
        testStride1.printStrideState();
        accessArray1 = LongStream.iterate(1000, n -> n + 4).limit(TEST_LIMIT).mapToObj(num -> BigInteger.valueOf(num)).toList();

        addressesToAdd1 =  List.of(BigInteger.valueOf(996), BigInteger.valueOf(1004), BigInteger.valueOf(1008), BigInteger.valueOf(TEST_LIMIT*4 + 1000),  BigInteger.valueOf(TEST_LIMIT*4 + 1004), BigInteger.valueOf(992));
        accessArray2 = List.of(BigInteger.valueOf(1004), BigInteger.valueOf(1040), BigInteger.valueOf(1002), BigInteger.valueOf(1060), BigInteger.valueOf(1064), BigInteger.valueOf(1056));
    }

    @Test
    void strideCreation() {
        logger.info("Initialising a non-empty test stride");
        testStride1 = new Stride(BigInteger.valueOf(2000), BigInteger.valueOf(-4), BigInteger.valueOf(10), 11L, new PCPair(BigInteger.valueOf(123456), MemoryAccess.WRITE));
        testStride1.printStrideState();

        Stride testStride2 = new Stride(BigInteger.valueOf(2000), BigInteger.valueOf(-4), BigInteger.valueOf(10), 11L, new PCPair(BigInteger.valueOf(123456), MemoryAccess.WRITE));
        testStride2.printTestStrideState();
    }

    @TestFactory
    Stream<DynamicTest> testWithinBlock() {
        testStride1 = new Stride(BigInteger.valueOf(1000), BigInteger.valueOf(4), BigInteger.valueOf(TEST_LIMIT), 15L, new PCPair(BigInteger.valueOf(123456), MemoryAccess.WRITE));

        return accessArray1.stream().map(entry -> dynamicTest("For address " + InstructionsFileReader.toHexString(entry), () -> {
            Assertions.assertTrue(testStride1.addressWithinBlock(entry));
        }));
    }

    @TestFactory
    Stream<DynamicTest> testContainsAddressInStride() {
        testStride1 = new Stride(BigInteger.valueOf(1000), BigInteger.valueOf(4), BigInteger.valueOf(TEST_LIMIT), 15L, new PCPair(BigInteger.valueOf(123456), MemoryAccess.WRITE));

        return accessArray2.stream().map(entry -> dynamicTest("For address " + entry, () -> {
            Assertions.assertTrue(testStride1.containsAddressInStride(entry));
        }));
    }

    @TestFactory
    Stream<DynamicTest> testStrideExpansionV1() {
        logger.info("Test stride expansion version 1");
        logger.info("The initial state of the test stride is: " + testStride1.getStringStrideState());
        logger.info("Commence addition of addresses form an array stream.");
        testStride1 = new Stride(BigInteger.valueOf(1000), BigInteger.valueOf(4), BigInteger.valueOf(1), 2L, new PCPair(BigInteger.valueOf(123456), MemoryAccess.WRITE));

        return accessArray1.stream().map(entry -> {
             testStride1.expandStride(entry);
             //testStride1.printStrideState();
             testStride1.printTestStrideState();

            return dynamicTest("For entry " + entry, () -> Assertions.assertTrue(testStride1.containsAddressInStride(entry)));
         });
    }

    @TestFactory
    Stream<DynamicTest> testStrideExpansionV2() {
        logger.info("Test stride expansion version 2");
        logger.info("The initial state of the test stride is: " + testStride1.getStringStrideState());
        logger.info("Commence addition of addresses form an array stream.");
        Stride testStride2 = new Stride(BigInteger.valueOf(1000), BigInteger.valueOf(4), BigInteger.valueOf(2), 2L, new PCPair(BigInteger.valueOf(123456), MemoryAccess.WRITE));
        testStride2.printTestStrideState();

        return addressesToAdd1.stream().map(entry -> {
            testStride2.expandStride(entry);
            testStride2.printTestStrideState();

            return dynamicTest("For entry " + entry, () -> Assertions.assertTrue(testStride2.containsAddressInStride(entry)));
        });
    }

}
