package analyzers.sd3_analyzer;

import analyzers.baseline_analyzer.IntervalType;
import analyzers.baseline_analyzer.MemoryAccess;
import analyzers.baseline_analyzer.PCPair;
import analyzers.baseline_analyzer.PointPC;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;



/**
 * Code has been adapted from https://github.com/charcuterie/interval-tree/blob/master/src/datastructures/IntervalTree.java
 * Methods have been implemented, that have been originally discussed in CLRS --- Introduction to Algorithms, 2nd edition:
 *
 * - In Chapter 12, a data structure, known as a Red-Black Tree has been introduced which refers to the
 *
 * - In Chapter 14.3, an augmenting data structure (extension), known as an Interval Tree, has been discussed outlining
 *   the main methods
 *
 * */

public class IntervalTree implements Iterable<IntervalTree.IntervalTreeNode> {

    private IntervalTreeNode root;
    final IntervalTreeNode nil = new IntervalTreeNode(); // sentinel (Nil) node
    private int treeSize;

    public IntervalTree() {
        treeSize = 0;
        //nil = new IntervalTreeNode();
        root = nil;
    }

    public IntervalTree(IntervalType intToInsert) {
        treeSize = 1;
        //nil = new IntervalTreeNode();
        root = new IntervalTreeNode(intToInsert);
        root.setToBlack(); // the root is set to be BLACK by default
    }

    public IntervalTree(IntervalTree otherTree) {
        root = otherTree.root.copyNode();
        //nil = new IntervalTreeNode();
        treeSize = otherTree.treeSize;
    }

    /**
     * (Deep-)Copy the interval tree and return a pointer to the root
     * */

    public IntervalTree copyTree() {
        if (!root.isNil()) {
            IntervalTree copy =  new IntervalTree(this);
            return copy;
        } else return new IntervalTree();
    }

    // General field query/set-up methods

    public int getTreeSize() {
        return treeSize;
    }

    public int getTreeDepth() {
        return (int) Math.floor(Math.log(treeSize));
    }

    public IntervalTreeNode getRoot() {
        return root;
    }

    public void incrementSize() { this.treeSize++; }


    // INSERTION METHODS
    /**
     * A method outlining an insertion of a new memory address (in the form of ) into the interval tree. It is based on the read-black tree insertion method.
     * */

    public boolean insertNewAddress(Stride strideLoc, BigInteger newAddress) {
        if (!root.isNil() && root.getInterval() instanceof Stride) {
            boolean flagDelIn = false;
            IntervalTreeNode matchNode = matchWithStride(strideLoc);

            if (!matchNode.isNil()) {
                if (strideLoc.getStartAddress().compareTo(newAddress) == 1) {
                    flagDelIn = true;
                }

                if (flagDelIn) {
                    delete(matchNode);
                }

                // Expand the stride
                Stride matchedNodeStride = ((Stride)matchNode.getInterval());
                matchedNodeStride.expandStride(newAddress);

                if (flagDelIn) {
                    insertInterval(matchedNodeStride);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * A method outlining an insertion of an interval into the interval tree. It is based on the read-black tree insertion method.
     * */

    public boolean insertInterval(IntervalType intToInsert) {
        IntervalTreeNode newNode = new IntervalTreeNode(intToInsert);
        IntervalTreeNode y = nil;
        IntervalTreeNode x = root;

        while (!x.isNil()) {
            y = x;
            if (intToInsert.compareTo(x.getInterval()) == -1) {
                x = x.leftChild;
            } else if (intToInsert.compareTo(x.getInterval()) == 1) {
                x = x.rightChild;
            } else {
                // we have two intervals with the same starting memory address
                IntervalType xInterval = x.getInterval();

                if ((intToInsert instanceof Stride) && (xInterval instanceof Stride)) {
                    BigInteger xStrideDist = ((Stride) xInterval).getStrideDistance();
                    BigInteger inputStrideDist = ((Stride) intToInsert).getStrideDistance();

                    // case 1: both are strides with a different stride distance
                    if (inputStrideDist.compareTo(xStrideDist) == -1 ) {
                        x = x.leftChild;
                    } else if (inputStrideDist.compareTo(xStrideDist) == 1) {
                        x = x.rightChild;
                    } else {
                        // case 2: both are strides with the same stride distance
                        // Check the end address and extend stride and merge statistics.
                        // For approximation purposes, set number of distinct addresses to the larger of the two
                        if (((Stride) intToInsert).getPCAndReadWrite().equals(((Stride) xInterval).getPCAndReadWrite())) {
                            BigInteger maxEndAddress = xInterval.getEndAddress().max(intToInsert.getEndAddress());
                            ((Stride) xInterval).updateHighAddress(maxEndAddress);

                            ((Stride) xInterval).addNumAccesses(((Stride) intToInsert).getTotalNumAccesses());
                            ((Stride) xInterval).setSizeOfAccess(((Stride) xInterval).getSizeOfAccess().max(((Stride) intToInsert).getSizeOfAccess()));
                            return true;
                        } else {
                            x = ((Stride) intToInsert).getPCAndReadWrite().compareTo(((Stride) xInterval).getPCAndReadWrite()) == -1 ? x.leftChild : x.rightChild;
                        }
                    }

                } /* else if ((intToInsert instanceof PointPC) && (xInterval instanceof Stride)) {
                    // case 3: if interval to insert is a pointPC -> expand interval
                    if ( (((PointPC) intToInsert).getPCPair().isWrite() && ((Stride) xInterval).getPCAndReadWrite().isWrite()) &&
                            ((PointPC) intToInsert).getPCPair().isRead() && ((Stride) xInterval).getPCAndReadWrite().isRead() ) {
                        BigInteger maxEndAddress = xInterval.getEndAddress().max(intToInsert.getEndAddress());
                        ((Stride) xInterval).updateHighAddress(maxEndAddress);
                        ((Stride) xInterval).addNumAccesses(((Stride) intToInsert).getTotalNumAccesses());
                        ((Stride) xInterval).setSizeOfAccess(((Stride) xInterval).getSizeOfAccess().max(((Stride) intToInsert).getSizeOfAccess()));
                    }}*/
                else {
                    // else for all remaining cases simply false -> only deal with strides in interval tree
                    return false;
                }
            }
        }

        newNode.parent = y;
        if (y.isNil()) {
            root = newNode;
            root.setToBlack();
        } else if (intToInsert.compareTo(y.getInterval()) == -1) {
            y.leftChild = newNode;
        } else {
            y.rightChild = newNode;
        }

        newNode.leftChild = nil;
        newNode.rightChild = nil;
        newNode.setToRed();

        insertFixUpRB(newNode);
        return true;
    }

    /**
     * A method that is used to maintain the red-black tree and interval tree constraints after the insertion of a node
     * */

    private void insertFixUpRB(@NotNull IntervalTreeNode z) {
        updateMaxUpwards(z);
        while (z.parent.isRed()) {
            IntervalTreeNode uncle = z.getUncle();
            if (z.parent.isLeftChild()) {
                if (uncle.isRed()) {
                    z.parent.setToBlack();
                    uncle.setToBlack();
                    z.getGrandparent().setToRed();
                    z = z.getGrandparent();
                } else {
                    // if z's uncle is black
                    if (z.isRightChild()) {
                        z = z.parent;
                        leftRotation(z);
                    }
                    z.parent.setToBlack();
                    z.getGrandparent().setToRed();
                    rightRotation(z.getGrandparent());
                }
            } else {
                // if z's parent is the right child of z's grandparent
                if (uncle.isRed()) {
                    z.parent.setToBlack();
                    uncle.setToBlack();
                    z.getGrandparent().setToRed();
                    z = z.getGrandparent();
                } else {
                    // if z's uncle is black
                    if (z.isLeftChild()) {
                        z = z.parent;
                        rightRotation(z);
                    }
                    z.parent.setToBlack();
                    z.getGrandparent().setToRed();
                    leftRotation(z.getGrandparent());
                }
            }
        }

        root.setToBlack();
    }

    // DELETION METHODS
    /**
     * A method outlining the deletion of an interval tree node
     * */
/*
    public boolean delete(@NotNull IntervalTreeNode z) {
        if (z.isNil()) return false;

        treeSize--;

        IntervalTreeNode y = z;
        boolean isYBlackInitially = y.isBlack();
        IntervalTreeNode x;

        if (z.leftChild.isNil()) {
            x = z.rightChild;
            transplantRB(z, z.rightChild);
            updateMaxUpwards(x);
        } else if (z.rightChild.isNil()) {
            x = z.leftChild;
            transplantRB(z, z.leftChild);
            updateMaxUpwards(x);
        } else {
            y = getSmallestNode(z.rightChild);
            isYBlackInitially = y.isBlack();
            x = y.rightChild;
            System.out.println(x.testStringOutput());
            if (y.parent == z)
                x.parent = y;
            else {
                transplantRB(y, y.rightChild);
                System.out.println(y.rightChild);
                updateMaxUpwards(x);

                y.rightChild = z.rightChild;
                y.rightChild.parent = y;
            }
            transplantRB(z, y);
            y.leftChild = z.leftChild;
            y.leftChild.parent = y;
            y.isBlackNode = z.isBlack();
            updateMaxUpwards(y);
        }
        if (isYBlackInitially)
            deleteFixUpRB(x);

        return true;
    }   */

    /*
    public boolean delete(IntervalTreeNode z) {
        if (z.isNil()) return false;

        z.max = BigInteger.valueOf(Long.MIN_VALUE);
        for (IntervalTreeNode i = z.parent; !i.isRoot(); i = i.parent)
            i.max = i.leftChild.getMaxAddress().max(i.rightChild.getMaxAddress());

        IntervalTreeNode y;
        IntervalTreeNode x;

        if (z.leftChild.isNil() || z.rightChild.isNil()) {
            y = z;
        } else y = getSuccessor(z);

        if (y.leftChild.isNil())
            x = y.rightChild;
        else x = y.leftChild;

        x.parent = y.parent;
        if (root == x.parent)
            root.leftChild = x;
        else if (y == y.parent.leftChild)
            y.parent.leftChild = x;
        else y.parent.rightChild = x;

        if ( y != z) {
            if (y.isBlack())
                deleteFixUpRB(x);

            y.leftChild = z.leftChild;
            y.rightChild = z.rightChild;
            y.parent = z.parent;
            y.isBlackNode = (z.isBlack());
            z.leftChild.parent = z.rightChild.parent = y;
            if (z == z.parent.leftChild)
                z.parent.leftChild = y;
            else z.parent.rightChild = y;
        }
        else if (y.isBlack())
            deleteFixUpRB(x);

        return true;
    } */

    /**
     * Due to time constraints, adapted from https://github.com/charcuterie/interval-tree/issues/1 (correction from) and
     *  https://github.com/charcuterie/interval-tree/blob/65dc2fc8f754127aa09fba0dff6f43b10ac151cb/src/datastructures/IntervalTree.java#L384
     * */


    public boolean delete(IntervalTreeNode z) {

        if (z.isNil()) {  // Can't delete the sentinel node.
            return false;
        }

        Assertions.assertTrue(nil.isBlackNode);
        IntervalTreeNode y = z;

        if (z.hasTwoChildren()) { // If the node to remove has two children,
            //System.out.println(" z is : " + z.testStringOutput());
            y = getSuccessor(z);    // copy the successor's data into it and
            //System.out.println("The successor is " + y.testStringOutput());
            copyInterval(z, y);        // remove the successor. The successor is
            updateMaxUpwards(z);      // guaranteed to both exist and have at most
        }                       // one child, so we've converted the two-
        // child case to a one- or no-child case.

        Assertions.assertTrue(nil.isBlackNode);
        IntervalTreeNode x = y.leftChild.isNil() ? y.rightChild : y.leftChild; // replacement

        if(!x.isNil()) { // if (replacement != null)
            x.parent = y.parent;

            if (y.isRoot()) {
                root = x;
            } else if (y.isLeftChild()) {
                y.parent.leftChild = x;
                updateMaxUpwards(y);
            } else {
                y.parent.rightChild = x;
                updateMaxUpwards(y);
            }

            if (y.isBlack()) {
                updateMaxUpwards(x);
            }
        } else if(y.parent.isNil()) { // return if we are the only node.
            root = nil;
        } else { //  No children. Use self as phantom replacement and unlink.
            Assertions.assertTrue(nil.isBlackNode);
            if (y.isBlack())
                deleteFixUpRB(y);

            if (!y.parent.isNil()) {
                if (y.isLeftChild())
                    y.parent.leftChild = nil;
                else if (y.isRightChild())
                    y.parent.rightChild = nil;
                y.parent = nil;
            }
        }

        treeSize--;
        return true;
    }

    private void copyInterval(@NotNull IntervalTreeNode node, @NotNull IntervalTreeNode otherNode) {
        System.out.println("The test String output is : " + otherNode.testStringOutput());
        node.interval = otherNode.getInterval().copy();
    }

    /**
     * A helper-function that is used to transplant(link) node v with the "upper" connections of u
     * */

    private void transplantRB(@NotNull IntervalTreeNode u, IntervalTreeNode v) {
        if (u.parent.isNil()) {
            root = v;
        } else if (u.isLeftChild()) {
            u.parent.leftChild = v;
        } else {
            u.parent.rightChild = v;
        }
        v.parent = u.parent;
        if (v.isNil())
            v.parent = nil;
    }

    /**
     * A fix-up function that is used to preserve the red-black properties after deletion of a node.
     * It is used to correct the cases of a red-and-black or double-black node x.
     * */

    private void deleteFixUpRB(@NotNull IntervalTreeNode x) {
/*
        while (!x.isRoot() && x.isBlack()) {
            if (x.isLeftChild()) {
                IntervalTreeNode w = x.parent.rightChild;  // get the sibling of x -> w
                if (w.isRed()) {
                    w.setToBlack();
                    x.parent.setToRed();
                    leftRotation(x.parent);
                    w = x.parent.rightChild;
                }
                if (w.leftChild.isBlack() && w.rightChild.isBlack()) {
                    w.setToRed();
                    x = x.parent;
                } else {
                    if (w.rightChild.isBlack()) {
                        w.leftChild.setToBlack();
                        w.setToRed();
                        rightRotation(w);
                        w = x.parent.rightChild;  //x.getUncle()
                    }
                    if (x.parent.isBlack()) {
                        w.setToBlack();
                    } else {
                        w.setToRed();
                    }
                    x.parent.setToBlack();
                    w.rightChild.setToBlack();
                    leftRotation(x.parent);
                    x = root;
                }
            } else {
                // x is the right child of its parent
                assert(x.isRightChild());
                IntervalTreeNode w = x.parent.leftChild;
                if (w.isRed()) {
                    w.setToBlack();
                    x.parent.setToRed();
                    rightRotation(x.parent);
                    w = x.parent.leftChild;
                }
                if (w.rightChild.isBlack() && w.leftChild.isBlack()) {
                    w.setToRed();
                    x = x.parent;
                } else {
                    System.out.println("The w.leftChild is " + (w.leftChild.isNil() ? "nil" : "not nil"));

                    if (!w.leftChild.isNil()  && w.leftChild.isBlack()) {
                        x.rightChild.setToBlack();
                        w.setToRed();
                        leftRotation(w);
                        w = x.parent.leftChild;
                    }
                    if (x.parent.isBlack()) {
                        w.setToBlack();
                    } else {
                        w.setToRed();
                    }
                    x.parent.setToBlack();
                    w.leftChild.setToBlack();
                    rightRotation(x.parent);
                    x = root;
                }
            }
        }
        x.setToBlack(); */
        //nil.parent = nil;
        //IntervalTree.nil.rightChild  = IntervalTree.nil;
        //IntervalTree.nil.leftChild = IntervalTree.nil;

        Assertions.assertTrue(nil.isBlackNode);

        while (!x.isRoot() && x.isBlack()) {
            if (x.isLeftChild()) {
                IntervalTreeNode w = x.parent.rightChild;
                if (w.isRed()) {
                    w.setToBlack();
                    x.parent.setToRed();
                    leftRotation(x.parent);
                    w = x.parent.rightChild;
                }
                if (w.leftChild.isBlack() && w.rightChild.isBlack()) {
                    w.setToRed();
                    x = x.parent;
                } else {
                    if (w.rightChild.isBlack()) {
                        w.leftChild.setToBlack();
                        w.setToRed();
                        rightRotation(w);
                        w = x.parent.rightChild;
                    }
                    w.isBlackNode = x.parent.isBlackNode;
                    x.parent.setToBlack();
                    w.rightChild.setToBlack();
                    leftRotation(x.parent);
                    x = root;
                }
            } else {
                IntervalTreeNode w = x.parent.leftChild;
                Assertions.assertTrue(nil.isBlackNode);

                if (w.isRed()) {
                    w.setToBlack();
                    x.parent.setToRed();
                    rightRotation(x.parent);
                    w = x.parent.leftChild;
                }
                if (w.leftChild.isBlackNode && w.rightChild.isBlackNode) {
                    w.setToRed();
                    x = x.parent;
                } else {
                    System.out.println("w.leftChild.isBlackNode " + (w.leftChild.isBlackNode ? "Y" : "N") );
                    Assertions.assertTrue(nil.isBlackNode);
                    System.out.println(nil.isBlackNode ? "B" : "R" );
                    if (w.leftChild.isBlackNode) {
                        w.rightChild.setToBlack();
                        w.setToRed();
                        leftRotation(w);
                        w = x.parent.leftChild;
                    }
                    w.isBlackNode = x.parent.isBlackNode;
                    x.parent.setToBlack();
                    w.leftChild.setToBlack();
                    rightRotation(x.parent);
                    x = root;
                }
            }
        }
        x.setToBlack();
    }


    /**
     * A method outlining the deletion of an interval of addresses to be killed entirely.
     * */

    public boolean killAddress(BigInteger killedAddress) {
        PointPC killedPoint = new PointPC(killedAddress, BigInteger.valueOf(0), null);
        List<IntervalTreeNode> overlapCollection = collectOverlapNodes(killedPoint);

        for (IntervalTreeNode overlapNode : overlapCollection) {
            if (overlapNode.getInterval() instanceof Stride) {
                Stride overlappedStride = (Stride)overlapNode.getInterval();
                if (overlappedStride.containsAddressInStride(killedAddress)) {
                    // if it is a part of the stride

                    if (killedAddress.compareTo(overlappedStride.getStartAddress()) == 0) {
                        // Case 1: the start address of the stride is killed -> delete the stride, update that stride (start address, number of distinct addresses and total number of accesses)
                        //         reinsert that updated stride into the Interval tree
                        delete(overlapNode);
                        if (overlappedStride.getEndAddress().compareTo(overlappedStride.getStartAddress()) == 0)
                            continue;

                        overlappedStride.deflateStride(killedAddress);
                        insertInterval(overlappedStride);

                    } else if (killedAddress.compareTo(overlappedStride.getEndAddress()) == 0) {
                        // Case 2: the end address of the stride is killed -> update that stride (start address, number of distinct addresses and total number of accesses)
                        if (overlappedStride.getEndAddress().compareTo(overlappedStride.getStartAddress()) == 0) {
                            delete(overlapNode);
                            continue;
                        }

                        overlappedStride.deflateStride(killedAddress);

                    } else {
                        // Case 3: It is within the stride -> just update the stride statistics
                        if (overlappedStride.containsAddressInStride(killedAddress)) {
                            BigInteger minBound = killedAddress.subtract(overlappedStride.getStrideDistance());
                            BigInteger maxBound = killedAddress.add(overlappedStride.getStrideDistance());

                            BigInteger newStride1SizeOfAccess = (overlappedStride.getEndAddress().subtract(overlappedStride.getStartAddress())).add(BigInteger.ONE);
                            Stride strideToInsert1 = new Stride(overlappedStride.getStartAddress(), minBound,
                                    overlappedStride.getStrideDistance(),
                                    newStride1SizeOfAccess,
                                    newStride1SizeOfAccess,
                                    overlappedStride.getPCAndReadWrite()
                            );

                            BigInteger newStride2SizeOfAccess = (overlappedStride.getEndAddress()).subtract(overlappedStride.getStartAddress()).add(BigInteger.ONE);
                            Stride strideToInsert2 = new Stride(maxBound, overlappedStride.getEndAddress(),
                                    overlappedStride.getStrideDistance(),
                                    newStride2SizeOfAccess,
                                    newStride2SizeOfAccess,
                                    overlappedStride.getPCAndReadWrite()
                            );

                            delete(overlapNode);
                            insertInterval(strideToInsert1);
                            insertInterval(strideToInsert2);
                        }
                    }
                }
            }
        }

        return true;
    }

    public boolean deleteMin() {
        return delete(getSmallestNode(root));
    }

    public boolean deleteMax() {
        return delete(getLargestNode(root));
    }

    public boolean deleteStride(Stride delStride) {
        if (root.getInterval() instanceof Stride) {
            IntervalTreeNode nodeMatch = matchWithStride(delStride);
            if (!nodeMatch.isNil()) {
                delete(nodeMatch);
                return true;
            }
            /*
            (!!!) LEAVE IT FOR NOW (!!!)

            List<IntervalTreeNode> collectionOverlaps = collectOverlapNodes(delStride);

            for (IntervalTreeNode overlapNode : collectionOverlaps) {
                Stride overlapStride = (Stride)overlapNode.getInterval();

                // Get the cross-section list
                List<BigInteger> crossSectionList = overlapStride.getCrossSection(delStride);
                BigInteger crossSectionStart = crossSectionList.get(0);
                BigInteger crossSectionEnd = crossSectionList.get(1);


            } */
        }
        return false;
    }


    // SEARCH-FOR-OVERLAPS QUERIES
    /**
     * Start searching from the root of the Interval tree for the specified intToSearch interval, and return the first node that contains (overlaps with) it.
     * Serves as a quick method of determining whether an overlap exists or not.
     * */

    public IntervalTreeNode searchFirstOverlapInterval(IntervalType intToSearch) {
        IntervalTreeNode currNode = root;

        while (!currNode.isNil() && !intToSearch.hasOverlap(currNode.getInterval())) {
            if (!currNode.leftChild.isNil() && currNode.leftChild.getMaxAddress().compareTo(intToSearch.getStartAddress()) != -1 ) {
                // if currNode.left != T.nil and currNode.left.max >= i.low
                currNode = currNode.leftChild;
            } else currNode = currNode.rightChild;
        }

        return currNode;
    }

    /**
     * @param - The queried stride
     * @return - the return node containing the matched stride
     * */

    public IntervalTreeNode matchWithStride(Stride strideToMatch) {
        IntervalTreeNode returnNode = nil;
        IntervalTreeNode currNodePtr = root;

        if (!root.isNil() && root.getInterval() instanceof Stride) {

            while (!currNodePtr.isNil()) {
                if (strideToMatch.compareTo(currNodePtr.getInterval()) != 0)
                    currNodePtr = strideToMatch.compareTo(currNodePtr.getInterval()) == -1 ? currNodePtr.leftChild : currNodePtr.rightChild;
                else {
                    // We have reached strides with a match
                    assert (currNodePtr.getInterval() instanceof Stride);
                    int strideComp = compareStrides((Stride) currNodePtr.getInterval(), strideToMatch);
                    /*System.out.println(" The current node pointer is " + currNodePtr.testStringOutput() +
                            "\nThe stride comparison is " + strideComp);*/
                    if (strideComp == 0) {
                        //System.out.println("HERE");
                        returnNode = currNodePtr;
                        break;
                    } else {
                        currNodePtr = strideComp == -1 ? currNodePtr.rightChild : currNodePtr.leftChild;
                    }
                }
            }
        }
        return returnNode;
    }

    /**
     * Return the node with the smallest starting address of its interval that overlaps with the @param interval-to-search,
     * from the subtree rooted at @param node.
     * (Return the T.nil sentinel node if there are no overlapping nodes)
     * */

    public IntervalTreeNode getMinOverlapNode(IntervalTreeNode node, IntervalType intToSearch) {
        IntervalTreeNode returnNode = nil;
        IntervalTreeNode currNodePtr = node;

        if (!currNodePtr.isNil() && currNodePtr.getMaxAddress().compareTo(intToSearch.getStartAddress()) != -1) {
            while (true) {

                //System.out.println(currNodePtr.getInterval().hasOverlap(intToSearch) ? "YES" : "NO");
                //System.out.println("Start: " + currNodePtr.getInterval().getStartAddress() + "  End: " + currNodePtr.getInterval().getEndAddress());
                //System.out.println("Start: " + intToSearch.getStartAddress() + "  End: " + intToSearch.getEndAddress());
                //System.out.println(currNodePtr.testStringOutput());

                if (currNodePtr.getInterval().hasOverlap(intToSearch)) {
                    // The current node has an interval that overlaps with the given Interval.
                    // We may want to inspect the intervals contained in the left subtree, since there may be another interval there
                    // that overlaps with the current one.
                    // Do not consider the overlappers in the right subtree as they will all be with greater starting addresses
                    // compared to the current node.

                    returnNode = currNodePtr;
                    currNodePtr = currNodePtr.leftChild;

                    if ( currNodePtr.isNil() || currNodePtr.getMaxAddress().compareTo(intToSearch.getStartAddress()) == -1) {
                        // either no left subtree or nodes cannot overlap
                        break;
                    }

                } else {
                    // node does not overlap
                    // check left subtree since a node with an overlapping interval may be present in there

                    IntervalTreeNode leftPtr = currNodePtr.leftChild;
                    if (!leftPtr.isNil() && leftPtr.getMaxAddress().compareTo(intToSearch.getStartAddress()) != -1) {
                        currNodePtr = leftPtr;
                    } else {
                        // As the left subtree does not contain an overlapper, check the right subtree if it contains one.
                        if (currNodePtr.getStartAddress().compareTo(intToSearch.getEndAddress()) == 1) {
                            // if the current node's start address is greater than the end address of the queried interval
                            // then none of the nodes in the right subtree have a chance of containing an interval that
                            // could overlap with the queried @param interval
                            break;
                        }

                        currNodePtr = currNodePtr.rightChild;
                        if (currNodePtr.isNil() || currNodePtr.getMaxAddress().compareTo(intToSearch.getStartAddress()) == -1 ) {
                            // Case of no right subtree or the nodes do not overlap there
                            break;
                        }
                    }
                }
            }
        }

        return returnNode;
    }

    /**
     * Tree-level overlapping of nodes' search
     * */

    public IntervalTreeNode getMinOverlapNode(IntervalType intToSearch) {
        IntervalTreeNode argNode = root;

        return getMinOverlapNode(argNode, intToSearch);
    }

    /**
     * The next node with an interval (with a >= start address) that overlaps the @param intToSearch interval otherwise return the T.nil sentinel node
     * */

    public IntervalTreeNode getNextOverlapNode(IntervalTreeNode node, IntervalType intToSearch) {
        IntervalTreeNode returnNode = nil;
        IntervalTreeNode currNodePtr = node;

        if (!currNodePtr.rightChild.isNil()) {
            returnNode = getMinOverlapNode(currNodePtr.rightChild, intToSearch);
        }

        while (returnNode.isNil() && !currNodePtr.parent.isNil()) {
            if (currNodePtr.isLeftChild()) {
                returnNode = currNodePtr.parent.getInterval().hasOverlap(intToSearch) ?  currNodePtr.parent : currNodePtr.rightChild;
            }
            currNodePtr = currNodePtr.parent;
        }

        return (returnNode != node) ? returnNode : nil;
    }

    /**
     * Get the number of nodes that contain intervals that overlap with the interval
     * */

    public int getNumberOfOverlappingNodes(IntervalTreeNode node, IntervalType interval) {
        int numOverlapNodes = 0;
        Iterator<IntervalTreeNode> iterator = new NodeLevelIteratorOverlaps(node, interval);

        while (iterator.hasNext()) {
            iterator.next();
            numOverlapNodes++;
        }

        return numOverlapNodes;
    }

    /**
     * Collect the overlapping nodes in the tree with the queried interval
     * */

    public List<IntervalTreeNode> collectOverlapNodes(IntervalTreeNode node, IntervalType interval) {
        List<IntervalTreeNode> collectionOverlaps = new ArrayList<>();
        Iterator<IntervalTreeNode> iter = new NodeLevelIteratorOverlaps(node, interval);
        while (iter.hasNext()) {
            collectionOverlaps.add(iter.next());
        }

        return collectionOverlaps;
    }

    public List<IntervalTreeNode> collectOverlapNodes(IntervalType interval) {
        return collectOverlapNodes(this.root, interval);
    }

    /**
     * A node-level iterator that iterates through the nodes that overlap with the
     * interval that has been given during instantiation.
     * */

    private class NodeLevelIteratorOverlaps implements Iterator<IntervalTreeNode> {

        private IntervalTreeNode nextElement;
        private IntervalType searchInterval;

        private NodeLevelIteratorOverlaps(IntervalTreeNode node, IntervalType intToSearch) {
            searchInterval = intToSearch;
            nextElement = getMinOverlapNode(node, intToSearch);
        }

        @Override
        public boolean hasNext() {
            return !nextElement.isNil();
        }

        @Override
        public @Nullable IntervalTreeNode next() {
            IntervalTreeNode returnPtr = nil;
            if (hasNext()) {
                returnPtr = nextElement;
                nextElement = getNextOverlapNode(returnPtr, this.searchInterval);
            } else {
                throw new NoSuchElementException("The last overlapping element has been reached. ");
            }
            return returnPtr;
        }
    }

    /**
     * Tree-level iterator that wraps around the node-level iterator
     * */

    private class TreeLevelIteratorOverlaps implements Iterator<IntervalTreeNode> {

        private NodeLevelIteratorOverlaps nodeIterator;

        private TreeLevelIteratorOverlaps(IntervalTreeNode node, IntervalType intToSearch) {
            nodeIterator = new NodeLevelIteratorOverlaps(node, intToSearch);
        }

        @Override
        public boolean hasNext() {
            return nodeIterator.hasNext();
        }

        @Override
        public IntervalTreeNode next() {
            return nodeIterator.next();
        }
    }

    /**
     * Simplified node-level search of the subtree rooted at the current intervalTreeNode instance for the interval
     * */

    private IntervalTreeNode searchForMatch(@NotNull IntervalTreeNode node, IntervalType intToSearch) {
        while (!node.isNil() && intToSearch.compareTo(node.getInterval()) != 0) {
            node = intToSearch.compareTo(node.getInterval()) == -1 ? node.leftChild : node.rightChild;
        }
        return node;
    }

    // a helper function

    private int compareStrides(@NotNull Stride stride1, @NotNull Stride stride2) {
        BigInteger searchStrideDist = stride1.getStrideDistance();
        BigInteger currNodeStrideDist = stride2.getStrideDistance();

        if (searchStrideDist.compareTo(currNodeStrideDist) == 0) {
            return stride1.getPCAndReadWrite().compareTo(stride2.getPCAndReadWrite());
        } else return searchStrideDist.compareTo(currNodeStrideDist);
    }

    /**
     * Tree-level search for the interval int
     * */

    /*
     * TODO
     * */

    public IntervalTree mergeWith(IntervalTree intervalTree) {

        return null;
    }

    // Interval-Tree-Node inner class

    public class IntervalTreeNode {

        public IntervalType interval; //
        public IntervalTreeNode parent; //
        public IntervalTreeNode leftChild; //
        public IntervalTreeNode rightChild; //

        private boolean isBlackNode; // could use an enumeration class (containing the constants RED, BLACK) but decided to use a boolean instead to represent this information to save up memory
        private BigInteger max; // the maximum value of interval high end-points contained within the subtree rooted at this interval-tree-node

        /**
         * Set the sentinel Nil node to be originally black
         * */

        public IntervalTreeNode() {
            parent = this;
            leftChild = this;
            rightChild = this;

            isBlackNode = true;
            interval = null;
            max = BigInteger.ZERO;
        }

        /**
         * Construct a node with an interval
         * */

        public IntervalTreeNode(IntervalType intToInsert) {
            interval = intToInsert;
            max = interval.getEndAddress();

            parent = nil;
            leftChild = nil;
            rightChild = nil;

            isBlackNode= false; // Set it to RED by default
        }

        public IntervalTreeNode(IntervalType intToInsert, BigInteger maxVal, IntervalTreeNode left, IntervalTreeNode right) {
            interval = intToInsert;
            max = maxVal;

            parent = nil;
            leftChild = left;
            rightChild = right;

            isBlackNode = false;
        }

        /**
         * Return the some fields in the tree node or the interval contained within it.
         * */

        public IntervalTreeNode getRightChild() { return rightChild; }

        public IntervalTreeNode getLeftChild() { return leftChild; }

        public IntervalType getInterval() { return interval; }

        public void setInterval(IntervalType intType) { this.interval = intType; }

        public BigInteger getStartAddress() { return interval.getStartAddress(); }

        public BigInteger getEndAddress() { return interval.getEndAddress(); }

        public BigInteger getMaxAddress() { return max; }

        public void setMaxAddress(BigInteger newMaxAddress) { this.max = newMaxAddress; }

        // Red-Black-Tree Node-level Query/Set-up Methods

        public boolean isRed() { return !isBlackNode; }

        public boolean isBlack() { return isBlackNode; }

        public void setToBlack() { isBlackNode = true; }

        public void setToRed() { isBlackNode = false; }

        // Node-level Query/Set-up methods

        public boolean isNil() { return this == nil; }

        public boolean isRoot() { return (!isNil() && this.parent.isNil()); }

        public IntervalTreeNode getParent() {return this.parent;}

        public boolean isLeftChild() { return (!isNil() && this == parent.leftChild); }

        public boolean isRightChild() { return (!isNil() && this == parent.rightChild); }

        public boolean hasTwoChildren() { return (!this.leftChild.isNil() && !this.rightChild.isNil()); }

        public boolean hasNoChildren() { return (this.leftChild.isNil() && this.rightChild.isNil()); }

        public boolean hasUncle() { return hasGrandparent() && this.getGrandparent().hasTwoChildren(); }

        public boolean hasGrandparent() { return !this.parent.isNil() && !this.parent.parent.isNil(); }

        private IntervalTreeNode getGrandparent() {
            if (hasGrandparent())
                return this.parent.parent;
            return nil;
        }

        private IntervalTreeNode getUncle() {
            if (hasUncle()) {
                IntervalTreeNode grandPtr = getGrandparent();
                if (this.parent.isLeftChild())
                    return grandPtr.rightChild;
                else
                    return grandPtr.leftChild;
            } else
                return nil;
        }

        @Override
        public String toString() {
            if (isNil()) {
                return "NILL";
            } else {
                return String.format(" | Start =  %s | End = %s | Max-End = %s | Colour = %s |",
                        getStartAddress().toString(10),
                        getEndAddress().toString(10),
                        getMaxAddress().toString(10),
                        isBlack() ? "BLACK" : "RED"
                );
            }
        }

        public String testStringOutput() {
            if (isNil()) return "NILL";
            else {
                return String.format( " ||%s %s|%s|%s|| ",
                        getStartAddress().toString(10),
                        getEndAddress().toString(10),
                        getMaxAddress().toString(10),
                        isBlack() ? "B" : "R"
                );
            }
        }

        /**
         * Adapted from: https://stackoverflow.com/questions/16098362/how-to-deep-copy-a-binary-tree
         * */

        public IntervalTreeNode copyNode() {
            IntervalTreeNode leftNode = nil;
            IntervalTreeNode rightNode = nil;

            leftNode = (!this.leftChild.isNil()) ? this.leftChild.copyNode() : nil;
            rightNode = (!this.rightChild.isNil()) ? this.rightChild.copyNode() : nil;

            IntervalType interval = this.interval.copy();

            IntervalTreeNode copiedTreeNode = new IntervalTreeNode(interval, this.max, leftNode, rightNode);
            if (!leftNode.isNil()) leftNode.parent = copiedTreeNode;
            if (!rightNode.isNil()) rightNode.parent = copiedTreeNode;

            return copiedTreeNode;
        }
    }

    // Interval-Tree-Node-Level Query Methods
    /**
     * Get the smallest and greatest elements (w.r.t. starting interval values)
     * */

    private IntervalTreeNode getSmallestNode(IntervalTreeNode node) {
        IntervalTreeNode currPtr = node;
        while (!currPtr.leftChild.isNil()) {
            System.out.println("The left child of the current pointer is:  " + currPtr.leftChild.testStringOutput());
            currPtr = currPtr.leftChild;
        }
        return currPtr;
    }

    private IntervalTreeNode getLargestNode(IntervalTreeNode node) {
        IntervalTreeNode currPtr = node;
        while (!currPtr.rightChild.isNil()) {
            currPtr = currPtr.rightChild;
        }
        return currPtr;
    }

    private IntervalTreeNode getPredecessor(@NotNull IntervalTreeNode node) {
        // if left child exists return its rightmost subtree node
        if (!node.leftChild.isNil()) {
            return this.getLargestNode(node.leftChild);
        }
        // else go up until you reach the sentinel node
        // or the first parent whose right child points to the current node that is being iterated upon
        IntervalTreeNode currPtr = node;
        IntervalTreeNode parentPtr = node.parent;

        while (!parentPtr.isNil() && currPtr == parentPtr.leftChild) {
            currPtr = parentPtr;
            parentPtr = parentPtr.parent;
        }

        return parentPtr;
    }

    private IntervalTreeNode getSuccessor(@NotNull IntervalTreeNode node) {
        // if the right child exists return its leftmost subtree node
        System.out.println("Here 1075 : " + node.testStringOutput());

        if (!node.rightChild.isNil()) {
            System.out.println("1078 -> " + (node.rightChild).testStringOutput());
            System.out.println("1078 -> " + this.getSmallestNode(node.rightChild));
            return this.getSmallestNode(node.rightChild);
        }
        // else go up until you reach the sentinel node
        // or the first parent whose left child points to the current node that is being iterated upon
        IntervalTreeNode currPtr = node;
        IntervalTreeNode parentPtr = node.parent;

        while (!parentPtr.isNil() && currPtr == parentPtr.rightChild) {
            currPtr = parentPtr;
            parentPtr = parentPtr.parent;
        }

        return parentPtr;
    }

    // Rotations
    /**
     * Left rotation centred at current node
     * */

    private void leftRotation(@NotNull IntervalTreeNode node) {
        IntervalTreeNode rightChildNode = node.rightChild;
        node.rightChild = rightChildNode.leftChild;
        if (!rightChildNode.leftChild.isNil()) {
            rightChildNode.leftChild.parent = node;
        }
        rightChildNode.parent = node.parent;

        if (node.parent.isNil()) {
            root = rightChildNode;
        } else if (node.isLeftChild()) {
            node.parent.leftChild = rightChildNode;
        } else {
            node.parent.rightChild = rightChildNode;
        }
        rightChildNode.leftChild = node;
        node.parent = rightChildNode;

        updateMax(node);
        updateMax(rightChildNode); // <--- this
    }

    /**
     * Right rotation centred at current node
     * */

    private void rightRotation(@NotNull IntervalTreeNode node) {
        IntervalTreeNode leftChildNode = node.leftChild;
        node.leftChild.rightChild = leftChildNode;
        if (!leftChildNode.rightChild.isNil()) {
            leftChildNode.rightChild.parent = node;
        }
        leftChildNode.parent = node.parent;

        if (node.parent.isNil()) {
            root = leftChildNode;
        } else if (node.isRightChild()) {
            node.parent.rightChild = leftChildNode;
        } else {
            node.parent.leftChild = leftChildNode;
        }
        leftChildNode.leftChild = node;
        node.parent = leftChildNode;

        this.updateMax(node);
        this.updateMax(leftChildNode);
    }

    // Node-Level Updating Methods

    private void updateMax(@NotNull IntervalTreeNode node) {
        //System.out.println("Print it :" + node.testStringOutput());

        BigInteger intervalVal = node.getInterval().getEndAddress();

        if (!node.leftChild.isNil()) {
            intervalVal = intervalVal.max(node.leftChild.getMaxAddress());
        }
        if (!node.rightChild.isNil()) {
            intervalVal = intervalVal.max(node.rightChild.getMaxAddress());
        }
        node.max = intervalVal;

    }

    private void updateMaxUpwards(IntervalTreeNode node) {
        this.updateMax(node);

        IntervalTreeNode currPtr = node;
        while (!currPtr.parent.isNil()) {
            currPtr = currPtr.parent;
            updateMax(currPtr);
        }
    }


    // Node-level debugging methods TODO

    // Tree-level debugging methods TODO

    // Interval Tree Iterator Methods

    private class TreeLevelIterator implements Iterator<IntervalTreeNode> {

        private IntervalTreeNode nextNode;

        private TreeLevelIterator(IntervalTreeNode node) {
            nextNode = node;
        }

        @Override
        public boolean hasNext() {
            return !nextNode.isNil();
        }

        @Override
        public IntervalTreeNode next() {
            if (hasNext()) {
                IntervalTreeNode returnNode = nextNode;
                nextNode = getSuccessor(returnNode);
                return returnNode;
            }

            return nil;
        }
    }

    @Override
    public Iterator<IntervalTreeNode> iterator() {
         return new TreeLevelIterator(root);
    }

    /**
     * Additional methods to be used for debugging & unit testing
     * */

    /**
     * Adapted from: https://www.geeksforgeeks.org/red-black-tree-set-2-insert/?ref=lbp
     * Will be useful for debugging.
     * */

    public void inorderTraversalHelper(@NotNull IntervalTreeNode node) {
        if (!node.isNil()) {
            inorderTraversalHelper(node.leftChild);
            System.out.printf("%s ", node.testStringOutput());
            inorderTraversalHelper(node.rightChild);
        }
    }

    public void inorderTraversal() {
        inorderTraversalHelper(root);
    }

    public void printTreeHelper(IntervalTreeNode node, int space) {
        int i;
        //System.out.println();
        if (!node.isNil()) {
            space = space + 10;
            //System.out.println("The node is : " + node.testStringOutput());
            printTreeHelper(node.rightChild, space);
            System.out.printf("\n");
            for ( i = 10; i < space; i++)
            {
                System.out.printf(" ");
            }
            System.out.printf("%s", node.testStringOutput());
            System.out.printf("\n");
            printTreeHelper(node.leftChild, space);
        }
    }

    public void printTree() {
        printTreeHelper(this.root, 0);
    }

    public static void main(String[] args) {
        IntervalTree intervalTree = new IntervalTree();

        //intervalTree.insertInterval(new Stride( BigInteger.valueOf(4), BigInteger.valueOf(10), BigInteger.TWO, BigInteger.valueOf(4), BigInteger.valueOf(4), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(5), BigInteger.valueOf(6), BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(50), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(3), BigInteger.valueOf(5), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(7), BigInteger.valueOf(7), BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(2), new PCPair(BigInteger.valueOf(10), MemoryAccess.READ) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(4), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(8), BigInteger.valueOf(30), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(9), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));
        intervalTree.insertInterval(new Stride( BigInteger.valueOf(10), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) ));

        IntervalTree.IntervalTreeNode matchedNode = intervalTree.matchWithStride(new Stride( BigInteger.valueOf(9), BigInteger.valueOf(20), BigInteger.TWO, BigInteger.valueOf(6), BigInteger.valueOf(3), new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE)));
        System.out.println("The matched queried node is " + matchedNode.testStringOutput());
        intervalTree.printTree();
        System.out.println("///////////////////////////////////////");

        intervalTree.delete(matchedNode);
        intervalTree.printTree();
        System.out.println("///////////////////////////////////////");

        IntervalTree.IntervalTreeNode matchedNode2 = intervalTree.matchWithStride(new Stride( BigInteger.valueOf(8),
                BigInteger.valueOf(30),
                BigInteger.TWO,
                BigInteger.valueOf(6),
                BigInteger.valueOf(3),
                new PCPair(BigInteger.valueOf(100), MemoryAccess.WRITE) )
        );
        //System.out.println("The matched second queried node is " + matchedNode2.testStringOutput());

        IntervalTree.IntervalTreeNode matchedNode4 =  intervalTree.matchWithStride(new Stride( BigInteger.valueOf(7),
                BigInteger.valueOf(7),
                BigInteger.ONE,
                BigInteger.valueOf(2),
                BigInteger.valueOf(2),
                new PCPair(BigInteger.valueOf(10), MemoryAccess.READ))
        );
        //System.out.println("The matched fourth queried node is " + matchedNode4.testStringOutput());

        //intervalTree.printTree();
        intervalTree.delete(matchedNode2);
        intervalTree.printTree();
        System.out.println("///////////////////////////////////////");

        //System.out.println(((Stride)(intervalTree.getRoot().getRightChild().getLeftChild().getInterval())).getTestStringStrideState());

        Assertions.assertTrue(intervalTree.nil.isBlack());

        intervalTree.delete(matchedNode4);
        intervalTree.printTree();
        System.out.println("///////////////////////////////////////");

        System.out.println(matchedNode2.testStringOutput());

        //intervalTree.nil.isBlackNode = true;
        System.out.print("The root is :" + intervalTree.getRoot());
        System.out.println("The root's right child is" + intervalTree.getRoot().rightChild);

        Assertions.assertTrue(intervalTree.nil.isBlack());

        intervalTree.delete(intervalTree.getRoot());

        Assertions.assertTrue(intervalTree.nil.isBlack());

        //System.out.println("NIl parent " + IntervalTree.nil.parent.testStringOutput() );
        //System.out.println("NIl left child " + IntervalTree.nil.leftChild.testStringOutput() );
        //System.out.println("NIl right child " + IntervalTree.nil.rightChild.testStringOutput() );

        //IntervalTree.nil.parent  = IntervalTree.nil;
        //IntervalTree.nil.rightChild  = IntervalTree.nil;
        //IntervalTree.nil.leftChild = IntervalTree.nil;

        System.out.println("The root is " + intervalTree.getRoot());
        System.out.println("the root right-child is " + intervalTree.getRoot().rightChild);
        //System.out.println("The successor of the root is " + intervalTree.getSuccessor(intervalTree.getRoot()));

        //intervalTree.printTree();
        /*
        System.out.println("The new root is " + intervalTree.getRoot());

        System.out.println("NIl parent " + IntervalTree.nil.parent.testStringOutput() );
        System.out.println("NIl left child " + IntervalTree.nil.leftChild.testStringOutput() );
        System.out.println("NIl right child " + IntervalTree.nil.rightChild.testStringOutput() );

        intervalTree.delete(intervalTree.getRoot());
        System.out.println("The new root is " + intervalTree.getRoot());
        System.out.println("The new root rightChild is " + intervalTree.getRoot().rightChild);

        //IntervalTree.nil.parent  = IntervalTree.nil;
        //IntervalTree.nil.rightChild  = IntervalTree.nil;
        //IntervalTree.nil.leftChild = IntervalTree.nil;
        //intervalTree.printTree();  */


    }

}
