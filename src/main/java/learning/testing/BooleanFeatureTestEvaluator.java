package learning.testing;

import feature.columntype.UnsupportedFieldTypeException;
import feature.tranform.UnsupportedFeatureTransformationException;
import org.jooq.Field;
import org.jooq.Table;

import java.util.*;

@SuppressWarnings("Duplicates")
public class BooleanFeatureTestEvaluator extends TestEvaluator {
//    @Override
//    public boolean evaluate(Object instanceID, Object featureValue, NodeTest nodeTest) {
//        return false;
//    }

//    public boolean evaluate2(BooleanFeatureTest booleanFeatureTest, Object instanceID,
//                             JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager) throws InvalidKeyInfoException, UnsupportedFeatureTransformationException {
//
//
//        System.out.println(booleanFeatureTest.toString());
//        System.out.println();
//
//        TraversalPath traversalPath = booleanFeatureTest.traversalPath;
//
//        Table tableToCollectFrom =
//                traversalPath.getLast().getDestination(jooqDatabaseInteractor);
//
//        String[] name_containing_underscore = booleanFeatureTest.unqualifiedFieldName.split("_");
//        String fieldName = name_containing_underscore[name_containing_underscore.length - 1];
//
//        Field fieldToCollect = tableToCollectFrom.field(fieldName);
//
//        // BUILD THE NECESSARY QUERY
//        ResultQuery<Record> query = buildQueryGatheringInfoForExample(instanceID, jooqDatabaseInteractor, targetTableManager, traversalPath, fieldToCollect);
//
//        // GATHER THE RESULTS FROM THE QUERY
//        Map<Object, List<Object>> valuesPerInstance = getMultiSetPerExampleID(targetTableManager, fieldToCollect, query);
//
////        // TRANSFORM THE RESULTS INTO FEATURE VALUES
////        Map<Object, Double> featureValuesPerInstance = getFeatureValuesPerExample(valuesPerInstance);
//
//        List<Object> valuesForExampleInstance = valuesPerInstance.get(instanceID);
//
//        if(valuesForExampleInstance.size() != 1){
//            System.out.println("categorical equality test for instance " + instanceID + " received " + valuesForExampleInstance.size() + " values; expected 1");
//            System.out.println("UNEXPECTED AND INCORRECT");
//        }
//
//        boolean featureValue = (Boolean) valuesForExampleInstance.get(0);
//
//        boolean testResult = booleanFeatureTest.evaluate(featureValue);
//        System.out.println("Does instance succeed test?: " + testResult);
//
//        return testResult;
//    }

//    public Set<Object> evaluate2Batch(BooleanFeatureTest booleanFeatureTest, Collection<Object> instanceIDs, JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager) throws InvalidKeyInfoException {
//
//        TraversalPath traversalPath = booleanFeatureTest.traversalPath;
//
//        Field fieldToCollect = this.getFieldToCollect(booleanFeatureTest, jooqDatabaseInteractor);
//
//        // BUILD THE NECESSARY QUERY
//        ResultQuery<Record> query = buildQueryGatheringInfoForExample(instanceIDs, jooqDatabaseInteractor, targetTableManager, traversalPath, fieldToCollect);
//
//        // GATHER THE RESULTS FROM THE QUERY
//        Map<Object, List<Object>> valuesPerInstance = getMultiSetPerExampleID(targetTableManager, fieldToCollect, query);
//
////        // TRANSFORM THE RESULTS INTO FEATURE VALUES
////        Map<Object, Double> featureValuesPerInstance = getFeatureValuesPerExample(valuesPerInstance);
//
//
//        Set<Object> instanceIDsForWhichTestSucceeds = new HashSet<>();
//        for (Object instanceID: instanceIDs) {
//            List<Object> valuesForExampleInstance = valuesPerInstance.get(instanceID);
//            if (valuesForExampleInstance.size() != 1) {
//                System.out.println("categorical equality test for instance " + instanceID + " received " + valuesForExampleInstance.size() + " values; expected 1");
//                System.out.println("UNEXPECTED AND INCORRECT");
//            }
//            boolean featureValue = (Boolean) valuesForExampleInstance.get(0);
//
//            boolean testResult = booleanFeatureTest.evaluate(featureValue);
//            System.out.println("Does instance succeed test?: " + testResult);
//            if(testResult){
//                instanceIDsForWhichTestSucceeds.add(instanceID);
//            }
//        }
//
//        return instanceIDsForWhichTestSucceeds;
//    }

    @Override
    protected Set<Object> evaluateSpecific(NodeTest nodeTest, Collection<Object> instanceIDs,
                                           Map<Object, List<Object>> multiSetPerInstance,
                                           Field fieldToCollect, Table tableToCollectFrom,
                                           boolean missingValueCorrespondsToSuccess) throws UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {

        BooleanFeatureTest booleanFeatureTest = (BooleanFeatureTest) nodeTest;
//        // TRANSFORM THE RESULTS INTO FEATURE VALUES
//        Map<Object, Double> featureValuesPerInstance = getFeatureValuesPerExample(valuesPerInstance);
        Set<Object> instanceIDsForWhichTestSucceeds = new HashSet<>();
        for (Object singleInstanceID: instanceIDs) {
            boolean testResult;


            List<Object> valuesForExampleInstance = multiSetPerInstance.get(singleInstanceID);
            if (valuesForExampleInstance.size() != 1) {
                System.out.println("categorical equality test for instance " + singleInstanceID + " received " + valuesForExampleInstance.size() + " values; expected 1");
                System.out.println("UNEXPECTED AND INCORRECT");
            }
            Object featureValue = valuesForExampleInstance.get(0);

            if(featureValue != null){
                testResult = booleanFeatureTest.evaluate(featureValue);
            }
            else{
                testResult = missingValueCorrespondsToSuccess;
            }
//            System.out.println("Does instance succeed test?: " + testResult);
            if(testResult){
                instanceIDsForWhichTestSucceeds.add(singleInstanceID);
            }
        }

        return instanceIDsForWhichTestSucceeds;
    }
}
