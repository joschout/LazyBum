package lazybum;

import database.JOOQDatabaseInteractor;

import dataset.Example;
import feature.columntype.UnsupportedFieldTypeException;
import feature.featuretable.FeatureColumn;
import feature.tranform.UnsupportedFeatureTransformationException;
import graph.GraphTraversalException;
import lazybum.unevaluatables.HighestScoreIncreaseStrategy;
import learning.split.InvalidSplitCriterionException;
import learning.split.SplitCriterionCalculator;
import learning.split.SplitCriterionCalculatorSettings;
import learning.split.SplitInfo;
import learning.testing.ExamplePartition;
import learning.testing.HasRowsTest;
import learning.testing.NodeTest;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 *  Finds the best test for splitting a node based on the node's training examples.
 *  It must be initialized with a SplitCriterionCalculator and TestEvaluator.
 *  Reports the split info using a SplitInfo object.
 */
@SuppressWarnings("Duplicates")
public class LazyBumSplitter {

    public static boolean VERBOSE = false;

    private SplitCriterionCalculatorSettings splitCriterionCalculatorSettings;

    public static int MAX_LOOKAHEAD = 2;
    public final long maxLookahead;

    private JOOQDatabaseInteractor jooqDatabaseInteractor;

    public LazyBumSplitter(SplitCriterionCalculatorSettings splitCriterionCalculatorSettings,
                           JOOQDatabaseInteractor jooqDatabaseInteractor) {
        this(splitCriterionCalculatorSettings, MAX_LOOKAHEAD, jooqDatabaseInteractor);
    }

    public LazyBumSplitter(SplitCriterionCalculatorSettings splitCriterionCalculatorSettings, long maxLookahead,
                           JOOQDatabaseInteractor jooqDatabaseInteractor) {
        this.splitCriterionCalculatorSettings = splitCriterionCalculatorSettings;
        this.maxLookahead = maxLookahead;
        this.jooqDatabaseInteractor = jooqDatabaseInteractor;
//        this.refactoredJoinTableConstruction = refactoredJoinTableConstruction;
    }


    /**
     * Splits the current decision tree node based on the best feature in the current feature table.
     * If no best feature is found, the feature table must be extended,
     * and the new features should be checked for a best split.
     *
     * NOTE: if no best split has been found on the non-extended feature table,
     *       these columns don't have to be re-checked in the extended table
     *
     *
     * @param instances
     * @param nodeToSplit
     * @return
     * @throws InvalidSplitCriterionException
     * @throws GraphTraversalException
     * @throws UnsupportedFieldTypeException
     * @throws UnsupportedFeatureTransformationException
     */
    public Optional<SplitInfo> getSplit(List<Example> instances, LazyBumTreeNode nodeToSplit, Set<FeatureTableExtension> featureTableExtensionsToCheck) throws InvalidSplitCriterionException, GraphTraversalException, UnsupportedFieldTypeException, UnsupportedFeatureTransformationException {
//    public Optional<SplitInfo> getSplit(List<Example> instances, LazyBumTreeNode nodeToSplit, FeatureTableInfo featureTableInfo) throws InvalidSplitCriterionException, GraphTraversalException, UnsupportedFieldTypeException, UnsupportedFeatureTransformationException {


        SplitInfo currentBestSplitInfo = null;
        SplitCriterionCalculator splitCriterionCalculator
                = splitCriterionCalculatorSettings.getSplitCriterionCalculator(instances);


        /*
        * Generate splits for the current feature table
        * */

//        Set<ImmutableTriple<NodeTest, JoinInfo, FeatureColumn>>  nodeTestsToTry
//                = JoinInfoTestGenerator.generateTests(nodeToSplit, featureTableInfo);

        Set<ImmutableTriple<NodeTest,Map<Object, Integer>,FeatureColumn>> nodeTestsToTry
                = FTTestGenerator.generateTests(
                        featureTableExtensionsToCheck,
                        jooqDatabaseInteractor,
                        instances
                );



        for (ImmutableTriple<NodeTest, Map<Object, Integer>, FeatureColumn> candidateTestTriple : nodeTestsToTry) {

            NodeTest candidateTest = candidateTestTriple.getLeft();
            Map<Object, Integer> instanceToListIndexMap = candidateTestTriple.getMiddle();
            FeatureColumn featureColumn = candidateTestTriple.getRight();

            ExamplePartition examplePartition
                    = splitExamplesGivenATest(
                            instances, featureColumn, candidateTest, instanceToListIndexMap);


            HighestScoreIncreaseStrategy unevaluatableTestStrategy = new HighestScoreIncreaseStrategy(examplePartition);
            double candidateTestScore = unevaluatableTestStrategy.score(splitCriterionCalculator);

//            double candidateTestScore = splitCriterionCalculator.calculate(examplePartition);
            if(currentBestSplitInfo == null
                    || candidateTestScore > currentBestSplitInfo.score){

                if(VERBOSE){
                    if(currentBestSplitInfo == null){
                        System.out.println("first score: " + candidateTestScore);
                        System.out.println("\tusing test " + candidateTest.toString("\t"));
                    }else {
                        System.out.println("new best score: " + candidateTestScore + " > " + currentBestSplitInfo.score);
                        System.out.println("\tusing test " + candidateTest.toString("\t"));
                    }
                }
                //NOTE: you cannot destroy the old table here, since it is necessary for lookahead
                currentBestSplitInfo = new SplitInfo(
                        candidateTest,
                        examplePartition,
                        unevaluatableTestStrategy,


                        candidateTestScore,
                        splitCriterionCalculator,

                        null,
                        candidateTestTriple.getRight()
                );
            }
        }
//        //note: drop other tables:
//        for (JoinInfo joinInfo: joinInfos) {
//            if(currentBestSplitInfo != null &&
//                    !joinInfo.equals(currentBestSplitInfo.joinInfo)){
//                joinInfo.destruct(jooqDatabaseInteractor.getDslContext());
//            }
//        }

        if(currentBestSplitInfo == null){
            return Optional.empty();
        } else{
            if(VERBOSE) {
            System.out.println("final best score: " + currentBestSplitInfo.score);
            }
            return Optional.of(currentBestSplitInfo);
        }
    }

    private ExamplePartition splitExamplesGivenATest(List<Example> instances,
                                                     FeatureColumn featureColumn,
                                                     NodeTest test,
                                                     Map<Object, Integer> instanceToListIndexMap ){


        // note: you already have something for this implemented in
        ExamplePartition examplePartition = new ExamplePartition();


        if(test instanceof HasRowsTest){
            Set<Object> instancesHavingRows = ((HasRowsTest) test).instancesHavingRows;
            for (Example example: instances){
                if(instancesHavingRows.contains(example.instanceID)){
                    examplePartition.examplesSucceedingTest.add(example);
                } else{
                    examplePartition.examplesFailingTest.add(example);
                }
            }
            ((HasRowsTest) test).instancesHavingRows = null;

        } else {


            for (Example instance : instances) {
                if (instanceToListIndexMap.containsKey(instance.instanceID)) {
                    int instanceIndex = instanceToListIndexMap.get(instance.instanceID);
                    Object featureValue = featureColumn.getFeatureValueForInstance(instanceIndex);
                    if(featureValue!= null) {

                        boolean succeedsTest = test.evaluate(featureValue);
                        if (succeedsTest) {
                            examplePartition.examplesSucceedingTest.add(instance);
                        } else {
                            examplePartition.examplesFailingTest.add(instance);
                        }
                    }else{
                        examplePartition.examplesUnevaluatableByTest.add(instance);
                        System.out.println("FOUND A NULL FEATURE VALUE for test\n" + test.toString("\t\t"));
//                        throw new RuntimeException("FOUND A NULL FEATURE VALUE.");
                    }

                } else { // no feature value, so the instance does not succeed the test
//                    System.out.println("treat as missing value");
                    examplePartition.examplesUnevaluatableByTest.add(instance);
                }
            }
//        List<List<Example>> splittedExampleInstances = new ArrayList<List<Example>>();
//        splittedExampleInstances.add(examplesSucceedingTest);
//        splittedExampleInstances.add(examplesFailingTest);
        }
        return examplePartition;
    }


}
