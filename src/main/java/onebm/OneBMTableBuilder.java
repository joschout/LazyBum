package onebm;

import database.JOOQDatabaseInteractor;

import dataset.Example;
import dataset.TargetTableManager;
import feature.columntype.UnsupportedFieldTypeException;
import feature.featuretable.FeatureTableHandler;
import feature.featuretable.persistance.CompleteFeatureTablePersistance;
import feature.featuretable.persistance.FeatureColumnEncoder;
import feature.tranform.UnsupportedFeatureTransformationException;
import graph.GraphTraversalException;
import graph.TraversalPath;
import research.JoinInfo;
import research.RefactoredJoinTableConstruction;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class OneBMTableBuilder {


    private final JOOQDatabaseInteractor jooqDatabaseInteractor;
    private RefactoredJoinTableConstruction refactoredJoinTableConstruction;
    private CompleteFeatureTablePersistance featureTablePersistance;


    public OneBMTableBuilder(RefactoredJoinTableConstruction refactoredJoinTableConstruction,
                             JOOQDatabaseInteractor jooqDatabaseInteractor) {
        this.refactoredJoinTableConstruction = refactoredJoinTableConstruction;
        this.jooqDatabaseInteractor = jooqDatabaseInteractor;

        this.featureTablePersistance = new CompleteFeatureTablePersistance(jooqDatabaseInteractor);

    }


    public void build(TargetTableManager targetTableManager) throws GraphTraversalException, UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {
        JoinInfo previousJoinInfo = null;
        List<Object> instanceIDs = targetTableManager.getAllExampleInstanceIDs();

        buildRecursive(previousJoinInfo, instanceIDs);

        //  last step: add labels:

        List<Example> exampleList = targetTableManager.getInstancesAsExamples();
        featureTablePersistance.addLabelColumn(exampleList, targetTableManager);
    }

    private void buildRecursive(JoinInfo previousJoinInfo, List<Object> instanceIDs) throws GraphTraversalException, UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {

        // training instances are all instances in the data set
        Set<JoinInfo> joinInfos  = refactoredJoinTableConstruction.oneStepJoinTableConstruction(previousJoinInfo, instanceIDs);

        /*
        * NOTE: problem 2: 2 kinds of missing values:
        *   - no instanceID in featureTableHandler --> no value
        *   - even with a value in instanceID --> NULL or NaN
        *
        *
        *
        * */



        for (JoinInfo currentJoinInfo : joinInfos) {
            FeatureTableHandler featureTableHandler = currentJoinInfo.featureTableHandler;
            Optional<TraversalPath> optionalTraversalPath = currentJoinInfo.getTraversalPath();

            String descriptionOfHowFeatureFieldsWereCollected =
                    optionalTraversalPath
                            .map(traversalPath -> traversalPath.toStringCompact(""))
                            .orElse("");

            featureTablePersistance.storeFeatureTable(featureTableHandler, descriptionOfHowFeatureFieldsWereCollected);

            // recurse
            this.buildRecursive(currentJoinInfo, instanceIDs);
            // drop table
            currentJoinInfo.destruct(jooqDatabaseInteractor.getDslContext());
        }
    }


    public FeatureColumnEncoder getFeatureColumnEncoder() {
        return featureTablePersistance.getFeatureColumnEncoder();
    }

}


