package analyzers.sd3_analyzer.unit_tests;

import analyzers.baseline_analyzer.MemoryAccess;
import analyzers.baseline_analyzer.PCPair;
import analyzers.baseline_analyzer.PointPC;
import analyzers.sd3_analyzer.IntervalTree;
import analyzers.sd3_analyzer.Stride;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.logging.Logger;

public class IntervalTreeTest {

    private final Logger logger = Logger.getLogger(IntervalTreeTest.class.getName());
    private IntervalTree intervalTree;

    @BeforeEach
    @Test
    void setUp() {
        logger.info("Initialising the interval tree");
        intervalTree = new IntervalTree();

    }

    @Test
    void strideInsertion() {
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(4), BigInteger.valueOf(10), BigInteger.TWO, BigInteger.valueOf(4), BigInteger.valueOf(4), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(5), BigInteger.valueOf(6), BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(50), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(3), BigInteger.valueOf(5), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(7), BigInteger.valueOf(7), BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(10), MemoryAccess.READ) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(4), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(8), BigInteger.valueOf(30), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(9), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(10), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(7), BigInteger.valueOf(8), BigInteger.TWO, BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(10), MemoryAccess.READ) ));
        intervalTree.printTree();
    }

    @Test
    void testGetMinOverlapNode() {
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(4), BigInteger.valueOf(10), BigInteger.TWO, BigInteger.valueOf(4), BigInteger.valueOf(4), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(5), BigInteger.valueOf(6), BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(50), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(3), BigInteger.valueOf(5), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(7), BigInteger.valueOf(7), BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(10), MemoryAccess.READ) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(4), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(8), BigInteger.valueOf(30), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(9), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(10), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));

        intervalTree.printTree();

        IntervalTree.IntervalTreeNode overlapNode = intervalTree.getMinOverlapNode(intervalTree.getRoot(), new PointPC(BigInteger.valueOf(4),BigInteger.valueOf(0), null));
        System.out.println(overlapNode.testStringOutput());

        IntervalTree.IntervalTreeNode overlapNode2 = intervalTree.getMinOverlapNode(intervalTree.getRoot(), new PointPC(BigInteger.valueOf(7),BigInteger.valueOf(7), null));
        System.out.println(overlapNode2.testStringOutput());

        IntervalTree.IntervalTreeNode overlapNode3 = intervalTree.getMinOverlapNode( new PointPC(BigInteger.valueOf(10),BigInteger.valueOf(10), null));
        System.out.println(overlapNode3.testStringOutput());
    }

    @Test
    void testGetNextOverlapNode() {
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(4), BigInteger.valueOf(10), BigInteger.TWO, BigInteger.valueOf(4), BigInteger.valueOf(4), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(5), BigInteger.valueOf(6), BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(50), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(3), BigInteger.valueOf(5), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(7), BigInteger.valueOf(7), BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(10), MemoryAccess.READ) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(4), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(8), BigInteger.valueOf(30), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(9), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(10), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.printTree();

        IntervalTree.IntervalTreeNode overlapNode = intervalTree.getMinOverlapNode(intervalTree.getRoot(), new PointPC(BigInteger.valueOf(4),BigInteger.valueOf(0), null));
        System.out.println(overlapNode.testStringOutput());

        IntervalTree.IntervalTreeNode overlapNode2 = intervalTree.getNextOverlapNode(overlapNode, new PointPC(BigInteger.valueOf(4),BigInteger.valueOf(0), null));
        System.out.println(overlapNode2.testStringOutput());

        IntervalTree.IntervalTreeNode overlapNode3 = intervalTree.getNextOverlapNode(overlapNode2, new PointPC(BigInteger.valueOf(4),BigInteger.valueOf(0), null));
        System.out.println(overlapNode3.testStringOutput());

        IntervalTree.IntervalTreeNode overlapNode4 = intervalTree.getNextOverlapNode(overlapNode3, new PointPC(BigInteger.valueOf(4),BigInteger.valueOf(0), null));
        System.out.println(overlapNode4.testStringOutput());
    }

    @Test
    void testStrideDeletion() {


    }

    @Test
    void testStrideExtension() {



    }

    @Test
    void testKillAddress() {



    }

}
