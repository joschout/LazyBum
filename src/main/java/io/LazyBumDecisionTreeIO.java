package io;

import lazybum.LazyBumDecisionTree;
import lazybum.LazyBumTreeNode;

import java.io.*;

/**
 * Created by joschout.
 */
public class LazyBumDecisionTreeIO {

    public static void writeDecisionTreeRootNodeToFile(LazyBumDecisionTree decisionTree, String fileName) {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream(fileName);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(decisionTree.getTreeRoot());
            out.close();
            fileOut.close();
            System.out.println("Serialized data is saved in decisionTreeRoot.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }


    public static LazyBumDecisionTree readDecisionTreeFromRootNodeFile(String fileName) throws IOException, ClassNotFoundException {

        LazyBumTreeNode rootNode = null;
        FileInputStream fileIn = new FileInputStream(fileName);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        rootNode = (LazyBumTreeNode) in.readObject();
        in.close();
        fileIn.close();

        LazyBumDecisionTree decisionTree = new LazyBumDecisionTree(rootNode);
        return decisionTree;


    }
}