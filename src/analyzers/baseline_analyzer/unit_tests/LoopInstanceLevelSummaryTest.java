package analyzers.baseline_analyzer.unit_tests;

import analyzers.baseline_analyzer.*;
import analyzers.readers.InstructionsFileReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@DisplayName("Loop Instance Level Summary Test")
public class LoopInstanceLevelSummaryTest {

    LoopInstanceLevelSummary loopInstanceLevelSummary;
    private final BigInteger LOOP_ID = BigInteger.valueOf(100000);
    private final Logger logger = Logger.getLogger(LoopInstanceLevelSummaryTest.class.getName());

    List<BigInteger> testPCs1;
    List<BigInteger> testRefAddr1;

    String[] testStrPCs1 = new String[]{"0x72c5feeee98a9d80", "0xaabbbbbae4114b48", "0xe0adabfe1501add9", "0x201174dce7faa1a7", "0x201174dce7faa1a7", "0x4f433b851acd575", "0xa6ef9edc95b89623", "0x676fdb5f5b441a47", "0x49be839055c5c8ae", "0x49be839055c5c8ae", "0xe15f2e180c42fca9", "0xab69077945d8f097", "0xab69077945d8f097", "0xab69077945d8f097", "0x201174dce7faa1a7", "0x201174dce7faa1a7" };
    String[] testStrRefAddr1 = new String[]{"0x76a1cde2aaab5225", "0xaaaaaaaae311bb17", "0x488cf7f988d63c53", "0xdd90d28ece6c61f1", "0xdd90d28ece6c61f1", "0xdd90d28ece6c61f1", "0x339266de0e99d99a", "0x339266de0e99d99a", "0xfdbbf4761ffaa85a", "0xfdbbf4761ffaa85a", "0xfdbbf4761ffaa85a", "0x7971f258e5090ba4", "0x7971f258e5090ba4", "0x7971f258e5090ba4", "0x7971f258e5090ba4","0x7971f258e5090ba4"};
    MemoryAccess[] dataDeps1 = new MemoryAccess[]{ MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.WRITE,  MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ};
    long[] tripCounts1 = LongStream.iterate(0, n -> n+1).limit(testStrRefAddr1.length).toArray();

    @BeforeEach
    @Test
    void setUpLoopInstanceSummaryState() {
        loopInstanceLevelSummary = new LoopInstanceLevelSummary(LOOP_ID);

        testPCs1 = Arrays.stream(testStrPCs1).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());
        testRefAddr1 = Arrays.stream(testStrRefAddr1).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());

        Assertions.assertTrue(testPCs1.size() == testRefAddr1.size());
        Assertions.assertTrue(testRefAddr1.size() == dataDeps1.length);
        Assertions.assertTrue(dataDeps1.length == tripCounts1.length);

        LinkedHashSet<PairwiseConflictLevelSummary> tempCollection = new LinkedHashSet<>();

        logger.info("Accumulating the pairwise conflicts in a temporary data structure");
        for (int i = 0; i < testPCs1.size() - 1; i++) {
            BigInteger firstRefAddress = testRefAddr1.get(i);
            BigInteger secondRefAddress = testRefAddr1.get(i + 1);

            BigInteger firstPCAddress = testPCs1.get(i);
            BigInteger secondPCAddress = testPCs1.get(i + 1);

            MemoryAccess memAcc1 = dataDeps1[i];
            MemoryAccess memAcc2 = dataDeps1[i + 1];

            if (firstRefAddress.equals(secondRefAddress)) {
                DataDependence dataDepType = DataDependence.getDependence(memAcc1, memAcc2);
                if (dataDepType.equals(DataDependence.RW) || dataDepType.equals(DataDependence.WR) || dataDepType.equals(DataDependence.WW)) {
                    PairwiseConflictLevelSummary pairwiseConflict = new PairwiseConflictLevelSummary(firstRefAddress,
                            new PCPair(firstPCAddress, memAcc1),
                            new PCPair(secondPCAddress, memAcc2),
                            1,
                            tripCounts1[i],
                            tripCounts1[i + 1]
                    );
                    tempCollection.add(pairwiseConflict);
                }
            }
        }

        long count = 0;
        for (PairwiseConflictLevelSummary conflictSummary : tempCollection) {
            System.out.println("The current freq count is " + conflictSummary.getFrequencyCount());
            count += conflictSummary.getFrequencyCount();
        }
        System.out.println("The counted number of conflicts is : " + count);
        loopInstanceLevelSummary.addLoopIterationConflicts(count, tempCollection);

        logger.info("Iterating & printing the contents of the loop instance level summary: ");
        System.out.println(loopInstanceLevelSummary.printToString());
    }

    @DisplayName("Addition of Loop Iterations Test")
    @Test
    void testAdditionOfLoopIterations() {
        Assertions.assertTrue(loopInstanceLevelSummary.getSizeOfInnerConflictCollection() > 0);

        logger.info("Accumulating the instructions for another loop iteration: ");
        LinkedHashSet<PairwiseConflictLevelSummary> testSet = new LinkedHashSet<>();
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
        testSet.add(testSummary1);
        testSet.add(testSummary2);
        testSet.add(testSummary3);

        long count = 0;
        for (PairwiseConflictLevelSummary conflictSummary : testSet) {
            System.out.println("The current freq count is " + conflictSummary.getFrequencyCount());
            count += conflictSummary.getFrequencyCount();
        }
        System.out.print("The counted number of conflicts is : " + count);
        loopInstanceLevelSummary.addLoopIterationConflicts(count, testSet);

        logger.info("Printing the updated loop instance level summary statistics");
        System.out.println(loopInstanceLevelSummary.printToString());

        loopInstanceLevelSummary.incrementIterationState(0);
        logger.info("Printing the updated loop instance level summary statistics after only incrementing the state (with 0 conflicts)");
        System.out.println(loopInstanceLevelSummary.printToString());

        loopInstanceLevelSummary.incrementIterationState(5);
        logger.info("Printing the updated loop instance level summary statistics after only incrementing the state (with 5 conflicts)");
        System.out.println(loopInstanceLevelSummary.printToString());

        logger.info("Testing an empty loop instance summary addition: ");
        LoopInstanceLevelSummary emptySummaryTest = new LoopInstanceLevelSummary(BigInteger.valueOf(123456));
        emptySummaryTest.incrementIterationState(0);
        System.out.println(emptySummaryTest.printToString());

        emptySummaryTest.incrementIterationState(100);
        System.out.println(emptySummaryTest.printToString());
    }

}
