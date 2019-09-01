package lazybum;


import graph.InvalidKeyInfoException;
import dataset.Example;
import feature.columntype.UnsupportedFieldTypeException;
import feature.tranform.UnsupportedFeatureTransformationException;
import graph.GraphTraversalException;
import lazybum.unevaluatables.UnevaluatableTestStrategy;
import learning.*;
import learning.split.InvalidSplitCriterionException;
import learning.split.NoSplitInfoFoundException;
import learning.split.SplitInfo;
import learning.testing.ExamplePartition;
import learning.testing.NodeTest;
import utils.Timer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 *
 * Builds a LazyBumTreeNode tree in a top-down fashion by recursively splitting nodes.
 * Uses:
 *   - a LazyBumSplitter to determine the best split of a node
 *   - a LeafBuilder to determine the leaf prediction strategy of leaf nodes
 *   - a StopCriterion to halt the recursion
 *
 */
public class LazyBumTreeBuilder {

    public static boolean VERBOSE = false;
    public static boolean VERBOSE_PRINT_FEATURE_TABLE_INFOS = false;


    public static boolean SHOULD_PRUNE = true;

    private LazyBumSplitter splitter;
    private LeafBuilder leafBuilder;
    private StopCriterion stopCriterion;

    private LazyBumTreeNode treeRoot;

    private FeatureTableExtensionCounter featureTableExtensionCounter;
    private double timeSpentOnFeatureTableExtensionCalculations = 0.0;
    private double timeSpentOnFindingTheBestSplit = 0.0;

    private FeatureTableBuilder featureTableBuilder;

    public LazyBumTreeBuilder(LazyBumSplitter splitter, LeafBuilder leafBuilder, StopCriterion stopCriterion, FeatureTableBuilder featureTableBuilder) {
        this.splitter = splitter;
        this.leafBuilder = leafBuilder;
        this.stopCriterion = stopCriterion;
        this.treeRoot = null;
        this.featureTableBuilder=featureTableBuilder;
    }

    public LazyBumTreeNode build(List<Example> instances) throws InvalidTreeBuilderException, NoSplitInfoFoundException, InvalidSplitCriterionException, GraphTraversalException, UnsupportedFeatureTransformationException, UnsupportedFieldTypeException, InvalidKeyInfoException {
        if(!isCorrectlyInitialized()){
            throw new InvalidTreeBuilderException("The tree builder has not been correctly instantiated");
        } else{
            this.treeRoot = new LazyBumTreeNode(0, null);
            this.timeSpentOnFindingTheBestSplit = 0.0;



            Timer timer = Timer.getStartedTimer();
            FeatureTableInfo rootFeatureTableInfo = featureTableBuilder.buildFeatureTableForRootNode();
            timeSpentOnFeatureTableExtensionCalculations = timer.stop();
            this.featureTableExtensionCounter = FeatureTableExtensionCounter.init(rootFeatureTableInfo);

            buildRecursive(instances, this.treeRoot, rootFeatureTableInfo);


            if(SHOULD_PRUNE){
                if(VERBOSE){
                    int nbOfNodesBeforePruning = LazyBumTreeNode.countNbOfNodes(treeRoot);
                    System.out.println(treeRoot.toString());
                    System.out.println("before pruning: " + nbOfNodesBeforePruning + " nodes");
                    System.out.println("pruning...");
                }

                LazyBumTreePruner.pruneRecursive(treeRoot);

                if(VERBOSE) {
                    int nbOfNodesAfterPruning = LazyBumTreeNode.countNbOfNodes(treeRoot);
                    System.out.println("after pruning: " + nbOfNodesAfterPruning + " nodes");
                    System.out.println(treeRoot.toString());
                }

            }


            return treeRoot;
        }
    }



    private Optional<SplitInfo> getBestTestBasedOnCurrentFeatureTable(List<Example> instances, LazyBumTreeNode currentTreeNode, @Nullable FeatureTableInfo featureTableInfo) throws GraphTraversalException, InvalidSplitCriterionException, UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {
        Set<FeatureTableExtension> allFeatureTableExtensions
                = featureTableInfo.getAllHierarchicalFeatureTableExtensions();
        //note: find the best test based on the current table
        Timer timerForFindingTheBestSplit = Timer.getStartedTimer();
        Optional<SplitInfo> optionalSplitInfo
                = this.splitter.getSplit(instances, currentTreeNode, allFeatureTableExtensions);
        this.timeSpentOnFindingTheBestSplit += timerForFindingTheBestSplit.stop();
        return optionalSplitInfo;
    }




    private void buildRecursive(List<Example> instances, LazyBumTreeNode currentTreeNode, @Nullable FeatureTableInfo featureTableInfo) throws NoSplitInfoFoundException, InvalidSplitCriterionException, UnsupportedFieldTypeException, GraphTraversalException, UnsupportedFeatureTransformationException, InvalidKeyInfoException {
        int currentDepth = currentTreeNode.getDepth();

        String indent = " ".repeat(currentDepth) + "|- ";


        if (VERBOSE){
            System.out.println(indent + "recursion depth: " + currentDepth);
            if(featureTableInfo != null ) {
                System.out.println(indent + "current featureTableInfo:");
                if(VERBOSE_PRINT_FEATURE_TABLE_INFOS){
                    System.out.println(featureTableInfo.toString());
                } else {
                    System.out.println("not shown for brevity");
                }
            } else{
                System.out.println(indent + "current featureTableInfo is null");
            }
        }


        if(this.stopCriterion.cannotSplitBeforeTest(instances, currentTreeNode.getDepth())){
            // note: reached tree depth or min number of instances
            if (VERBOSE){System.out.println(indent + "node should be turned into a leaf");}
            currentTreeNode.setLeafStrategy(
                    this.leafBuilder.build(instances)
            );
        } else{
            if (VERBOSE){System.out.println( indent + "search for a good feature test for this node");}
            /*
            * Check if there already is a feature good enough to split on.
            * */
            //note: find the best test based on the current table
            Optional<SplitInfo> optionalSplitInfo
                    = getBestTestBasedOnCurrentFeatureTable(instances, currentTreeNode, featureTableInfo);

            /*
            3 cases:
                isPresent && canSplitOnTest --> extendTree
                isPresent && ! canSplitOnTest --> extend feature table
                ! isPresent --> extend feature table
             */


            if(optionalSplitInfo.isPresent()
                    && this.stopCriterion.canSplitOnTest(optionalSplitInfo.get()))
            {

                if (VERBOSE){
                    System.out.println(indent + "found the best test based on the current table");
                    System.out.println(indent + "we can actually split on this test");
                }
                extendTree2(optionalSplitInfo.get(), instances, currentTreeNode, featureTableInfo);


//                extendTree(optionalSplitInfo.get(), instances, currentTreeNode, featureTableInfo);
            } else {
                if (VERBOSE) {
                    if (!optionalSplitInfo.isPresent()) {
                        System.out.println(indent + "did not find any test on the current feature table");
                    } else {
                        System.out.println(indent + "found a best test on the current feature table, but it did not score high enough");
                    }
                    System.out.println(indent + "try to extend the current feature table");
                }
                // extend the current feature table
                // heuristic: possible tables to extend the current_feature_table are those that:
                //   1. are used in a feature test of an ancestor node
                //   2. still have (?unjoined) neighbors to join on.
                //
                // choice: which table to find neighbors for?
                //   all the tables found in the heuristic, or only a subset?
                //
                Timer timerExtendingFeatureTable = Timer.getStartedTimer();
                Optional<FeatureTableInfo> optionalExtendedFeatureTableInfo
                        = featureTableBuilder.extendFeatureTable(featureTableInfo, currentTreeNode);
                this.timeSpentOnFeatureTableExtensionCalculations += timerExtendingFeatureTable.stop();

                if(optionalExtendedFeatureTableInfo.isPresent()){ // if there is a feature table extension
                    if (VERBOSE){System.out.println(indent + "found extensions for the current feature table");}
                    FeatureTableInfo extendedFeatureTableInfo
                            = optionalExtendedFeatureTableInfo.get();

                    /*
                     * Check if there is a feature good enough to split on in the extended feature table
                     * NOTE: this should only check the new FeatureTableExtensions
                     * */
                    Set<FeatureTableExtension> newlyCalculatedFeatureTableExtensions
                            = extendedFeatureTableInfo.getFeatureTableExtensions();
                    featureTableExtensionCounter.countCalculatedExtensions(newlyCalculatedFeatureTableExtensions);


                    Timer timerForSplittingOnExtendedFeatureTable = Timer.getStartedTimer();
                    Optional<SplitInfo> optionalSplitInfoExtendedFeatureTable
                            = this.splitter.getSplit(instances, currentTreeNode, newlyCalculatedFeatureTableExtensions);
                    this.timeSpentOnFindingTheBestSplit += timerForSplittingOnExtendedFeatureTable.stop();
                    if(optionalSplitInfoExtendedFeatureTable.isPresent() && this.stopCriterion.canSplitOnTest(optionalSplitInfoExtendedFeatureTable.get())){

                        featureTableExtensionCounter.countAcceptedExtensions(newlyCalculatedFeatureTableExtensions);

                        extendTree2(optionalSplitInfoExtendedFeatureTable.get(), instances, currentTreeNode, extendedFeatureTableInfo);
                    }
                    else{
                        if (VERBOSE){System.out.println("No split found in extended feature table");}
                        currentTreeNode.setLeafStrategy(
                                this.leafBuilder.build(instances)
                        );
                    }
                }
                else{
                    if (VERBOSE){System.out.println(indent + "No extended feature table found");}
                    currentTreeNode.setLeafStrategy(
                            this.leafBuilder.build(instances)
                    );
                }
            }
        }
    }


    private void extendTree2(SplitInfo splitInfo, List<Example> instances, LazyBumTreeNode currentTreeNode, FeatureTableInfo featureTableInfo) throws GraphTraversalException, NoSplitInfoFoundException, UnsupportedFieldTypeException, InvalidSplitCriterionException, UnsupportedFeatureTransformationException, InvalidKeyInfoException {

        if (VERBOSE){
            int currentDepth = currentTreeNode.getDepth();

            String indent = " ".repeat(currentDepth) + "|- ";
            System.out.println(indent + "extending tree using found test");
        }


        ExamplePartition examplePartition = splitInfo.examplePartition;
        UnevaluatableTestStrategy unevaluatableTestStrategy = splitInfo.unevaluatableTestStrategy;

        NodeTest currentTreeNodeTest = splitInfo.test;
        currentTreeNode.setNodeTest(currentTreeNodeTest);
        currentTreeNode.setNodeInfo(
                new InternalNodeInfo(
                        instances, splitInfo.score, splitInfo.splitCriterionCalculator,
                        examplePartition.getNbOfExamplesUnevaluatableByTest(),
                        unevaluatableTestStrategy.shouldUnevaluatableTestCorrespondToSuccess()
                )
        );

        int childDepth = currentTreeNode.getDepth() + 1;

        // left child
        LazyBumTreeNode leftChild = new LazyBumTreeNode(childDepth, currentTreeNode);
        currentTreeNode.setLeftChild(leftChild);
        buildRecursive(unevaluatableTestStrategy.getExamplesLeftChild(), leftChild, featureTableInfo);

        // right child
        LazyBumTreeNode rightChild = new LazyBumTreeNode(childDepth, currentTreeNode);
        currentTreeNode.setRightChild(rightChild);
        buildRecursive(unevaluatableTestStrategy.getExamplesRightChild(), rightChild, featureTableInfo);
    }
//



//    private void extendTree(SplitInfo splitInfo, List<Example> instances, LazyBumTreeNode currentTreeNode, FeatureTableInfo featureTableInfo) throws GraphTraversalException, NoSplitInfoFoundException, UnsupportedFieldTypeException, InvalidSplitCriterionException, UnsupportedFeatureTransformationException, InvalidKeyInfoException {
//
//        int currentDepth = currentTreeNode.getDepth();
//
//        String indent = " ".repeat(currentDepth) + "|- ";
//
//        if(this.stopCriterion.cannotSplitOnTest(splitInfo)){
//
//            System.out.println(indent + "should extend tree, but cannot split on best test found");
//
//            currentTreeNode.setLeafStrategy(
//                    this.leafBuilder.build(instances)
//            );
//        }else{
//            System.out.println(indent + "extending tree using found test");
//
//            ExamplePartition examplePartition = splitInfo.examplePartition;
//            UnevaluatableTestStrategy unevaluatableTestStrategy = splitInfo.unevaluatableTestStrategy;
//
//            NodeTest currentTreeNodeTest = splitInfo.test;
//            currentTreeNode.setNodeTest(currentTreeNodeTest);
//            currentTreeNode.setNodeInfo(
//                    new InternalNodeInfo(
//                            instances, splitInfo.score, splitInfo.splitCriterionCalculator,
//                            examplePartition.getNbOfExamplesUnevaluatableByTest(),
//                            unevaluatableTestStrategy.shouldUnevaluatableTestCorrespondToSuccess()
//                        )
//                    );
//
//            int childDepth = currentTreeNode.getDepth() + 1;
//
//            // left child
//            LazyBumTreeNode leftChild = new LazyBumTreeNode(childDepth, currentTreeNode);
//            currentTreeNode.setLeftChild(leftChild);
//            buildRecursive(unevaluatableTestStrategy.getExamplesLeftChild(), leftChild, featureTableInfo);
//
//            // right child
//            LazyBumTreeNode rightChild = new LazyBumTreeNode(childDepth, currentTreeNode);
//            currentTreeNode.setRightChild(rightChild);
//            buildRecursive(unevaluatableTestStrategy.getExamplesRightChild(), rightChild, featureTableInfo);
//        }
//    }
//
//    private void buildRecursive(List<Example> instances, LazyBumTreeNode currentTreeNode, @Nullable FeatureTableInfo featureTableInfo) throws NoSplitInfoFoundException, InvalidSplitCriterionException, UnsupportedFieldTypeException, GraphTraversalException, UnsupportedFeatureTransformationException, InvalidKeyInfoException {
//        int currentDepth = currentTreeNode.getDepth();
//
//        String indent = " ".repeat(currentDepth) + "|- ";
//
//        System.out.println(indent + "recursion depth: " + currentDepth);
//
//        if(featureTableInfo != null) {
//            System.out.println(indent + "current featureTableInfo:");
//            System.out.println(featureTableInfo.toString());
//        } else{
//            System.out.println(indent + "current featureTableInfo is null");
//        }
//
//        if(this.stopCriterion.cannotSplitBeforeTest(instances, currentTreeNode.getDepth())){
//            System.out.println(indent + "node should be turned into a leaf");
//            currentTreeNode.setLeafStrategy(
//                    this.leafBuilder.build(instances)
//            );
//        } else{
//            System.out.println( indent + "search for a good feature test for this node");
//            /*
//            * Check if there already is a feature good enough to split on.
//            * */
//
//            Set<FeatureTableExtension> allFeatureTableExtensions
//                    = featureTableInfo.getAllHierarchicalFeatureTableExtensions();
//            Optional<SplitInfo> optionalSplitInfo
//                    = this.splitter.getSplit(instances, currentTreeNode, allFeatureTableExtensions);
//
//            //
//            if(optionalSplitInfo.isPresent()) { // if there is a feature good enough to split on
//                System.out.println(indent + "a good test exists in the current feature table");
//                //NOTE: WE HAVE NOT CHECKED IF THIS TEST ACTUALLY SCORES BETTER THAN OUR THRESHOLD
//
//
//
//                extendTree(optionalSplitInfo.get(), instances, currentTreeNode, featureTableInfo);
//            } else{
//                System.out.println(indent + "NO good test exists in the current feature table");
//                System.out.println(indent + "try to extend the current feature table");
//                // extend the current feature table
//                // heuristic: possible tables to extend the current_feature_table are those that:
//                //   1. are used in a feature test of an ancestor node
//                //   2. still have (?unjoined) neighbors to join on.
//                //
//                // choice: which table to find neighbors for?
//                //   all the tables found in the heuristic, or only a subset?
//                //
//                Optional<FeatureTableInfo> optionalExtendedFeatureTableInfo
//                        = featureTableBuilder.extendFeatureTable(featureTableInfo, currentTreeNode);
//
//
//                if(optionalExtendedFeatureTableInfo.isPresent()){ // if there is a feature table extension
//                    System.out.println(indent + "found extensions for the current feature table");
//                    FeatureTableInfo extendedFeatureTableInfo
//                            = optionalExtendedFeatureTableInfo.get();
//
//                    /*
//                     * Check if there is a feature good enough to split on in the extended feature table
//                     * NOTE: this should only check the new FeatureTableExtensions
//                     * */
//                    Set<FeatureTableExtension> newlyCalculatedFeatureTableExtensions
//                            = extendedFeatureTableInfo.getFeatureTableExtensions();
//                    featureTableExtensionCounter.countCalculatedExtensions(newlyCalculatedFeatureTableExtensions);
//
//                    Optional<SplitInfo> optionalSplitInfoExtendedFeatureTable
//                            = this.splitter.getSplit(instances, currentTreeNode, newlyCalculatedFeatureTableExtensions);
//                    if(optionalSplitInfoExtendedFeatureTable.isPresent()){
//
//                        featureTableExtensionCounter.countCalculatedExtensions(newlyCalculatedFeatureTableExtensions);
//
//                        extendTree(optionalSplitInfoExtendedFeatureTable.get(), instances, currentTreeNode, extendedFeatureTableInfo);
//                    }
//                    else{
//                        System.out.println("No split found in extended feature table");
//                        currentTreeNode.setLeafStrategy(
//                                this.leafBuilder.build(instances)
//                        );
//                    }
//                }
//                else{
//                    System.out.println(indent + "No extended feature table found");
//                    currentTreeNode.setLeafStrategy(
//                            this.leafBuilder.build(instances)
//                    );
//                }
//            }
//        }
//    }
//
//    private void extendTree(SplitInfo splitInfo, List<Example> instances, LazyBumTreeNode currentTreeNode, FeatureTableInfo featureTableInfo) throws GraphTraversalException, NoSplitInfoFoundException, UnsupportedFieldTypeException, InvalidSplitCriterionException, UnsupportedFeatureTransformationException, InvalidKeyInfoException {
//
//        int currentDepth = currentTreeNode.getDepth();
//
//        String indent = " ".repeat(currentDepth) + "|- ";
//
//        if(this.stopCriterion.cannotSplitOnTest(splitInfo)){
//
//            System.out.println(indent + "should extend tree, but cannot split on best test found");
//
//            currentTreeNode.setLeafStrategy(
//                    this.leafBuilder.build(instances)
//            );
//        }else{
//            System.out.println(indent + "extending tree using found test");
//
//            ExamplePartition examplePartition = splitInfo.examplePartition;
//            UnevaluatableTestStrategy unevaluatableTestStrategy = splitInfo.unevaluatableTestStrategy;
//
//            NodeTest currentTreeNodeTest = splitInfo.test;
//            currentTreeNode.setNodeTest(currentTreeNodeTest);
//            currentTreeNode.setNodeInfo(
//                    new InternalNodeInfo(
//                            instances, splitInfo.score, splitInfo.splitCriterionCalculator,
//                            examplePartition.getNbOfExamplesUnevaluatableByTest(),
//                            unevaluatableTestStrategy.shouldUnevaluatableTestCorrespondToSuccess()
//                        )
//                    );
//
//            int childDepth = currentTreeNode.getDepth() + 1;
//
//            // left child
//            LazyBumTreeNode leftChild = new LazyBumTreeNode(childDepth, currentTreeNode);
//            currentTreeNode.setLeftChild(leftChild);
//            buildRecursive(unevaluatableTestStrategy.getExamplesLeftChild(), leftChild, featureTableInfo);
//
//            // right child
//            LazyBumTreeNode rightChild = new LazyBumTreeNode(childDepth, currentTreeNode);
//            currentTreeNode.setRightChild(rightChild);
//            buildRecursive(unevaluatableTestStrategy.getExamplesRightChild(), rightChild, featureTableInfo);
//        }
//    }

    private boolean isCorrectlyInitialized(){
        return splitter != null && leafBuilder != null && stopCriterion != null;
    }

    public LazyBumSplitter getSplitter() {
        return splitter;
    }

    public FeatureTableExtensionCounter getFeatureTableExtensionCounter() {
        return featureTableExtensionCounter;
    }


    public double getTimeSpentOnFeatureTableExtensionCalculations() {
        return timeSpentOnFeatureTableExtensionCalculations;
    }

    public double getTimeSpentOnFindingTheBestSplit() {
        return timeSpentOnFindingTheBestSplit;
    }


    public StopCriterion getStopCriterion() {
        return stopCriterion;
    }


    public int getTotalNbOfFeaturesBuilt(){
        return featureTableBuilder.getNbOfFeaturesBuilt();
    }
}
