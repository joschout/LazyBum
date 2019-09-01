package learning.testing;

import feature.columntype.UnsupportedFieldTypeException;
import feature.tranform.UnsupportedFeatureTransformationException;
import org.jooq.Field;
import org.jooq.Table;

import java.util.*;


@SuppressWarnings("Duplicates")
public class CategoricalEqualityTestEvaluator<T> extends TestEvaluator {

    private boolean foundAnInstanceWithNoValue = false;

//    @Override
//    public boolean evaluate(Object instanceID, Object featureValue, NodeTest nodeTest) {
//        return false;
//    }

//    public boolean evaluate2(CategoricalEqualityTest<T> categoricalEqualityTest, Object instanceID,
//                             JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager) throws InvalidKeyInfoException, UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {
//
//
//        System.out.println(categoricalEqualityTest.toString());
//        System.out.println();
//
//        TraversalPath traversalPath = categoricalEqualityTest.traversalPath;
//
//        Field fieldToCollect = this.getFieldToCollect(categoricalEqualityTest, jooqDatabaseInteractor);
//
//        // BUILD THE NECESSARY QUERY
//        ResultQuery<Record> query = buildQueryGatheringInfoForExample(instanceID, jooqDatabaseInteractor, targetTableManager, traversalPath, fieldToCollect);
//
//        // GATHER THE RESULTS FROM THE QUERY
//        Map<Object, List<Object>> multiSetPerInstance = getMultiSetPerExampleID(targetTableManager, fieldToCollect, query);
//        Map<Object, Object> featureValuePerInstance = getFeatureValuePerExampleMap(null, fieldToCollect, multiSetPerInstance);
//
//        //NOTE: THERE MIGHT NOT EXIST A FEATURE VALUE FOR THE GIVEN EXAMPLE
//        boolean testResult = false;
//        Object instanceFeatureValue = featureValuePerInstance.get(instanceID);
//        if(instanceFeatureValue != null){
//            testResult = categoricalEqualityTest.evaluate(instanceFeatureValue);
//        } else{
//            System.out.println("NO VALUE FOR THIS INSTANCE; TEST FAILS BY DEFAULT");
//        }
//        System.out.println("Does instance succeed test?: " + testResult);
////        boolean testResult = cat.evaluate(featureValuesPerInstance.get(example.instanceID));
//        System.out.println("Does instance succeed test?: " + testResult);
//
//        return testResult;
//    }


    @Override
    protected Set<Object> evaluateSpecific(
            NodeTest nodeTest, Collection<Object> instanceIDs,
            Map<Object, List<Object>> multiSetPerInstance,
            Field fieldToCollect, Table tableOfFieldToCollect,
            boolean missingValueCorrespondsToSuccess) throws UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {

        CategoricalEqualityTest<T> categoricalEqualityTest = (CategoricalEqualityTest<T>) nodeTest;

        Map<Object, Object> featureValuePerInstance = getFeatureValuePerExampleMap(
                null, fieldToCollect, tableOfFieldToCollect, multiSetPerInstance);

        //NOTE: THERE MIGHT NOT EXIST A FEATURE VALUE FOR THE GIVEN EXAMPLE

        Set<Object> instanceIDsForWhichTestSucceeds = new HashSet<>();
        for (Object singleInstanceID: instanceIDs) {
            boolean testResult;
            Object instanceFeatureValue = featureValuePerInstance.get(singleInstanceID);
//            System.out.println("instanceID" + singleInstanceID.toString() +", feature value: " + instanceFeatureValue);
            if (instanceFeatureValue != null) {
                testResult = categoricalEqualityTest.evaluate(instanceFeatureValue);
            } else {
                testResult = missingValueCorrespondsToSuccess;


                if(! foundAnInstanceWithNoValue){
//                    System.out.println("NO VALUE FOR THIS INSTANCE; TEST FAILS BY DEFAULT");
                    System.out.println("AT LEAST ONE INSTANCE (" + String.valueOf(singleInstanceID)+ ")" +
                            " WITH NO VALUE FOR NODETEST " + nodeTest.toString().replace("\n" , ", ") + "" +
                            " ; TEST FAILS BY DEFAULT FOR THESE INSTANCES");
                    foundAnInstanceWithNoValue = true;
                }
            }
//            System.out.println("Does instance succeed test?: " + testResult);
            if(testResult){
                instanceIDsForWhichTestSucceeds.add(singleInstanceID);
            }
        }

        return instanceIDsForWhichTestSucceeds;

    }
}
