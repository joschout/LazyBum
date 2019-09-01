package learning.testing;

import feature.columntype.UnsupportedFieldTypeException;
import feature.tranform.UnsupportedFeatureTransformationException;
import org.jooq.Field;
import org.jooq.Table;

import java.util.*;

public class ExistsInMultiSetTestEvaluator extends TestEvaluator {
    @Override
    protected Set<Object> evaluateSpecific(
            NodeTest nodeTest, Collection<Object> instanceIDs,
            Map<Object, List<Object>> multiSetPerInstance, Field fieldToCollect, Table tableToCollectFrom,
            boolean missingValueCorrespondsToSuccess) throws UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {

        ExistsInMultiSetTest existsInMultiSetTest = (ExistsInMultiSetTest) nodeTest;

        Set<Object> instanceIDsForWhichTestSucceeds = new HashSet<>();
        for (Object singleInstanceID: instanceIDs) {
            List<Object> valuesForExampleInstance = multiSetPerInstance.get(singleInstanceID);


            boolean testResult;
            if(valuesForExampleInstance != null) {
                Object valueToCheckForExistence = existsInMultiSetTest.getTransformation().getValueToCheckForExistence();
                testResult = valuesForExampleInstance.contains(valueToCheckForExistence);
            } else{

                testResult = missingValueCorrespondsToSuccess;
            }




//            boolean testResult = existsInMultiSetTest.evaluate(featureValue);
//            System.out.println("Does instance succeed test?: " + featureValue);
            if (testResult) {
                instanceIDsForWhichTestSucceeds.add(singleInstanceID);
            }
        }
        return instanceIDsForWhichTestSucceeds;
    }
}
