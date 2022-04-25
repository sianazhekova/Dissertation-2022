package analyzers.sd3_analyzer.unit_tests;

import analyzers.baseline_analyzer.MemoryAccess;
import analyzers.baseline_analyzer.PCPair;
import analyzers.baseline_analyzer.PointPC;
import analyzers.sd3_analyzer.IntervalTree;
import analyzers.sd3_analyzer.Stride;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Iterator;
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
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(9), BigInteger.valueOf(21), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(10), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
    }

    @Test
    void testOverlapQueries() {
        initialiseTreeHelper1();
        intervalTree.printTree();

        IntervalTree.IntervalTreeNode overlapNode = intervalTree.getMinOverlapNode(intervalTree.getRoot(), new PointPC(BigInteger.valueOf(9),BigInteger.valueOf(0), null));
        System.out.println("For query point 9, the test stride state is : " + ((Stride)overlapNode.getInterval()).getTestStringStrideState());

        IntervalTree.IntervalTreeNode nextNode = intervalTree.getNextOverlapNode(overlapNode, new PointPC(BigInteger.valueOf(9),BigInteger.valueOf(0), null));
        System.out.println("For query point 9, the next test stride state is : " + ((Stride)nextNode.getInterval()).getTestStringStrideState());

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
        System.out.println("The number of overlaps is : " + testCollection.size());

        List<IntervalTree.IntervalTreeNode> testCollection2 = intervalTree.collectOverlapNodes(new PointPC(BigInteger.valueOf(5),BigInteger.valueOf(0), null));
        testCollection2.forEach(node -> System.out.println(node.testStringOutput()));

        System.out.println("The number of overlaps in the second collection is: " +
                intervalTree.getNumberOfOverlappingNodes(intervalTree.getRoot(), new PointPC(BigInteger.valueOf(5),BigInteger.valueOf(0), null))
        );

        System.out.println();

        List<IntervalTree.IntervalTreeNode> testCollection3 = intervalTree.collectOverlapNodes(new PointPC(BigInteger.valueOf(9),BigInteger.valueOf(0), null));
        testCollection3.forEach(node -> System.out.println(node.testStringOutput()));

        System.out.println("The number of overlaps in the second collection is: " +
                intervalTree.getNumberOfOverlappingNodes(intervalTree.getRoot(), new PointPC(BigInteger.valueOf(9),BigInteger.valueOf(0), null))
        );

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

        System.out.println("Expanding [8, 30] to [7, 30]");
        intervalTree.insertNewAddress(new Stride( BigInteger.valueOf(8), BigInteger.valueOf(30), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ), BigInteger.valueOf(7));
        intervalTree.printTree();

        System.out.println("Expanding [5, 6] to [4, 6]");
        intervalTree.insertNewAddress(new Stride( BigInteger.valueOf(5), BigInteger.valueOf(6), BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(50), MemoryAccess.WRITE) ), BigInteger.valueOf(4));
        intervalTree.printTree();

        System.out.println("Expanding [9, 21] to [9,41]");
        intervalTree.insertNewAddress(new Stride( BigInteger.valueOf(9), BigInteger.valueOf(21), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ), BigInteger.valueOf(41));
        intervalTree.printTree();


    }

    @Test
    void testStrideDeletion() {
        initialiseTreeHelper1();
        intervalTree.printTree();

        IntervalTree.IntervalTreeNode matchedNode = intervalTree.matchWithStride(new Stride( BigInteger.valueOf(9), BigInteger.valueOf(21), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE)));
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

        intervalTree.delete(matchedNode2);
        intervalTree.printTree();

        System.out.println(((Stride)(intervalTree.getRoot().getRightChild().getLeftChild().getInterval())).getTestStringStrideState());


        intervalTree.delete(matchedNode4);
        intervalTree.printTree();

        //intervalTree.delete(matchedNode2);
        //intervalTree.printTree();
    }

    @Test
    void testStrideDeletion2() {
        initialiseTreeHelper1();
        intervalTree.printTree();
        IntervalTree.IntervalTreeNode matchedNode = intervalTree.matchWithStride(new Stride( BigInteger.valueOf(9), BigInteger.valueOf(21), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE)));
        intervalTree.delete(matchedNode);
        intervalTree.printTree();


    }

    @Test
    void testKillAddress() {
        initialiseTreeHelper1();
        intervalTree.printTree();
        //iteratorHelper(intervalTree);

        intervalTree.killAddress(BigInteger.valueOf(5));
        System.out.println("Printing the tree after killing address 5");
        intervalTree.printTree();

        //iteratorHelper(intervalTree);

        intervalTree.killAddress(BigInteger.valueOf(4));
        System.out.println("Printing the tree after killing address 4");
        intervalTree.printTree();
        //iteratorHelper(intervalTree);

        intervalTree.killAddress(BigInteger.valueOf(9));
        System.out.println("Printing the tree after killing address 9");
        intervalTree.printTree();
        //iteratorHelper(intervalTree);

        intervalTree.killAddress(BigInteger.valueOf(13));
        System.out.println("Printing the tree after killing address 13");
        intervalTree.printTree();



    }

    void iteratorHelper(@NotNull IntervalTree treeToIter) {
        Iterator<IntervalTree.IntervalTreeNode> iterator = treeToIter.iterator();

        while (iterator.hasNext()) {

            IntervalTree.IntervalTreeNode nodeToIter = iterator.next();
            System.out.println("The next node is :" + nodeToIter.testStringOutput());

            Stride iterStride = (Stride)nodeToIter.getInterval();
            System.out.println("The stride is : " + iterStride.getTestStringStrideState());
        }
    }

    @Test
    void testMergeTree() {
        logger.info("Initialising the member tree.");
        initialiseTreeHelper1();
        intervalTree.printTree();

        IntervalTree intervalTree2 = new IntervalTree();
        intervalTree2.insertInterval(new Stride( BigInteger.valueOf(4),
                BigInteger.valueOf(10),
                BigInteger.TWO,
                BigInteger.valueOf(4),
                BigInteger.valueOf(4),
                new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE)
        ));

        intervalTree2.insertInterval(new Stride( BigInteger.valueOf(3),
                BigInteger.valueOf(40),
                BigInteger.TWO,
                BigInteger.valueOf(6),
                BigInteger.valueOf(3),
                new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) )
        );
        intervalTree2.insertInterval(new Stride( BigInteger.valueOf(10),
                BigInteger.valueOf(50),
                BigInteger.valueOf(2),
                BigInteger.valueOf(4),
                BigInteger.valueOf(4),
                new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE)
        ));
        intervalTree2.insertInterval(new Stride( BigInteger.valueOf(7),
                BigInteger.valueOf(17),
                BigInteger.ONE,
                BigInteger.valueOf(11),
                BigInteger.valueOf(12),
                new PCPair(BigInteger.valueOf(10), MemoryAccess.READ)
        ));
        intervalTree2.insertInterval(new Stride( BigInteger.valueOf(1),
                BigInteger.valueOf(10),
                BigInteger.ONE,
                BigInteger.valueOf(11),
                BigInteger.valueOf(12),
                new PCPair(BigInteger.valueOf(10), MemoryAccess.READ)
        ));
        System.out.println("");
        logger.info("Printing the second interval tree");
        intervalTree2.printTree();

        logger.info("Merging the interval trees");
        this.intervalTree.mergeWith(intervalTree2);
        this.intervalTree.printTree();

        Stride matchedExtStride = (Stride)intervalTree.matchWithStride(new Stride( BigInteger.valueOf(7),
                BigInteger.valueOf(17),
                BigInteger.ONE,
                BigInteger.ZERO,
                BigInteger.ZERO,
                new PCPair(BigInteger.valueOf(10), MemoryAccess.READ)
        )).getInterval();


        System.out.println("Matched stride is :" + matchedExtStride.getTestStringStrideState());
    }

    @Test
    void mergeTreeTest2() {
        logger.info("Initialising the member tree.");

        intervalTree.insertInterval(new Stride( BigInteger.valueOf(5), BigInteger.valueOf(6), BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(50), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(3), BigInteger.valueOf(5), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(7), BigInteger.valueOf(7), BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(10), MemoryAccess.READ) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(4), BigInteger.valueOf(20), BigInteger.valueOf(4), BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(8), BigInteger.valueOf(32), BigInteger.valueOf(6), BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(9), BigInteger.valueOf(21), BigInteger.valueOf(6), BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(10), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(10), BigInteger.valueOf(24), BigInteger.valueOf(7), BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));

        intervalTree.printTree();

        IntervalTree intervalTree2 = new IntervalTree();
        intervalTree2.insertInterval(new Stride( BigInteger.valueOf(2),
                BigInteger.valueOf(4),
                BigInteger.valueOf(4),
                BigInteger.valueOf(4),
                BigInteger.valueOf(4),
                new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE)
        ));
        intervalTree2.insertInterval(new Stride( BigInteger.valueOf(5),
                BigInteger.valueOf(40),
                BigInteger.ONE,
                BigInteger.valueOf(4),
                BigInteger.valueOf(4),
                new PCPair(BigInteger.valueOf(50), MemoryAccess.WRITE)
        ));
        intervalTree2.insertInterval(new Stride( BigInteger.valueOf(10),
                        BigInteger.valueOf(50),
                        BigInteger.valueOf(7),
                        BigInteger.valueOf(6),
                        BigInteger.valueOf(3),
                        new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE)
        ));
        intervalTree2.insertInterval(new Stride( BigInteger.valueOf(7),
                BigInteger.valueOf(17),
                BigInteger.ONE,
                BigInteger.valueOf(11),
                BigInteger.valueOf(12),
                new PCPair(BigInteger.valueOf(10), MemoryAccess.READ)
        ));
        intervalTree2.insertInterval(new Stride( BigInteger.valueOf(1),
                BigInteger.valueOf(10),
                BigInteger.valueOf(7),
                BigInteger.valueOf(11),
                BigInteger.valueOf(12),
                new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE)
        ));
        intervalTree2.printTree();

        intervalTree.mergeWithHelper(intervalTree2, true);
        System.out.println("NEW TREE");
        intervalTree.printTree();



    }

    @Test
    void testTreeIteration() {
        logger.info("Initialising the member tree.");
        initialiseTreeHelper1();
        intervalTree.printTree();

        logger.info("Iterating through the entries of the interval tree");
        Iterator<IntervalTree.IntervalTreeNode> iterator = intervalTree.iterator();

        while (iterator.hasNext()) {
            IntervalTree.IntervalTreeNode nextNode = iterator.next();
            Assertions.assertTrue(!nextNode.isNil());
            System.out.println("The next node to be iterated is : " + nextNode);
        }

    }


    @Test
    void copyTreeTest() {
        initialiseTreeHelper1();
        System.out.println("The initial member interval tree");
        intervalTree.printTree();

        IntervalTree treeCopy1 = intervalTree.copyTree();
        System.out.println("Printing the member interval tree after deletion of its root");
        intervalTree.delete(intervalTree.getRoot());
        intervalTree.printTree();

        System.out.println("Printing the tree copy");
        treeCopy1.printTree();

        IntervalTree intervalTree2 = new IntervalTree();
        intervalTree2.insertInterval(new Stride( BigInteger.valueOf(4),
                BigInteger.valueOf(10),
                BigInteger.TWO,
                BigInteger.valueOf(4),
                BigInteger.valueOf(4),
                new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE)
        ));
        intervalTree2.insertInterval(new Stride( BigInteger.valueOf(3),
                BigInteger.valueOf(40),
                BigInteger.TWO,
                BigInteger.valueOf(4),
                BigInteger.valueOf(4),
                new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE)
        ));
        intervalTree2.insertInterval(new Stride( BigInteger.valueOf(10),
                BigInteger.valueOf(50),
                BigInteger.valueOf(3),
                BigInteger.valueOf(4),
                BigInteger.valueOf(4),
                new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE)
        ));
        intervalTree2.insertInterval(new Stride( BigInteger.valueOf(7),
                BigInteger.valueOf(17),
                BigInteger.ONE,
                BigInteger.valueOf(11),
                BigInteger.valueOf(12),
                new PCPair(BigInteger.valueOf(10), MemoryAccess.READ)
        ));
        /*System.out.println("Printing the interval tree");


        IntervalTree copiedTree2 = intervalTree2.copyTree();
        intervalTree2.delete(intervalTree2.getRoot());
        */


    }

}
