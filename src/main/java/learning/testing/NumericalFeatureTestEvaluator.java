package learning.testing;

import feature.columntype.UnsupportedFieldTypeException;
import feature.tranform.UnsupportedFeatureTransformationException;
import org.jooq.Field;
import org.jooq.Table;

import java.util.*;

@SuppressWarnings("Duplicates")
public class NumericalFeatureTestEvaluator extends TestEvaluator {


    private boolean foundAnInstanceWithNoValue = false;

//    @Override
//    public boolean evaluate(Object instanceID, Object featureValue, NodeTest nodeTest) {
//        return false;
//    }


//    public boolean evaluate2(NumericalFeatureTest numericalFeatureTest, Object instanceID,
//                             JOOQDatabaseInteractor jooqDatabaseInteractor,TargetTableManager targetTableManager) throws InvalidKeyInfoException, UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {
//
//
//        System.out.println(numericalFeatureTest.toString());
//        TraversalPath traversalPath = numericalFeatureTest.traversalPath;
//
//        // the field to collect values for
//        Table tableToCollectFrom =
//                traversalPath.getLast().getDestination(jooqDatabaseInteractor);
//        String[] name_containing_underscore = numericalFeatureTest.unqualifiedFieldName.split("_");
//        String fieldName = name_containing_underscore[name_containing_underscore.length - 1];
//        Field fieldToCollect = tableToCollectFrom.field(fieldName);
//
//
//        // BUILD THE NECESSARY QUERY
//        ResultQuery<Record> query = buildQueryGatheringInfoForExample(instanceID, jooqDatabaseInteractor, targetTableManager, traversalPath, fieldToCollect);
//
//        // GATHER THE RESULTS FROM THE QUERY
//        Map<Object, List<Object>> multiSetPerInstance = getMultiSetPerExampleID(targetTableManager, fieldToCollect, query);
//
//        List<Object> instanceMultiSet = multiSetPerInstance.get(instanceID);
//        if(instanceMultiSet != null){
//            System.out.println("multiset: " + PrettyPrinter.listToCSVString(instanceMultiSet));
//        } else{
//            System.out.println("multiset: null");
//        }
//
//        Map<Object, Object> featureValuePerInstance = getFeatureValuePerExampleMap(numericalFeatureTest.transformation, fieldToCollect, multiSetPerInstance);
//
//        //NOTE: THERE MIGHT NOT EXIST A FEATURE VALUE FOR THE GIVEN EXAMPLE
//        boolean testResult = false;
//        Object instanceFeatureValue = featureValuePerInstance.get(instanceID);
//        System.out.println("feature value: " + instanceFeatureValue);
//        if(instanceFeatureValue != null){
//            testResult = numericalFeatureTest.evaluate(instanceFeatureValue);
//        } else{
//            System.out.println("NO VALUE FOR THIS INSTANCE; TEST FAILS BY DEFAULT");
//        }
//        System.out.println("Does instance succeed test?: " + testResult);
//
//        return testResult;
//    }

//    public Set<Object> evaluate2Batch(NumericalFeatureTest numericalFeatureTest, Collection<Object> instanceIDs, JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager) throws InvalidKeyInfoException, UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {
//
//
//        Field fieldToCollect = this.getFieldToCollect(numericalFeatureTest, jooqDatabaseInteractor);
//        Map<Object, List<Object>> multiSetPerInstance = buildAndExecuteQueryToGetMultiSetPerInstance(
//                numericalFeatureTest, instanceIDs, jooqDatabaseInteractor, targetTableManager, fieldToCollect);
//
//        Map<Object, Object> featureValuePerInstance = getFeatureValuePerExampleMap(numericalFeatureTest.transformation, fieldToCollect, multiSetPerInstance);
//
//        //NOTE: THERE MIGHT NOT EXIST A FEATURE VALUE FOR THE GIVEN EXAMPLE
//
//        Set<Object> instanceIDsForWhichTestSucceeds = new HashSet<>();
//        for (Object instanceID: instanceIDs){
//            boolean testResult = false;
//            Object instanceFeatureValue = featureValuePerInstance.get(instanceID);
//            System.out.println("instanceID" + instanceID.toString() +", feature value: " + instanceFeatureValue);
//            if(instanceFeatureValue != null){
//                testResult = numericalFeatureTest.evaluate(instanceFeatureValue);
//            } else{
//                System.out.println("NO VALUE FOR THIS INSTANCE; TEST FAILS BY DEFAULT");
//            }
//            System.out.println("Does instance succeed test?: " + testResult);
//            if(testResult){
//                instanceIDsForWhichTestSucceeds.add(instanceFeatureValue);
//            }
//        }
//
//        return instanceIDsForWhichTestSucceeds;
//    }

    @Override
    protected Set<Object> evaluateSpecific(NodeTest nodeTest, Collection<Object> instanceIDs,
                                           Map<Object, List<Object>> multiSetPerInstance,
                                           Field fieldToCollect, Table tableToCollectFrom,
                                           boolean missingValueCorrespondsToSuccess)
            throws UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {
        NumericalFeatureTest numericalFeatureTest = (NumericalFeatureTest) nodeTest;

        Map<Object, Object> featureValuePerInstance = getFeatureValuePerExampleMap(
                numericalFeatureTest.transformation, fieldToCollect, tableToCollectFrom, multiSetPerInstance);

        //NOTE: THERE MIGHT NOT EXIST A FEATURE VALUE FOR THE GIVEN EXAMPLE

        Set<Object> instanceIDsForWhichTestSucceeds = new HashSet<>();
        for (Object singleInstanceID: instanceIDs){
            boolean testResult;
            Object instanceFeatureValue = featureValuePerInstance.get(singleInstanceID);
//            System.out.println("instanceID" + singleInstanceID.toString() +", feature value: " + instanceFeatureValue);
            if(instanceFeatureValue != null){
                testResult = numericalFeatureTest.evaluate(instanceFeatureValue);
            } else{
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



//
//    private Map<Object, Object> getFeatureValuePerExampleMap(NumericalFeatureTest numericalFeatureTest, Field fieldToCollect,
//                                                             Map<Object, List<Object>> multiSetPerInstance) throws UnsupportedFieldTypeException, UnsupportedFeatureTransformationException {
//        ColumnFieldHandler columnFieldHandler = FieldTypeResolver.resolveColumn(fieldToCollect);
//        Map<Object, Object> featureValuePerInstance = null;
//        if(numericalFeatureTest.transformation == null){
//            // no transformation
//            for (Object instanceID : multiSetPerInstance.keySet()) {
//                List<Object> instanceMultiSet = multiSetPerInstance.get(instanceID);
//                if(instanceMultiSet.size() != 1){
//                    throw new UnsupportedFeatureTransformationException("Expected 1 value in each multiset, found (for a specific multiset): " + instanceMultiSet.size() + " values");
//                }
//                featureValuePerInstance.put(instanceID, instanceMultiSet.get(0));
//            }
//        } else{
//            featureValuePerInstance = columnFieldHandler.getFeatureValuesPerInstance(multiSetPerInstance, numericalFeatureTest.transformation);
//
//        }
//        return featureValuePerInstance;
//    }

//    private Map<Object, Double> getFeatureValuesPerExample(Map<Object, List<Object>> valuesPerInstance, NumericFeatureTransformationEnum transformation) throws UnsupportedFeatureTransformationException {
//
//
//        Map<Object, Double> featureValuesPerInstance = new HashMap<>();
//        for (Object instanceID : valuesPerInstance.keySet()) {
//            System.out.println("instance: " + instanceID.toString());
//            List<Object> values = valuesPerInstance.get(instanceID);
//            System.out.println(" \t" + values.stream().map(Object::toString).collect(Collectors.joining(",")));
//
//            NumericMultiSetTransformer<Double> numericMultiSetTransformer = new NumericMultiSetTransformer<>();
////            CategoricMultiSetTransformer<String> categoricMultiSetTransformer = new CategoricMultiSetTransformer<>();
//
//            boolean containsNull = false;
//            for (Object value : values) {// only add non-null values
//                if(value == null){
//                    containsNull = true;
//                } else{
//                    numericMultiSetTransformer.add((double)value);
//                }
//            }
//            if(containsNull){
//                System.out.println("FOUND NULL IN FOLLOWING MULISET:\n" + numericMultiSetTransformer.toString());
//            }
//
//            double featureValue = NumericFeatureTransformationEnum.get(numericMultiSetTransformer, transformation);
////            double featureValue = CategoricalFeatureTransformationEnum.get(categoricMultiSetTransformer, transformation);
//            System.out.println("feature value: " + featureValue);
//            featureValuesPerInstance.put(instanceID, featureValue);
//        }
//        return featureValuesPerInstance;
//    }


}
