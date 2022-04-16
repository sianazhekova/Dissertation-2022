package analyzers.sd3_analyzer.unit_tests;

import analyzers.baseline_analyzer.MemoryAccess;
import analyzers.baseline_analyzer.PCPair;
import analyzers.baseline_analyzer.PointPC;
import analyzers.sd3_analyzer.IntervalTree;
import analyzers.sd3_analyzer.Stride;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
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

        IntervalTree.IntervalTreeNode overlapNode5 = intervalTree.getNextOverlapNode(overlapNode4, new PointPC(BigInteger.valueOf(4),BigInteger.valueOf(0), null));
        System.out.println(overlapNode5.testStringOutput());
    }

    void initialiseTreeHelper1() {
        //intervalTree.insertInterval(new Stride( BigInteger.valueOf(4), BigInteger.valueOf(10), BigInteger.TWO, BigInteger.valueOf(4), BigInteger.valueOf(4), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(5), BigInteger.valueOf(6), BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(50), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(3), BigInteger.valueOf(5), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(7), BigInteger.valueOf(7), BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(10), MemoryAccess.READ) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(4), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(8), BigInteger.valueOf(30), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(9), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(10), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
    }

    @Test
    void testStrideOverlapIteratorsAndQueries() {
        initialiseTreeHelper1();
        intervalTree.printTree();

        System.out.println("The number of overlaps is: " +
                intervalTree.getNumberOfOverlappingNodes(intervalTree.getRoot(), new PointPC(BigInteger.valueOf(4),BigInteger.valueOf(0), null))
        );

        List<IntervalTree.IntervalTreeNode> testCollection = intervalTree.collectOverlapNodes(intervalTree.getRoot(), new PointPC(BigInteger.valueOf(4),BigInteger.valueOf(0), null));
        testCollection.forEach(node -> System.out.println(node.testStringOutput()));

        List<IntervalTree.IntervalTreeNode> testCollection2 = intervalTree.collectOverlapNodes(new PointPC(BigInteger.valueOf(5),BigInteger.valueOf(0), null));
        testCollection2.forEach(node -> System.out.println(node.testStringOutput()));

        System.out.println("The number of overlaps in the second collection is: " +
                intervalTree.getNumberOfOverlappingNodes(intervalTree.getRoot(), new PointPC(BigInteger.valueOf(5),BigInteger.valueOf(0), null))
        );

        System.out.println();

    }

    @Test
    void testMatchingStrides() {
        initialiseTreeHelper1();
        intervalTree.printTree();

        IntervalTree.IntervalTreeNode matchedNode = intervalTree.matchWithStride(new Stride( BigInteger.valueOf(9), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE)));
        System.out.println("The matched queried node is " + matchedNode.testStringOutput());

        IntervalTree.IntervalTreeNode matchedNode2 = intervalTree.matchWithStride(new Stride( BigInteger.valueOf(8), BigInteger.valueOf(30), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        System.out.println("The matched second queried node is " + matchedNode2.testStringOutput());

        intervalTree.insertInterval(new Stride( BigInteger.valueOf(7), BigInteger.valueOf(7), BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(10), MemoryAccess.WRITE)));
        intervalTree.printTree();

        IntervalTree.IntervalTreeNode matchedNode3 = intervalTree.matchWithStride(new Stride( BigInteger.valueOf(7), BigInteger.valueOf(7), BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(10), MemoryAccess.READ)));
        System.out.println("The third matched queried node is " + matchedNode3.testStringOutput());

        IntervalTree.IntervalTreeNode matchedNode4 = intervalTree.matchWithStride(new Stride( BigInteger.valueOf(7), BigInteger.valueOf(7), BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(10), MemoryAccess.WRITE)));
        System.out.println("The 4th matched queried node is " + matchedNode4.testStringOutput());

        System.out.println("R W comparison  " + Integer.compare(MemoryAccess.READ.getIntID(), MemoryAccess.WRITE.getIntID()) );
    }

    @Test
    void testStrideExtension() {
        initialiseTreeHelper1();
        intervalTree.printTree();



    }

    @Test
    void testStrideDeletion() {
        initialiseTreeHelper1();
        intervalTree.printTree();

        IntervalTree.IntervalTreeNode matchedNode = intervalTree.matchWithStride(new Stride( BigInteger.valueOf(9), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE)));
        System.out.println("The matched queried node is " + matchedNode.testStringOutput());

        intervalTree.delete(matchedNode);
        intervalTree.printTree();

        IntervalTree.IntervalTreeNode matchedNode2 = intervalTree.matchWithStride(new Stride( BigInteger.valueOf(8),
                BigInteger.valueOf(30),
                BigInteger.TWO,
                BigInteger.valueOf(6),
                BigInteger.valueOf(3),
                new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) )
        );
        System.out.println("The matched second queried node is " + matchedNode2.testStringOutput());

        IntervalTree.IntervalTreeNode matchedNode4 =  intervalTree.matchWithStride(new Stride( BigInteger.valueOf(7),
                BigInteger.valueOf(7),
                BigInteger.ONE,
                BigInteger.valueOf(2),
                BigInteger.valueOf(2),
                new PCPair(BigInteger.valueOf(10), MemoryAccess.READ))
        );
        System.out.println("The matched fourth queried node is " + matchedNode4.testStringOutput());

        //intervalTree.delete(matchedNode2);
        //intervalTree.printTree();

        System.out.println(((Stride)(intervalTree.getRoot().getRightChild().getLeftChild().getInterval())).getTestStringStrideState());


        intervalTree.delete(matchedNode4);
        intervalTree.printTree();

        intervalTree.delete(matchedNode2);
        intervalTree.printTree();
    }

    @Test
    void testKillAddress() {



    }

    @Test
    void copyTree() {
        initialiseTreeHelper1();
        intervalTree.copyTree();



    }

}
