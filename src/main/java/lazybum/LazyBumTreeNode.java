package lazybum;

import learning.InvalidTreeException;
import learning.NodeInfo;
import learning.testing.NodeTest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LazyBumTreeNode implements Serializable {

    private static final long serialVersionUID = 8634631905918333617L;


    private LazyBumTreeNode parent;
    private LazyBumTreeNode leftChild;
    private LazyBumTreeNode rightChild;

    private int depth;

    //TODO: wrap this
    private NodeTest nodeTest;
//    public TraversalPath traversalPath;
//    public Field field;
//    public FieldTransformerEnumInterface transformation;


    private NodeInfo nodeInfo;

    public LazyBumTreeNode(int depth, LazyBumTreeNode parent){
        this.depth = depth;
        this.parent = parent;

        this.leftChild = null;
        this.rightChild = null;

        this.nodeTest = null;
        this.nodeInfo = null;
    }


    public boolean isLeaf(){
        return leftChild == null && rightChild == null;
    }


    public NodeInfo getLeafStrategy() throws InvalidTreeException {
        if(isLeaf()){
            return nodeInfo;
        } else{
            throw new InvalidTreeException("Asked for a LeafNodeInfo from an internal node");
        }
    }

    public void setLeafStrategy(NodeInfo leafStrategy) {
        this.nodeInfo = leafStrategy;
    }

    public NodeTest getNodeTest() {
        return nodeTest;
    }

    public void setNodeTest(NodeTest nodeTest) {
        this.nodeTest = nodeTest;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public LazyBumTreeNode getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(LazyBumTreeNode leftChild) {
        this.leftChild = leftChild;
    }

    public LazyBumTreeNode getRightChild() {
        return rightChild;
    }

    public void setRightChild(LazyBumTreeNode rightChild) {
        this.rightChild = rightChild;
    }

    public LazyBumTreeNode getParent() {
        return parent;
    }

    public void setParent(LazyBumTreeNode parent) {
        this.parent = parent;
    }

    public int getDepth() {
        return depth;
    }

    public String toString(){
        try {
            return LazyBumTreeNodePrinter.toString(this);
        } catch (InvalidTreeException e) {
            e.printStackTrace();
        }
        return "INVALID TREE STRUCTURE";
    }

    public static int countNbOfNodes(LazyBumTreeNode treeNode){
        if(treeNode == null){
            return 0;
        }
        int count = 1; // count the node itself
        count += countNbOfNodes(treeNode.leftChild);
        count += countNbOfNodes(treeNode.rightChild);
        return count;
    }

    public static int countNbOfInnerNodes(LazyBumTreeNode treeNode){
        if(treeNode.isLeaf()){
            return 0;
        }
        int count = 1; // count the node itself
        count += countNbOfNodes(treeNode.leftChild);
        count += countNbOfNodes(treeNode.rightChild);
        return count;
    }

    public List<NodeTest> getAncestorTests(){
        List<NodeTest> nodeTests = new ArrayList<>();
        getAncestorTestsRecursive(nodeTests);
        return nodeTests;
    }

    private void getAncestorTestsRecursive(List<NodeTest> nodeTests){
        if(this.parent != null){
            parent.getAncestorTestsRecursive(nodeTests);
        }
        if(this.nodeTest != null){
            nodeTests.add(this.nodeTest);
        }
    }


}
