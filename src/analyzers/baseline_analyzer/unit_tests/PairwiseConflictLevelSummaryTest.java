package analyzers.baseline_analyzer.unit_tests;

import analyzers.baseline_analyzer.DataDependence;
import analyzers.baseline_analyzer.MemoryAccess;
import analyzers.baseline_analyzer.PCPair;
import analyzers.baseline_analyzer.PairwiseConflictLevelSummary;
import analyzers.readers.InstructionsFileReader;
import org.junit.jupiter.api.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@DisplayName("Pairwise Conflict Level Summary Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PairwiseConflictLevelSummaryTest {

    private List<PairwiseConflictLevelSummary> conflictLevelSummary = new ArrayList<>();

    private final Logger logger = Logger.getLogger(PairwiseConflictLevelSummaryTest.class.getName());

    private final BigInteger SIZE_ACCESS = BigInteger.valueOf(1000);

    List<BigInteger> testPCs1;
    List<BigInteger> testRefAddr1;

    String[] testStrPCs1 = new String[]{"0x72c5feeee98a9d80", "0xaabbbbbae4114b48", "0xe0adabfe1501add9", "0x201174dce7faa1a7", "0x201174dce7faa1a7", "0x4f433b851acd575", "0xa6ef9edc95b89623", "0x676fdb5f5b441a47", "0x49be839055c5c8ae", "0x49be839055c5c8ae", "0xe15f2e180c42fca9", "0xab69077945d8f097", "0xab69077945d8f097", "0xab69077945d8f097", "0x201174dce7faa1a7", "0x201174dce7faa1a7" };
    String[] testStrRefAddr1 = new String[]{"0x76a1cde2aaab5225", "0xaaaaaaaae311bb17", "0x488cf7f988d63c53", "0xdd90d28ece6c61f1", "0xdd90d28ece6c61f1", "0xdd90d28ece6c61f1", "0x339266de0e99d99a", "0x339266de0e99d99a", "0xfdbbf4761ffaa85a", "0xfdbbf4761ffaa85a", "0xfdbbf4761ffaa85a", "0x7971f258e5090ba4", "0x7971f258e5090ba4", "0x7971f258e5090ba4", "0x7971f258e5090ba4","0x7971f258e5090ba4"};
    MemoryAccess[] dataDeps1 = new MemoryAccess[]{ MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.WRITE,  MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ};
    long[] tripCounts1 = LongStream.iterate(0, n -> n+1).limit(testStrRefAddr1.length).toArray();

    @BeforeEach
    public void setUpTestInputData() {
        testPCs1 = Arrays.stream(testStrPCs1).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());
        testRefAddr1 = Arrays.stream(testStrRefAddr1).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());

        Assertions.assertTrue(testPCs1.size() == testRefAddr1.size());
        Assertions.assertTrue(testRefAddr1.size() == dataDeps1.length);
        Assertions.assertTrue(dataDeps1.length == tripCounts1.length);

        for (int i = 0; i < testPCs1.size() - 1; i++) {
            BigInteger firstRefAddress = testRefAddr1.get(i);
            BigInteger secondRefAddress = testRefAddr1.get(i + 1);

            BigInteger firstPCAddress = testPCs1.get(i);
            BigInteger secondPCAddress = testPCs1.get(i + 1);

            MemoryAccess memAcc1 = dataDeps1[i];
            MemoryAccess memAcc2 = dataDeps1[i + 1];

            if (firstRefAddress.equals(secondRefAddress)) {
                System.out.println("Ref address equality: 0x" + InstructionsFileReader.toHexString(firstRefAddress));
                DataDependence dataDepType = DataDependence.getDependence(memAcc1, memAcc2);
                if (dataDepType.equals(DataDependence.RW) || dataDepType.equals(DataDependence.WR) || dataDepType.equals(DataDependence.WW)) {
                    PairwiseConflictLevelSummary pairwiseConflict = new PairwiseConflictLevelSummary(firstRefAddress, new PCPair(firstPCAddress, memAcc1), new PCPair(secondPCAddress, memAcc2), 1, tripCounts1[i], tripCounts1[i + 1]);
                    conflictLevelSummary.add(pairwiseConflict);
                }
            }
        }
        logger.info("Printing the entries in the list containing the discovered Pairwise Conflict Summaries");

        for (PairwiseConflictLevelSummary pairwiseConflict : conflictLevelSummary) {
            System.out.println(pairwiseConflict.printToString());
        }
    }

    @DisplayName("Pairwise Conflict Additivity Resolution Test")
    @Test
    public void testAdditivity() {
        Assertions.assertTrue(conflictLevelSummary.size() > 0);
        PairwiseConflictLevelSummary testSummary1 = new PairwiseConflictLevelSummary(new BigInteger("88888858e5090ab5", 16),
                new PCPair(new BigInteger("ab69077945d8f097", 16), MemoryAccess.READ),
                new PCPair(new BigInteger("201174dce7faa1a7", 16), MemoryAccess.WRITE),
                1,
                5,
                100
        );
        PairwiseConflictLevelSummary testSummary2 = new PairwiseConflictLevelSummary(new BigInteger("77777757a5990ab5", 16),
                new PCPair(new BigInteger("49be839055c5c8ae", 16), MemoryAccess.WRITE),
                new PCPair(new BigInteger("e15f2e180c42fca9", 16), MemoryAccess.WRITE),
                2,
                10,
                110
        );
        PairwiseConflictLevelSummary testSummary3 = new PairwiseConflictLevelSummary(new BigInteger("111111aaa1191ab5", 16),
                new PCPair(new BigInteger("201174dce7faa1a7", 16), MemoryAccess.WRITE),
                new PCPair(new BigInteger("4f433b851acd575", 16), MemoryAccess.READ),
                3,
                20,
                310
        );

        logger.info("Checking the additivity of the dependencies of the three test summaries");
        for (int i = 0; i < conflictLevelSummary.size(); i++) {
            PairwiseConflictLevelSummary iterConflict = conflictLevelSummary.get(i);

            if (testSummary1.isAdditive(iterConflict)) {
                System.out.println("A match with test summary 1");
                System.out.println("The summary is additive for PC addresses prev : " + InstructionsFileReader.toHexString(iterConflict.getPrevInstruction().getPC()) +
                        " and access type of " + iterConflict.getPrevInstruction().getMemAccessType().name() +
                        ", and next PC address : " + InstructionsFileReader.toHexString(iterConflict.getNextInstruction().getPC()) +
                        " and access type of " + iterConflict.getNextInstruction().getMemAccessType().name() +
                        ", so the data dependence type is " + DataDependence.getStringDepType(DataDependence.getDependence(iterConflict.getPrevInstruction().getMemAccessType(), iterConflict.getNextInstruction().getMemAccessType())));
                continue;
            }

            if (testSummary2.isAdditive(iterConflict)) {
                System.out.println("A match with test summary 2");
                System.out.println("The summary is additive for PC addresses prev : " + InstructionsFileReader.toHexString(iterConflict.getPrevInstruction().getPC()) +
                        " and access type of " + iterConflict.getPrevInstruction().getMemAccessType().name() +
                        ", and next PC address : " + InstructionsFileReader.toHexString(iterConflict.getNextInstruction().getPC()) +
                        " and access type of " + iterConflict.getNextInstruction().getMemAccessType().name() +
                        ", so the data dependence type is " + DataDependence.getStringDepType(DataDependence.getDependence(iterConflict.getPrevInstruction().getMemAccessType(), iterConflict.getNextInstruction().getMemAccessType())));
                continue;
            }

            if (testSummary3.isAdditive(iterConflict)) {
                System.out.println("A match with test summary 3");
                System.out.println("The summary is additive for PC addresses prev : " + InstructionsFileReader.toHexString(iterConflict.getPrevInstruction().getPC()) +
                        " and access type of " + iterConflict.getPrevInstruction().getMemAccessType().name() +
                        ", and next PC address : " + InstructionsFileReader.toHexString(iterConflict.getNextInstruction().getPC()) +
                        " and access type of " + iterConflict.getNextInstruction().getMemAccessType().name() +
                        ", so the data dependence type is " + DataDependence.getStringDepType(DataDependence.getDependence(iterConflict.getPrevInstruction().getMemAccessType(), iterConflict.getNextInstruction().getMemAccessType())));
                continue;
            }
        }
    }

    @DisplayName("Pairwise Conflict Addition Test")
    @Test
    public void testAdditionOfCounts() {
        Assertions.assertTrue(conflictLevelSummary.size() > 0);
        PairwiseConflictLevelSummary testSummary1 = new PairwiseConflictLevelSummary(new BigInteger("88888858e5090ab5", 16), new PCPair(new BigInteger("ab69077945d8f097", 16), MemoryAccess.READ), new PCPair(new BigInteger("201174dce7faa1a7", 16), MemoryAccess.WRITE), 1, 5, 100);
        PairwiseConflictLevelSummary testSummary2 = new PairwiseConflictLevelSummary(new BigInteger("77777757a5990ab5", 16), new PCPair(new BigInteger("49be839055c5c8ae", 16), MemoryAccess.WRITE), new PCPair(new BigInteger("e15f2e180c42fca9", 16), MemoryAccess.WRITE), 2, 10, 110);
        PairwiseConflictLevelSummary testSummary3 = new PairwiseConflictLevelSummary(new BigInteger("111111aaa1191ab5", 16), new PCPair(new BigInteger("201174dce7faa1a7", 16), MemoryAccess.WRITE), new PCPair(new BigInteger("4f433b851acd575", 16), MemoryAccess.READ), 3, 20, 310);

        logger.info("Testing the addition of pairwise conflict summaries.");
        for (int i = 0; i < conflictLevelSummary.size(); i++) {
            PairwiseConflictLevelSummary iterConflict = conflictLevelSummary.get(i);

            if (testSummary1.isAdditive(iterConflict)) {
                System.out.println("A match with test summary 1");
                testSummary1.addCountsFrom(iterConflict);
                System.out.println("The summary is additive for PC addresses prev : " + InstructionsFileReader.toHexString(testSummary1.getPrevInstruction().getPC()) +
                        " and access type of " + testSummary1.getPrevInstruction().getMemAccessType().name() +
                        ", and next PC address : " + InstructionsFileReader.toHexString(testSummary1.getNextInstruction().getPC()) +
                        " and access type of " + testSummary1.getNextInstruction().getMemAccessType().name() +
                        ", so the data dependence type is " + DataDependence.getStringDepType(DataDependence.getDependence(testSummary1.getPrevInstruction().getMemAccessType(), testSummary1.getNextInstruction().getMemAccessType())) +
                        " and the updated frequency count: " + testSummary1.getFrequencyCount() +
                        " and the updated end trip time: " + testSummary1.getEndTripCount()
                );
                continue;
            }

            if (testSummary2.isAdditive(iterConflict)) {
                System.out.println("A match with test summary 2");
                testSummary2.addCountsFrom(iterConflict);
                System.out.println("The summary is additive for PC addresses prev : " + InstructionsFileReader.toHexString(testSummary2.getPrevInstruction().getPC()) +
                        " and access type of " + testSummary2.getPrevInstruction().getMemAccessType().name() +
                        ", and next PC address : " + InstructionsFileReader.toHexString(testSummary2.getNextInstruction().getPC()) +
                        " and access type of " + testSummary2.getNextInstruction().getMemAccessType().name() +
                        ", so the data dependence type is " + DataDependence.getStringDepType(DataDependence.getDependence(testSummary2.getPrevInstruction().getMemAccessType(), testSummary2.getNextInstruction().getMemAccessType())) +
                        " and the updated frequency count: " + testSummary2.getFrequencyCount() +
                        " and the updated end trip time: " + testSummary2.getEndTripCount()
                );
                continue;
            }

            if (testSummary3.isAdditive(iterConflict)) {
                System.out.println("A match with test summary 3");
                testSummary3.addCountsFrom(iterConflict);
                System.out.println("The summary is additive for PC addresses prev : " + InstructionsFileReader.toHexString(testSummary3.getPrevInstruction().getPC()) +
                        " and access type of " + testSummary3.getPrevInstruction().getMemAccessType().name() +
                        ", and next PC address : " + InstructionsFileReader.toHexString(testSummary3.getNextInstruction().getPC()) +
                        " and access type of " + testSummary3.getNextInstruction().getMemAccessType().name() +
                        ", so the data dependence type is " + DataDependence.getStringDepType(DataDependence.getDependence(testSummary3.getPrevInstruction().getMemAccessType(), testSummary3.getNextInstruction().getMemAccessType())) +
                        " and the updated frequency count: " + testSummary3.getFrequencyCount() +
                        " and the updated end trip time: " + testSummary3.getEndTripCount()
                );
                continue;
            }
        }

    }
}
