package learning.testing;

import database.JOOQDatabaseInteractor;
import graph.InvalidKeyInfoException;
import dataset.TargetTableManager;
import feature.columntype.ColumnFieldHandler;
import feature.columntype.FieldTypeResolver;
import feature.columntype.UnsupportedFieldTypeException;
import feature.tranform.FieldTransformerEnumInterface;
import feature.tranform.UnsupportedFeatureTransformationException;
import graph.TraversalPath;
import graph.TraversalPathDataGatheringQueryBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jooq.*;

import java.util.*;

public abstract class TestEvaluator {


    protected Map<Object, List<Object>> getMultiSetPerExampleID(TargetTableManager targetTableManager, Field fieldToCollect, Select<Record> query) {
        Result<Record> records = query.fetch();
        Map<Object, List<Object>> valuesPerInstance = new HashMap<>();
        for (Record record : records) {
            Object instanceID = record.get(targetTableManager.getTargetID());
            Object fieldValue = record.getValue(fieldToCollect);
            if(! valuesPerInstance.containsKey(instanceID)){
                List<Object> fieldValues = new ArrayList<>();
                fieldValues.add(fieldValue);
                valuesPerInstance.put(instanceID, fieldValues);
            } else{
                valuesPerInstance.get(instanceID).add(fieldValue);
            }
        }
        return valuesPerInstance;
    }

    protected ImmutablePair<Field, Table> getFieldToCollect(NodeTest nodeTest, JOOQDatabaseInteractor jooqDatabaseInteractor, Table targetTable){

        Optional<TraversalPath> optionalTraversalPath = nodeTest.getTraversalPath();

        // the field to collect values for
        Table tableToCollectFrom;
        if(optionalTraversalPath.isPresent()
            && optionalTraversalPath.get().size() > 0
        ) {
            tableToCollectFrom =
                    optionalTraversalPath.get().getLastTable(jooqDatabaseInteractor);

        } else{
            tableToCollectFrom = targetTable;
        }

        String fieldName = nodeTest.unqualifiedFieldName;

        Field fieldToCollect = tableToCollectFrom.field(fieldName);
        if(fieldToCollect == null){
            throw new IllegalArgumentException("fieldToCollect should not be null at is position");
        }


        return new ImmutablePair<>(fieldToCollect, tableToCollectFrom);
    }


    public Set<Object> evaluate2Batch(NodeTest nodeTest, Collection<Object> instanceIDs,
                                      JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager,
                                      boolean missingValueCorrespondsToSuccess
                                      ) throws InvalidKeyInfoException, UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {
//        System.out.println(nodeTest.toString());
        ImmutablePair<Field, Table> fieldAndTableToCollectFrom = this.getFieldToCollect(nodeTest, jooqDatabaseInteractor, targetTableManager.getTargetTable());
        Field fieldToCollect = fieldAndTableToCollectFrom.getLeft();

        Table tableToCollectFrom = fieldAndTableToCollectFrom.getRight();

        // BUILD THE NECESSARY QUERY
        if(fieldToCollect == null){
            throw new IllegalArgumentException("fieldToCollect should not be null at is position");
        }
        if(instanceIDs == null){
            throw new IllegalArgumentException("instanceIDs should not be null at this position");
        }

        Optional<TraversalPath> optionalTraversalPath = nodeTest.getTraversalPath();
        if(optionalTraversalPath.isPresent()){

            TraversalPath traversalPath = optionalTraversalPath.get();
            Select<Record> query = TraversalPathDataGatheringQueryBuilder
                    .buildQueryGatheringInfoForExamplesBatch(instanceIDs, jooqDatabaseInteractor, targetTableManager, traversalPath, fieldToCollect).getRight();

            // GATHER THE RESULTS FROM THE QUERY
            Map<Object, List<Object>> multiSetPerInstance = getMultiSetPerExampleID(targetTableManager, fieldToCollect, query);

            return evaluateSpecific(nodeTest, instanceIDs, multiSetPerInstance, fieldToCollect, tableToCollectFrom,
                    missingValueCorrespondsToSuccess);
        } else{ // the table to collect from is the target table
            Table targetTable = targetTableManager.getTargetTable();
            if(! tableToCollectFrom.equals(targetTable)){
                throw new UnsupportedOperationException("I expect the table to collect info to be the target table," +
                        " but this is not the case. The table has name " + tableToCollectFrom.getName());
            }

            // todo: wrapping each single value in a list is very inefficient
            Map<Object, List<Object>> instanceToValueMap = TraversalPathDataGatheringQueryBuilder
                    .selectFromTargetTable(instanceIDs, jooqDatabaseInteractor, targetTableManager, fieldToCollect);
            return evaluateSpecific(nodeTest, instanceIDs, instanceToValueMap, fieldToCollect, tableToCollectFrom, missingValueCorrespondsToSuccess);
        }

    }

    protected abstract Set<Object> evaluateSpecific(
            NodeTest nodeTest,
            Collection<Object> instanceIDs, Map<Object, List<Object>> multiSetPerInstance,
            Field fieldToCollect, Table tableOfFieldToCollect,
            boolean missingValueCorrespondsToSuccess) throws UnsupportedFeatureTransformationException, UnsupportedFieldTypeException;

//    protected abstract Set<Object> evaluateSpecificTargetTable(
//            NodeTest nodeTest,
//            Collection<Object> instanceIDs, Map<Object, Object> singleElementPerInstance,
//            Field fieldToCollect, Table tableOfFieldToCollect) throws UnsupportedFeatureTransformationException, UnsupportedFieldTypeException;
//



    protected Map<Object, Object> getFeatureValuePerExampleMap(FieldTransformerEnumInterface optionalTransformation, Field fieldToCollect, Table tableOfFieldToCollect,
                                                             Map<Object, List<Object>> multiSetPerInstance) throws UnsupportedFieldTypeException, UnsupportedFeatureTransformationException {
        ColumnFieldHandler columnFieldHandler = FieldTypeResolver.resolveColumn(fieldToCollect, tableOfFieldToCollect);
        Map<Object, Object> featureValuePerInstance;
        if(optionalTransformation == null){
            // no transformation

            featureValuePerInstance = new HashMap<>();

            for (Object instanceID : multiSetPerInstance.keySet()) {
                List<Object> instanceMultiSet = multiSetPerInstance.get(instanceID);
                if(instanceMultiSet.size() != 1){
                    throw new UnsupportedFeatureTransformationException("Expected 1 value in each multiset, found (for a specific multiset): " + instanceMultiSet.size() + " values");
                }
                featureValuePerInstance.put(instanceID, instanceMultiSet.get(0));
            }
        } else{
            featureValuePerInstance = columnFieldHandler.getFeatureValuesPerInstance(multiSetPerInstance, optionalTransformation);

        }
        return featureValuePerInstance;
    }


}
