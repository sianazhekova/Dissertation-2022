package analyzers.baseline_analyzer.unit_tests;

import analyzers.baseline_analyzer.*;
import analyzers.readers.InstructionsFileReader;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@DisplayName("Loop Instance Test")
public class LoopInstanceTest {

    private LoopInstance loopInstance;
    private final Logger logger = Logger.getLogger(LoopInstanceTest.class.getName());

    private final BigInteger SIZE_ACCESS = BigInteger.valueOf(1000);

    List<BigInteger> testPCs1;
    List<BigInteger> testRefAddr1;

    List<BigInteger> testPCs2;
    List<BigInteger> testRefAddr2;

    List<BigInteger> testPCs3;
    List<BigInteger> testRefAddr3;

    String[] testStrPCs1 = new String[]{"0x49be839055c5c8ae", "0x38c8bd7fe1bd6989", "0x80f9d982a705bb27", "0xc0e723a6cacaccbf", "0x6dcc3e48b31a46e9", "0x9081414ef1e478d", "0x8eacc81f2a4839c7", "0xa6ef9edc95b89623", "0xa6ef9edccccc9623", "0xa12341111119623","0xb12341111119623", "0xc12341111119623", "0xc12341111119623", "0xc12341111119623", "0xa6ef9edc95b89623", "0x5eb3381f2a4839c7", "0x19f9d999a705bb27", "0x9081414ef1e478d", "0x9081414ef1e478d"};
    String[] testStrRefAddr1 = new String[]{"0xfdbbf4761ffaa85a", "0x7a01103a5e436a5c", "0xccf26fbc667f16b0", "0xcc6a29025a7591f8", "0xdf5d48cb88915f64", "0x7cf35f1cb3fca6cc", "0xe909c4bbe311bb17", "0x339266de0e99d99a", "0x3ae0735e1d908b4", "0x7a01103a5e436a5c", "0x7a01103a5e436a5c", "0x7a01103a5e436a5c", "0x7a01103a5e436a5c", "0x7a01103a5e436a5c", "0x339266de0e99d99a", "0xe909c4bbe311bb17", "0xccf26fbc667f16b0", "0x7cf35f1cb3fca6cc", "0x7cf35f1cb3fca6cc"};
    MemoryAccess[] dataDeps1 = new MemoryAccess[]{MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE};
    long[] tripCounts1 = LongStream.iterate(0, n -> n+1).limit(testStrRefAddr1.length).toArray();

    String[] testStrPCs2 = new String[]{"0x49be839055c5c8ae", "0x1234bd7fe1bd6989", "0xa6ef9edc95b89623", "0xc0e723a6cacaccbf", "0x49be839055c5c8ae", "0xaabbbbbae4114b48", "0x1e636781358a8c94", "0x72c5feeee98a9d80", "0xab69077945d8f097", "0xab69077945d8f097", "0x201174dce7faa1a7", "0x201174dce7faa1a7"};
    String[] testStrRefAddr2 = new String[]{"0xfdbbf4761ffaa85a", "0x7a01103a5e436a5c", "0x339266de0e99d99a", "0xcc6a29025a7591f8", "0xfdbbf4761ffaa85a", "0xaaaaaaaae311bb17", "0x552dcc1a59ac362b", "0x76a1cde2aaab5225", "0x7971f258e5090ba4", "0x7971f258e5090ba4", "0xdd90d28ece6c61f1", "0xdd90d28ece6c61f1"};
    MemoryAccess[] dataDeps2 = new MemoryAccess[]{MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE};
    long[] tripCounts2 = LongStream.iterate(1000, n -> n+1).limit(testStrRefAddr2.length).toArray();

    String[] testStrPCs3 = new String[]{"0xe0adabfe1501add9", "0xa6ef9edc95b89623", "0xa6ef9edc95b89623", "0x52b85d24d1cc464a", "0xe15f2e180c42fca9", "0xd1f9c63ebc26a9f", "0x19cd5c05eb234fa7", "0x676fdb5f5b441a47",     "0x201174dce7faa1a7", "0x201174dce7faa1a7", "0x201174dce7faa1a7",   "0x4f433b851acd575"};
    String[] testStrRefAddr3 = new String[]{"0x488cf7f988d63c53", "0x9e91e4563e8c80d", "0x9e91e4563e8c80d", "0xf8022320b0fb1a60", "0xfdbbf4761ffaa85a", "0x552dcc1a59ac362b", "0x7a01103a5e436a5c", "0x339266de0e99d99a",      "0x7971f258e5090ba4", "0x7971f258e5090ba4", "0x7971f258e5090ba4",     "0xdd90d28ece6c61f1"};
    MemoryAccess[] dataDeps3 = new MemoryAccess[]{MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.WRITE,     MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ,   MemoryAccess.READ };
    long[] tripCounts3 = LongStream.iterate(2000, n -> n+1).limit(testStrRefAddr3.length).toArray();


    @BeforeEach
    @DisplayName("Test set-up")
    void setUpInstance() {
        loopInstance = new LoopInstance(BigInteger.valueOf(123));

        testPCs1 = Arrays.stream(testStrPCs1).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());
        testRefAddr1 = Arrays.stream(testStrRefAddr1).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());

        testPCs2 = Arrays.stream(testStrPCs2).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());
        testRefAddr2 = Arrays.stream(testStrRefAddr2).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());

        testPCs3 = Arrays.stream(testStrPCs3).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());
        testRefAddr3 = Arrays.stream(testStrRefAddr3).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());
    }

    // A helper util function to aid appending new memory access entries to the pending table
    public void paramAdditionToPendingTable(LoopInstance loopToTest, @NotNull List<BigInteger> testPCs, List<BigInteger> testRefAddr, MemoryAccess[] memAccesses, long[] tripCounts) {
        for (int i = 0; i < testPCs.size(); i++) {
            long tripCountTemp = tripCounts[i];
            PointPC pointTemp = new PointPC(testRefAddr.get(i), BigInteger.valueOf(1000), new PCPair(testPCs.get(i), memAccesses[i]));
            logger.info(String.format("Addition of a new memory access with an approximate reference address of %s, PC address of %s, on trip count %d, with a size of %d",
                    InstructionsFileReader.toHexString(testRefAddr.get(i)),
                    InstructionsFileReader.toHexString(testPCs.get(i)),
                    tripCounts[i],
                    SIZE_ACCESS)
            );
            loopToTest.addNewMemoryAccess(pointTemp, tripCountTemp);
        }
        loopToTest.printPendingPointTable();
    }

    @Test
    void testNewMemAccessAddition() {
        Assertions.assertTrue(testPCs1.size() == testRefAddr1.size());
        Assertions.assertEquals(testRefAddr1.size(), dataDeps1.length);
        Assertions.assertEquals(testRefAddr1.size(), tripCounts1.length);

        paramAdditionToPendingTable(this.loopInstance, testPCs1, testRefAddr1, dataDeps1, tripCounts1);
    }

    @Test
    void testIsKilled() {
        // TODO
    }

    @Test
    void testMergePendingIntoHistoryRounds2() {
        logger.info("Testing the addition of a starting sequence of new memory accesses to the loop instance:");
        paramAdditionToPendingTable(this.loopInstance, testPCs1, testRefAddr1, dataDeps1, tripCounts1);
        logger.info("Printing the set of killed bits: ");
        this.loopInstance.printKilledBits();

        logger.info("Testing merging of the pending point table into the history point table, for the first time.");
        loopInstance.mergePendingIntoHistory();

        Assertions.assertTrue(loopInstance.isKilledBitsSetEmpty());
        Assertions.assertTrue(loopInstance.isPendingPointTableEmpty());

        logger.info("Printing the entries contained in the history point table of the member loop instance after merging it with the point table:");
        loopInstance.printHistoryPointTable();
        logger.info("Printing the resultant set of killed bits");
        loopInstance.printKilledBits();

        logger.info("Testing the addition of a second sequence of new memory accesses to the loop instance:");
        paramAdditionToPendingTable(this.loopInstance, testPCs2, testRefAddr2, dataDeps2, tripCounts2);

        logger.info("Printing the resultant set of killed bits after the addition of the new pending points:");
        loopInstance.printKilledBits();

        logger.info("Testing merging of the pending point table into the history point table, for the second time.");
        loopInstance.mergePendingIntoHistory();

        Assertions.assertTrue(loopInstance.isKilledBitsSetEmpty());
        Assertions.assertTrue(loopInstance.isPendingPointTableEmpty());

        logger.info("Printing the entries contained in the history point table after merging it with the pending point table in the second round:");
        loopInstance.printHistoryPointTable();

        logger.info("Printing the resultant set of killed bits after the merging of the history point table with the pending point table in the second round:");
        loopInstance.printKilledBits();
    }

    @Test
    void testMergeHistoryIntoPending() {
        logger.info("Testing the construction of the primary (member) loop instance:");
        logger.info("Testing the addition of a starting sequence of new memory accesses to the loop instance:");
        paramAdditionToPendingTable(this.loopInstance, testPCs1, testRefAddr1, dataDeps1, tripCounts1);
        logger.info("Printing the set of killed bits: ");
        this.loopInstance.printKilledBits();

        logger.info("Testing merging of the pending point table into the history point table, for the first time.");
        loopInstance.mergePendingIntoHistory();

        Assertions.assertTrue(loopInstance.isKilledBitsSetEmpty());
        Assertions.assertTrue(loopInstance.isPendingPointTableEmpty());

        logger.info("Printing the entries contained in the history point table of the member loop instance after merging it with the point table:");
        loopInstance.printHistoryPointTable();
        logger.info("Printing the resultant set of killed bits");
        loopInstance.printKilledBits();

        logger.info("Testing the addition of a second sequence of new memory accesses to the loop instance:");
        paramAdditionToPendingTable(this.loopInstance, testPCs2, testRefAddr2, dataDeps2, tripCounts2);

        logger.info("Printing the resultant set of killed bits after the addition of the new pending points:");
        loopInstance.printKilledBits();


        LoopInstance anotherLoop = new LoopInstance(BigInteger.valueOf(3001));
        logger.info("Testing the construction of another loop instance (and yielding its resulting pending point table): ");
        paramAdditionToPendingTable(anotherLoop, testPCs3, testRefAddr3, dataDeps3, tripCounts3);
        anotherLoop.printKilledBits();

        logger.info("Testing the merging of the pending point table into the history point table of the inner loop: ");
        anotherLoop.mergePendingIntoHistory();

        Assertions.assertTrue(anotherLoop.isKilledBitsSetEmpty());
        Assertions.assertTrue(anotherLoop.isPendingPointTableEmpty());

        logger.info("Printing the history point table of the inner loop after its pending point table has been merged: ");
        anotherLoop.printHistoryPointTable();
        logger.info("Printing the set of killed bits: ");
        anotherLoop.printKilledBits();

        logger.info("Merging the history point table of the inner loop instance with the pending point table of the member loop instance");
        try {
            this.loopInstance.mergeHistoryIntoPending(anotherLoop);
        } catch (NullLoopInstanceException e) {
            e.printStackTrace();
        }

        logger.info("Printing the pending point table of the member loop instance (after the inner loop instance's history point table has been merged into its pending point table)");
        this.loopInstance.printPendingPointTable();

        logger.info("Printing the updated set of killed bits: ");
        this.loopInstance.printKilledBits();

    }
    // WORKS UP TO HERE!!!

    @Test
    void testRecordDataConflicts() {
        logger.info("Testing the addition of a starting sequence of new memory accesses to the loop instance:");
        paramAdditionToPendingTable(this.loopInstance, testPCs1, testRefAddr1, dataDeps1, tripCounts1);
        logger.info("Printing the set of killed bits: ");
        this.loopInstance.printKilledBits();

        this.loopInstance.loopIterationEnd();
        System.out.println(loopInstance.getOutputInstanceStatistics());

        Assertions.assertTrue(loopInstance.isKilledBitsSetEmpty());
        Assertions.assertTrue(loopInstance.isPendingPointTableEmpty());

        logger.info("Printing the entries contained in the history point table of the member loop instance after merging it with the point table:");
        loopInstance.printHistoryPointTable();
        logger.info("Printing the resultant set of killed bits");
        loopInstance.printKilledBits();

        logger.info("Testing the addition of a second sequence of new memory accesses to the loop instance:");
        paramAdditionToPendingTable(this.loopInstance, testPCs2, testRefAddr2, dataDeps2, tripCounts2);

        logger.info("Printing the resultant set of killed bits after the addition of the new pending points:");
        loopInstance.printKilledBits();

        logger.info("Testing merging of the pending point table into the history point table, for the second time.");
        loopInstance.loopIterationEnd();
        System.out.println(loopInstance.getOutputInstanceStatistics());

        Assertions.assertTrue(loopInstance.isKilledBitsSetEmpty());
        Assertions.assertTrue(loopInstance.isPendingPointTableEmpty());

        logger.info("Printing the entries contained in the history point table after merging it with the pending point table in the second round:");
        loopInstance.printHistoryPointTable();

        logger.info("Printing the resultant set of killed bits after the merging of the history point table with the pending point table in the second round:");
        loopInstance.printKilledBits();

        logger.info("Testing the construction of another loop instance (and yielding its resulting pending point table): ");
        paramAdditionToPendingTable(loopInstance, testPCs3, testRefAddr3, dataDeps3, tripCounts3);
        loopInstance.printKilledBits();

        logger.info("Testing the merging of the pending point table into the history point table of the inner loop: ");
        loopInstance.loopIterationEnd();
        System.out.println(loopInstance.getOutputInstanceStatistics());

        Assertions.assertTrue(loopInstance.isKilledBitsSetEmpty());
        Assertions.assertTrue(loopInstance.isPendingPointTableEmpty());

        logger.info("Printing the history point table of the inner loop after its pending point table has been merged: ");
        loopInstance.printHistoryPointTable();
        logger.info("Printing the set of killed bits: ");
        loopInstance.printKilledBits();
    }

    // TODO: Add an exception tester class
    @Nested
    class LoopInstanceExceptionTests {

    }

    @Test
    void testLoopIndependentDependencies() {
        // NOTE: These have already been tested in the new access additions, and appear to work as expected.
    }

}
