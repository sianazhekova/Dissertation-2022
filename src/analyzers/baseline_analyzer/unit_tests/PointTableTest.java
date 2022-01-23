package analyzers.baseline_analyzer.unit_tests;
import analyzers.baseline_analyzer.MemoryAccess;
import analyzers.baseline_analyzer.PointTable;
import analyzers.baseline_analyzer.TableEntryPC;
import analyzers.readers.InstructionsFileReader;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.math.BigInteger;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PointTable Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PointTableTest {

    PointTable pointTable;
    List<BigInteger> testPCs;
    List<BigInteger> testRefAddr1;

    List<BigInteger> testPCs2;
    List<BigInteger> testRefAddr2;


    String[] testStrPCs1 = new String[]{"0x49be839055c5c8ae", "0x38c8bd7fe1bd6989", "0x80f9d982a705bb27", "0xc0e723a6cacaccbf", "0x6dcc3e48b31a46e9", "0x9081414ef1e478d", "0x8eacc81f2a4839c7", "0xa6ef9edc95b89623", "0xa6ef9edccccc9623", "0xa12341111119623","0xb12341111119623", "0xc12341111119623", "0xc12341111119623", "0xc12341111119623", "0xa6ef9edc95b89623"};
    String[] testStrRefAddr1 = new String[]{"0xfdbbf4761ffaa85a", "0x7a01103a5e436a5c", "0xccf26fbc667f16b0", "0xcc6a29025a7591f8", "0xdf5d48cb88915f64", "0x7cf35f1cb3fca6cc", "0xe909c4bbe311bb17", "0x339266de0e99d99a", "0x3ae0735e1d908b4", "0x7a01103a5e436a5c", "0x7a01103a5e436a5c", "0x7a01103a5e436a5c", "0x7a01103a5e436a5c", "0x7a01103a5e436a5c", "0x339266de0e99d99a"};
    MemoryAccess[] dataDeps = new MemoryAccess[]{MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.WRITE, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.WRITE};
    long[] tripCounts = LongStream.iterate(0, n -> n+1).limit(testStrRefAddr1.length).toArray();

    String[] testStrPCs2 = new String[]{"0x49be839055c5c8ae", "0x1234bd7fe1bd6989", "0xa6ef9edc95b89623", "0xc0e723a6cacaccbf", "0x49be839055c5c8ae", "0xaabbbbbae4114b48" };
    String[] testStrRefAddr2 = new String[]{"0xfdbbf4761ffaa85a", "0x7a01103a5e436a5c", "0x339266de0e99d99a", "0xcc6a29025a7591f8", "0xfdbbf4761ffaa85a", "0xaaaaaaaae311bb17"};
    MemoryAccess[] dataDeps2 = new MemoryAccess[]{MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.READ, MemoryAccess.WRITE, MemoryAccess.READ };
    long[] tripCounts2 = LongStream.iterate(1000, n -> n+1).limit(testStrRefAddr2.length).toArray();

    public static void printPointTable(PointTable pointTable1) {
        for (BigInteger keyAddr : pointTable1.getKeySet()) {
            System.out.println("The key is 0x" + InstructionsFileReader.toHexString(keyAddr));
            Assertions.assertTrue(pointTable1.containsKey(keyAddr));
            Assertions.assertTrue(pointTable1.getTableEntry(keyAddr).size() != 0);
            ListIterator<TableEntryPC> iteratorsEntries = pointTable1.getTableEntry(keyAddr).listIterator();
            for (TableEntryPC tableEntry : pointTable1.getTableEntry(keyAddr)) {
                System.out.println("The Table Entry has a PC address of 0x" + InstructionsFileReader.toHexString(tableEntry.getAddressPC())
                        + ", and has a trip count of " + tableEntry.getTripCount()
                        + ", and has an access type of " + MemoryAccess.getStringMemAccess(tableEntry.getMemAccessType())
                        + ", and a frequency of " + tableEntry.getNumOccurrence()
                );
            }
            System.out.println("\n");
        }
    }

    @BeforeEach
    void setUp() {
        pointTable = new PointTable();
        testPCs = Arrays.stream(testStrPCs1).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());
        testRefAddr1 = Arrays.stream(testStrRefAddr1).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());

        testPCs2 = Arrays.stream(testStrPCs2).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());
        testRefAddr2 = Arrays.stream(testStrRefAddr2).map(strEntry -> new BigInteger(strEntry.substring(2), 16)).collect(Collectors.toList());
    }

    @TestFactory
    @Order(1)
    Stream<DynamicTest> setParamsPCTest1() {

        return IntStream.iterate(0, n -> n + 1).limit(testStrPCs1.length)
                    .mapToObj(n -> {
                        return DynamicTest.dynamicTest("Test" + n,
                                () -> {
                                    assertEquals(InstructionsFileReader.toHexString(testPCs.get(n)), testStrPCs1[n].substring(2));
                                });
                    });
    }

    @TestFactory
    @Order(2)
    Stream<DynamicTest> setParamsRefAddrTest1() {

        return IntStream.iterate(0, n -> n + 1).limit(testStrRefAddr1.length)
                .mapToObj(n -> {
                    return DynamicTest.dynamicTest("Test" + n,
                            () -> {
                                assertEquals(InstructionsFileReader.toHexString(testRefAddr1.get(n)), testStrRefAddr1[n].substring(2));
                            });
                });
    }


    @Test
    @Order(3)
    void testEntryAddition() {
        for (int i = 0; i < testPCs.size(); i++) {
            pointTable.addNewEntryForAddress(testRefAddr1.get(i), testPCs.get(i), dataDeps[i], tripCounts[i]);
        }
        for (BigInteger keyAddr : pointTable.getKeySet()) {
            System.out.println("The key is 0x" + InstructionsFileReader.toHexString(keyAddr));
            Assertions.assertTrue(pointTable.containsKey(keyAddr));
            Assertions.assertTrue(pointTable.getTableEntry(keyAddr).size() != 0);
            ListIterator<TableEntryPC> iteratorsEntries = pointTable.getTableEntry(keyAddr).listIterator();
            while (iteratorsEntries.hasNext()) {
                TableEntryPC tableEntry = iteratorsEntries.next();
                System.out.println("The Table Entry has a PC address of 0x" + InstructionsFileReader.toHexString(tableEntry.getAddressPC())
                        + ", and has a trip count of " + tableEntry.getTripCount()
                        + ", and has an access type of " + MemoryAccess.getStringMemAccess(tableEntry.getMemAccessType())
                        + ", and a frequency of " + tableEntry.getNumOccurrence()
                );
            }
            System.out.println("\n");
        }
    }

    @Test
    @Order(4)
    void testContainsAccessType() {
        System.out.println(pointTable.getKeySet().size());
        testEntryAddition();

        for (BigInteger key : pointTable.getKeySet()) {
            System.out.println(key);
        }
        System.out.println("\n");
        BigInteger testKey = new BigInteger("fdbbf4761ffaa85a", 16);
        System.out.println(testKey);

        Assertions.assertTrue(pointTable.containsKey(new BigInteger("fdbbf4761ffaa85a", 16)));
        Assertions.assertTrue(pointTable.containsAccessType(new BigInteger("0x339266de0e99d99a".substring(2), 16), MemoryAccess.WRITE));
        Assertions.assertFalse(pointTable.containsAccessType(new BigInteger("0x339266de0e99d99a".substring(2), 16), MemoryAccess.READ));
        Assertions.assertFalse(pointTable.containsAccessType(new BigInteger("0x339266de0e99d99a".substring(2), 16), MemoryAccess.INVALID));

        Assertions.assertTrue(pointTable.containsAccessType(new BigInteger("0x7a01103a5e436a5c".substring(2), 16), MemoryAccess.WRITE));
        Assertions.assertTrue(pointTable.containsAccessType(new BigInteger("0x7a01103a5e436a5c".substring(2), 16), MemoryAccess.READ));
        Assertions.assertFalse(pointTable.containsAccessType(new BigInteger("0x7a01103a5e436a5c".substring(2), 16), MemoryAccess.INVALID));

        Assertions.assertTrue(pointTable.containsAccessType(new BigInteger("0xcc6a29025a7591f8".substring(2), 16), MemoryAccess.READ));
        Assertions.assertFalse(pointTable.containsAccessType(new BigInteger("0xcc6a29025a7591f8".substring(2), 16), MemoryAccess.WRITE));
        Assertions.assertFalse(pointTable.containsAccessType(new BigInteger("0xcc6a29025a7591f8".substring(2), 16), MemoryAccess.INVALID));

        Assertions.assertTrue(pointTable.containsAccessType(new BigInteger("0xfdbbf4761ffaa85a".substring(2), 16), MemoryAccess.READ));
        Assertions.assertFalse(pointTable.containsAccessType(new BigInteger("0xfdbbf4761ffaa85a".substring(2), 16), MemoryAccess.WRITE));
        Assertions.assertFalse(pointTable.containsAccessType(new BigInteger("0xfdbbf4761ffaa85a".substring(2), 16), MemoryAccess.INVALID));
    }

    @TestFactory
    @Order(5)
    Stream<DynamicTest> setParamsPCTest2() {

        return IntStream.iterate(0, n -> n + 1).limit(testStrPCs2.length)
                .mapToObj(n -> {
                    return DynamicTest.dynamicTest("Test" + n,
                            () -> {
                                assertEquals(InstructionsFileReader.toHexString(testPCs2.get(n)), testStrPCs2[n].substring(2));
                            });
                });
    }

    @TestFactory
    @Order(6)
    Stream<DynamicTest> setParamsRefAddrTest2() {

        return IntStream.iterate(0, n -> n + 1).limit(testStrRefAddr2.length)
                .mapToObj(n -> {
                    return DynamicTest.dynamicTest("Test" + n,
                            () -> {
                                assertEquals(InstructionsFileReader.toHexString(testRefAddr2.get(n)), testStrRefAddr2[n].substring(2));
                            });
                });
    }

    @Test
    PointTable constructPointTable(@NotNull List<BigInteger> refAddresses, @NotNull List<BigInteger> PCAddresses, long @NotNull [] tripCounts, MemoryAccess @NotNull [] memAccesses) {
        Assertions.assertTrue(refAddresses.size() == PCAddresses.size());
        Assertions.assertTrue(PCAddresses.size() == tripCounts.length);
        Assertions.assertTrue(tripCounts.length == memAccesses.length);

        PointTable pt = new PointTable();

        for (int i = 0; i < refAddresses.size(); i++) {
            pt.addNewEntryForAddress(refAddresses.get(i), PCAddresses.get(i), memAccesses[i], tripCounts[i]);
        }
        System.out.println("CUSTOM TABLE");
        for (BigInteger keyAddr : pt.getKeySet()) {
            System.out.println("The key is 0x" + InstructionsFileReader.toHexString(keyAddr));
            Assertions.assertTrue(pt.containsKey(keyAddr));
            Assertions.assertTrue(pt.getTableEntry(keyAddr).size() != 0);
            ListIterator<TableEntryPC> iteratorsEntries = pt.getTableEntry(keyAddr).listIterator();
            for (TableEntryPC tableEntry : pt.getTableEntry(keyAddr)) {
                System.out.println("The Table Entry has a PC address of 0x" + InstructionsFileReader.toHexString(tableEntry.getAddressPC())
                        + ", and has a trip count of " + tableEntry.getTripCount()
                        + ", and has an access type of " + MemoryAccess.getStringMemAccess(tableEntry.getMemAccessType())
                        + ", and a frequency of " + tableEntry.getNumOccurrence()
                );
            }
            System.out.println("\n");
        }

        return pt;
    }

    @Test
    void testMergeWith() {
        testEntryAddition();

        PointTable otherPointTable = constructPointTable(testRefAddr2, testPCs2, tripCounts2, dataDeps2);

        pointTable.mergeWith(otherPointTable);

        System.out.println("Newly Merged Table:");

        PointTableTest.printPointTable(pointTable);

    }

    @Test
    void testMergeAddressLine(){
        testEntryAddition();

        PointTable otherPointTable = constructPointTable(testRefAddr2, testPCs2, tripCounts2, dataDeps2);

        BigInteger bigIntKey = new BigInteger("fdbbf4761ffaa85a", 16);
        pointTable.mergeAddressLine(bigIntKey, otherPointTable.getTableEntry(new BigInteger("fdbbf4761ffaa85a", 16)));

        System.out.println("Updated Member Point Table:");

        PointTableTest.printPointTable(pointTable);
    }

    @Test
    void testClearReset(){
        testEntryAddition();

        pointTable.clear();

        System.out.println("The following key set size of the cleared table is: " + pointTable.getKeySet().size());

        pointTable.addNewEntryForAddress(new BigInteger("12345fdfdfdfdf", 16), new BigInteger("0xa7ef9edc95b89611".substring(2), 16), MemoryAccess.READ, 2000);
        pointTable.addNewEntryForAddress(new BigInteger("0x7a01103a5e436a5c".substring(2), 16), new BigInteger("0xc9be839055c5c8ae".substring(2), 16), MemoryAccess.READ, 2001);
        pointTable.addNewEntryForAddress(new BigInteger("0xe909c4bbe311bb17".substring(2), 16), new BigInteger("0x8eacc81f2a4839c7".substring(2), 16), MemoryAccess.WRITE, 2002);
        pointTable.addNewEntryForAddress(new BigInteger("0xe909c4bbe311bb17".substring(2), 16), new BigInteger("0xaa123213123839c7".substring(2), 16), MemoryAccess.READ, 2003);

        System.out.println("The following key set size of the updated table is: " + pointTable.getKeySet().size());
    }
}
