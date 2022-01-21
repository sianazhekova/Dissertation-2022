package analyzers.baseline_analyzer.unit_tests;

import analyzers.baseline_analyzer.MemoryAccess;
import analyzers.baseline_analyzer.PCPair;
import analyzers.baseline_analyzer.PointPC;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class PointPCTest {

    PointPC point1;
    PointPC point2;
    PointPC point3;
    PointPC point4;

    @BeforeEach
    void setUp() {
        point1 = new PointPC(new BigInteger("0xdf5d48cb88915f64".substring(2), 16), BigInteger.valueOf(123456), MemoryAccess.WRITE, new BigInteger("0x6dcc3e48b31a46e9".substring(2), 16));
        point2 = new PointPC(new BigInteger("0x38c8bd7fe1bd6989".substring(2), 16), BigInteger.valueOf(123456), new PCPair(new BigInteger("0xc0e723a6cacaccbf".substring(2), 16),  MemoryAccess.READ));

    }

    @Test
    void additionTest() {
        Assertions.assertEquals(new BigInteger("0xdf5d48cb88915f64".substring(2), 16), point1.getRefStartAddress());
        Assertions.assertEquals(new BigInteger("0x38c8bd7fe1bd6989".substring(2), 16), point2.getRefStartAddress());

        Assertions.assertEquals(new BigInteger("0xdf5d48cb88915f64".substring(2), 16).add(BigInteger.valueOf(123456)), point1.getEndAddress());
        Assertions.assertEquals(new BigInteger("0x38c8bd7fe1bd6989".substring(2), 16).add(BigInteger.valueOf(123456)), point2.getEndAddress());
    }
}
