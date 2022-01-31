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

@DisplayName("Loop Level Summary Test Class")
public class LoopLevelSummaryTest {

    private LoopLevelSummary loopSummary;
    private final Logger logger = Logger.getLogger(LoopLevelSummaryTest.class.getName());

    private final BigInteger SIZE_ACCESS = BigInteger.valueOf(1000);
    private final BigInteger LOOP_ID = BigInteger.valueOf(123456);

    List<BigInteger> testPCs1;
    List<BigInteger> testRefAddr1;

    String[] testStrPCs1 = new String[]{"0x72c5feeee98a9d80", "0xaabbbbbae4114b48", "0xe0adabfe1501add9", "0x201174dce7faa1a7", "0x201174dce7faa1a7", "0x4f433b851acd575", "0xa6ef9edc95b89623", "0x676fdb5f5b441a47", "0x49be839055c5c8ae", "0x49be839055c5c8ae", "0xe15f2e180c42fca9", "0xab69077945d8f097", "0xab69077945d8f097", "0xab69077945d8f097", "0x201174dce7faa1a7", "0x201174dce7faa1a7" };
    String[] testStrRefAddr1 = new String[]{"0x76a1cde2aaab5225", "0xaaaaaaaae311bb17", "0x488cf7f988d63c53", "0xdd90d28ece6c61f1", "0xdd90d28ece6c61f1", "0xdd90d28ece6c61f1", "0x339266de0e99d99a", "0x339266de0e99d99a", "0xfdbbf4761ffaa85a", "0xfdbbf4761ffaa85a", "0xfdbbf4761ffaa85a", "0x7971f258e5090ba4", "0x7971f258e5090ba4", "0x7971f258e5090ba4", "0x7971f258e5090ba4","0x7971f258e5090ba4"};
    MemoryAccess[] dataDeps1 = new MemoryAccess[]{ MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.WRITE,  MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ};
    long[] tripCounts1 = LongStream.iterate(0, n -> n+1).limit(testStrRefAddr1.length).toArray();


    @BeforeEach
    @Test
    public void setUpTestInputData() {
        logger.info("Setting up the testing state and the loop level summary initialization.");
        testPCs1 = Arrays.stream(testStrPCs1).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());
        testRefAddr1 = Arrays.stream(testStrRefAddr1).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());

        Assertions.assertTrue(testPCs1.size() == testRefAddr1.size());
        Assertions.assertTrue(testRefAddr1.size() == dataDeps1.length);
        Assertions.assertTrue(dataDeps1.length == tripCounts1.length);

        loopSummary = new LoopLevelSummary();
        System.out.println(loopSummary.printToString());
    }

    @Test
    public void testAdditionOfLoopInstanceConflicts() {

        LinkedHashSet<PairwiseConflictLevelSummary> conflictsSet = new LinkedHashSet<>();

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
                    PairwiseConflictLevelSummary pairwiseConflict = new PairwiseConflictLevelSummary(firstRefAddress, new PCPair(firstPCAddress, memAcc1), new PCPair(secondPCAddress, memAcc2), 1, tripCounts1[i], tripCounts1[i + 1]);
                    conflictsSet.add(pairwiseConflict);
                }
            }
        }
        logger.info("Printing the entries in the list containing the discovered Pairwise Conflict Summaries");

        for (PairwiseConflictLevelSummary pairwiseConflict : conflictsSet) {
            System.out.println(pairwiseConflict.printToString());
        }

        logger.info("Accumulating the total frequency count from the entries. ");
        long count = conflictsSet.stream()
                .map(conflictSummary -> conflictSummary.getFrequencyCount())
                .mapToLong(n -> n)
                .sum();

        logger.info("Adding a loop iteration of conflicts to the member loop level summary object. ");
        LoopInstanceLevelSummary tempLoopInstanceSummary = new LoopInstanceLevelSummary(LOOP_ID);
        tempLoopInstanceSummary.addLoopIterationConflicts(count, conflictsSet);
        loopSummary.addLoopInstanceConflicts(tempLoopInstanceSummary);

        logger.info("Printing out the contents of the member loop-level summary object.");
        System.out.println(loopSummary.printToString());

        LinkedHashSet<PairwiseConflictLevelSummary> testSet = new LinkedHashSet<>();

        logger.info("Adding new entries to the pairwise conflict summary test.");
        testSet.add(new PairwiseConflictLevelSummary(new BigInteger("88888858e5090ab5", 16),
                new PCPair(new BigInteger("ab69077945d8f097", 16), MemoryAccess.READ),
                new PCPair(new BigInteger("201174dce7faa1a7", 16), MemoryAccess.WRITE),
                1,
                5,
                100
        ));
        testSet.add(new PairwiseConflictLevelSummary(new BigInteger("77777757a5990ab5", 16),
                new PCPair(new BigInteger("49be839055c5c8ae", 16), MemoryAccess.WRITE),
                new PCPair(new BigInteger("e15f2e180c42fca9", 16), MemoryAccess.WRITE),
                2,
                10,
                110
        ));
        testSet.add(new PairwiseConflictLevelSummary(new BigInteger("111111aaa1191ab5", 16),
                new PCPair(new BigInteger("201174dce7faa1a7", 16), MemoryAccess.WRITE),
                new PCPair(new BigInteger("4f433b851acd575", 16), MemoryAccess.READ),
                3,
                20,
                310
        ));
        testSet.add(new PairwiseConflictLevelSummary(new BigInteger("12345671191ab5", 16),
                new PCPair(new BigInteger("1211756677f23137", 16), MemoryAccess.WRITE),
                new PCPair(new BigInteger("4f433b851acd575", 16), MemoryAccess.READ),
                1,
                20,
                40
        ));

        testSet.add(new PairwiseConflictLevelSummary(new BigInteger("12345671191ab5", 16),
                new PCPair(new BigInteger("cb146ea762776ff7", 16), MemoryAccess.WRITE),
                new PCPair(new BigInteger("3ae0735e1d908b4", 16), MemoryAccess.READ),
                1,
                31,
                55
        ));

        tempLoopInstanceSummary = new LoopInstanceLevelSummary(BigInteger.valueOf(123456));
        logger.info("Accumulating the total frequency count from the entries: ");
        long countSecondSet = testSet.stream()
                .map(conflictSummary -> conflictSummary.getFrequencyCount())
                .mapToLong(n -> n)
                .sum();

        logger.info("Adding the new loop iteration to the member loop level summary. ");
        tempLoopInstanceSummary.addLoopIterationConflicts(countSecondSet, testSet);
        loopSummary.addLoopInstanceConflicts(tempLoopInstanceSummary);

        System.out.println(loopSummary.printToString());

        logger.info("Printing a loop instance ");
        tempLoopInstanceSummary = new LoopInstanceLevelSummary(BigInteger.valueOf(1010101));
        tempLoopInstanceSummary.addLoopIterationConflicts(0, new LinkedHashSet<>());
        System.out.println(tempLoopInstanceSummary.printToString());

        loopSummary.addLoopInstanceConflicts(tempLoopInstanceSummary);

        System.out.println(loopSummary.printToString());
    }
}
