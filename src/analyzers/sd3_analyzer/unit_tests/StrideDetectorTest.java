package analyzers.sd3_analyzer.unit_tests;

import analyzers.baseline_analyzer.unit_tests.PairwiseConflictLevelSummaryTest;
import java.util.stream.LongStream;
import analyzers.sd3_analyzer.StrideDetection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StrideDetectorTest {

    private StrideDetection FSMDetectorMonotonic;
    private StrideDetection FSMDetectorGeneral;

    private List<BigInteger> testInputArray1;
    private List<BigInteger> testInputArray2;

    private final Logger logger = Logger.getLogger(StrideDetectorTest.class.getName());

    @BeforeEach
    @Test
    void setUp() {
        logger.info("Initialising the monotonic- and general-case FSM detectors. ");
        FSMDetectorMonotonic = new StrideDetection();
        FSMDetectorGeneral = new StrideDetection(false);

        logger.info("Initialise the input arrays.");
        testInputArray1 = LongStream.iterate(123456, n -> n + 4).limit(11).mapToObj(i -> BigInteger.valueOf(i)).collect(Collectors.toList());
        testInputArray1.add(BigInteger.valueOf(1000));
        testInputArray1.add(BigInteger.valueOf(1006));
        testInputArray1.add(BigInteger.valueOf(1012));
        testInputArray1.add(BigInteger.valueOf(1006));
        testInputArray1.add(BigInteger.valueOf(1000));
        testInputArray1.add(BigInteger.valueOf(994));
        testInputArray1.add(BigInteger.valueOf(1000));
        testInputArray1.add(BigInteger.valueOf(994));
        testInputArray1.add(BigInteger.valueOf(988));
        testInputArray1.add(BigInteger.valueOf(999));
        testInputArray1.add(BigInteger.valueOf(1004));
        testInputArray1.add(BigInteger.valueOf(1009));
        testInputArray1.add(BigInteger.valueOf(1014));
        testInputArray1.add(BigInteger.valueOf(1009));
        testInputArray1.add(BigInteger.valueOf(1014));
        testInputArray1.add(BigInteger.valueOf(6000));
        testInputArray1.add(BigInteger.valueOf(7000));

        testInputArray2 = LongStream.iterate(3000, n -> n - 5).limit(8).mapToObj(i -> BigInteger.valueOf(i)).collect(Collectors.toList());
        testInputArray2.add(BigInteger.valueOf(2970));
    }

    @Test
    void testFSMConstruction() {
        Assertions.assertTrue(FSMDetectorGeneral != null);
        System.out.println("Printing the internal state of the General-case FSM Stride Detector ");
        FSMDetectorGeneral.printFSMState();

        System.out.println("Printing the internal state of the Monotonic-case FSM Stride Detector ");
        FSMDetectorMonotonic.printFSMState();
    }

    @Test
    void testGetPointOrStride() {
        testInputArray1.forEach(inputAddress -> {
            System.out.println("The FSM is currently at " + FSMDetectorMonotonic.getPointOrStride(inputAddress).toString());
            FSMDetectorMonotonic.printFSMState();
        });
    }

    @Test
    void testUpdateFSMStateMonotonic() {
        logger.info("Testing the monotonic-case FSM");
        testInputArray1.forEach(inputAddress -> {
            System.out.println("After the newly added address: " + inputAddress );
            FSMDetectorMonotonic.updateFSMStateMonotonic(inputAddress);
            FSMDetectorMonotonic.printFSMState();
        });

        testInputArray2.forEach(inputAddress -> {
            System.out.println("After the newly added address : " + inputAddress );
            FSMDetectorMonotonic.updateFSMStateMonotonic(inputAddress);
            FSMDetectorMonotonic.printFSMState();
        });

        FSMDetectorMonotonic = new StrideDetection();
        testInputArray2.forEach(inputAddress -> {
            System.out.println("After the current newly added address : " + inputAddress );
            FSMDetectorMonotonic.updateFSMStateMonotonic(inputAddress);
            FSMDetectorMonotonic.printFSMState();
        });
    }

    @Test
    void testUpdateFSMStateGeneral() {
        logger.info("Testing the general-case FSM");
        testInputArray1.forEach(inputAddress -> {
            System.out.println("After the newly added address: " + inputAddress );
            FSMDetectorGeneral.updateFSMStateGeneral(inputAddress);
            FSMDetectorGeneral.printFSMState();
        });

        testInputArray2.forEach(inputAddress -> {
            System.out.println("After the newly added address: " + inputAddress );
            FSMDetectorGeneral.updateFSMStateGeneral(inputAddress);
            FSMDetectorGeneral.printFSMState();
        });

        FSMDetectorGeneral = new StrideDetection();
        testInputArray2.forEach(inputAddress -> {
            System.out.println("After the newly added address: " + inputAddress );
            FSMDetectorGeneral.updateFSMStateGeneral(inputAddress);
            FSMDetectorGeneral.printFSMState();
        });
    }
}
