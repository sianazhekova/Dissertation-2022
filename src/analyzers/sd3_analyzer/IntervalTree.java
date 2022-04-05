package analyzers.sd3_analyzer;

import analyzers.baseline_analyzer.IntervalType;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Iterator;

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
    private static IntervalTreeNode nil; // sentinel (Nil) node
    private int treeSize;

    public IntervalTree() {
        treeSize = 0;
        nil = new IntervalTreeNode();
        root = nil;
    }

    public IntervalTree(IntervalType intToInsert) {
        treeSize = 1;
        nil = new IntervalTreeNode();
        root = new IntervalTreeNode(intToInsert);
        root.setToBlack(); // the root is set to be BLACK by default
    }

    /**
     *
     * */

    public IntervalTree(IntervalTree treeToCopy) {


    }

    /**
     *
     * */

    public void copyTree(IntervalTree anotherTree) {


    }

    /**
     *
     * */

    public IntervalType insertInterval(IntervalType intToInsert) {



        return null;
    }

    /**
     *
     * */

    public void deleteInterval(IntervalType intToDelete) {



    }

    /**
     *
     * */

    public IntervalType searchInterval(IntervalType intToSearch) {


        return null;
    }

    /**
     *
     * */

    public void extendInterval(IntervalType intervalToSearch, BigInteger newIntAddress) {



    }

    @Override
    public Iterator<IntervalTreeNode> iterator() {
        return null;
    }


    /**
     *  a
     * */

    // General query methods

    public class IntervalTreeNode implements Comparable<IntervalTreeNode> {

        private IntervalType interval; //
        private IntervalTreeNode parent; //
        private IntervalTreeNode leftChild; //
        private IntervalTreeNode rightChild; //

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

        /**
         * Return the some fields in the tree node or the interval contained within it.
         * */

        public IntervalType getInterval() { return interval; }

        public BigInteger getStartAddress() { return interval.getStartAddress(); }

        public BigInteger getEndAddress() { return interval.getEndAddress(); }

        public BigInteger getMaxAddress() { return max; }

        // Red-Black-Tree Node-level Query/Set-up Methods

        public boolean isRed() { return !isBlackNode; }

        public boolean isBlack() { return isBlackNode; }

        public void setToBlack() { isBlackNode = true; }

        public void setToRed() { isBlackNode = false; }

        // Node-level Query/Set-up methods

        public boolean isNil() { return this == nil; }

        public boolean isRoot() { return (!isNil() && !this.parent.isNil()); }

        public boolean isLeftChild() { return (); }

        public boolean isRightChild() { }

        public boolean hasTwoChildren() { }

        public boolean hasNoChildren() { }

        private IntervalTreeNode getGrandparent() { return this.parent.parent; }

        // Interval-Tree-Node-Level Query Methods

        /**
         * Get the smallest and greatest elements (w.r.t. starting interval values)
         * */

        private IntervalTreeNode getSmallestNode() {
            IntervalTreeNode currPtr = this;
            while (!currPtr.leftChild.isNil()) {
                currPtr = currPtr.leftChild;
            }
            return currPtr.leftChild;
        }

        private IntervalTreeNode getLargestNode() {
            IntervalTreeNode currPtr = this;
            while (!currPtr.rightChild.isNil()) {
                currPtr = currPtr.rightChild;
            }
            return currPtr;
        }

        private IntervalTreeNode getPredecessor() {
            // if left child exists return its rightmost subtree node
            if (!leftChild.isNil()) {
                return leftChild.getLargestNode();
            }
            // else go up until you reach the sentinel node or the first parent


        }

        private IntervalTreeNode getSuccessor() {
            // if the right child exists return its leftmost subtree node
            if
        }



        // Rotations

        /**
         * Left rotation centred at current node
         * */

        private void leftRotation() {
            IntervalTreeNode rightChildNode = this.rightChild;
            this.rightChild = rightChildNode.leftChild;
            if (!rightChildNode.leftChild.isNil()) {
                rightChildNode.leftChild.parent = this;
            }
            rightChildNode.parent = this.parent;

            if (this.parent.isNil()) {
                root = rightChildNode;
            } else if (isLeftChild()) {
                this.parent.leftChild = rightChildNode;
            } else {
                this.parent.rightChild = rightChildNode;
            }
            rightChildNode.leftChild = this;
            this.parent = rightChildNode;

            this.updateMax();
            rightChildNode.updateMax();
        }

        /**
         * Right rotation centred at current node
         * */

        private void rightRotation() {
            IntervalTreeNode leftChildNode = this.leftChild;
            this.leftChild.rightChild = leftChildNode;
            if (!leftChildNode.rightChild.isNil()) {
                leftChildNode.rightChild.parent = this;
            }
            leftChildNode.parent = this.parent;

            if (this.parent.isNil()) {
                root = leftChildNode;
            } else if (isRightChild()) {
                this.parent.rightChild = leftChildNode;
            } else {
                this.parent.leftChild = leftChildNode;
            }
            leftChildNode.leftChild = this;
            this.parent = leftChildNode;

            this.updateMax();
            leftChildNode.updateMax();
        }

        /**
         * Search the subtree rooted at the current intervalTreeNode instance for the interval
         * */

        private IntervalTreeNode searchInterval(IntervalType intToSearch) {



            return null;
        }

        @Override
        public int compareTo(@NotNull IntervalTree.IntervalTreeNode o) {
            return 0;
        }


        // Node-Level Updating Methods

        private void updateMax() {
            BigInteger intervalVal = this.getInterval().getEndAddress();

            if (!this.leftChild.isNil()) {
                intervalVal = intervalVal.max(this.leftChild.getMaxAddress());
            }
            if (!this.rightChild.isNil()) {
                intervalVal = intervalVal.max(this.rightChild.getMaxAddress());
            }
            this.max = intervalVal;
        }

        private void updateMaxUpwards() {
            this.updateMax();

            IntervalTreeNode currPtr = this;
            while (!currPtr.parent.isNil()) {
                currPtr = currPtr.parent;
                currPtr.updateMax();
            }
        }










    }


    // Interval Tree Iterator






}
