package learning;

import lazybum.LazyBumDecisionTree;
import lazybum.LazyBumTreeNode;

/**
 * Created by joschout.
 */
public class LazyBumTreePruner {


    public static LazyBumDecisionTree prune(LazyBumDecisionTree lazyBumDecisionTree){


        LazyBumTreeNode root = lazyBumDecisionTree.getTreeRoot();
        pruneRecursive(root);
        return lazyBumDecisionTree;
    }



    public static void pruneRecursive(LazyBumTreeNode treeNode){

        if(treeNode == null){
            return;
        }


        LazyBumTreeNode leftChildNode = treeNode.getLeftChild();
        LazyBumTreeNode rightChildNode = treeNode.getRightChild();

        if(leftChildNode == null || rightChildNode == null){
            return;
        }

        if(! leftChildNode.isLeaf()){
            pruneRecursive(leftChildNode);
        }
        if(! rightChildNode.isLeaf()){
            pruneRecursive(rightChildNode);
        }

        if(leftChildNode.isLeaf() && rightChildNode.isLeaf()){
            if(leftChildNode.getNodeInfo().majorityLabel.equals(rightChildNode.getNodeInfo().majorityLabel)){
                treeNode.setLeftChild(null);
                treeNode.setRightChild(null);
            }
        }
    }


}
