package weka;

import config.ProgramConfiguration;
import database.JOOQDatabaseInteractor;
import dataset.Example;
import dataset.FoldDataSetSplitter;
import dataset.TargetTableManager;
import org.apache.commons.lang3.tuple.ImmutablePair;
import dataset.ExampleIDFoldPersistor;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by joschout.
 */
public class WekaPredefinedFoldInstancesSplitter {


    private FoldDataSetSplitter<Example> foldDataSetSplitter;

    private Instances data;
    private Attribute targetIdAttribute;

    private boolean removeTargetIdColumn;
    private boolean isTargetIdAttributeNumeric;


    private Remove removeFilter;

    public WekaPredefinedFoldInstancesSplitter(String foldDir, JOOQDatabaseInteractor jooqDatabaseInteractor, ProgramConfiguration programConfiguration,
                                               Instances data, Attribute targetIdAttribute, boolean removeTargetIdColumn) throws Exception {

        /**
         * NOTE: this can be made to work without the database connection
         */


        TargetTableManager targetTableManager = TargetTableManager.getInstanceManager(jooqDatabaseInteractor, programConfiguration);
        List<Example> allExamples = targetTableManager.getInstancesAsExamples();
        ExampleIDFoldPersistor exampleIDFoldPersistor = new ExampleIDFoldPersistor();
        this.foldDataSetSplitter = exampleIDFoldPersistor.loadFoldDataSetSplitter(foldDir, allExamples);

        this.data = data;
        this.targetIdAttribute = targetIdAttribute;

        int[] attrIndicesToRemove = {targetIdAttribute.index()};

        this.removeFilter = new Remove();
        removeFilter.setAttributeIndicesArray(attrIndicesToRemove);
        removeFilter.setInputFormat(data);

        this.removeTargetIdColumn = removeTargetIdColumn;
        this.isTargetIdAttributeNumeric = targetIdAttribute.isNumeric();
    }

    public int getNbOfFolds(){
        return foldDataSetSplitter.getNbOfFolds();
    }


    public Instances[][] crossValidationPredefinedSplit() throws Exception {

        int numberOfFolds = foldDataSetSplitter.getNbOfFolds();
        foldDataSetSplitter.getFolds();

        Instances[][] split = new Instances[2][numberOfFolds];

        for (int i = 0; i < numberOfFolds; i++) {

            List<Example> testExampleSet = foldDataSetSplitter.getValidationSet(i);
            List<Example> trainingExampleSet = foldDataSetSplitter.getTrainingSet(i);

            Set<String> testExampleIds;
            Set<String> trainingExampleIds;

            if(isTargetIdAttributeNumeric) {
                testExampleIds = testExampleSet.stream().map(
                        example ->
                                Double.toString(
                                        ((Number) example.instanceID )
                                                .doubleValue()
                                )
                ).collect(Collectors.toSet());
                trainingExampleIds = trainingExampleSet.stream().map(
                        example ->
                                Double.toString(
                                        ((Number)example.instanceID )
                                                .doubleValue()
                                )
                ).collect(Collectors.toSet());
            } else{
                testExampleIds = testExampleSet.stream().map(example ->
                        example.instanceID.toString()).collect(Collectors.toSet());
                trainingExampleIds = trainingExampleSet.stream().map(example ->
                        example.instanceID.toString()).
                        collect(Collectors.toSet());
            }

            ImmutablePair<Instances, Instances> trainingAndTestSetOfFold
                    = splitIntoTrainingAndTestSet(trainingExampleIds, testExampleIds);

            split[0][i] = trainingAndTestSetOfFold.getLeft();
            split[1][i] = trainingAndTestSetOfFold.getRight();
        }

        return split;

    }


    private ImmutablePair<Instances, Instances> splitIntoTrainingAndTestSet(Set<String> trainingIdSet, Set<String> testIdSet) throws Exception {


//        Remove r = new Remove();
//        r.setAttributeIndicesArray(attrIndicesToRemove);
//        r.setInputFormat(data);
//        Instances filtered = Filter.useFilter(data, r);
//        System.out.println(filtered);


        // training Instances
        Instances trainingTmp = new Instances(data, trainingIdSet.size());
        Instances testTmp = new Instances(data, testIdSet.size());
        trainingTmp.setClass(trainingTmp.attribute(data.classAttribute().name()));
        testTmp.setClass(testTmp.attribute(data.classAttribute().name()));

        for (Instance instance : data) {

            String value;
            if(isTargetIdAttributeNumeric){
                value = Double.toString((double)instance.value(targetIdAttribute));
            } else{
                value = instance.toString();
            }


            if(trainingIdSet.contains(value)){
                trainingTmp.add(instance);
            } else if(testIdSet.contains(value)){
                testTmp.add(instance);
            } else{
                throw new Exception("the id of the instance does not belong to training or test folds");
            }
        }



        Instances training;
        Instances test;

        if(removeTargetIdColumn) {

//        Remove removeFilter = new Remove();

//        removeFilter.setAttributeIndicesArray(attrIndicesToRemove);
//        removeFilter.setInputFormat(trainingTmp);

            Filter removeFilterTraining = Filter.makeCopy(removeFilter);
//            Remove removeFilterTraining = new Remove();
//            removeFilterTraining.setAttributeIndicesArray(attrIndicesToRemove);
//            removeFilterTraining.setInputFormat(trainingTmp);

            training = Filter.useFilter(trainingTmp, removeFilterTraining);

            //todo: remove assumption that class attribute is the last attribute
            training.setClassIndex(training.numAttributes() - 1 );
            training.setClass(training.attribute(data.classAttribute().name()));


            Filter removeFilterTest = Filter.makeCopy(removeFilter);

            test = Filter.useFilter(testTmp, removeFilterTest);
            //todo: remove assumption that class attribute is the last attribute
            test.setClass(test.attribute(data.classAttribute().name()));
//            test.setClassIndex(test.numAttributes() - 1 );
//
//            System.out.println(testTmp.numAttributes());
//            System.out.println(test.numAttributes());
        } else{
            training = trainingTmp;
            test = testTmp;
        }
        return new ImmutablePair<>(training, test);

    }


}
