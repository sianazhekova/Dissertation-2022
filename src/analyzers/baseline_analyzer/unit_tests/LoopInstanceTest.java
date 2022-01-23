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

    String[] testStrPCs1 = new String[]{"0x49be839055c5c8ae", "0x38c8bd7fe1bd6989", "0x80f9d982a705bb27", "0xc0e723a6cacaccbf", "0x6dcc3e48b31a46e9", "0x9081414ef1e478d", "0x8eacc81f2a4839c7", "0xa6ef9edc95b89623", "0xa6ef9edccccc9623", "0xa12341111119623","0xb12341111119623", "0xc12341111119623", "0xc12341111119623", "0xc12341111119623", "0xa6ef9edc95b89623", "0x5eb3381f2a4839c7", "0x19f9d999a705bb27", "0x9081414ef1e478d", "0x9081414ef1e478d"};
    String[] testStrRefAddr1 = new String[]{"0xfdbbf4761ffaa85a", "0x7a01103a5e436a5c", "0xccf26fbc667f16b0", "0xcc6a29025a7591f8", "0xdf5d48cb88915f64", "0x7cf35f1cb3fca6cc", "0xe909c4bbe311bb17", "0x339266de0e99d99a", "0x3ae0735e1d908b4", "0x7a01103a5e436a5c", "0x7a01103a5e436a5c", "0x7a01103a5e436a5c", "0x7a01103a5e436a5c", "0x7a01103a5e436a5c", "0x339266de0e99d99a", "0xe909c4bbe311bb17", "0xccf26fbc667f16b0", "0x7cf35f1cb3fca6cc", "0x7cf35f1cb3fca6cc"};
    MemoryAccess[] dataDeps1 = new MemoryAccess[]{MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE};
    long[] tripCounts1 = LongStream.iterate(0, n -> n+1).limit(testStrRefAddr1.length).toArray();

    String[] testStrPCs2 = new String[]{"0x49be839055c5c8ae", "0x1234bd7fe1bd6989", "0xa6ef9edc95b89623", "0xc0e723a6cacaccbf", "0x49be839055c5c8ae", "0xaabbbbbae4114b48" };
    String[] testStrRefAddr2 = new String[]{"0xfdbbf4761ffaa85a", "0x7a01103a5e436a5c", "0x339266de0e99d99a", "0xcc6a29025a7591f8", "0xfdbbf4761ffaa85a", "0xaaaaaaaae311bb17"};
    MemoryAccess[] dataDeps2 = new MemoryAccess[]{MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ };
    long[] tripCounts2 = LongStream.iterate(1000, n -> n+1).limit(testStrRefAddr2.length).toArray();


    @BeforeEach
    @DisplayName("Test set-up")
    void setUpInstance() {
        loopInstance = new LoopInstance(BigInteger.valueOf(123));

        testPCs1 = Arrays.stream(testStrPCs1).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());
        testRefAddr1 = Arrays.stream(testStrRefAddr1).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());

        testPCs2 = Arrays.stream(testStrPCs2).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());
        testRefAddr2 = Arrays.stream(testStrRefAddr2).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());
    }

    // A helper util function to aid appending new memory access entries to the pending table
    public void paramAdditionToPendingTable(@NotNull List<BigInteger> testPCs, List<BigInteger> testRefAddr, MemoryAccess[] memAccesses, long[] tripCounts) {
        for (int i = 0; i < testPCs.size(); i++) {
            long tripCountTemp = tripCounts[i];
            PointPC pointTemp = new PointPC(testRefAddr.get(i), BigInteger.valueOf(1000), new PCPair(testPCs.get(i), memAccesses[i]));
            logger.info(String.format("Addition of a new memory access with an approximate reference address of %s, PC address of %s, on trip count %d, with a size of %d",
                    InstructionsFileReader.toHexString(testRefAddr.get(i)),
                    InstructionsFileReader.toHexString(testPCs.get(i)),
                    tripCounts[i],
                    SIZE_ACCESS)
            );
            loopInstance.addNewMemoryAccess(pointTemp, tripCountTemp);
        }
        loopInstance.printPendingPointTable();
    }

    @Test
    void testNewMemAccessAddition() {
        Assertions.assertTrue(testPCs1.size() == testRefAddr1.size());
        Assertions.assertEquals(testRefAddr1.size(), dataDeps1.length);
        Assertions.assertEquals(testRefAddr1.size(), tripCounts1.length);

        paramAdditionToPendingTable(testPCs1, testRefAddr1, dataDeps1, tripCounts1);
    }

    @Test
    void testIsKilled() {

    }

    @Test
    void testMergePendingIntoHistory(){
        logger.info("Testing the addition of a sequence of new memory accesses to the loop instance:");
        paramAdditionToPendingTable(testPCs1, testRefAddr1, dataDeps1, tripCounts1);

        logger.info("Testing merging of the pending point table into the history point table.");
        loopInstance.mergePendingIntoHistory();

        Assertions.assertTrue(loopInstance.isKilledBitsSetEmpty());
        Assertions.assertTrue(loopInstance.isPendingPointTableEmpty());

        logger.info("Printing the entries contained in the history point table after merging it with the point table:");
        loopInstance.printHistoryPointTable();
    }

    @Test
    void testMergeHistoryIntoPending() {


    }

    @Test
    void testLoopIterationEnd() {

    }

    @Test
    void testRecordDataConflicts() {

    }

    // TODO: Add an exception tester class
    @Test
    void testExceptionThrows() {

    }

    @Test
    void testLoopIndependentDependencies() {

    }

}
