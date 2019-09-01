package lazybum;

import database.JOOQDatabaseInteractor;

import graph.InvalidKeyInfoException;
import dataset.Example;
import dataset.TargetTableManager;
import feature.columntype.UnsupportedFieldTypeException;
import feature.tranform.UnsupportedFeatureTransformationException;
import graph.GraphTraversalException;
import learning.InternalNodeInfo;
import learning.InvalidTreeBuilderException;
import learning.InvalidTreeException;
import learning.Prediction;
import learning.split.InvalidSplitCriterionException;
import learning.split.NoSplitInfoFoundException;

import java.util.*;

/**
 * Decision tree used for making predictions. Initially empty.
 * An internal TreeNode tree is fitted on training examples using a TreeBuilder.
 *
 * Created by joschout.
 */
@SuppressWarnings("Duplicates")
public class LazyBumDecisionTree {


    private LazyBumTreeNode treeRoot;


    public LazyBumDecisionTree(LazyBumTreeNode treeRoot){
        this.treeRoot = treeRoot;
    }

    public LazyBumDecisionTree(){
        this.treeRoot = null;
//        this.treeBuilder = null;
    }

    public void fit(List<Example> instanceIDs, LazyBumTreeBuilder treeBuilder)
            throws InvalidTreeBuilderException, NoSplitInfoFoundException, InvalidSplitCriterionException, GraphTraversalException, UnsupportedFieldTypeException, UnsupportedFeatureTransformationException, InvalidKeyInfoException {
        treeRoot = treeBuilder.build(instanceIDs);
//        testEvaluator = treeBuilder.getSplitter().getTestEvaluator();
    }


    public Prediction predict(Object instanceID, JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager) throws InvalidTreeException, InvalidKeyInfoException, UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {
        if(treeRoot == null){
            throw new InvalidTreeException("Cannot predict as there is no learned tree");
        } else{

            List<Object> singleInstanceIDWrapper = new ArrayList<>(1);
            singleInstanceIDWrapper.add(instanceID);
            Map<Object, Prediction> instanceIDToPredictionMap = new HashMap<>();
            predictAsBatchRecursive(
                    singleInstanceIDWrapper, treeRoot, instanceIDToPredictionMap, jooqDatabaseInteractor, targetTableManager);

            return instanceIDToPredictionMap.get(instanceID);

        }
    }

    public Map<Object, Prediction> predictAsBatch(Collection<Object> instanceIDs,
                                                  JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager) throws InvalidTreeException, UnsupportedFeatureTransformationException, UnsupportedFieldTypeException, InvalidKeyInfoException {
        if(treeRoot == null){
            throw new InvalidTreeException("Cannot predict as there is no learned tree");
        } else{

            Map<Object, Prediction> instanceIDToPredictionMap = new HashMap<>();

            predictAsBatchRecursive(
                    instanceIDs, treeRoot, instanceIDToPredictionMap, jooqDatabaseInteractor, targetTableManager);

            return instanceIDToPredictionMap;

        }
    }

    private void predictAsBatchRecursive(Collection<Object> instanceIDs, LazyBumTreeNode treeNode,
                                         Map<Object, Prediction> instanceIDToPredictionMap,
                                         JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager) throws InvalidTreeException, UnsupportedFeatureTransformationException, UnsupportedFieldTypeException, InvalidKeyInfoException {
        if(treeNode.isLeaf()){
            for(Object instanceID: instanceIDs){
                instanceIDToPredictionMap.put(instanceID, treeNode.getLeafStrategy().predict());
            }
        } else{
            // NOTE: this might be more efficient by creating an ID table and joining with that table

            Set<Object> instanceIDsForWhichTestSucceeds = treeNode.getNodeTest()
                    .evaluateBatch(instanceIDs, jooqDatabaseInteractor, targetTableManager,
                            ((InternalNodeInfo)treeNode.getNodeInfo()).shouldMissingValueCorrespondToSuccess()
                    );
            Set<Object> instanceIDsForWhichTestFails = new HashSet<>();
            for(Object instanceID: instanceIDs){
                if(instanceIDsForWhichTestSucceeds == null){
                    System.out.println("should not be the case. Also, remove this");
                }


                if(! instanceIDsForWhichTestSucceeds.contains(instanceID)){
                    instanceIDsForWhichTestFails.add(instanceID);
                }
            }

            if(! instanceIDsForWhichTestSucceeds.isEmpty()) {
                predictAsBatchRecursive(instanceIDsForWhichTestSucceeds, treeNode.getLeftChild(),
                        instanceIDToPredictionMap,
                        jooqDatabaseInteractor, targetTableManager);
            }

            if(! instanceIDsForWhichTestFails.isEmpty()) {
                predictAsBatchRecursive(instanceIDsForWhichTestFails, treeNode.getRightChild(),
                        instanceIDToPredictionMap,
                        jooqDatabaseInteractor, targetTableManager);
            }

        }
    }


    public String toString(){
        return treeRoot.toString();
    }

    public int getNbOfNodes(){
        return LazyBumTreeNode.countNbOfNodes(treeRoot);
    }

    public int getNbOfInnerNodes(){
        return LazyBumTreeNode.countNbOfInnerNodes(treeRoot);
    }

    public LazyBumTreeNode getTreeRoot() {
        return treeRoot;
    }
}
