package lazybum;

import learning.InvalidTreeException;

public class LazyBumTreeNodePrinter {

    public enum NodeType{
        ROOT, LEFT, RIGHT
    }


    public static String toString(LazyBumTreeNode treeNode) throws InvalidTreeException {
        return toStringCompact(treeNode, "", NodeType.ROOT);
    }


    public static String toStringCompact(LazyBumTreeNode treeNode, String indentation, NodeType nodeType) throws InvalidTreeException {
        String nodeIndentation = indentation;
        String childIndentation = indentation;

        if (nodeType == NodeType.ROOT) { // root node
            childIndentation = "";
        } else if (nodeType == NodeType.LEFT) { // this node is the left child node of its paren
            nodeIndentation += "+--";
            childIndentation += "|       ";
        } else {
            nodeIndentation += "+--";
            childIndentation += "        ";
        }


        if (treeNode.isLeaf()) {
            return leafNodeToString(treeNode, nodeType, nodeIndentation, childIndentation);
        } else { // NOT A LEAF
            return innerNodeToString(treeNode, nodeType, nodeIndentation, childIndentation);
        }
    }

    private static String leafNodeToString(LazyBumTreeNode treeNode, NodeType nodeType,
                                           String nodeIndentation, String childIndentation) throws InvalidTreeException {
        String result = "";
        String labelCountsIndent = childIndentation + "    ";

        if (nodeType == NodeType.ROOT) {
            result += treeNode.getLeafStrategy().toStringAsLeaf();
        } else if (nodeType == NodeType.LEFT) {
            result += nodeIndentation + "yes: " + treeNode.getLeafStrategy().toStringAsLeaf(labelCountsIndent);
        } else {
            result += nodeIndentation + "no: " + treeNode.getLeafStrategy().toStringAsLeaf(labelCountsIndent);
        }
        return result;
    }

    private static String innerNodeToString(LazyBumTreeNode treeNode, NodeType nodeType,
                                            String nodeIndentation, String childIndentation) throws InvalidTreeException {
        String result = "";
        int nodeIndentationLength = nodeIndentation.length();
//            String testFillerString ="\t" + String.join("", Collections.nCopies(nodeIndentationLength, " "));
        String testFillerString = childIndentation + "\t\t";



        // print for this non-leaf node
        if (nodeType == NodeType.ROOT) {
            //todo
            result += nodeIndentation + "\n";

        } else if (nodeType == NodeType.LEFT) { // this node is the left child node of its parent
            //todo
            result += nodeIndentation + "yes: \n";
        } else {
            //todo
            result += nodeIndentation + "no: \n";
        }

//            result += testFillerString +"\n";
//            result += treeNode.getNodeInfo().toString(testFillerString);
//            result += testFillerString +"\n";

        result += treeNode.getNodeInfo().toString(testFillerString); // already ends with a line break

        result += treeNode.getNodeTest().toString(testFillerString) + " ?\n";

        // print for its children
        if(treeNode.getLeftChild() != null){
            result += LazyBumTreeNodePrinter.toStringCompact(treeNode.getLeftChild(), childIndentation, NodeType.LEFT);
        }
        if(treeNode.getRightChild() != null){
            result += LazyBumTreeNodePrinter.toStringCompact(treeNode.getRightChild(), childIndentation, NodeType.RIGHT);
        }

        return result;
    }


}
