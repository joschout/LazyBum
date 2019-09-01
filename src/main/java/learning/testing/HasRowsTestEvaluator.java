package learning.testing;

import feature.columntype.UnsupportedFieldTypeException;
import feature.tranform.UnsupportedFeatureTransformationException;
import org.jooq.Field;
import org.jooq.Table;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HasRowsTestEvaluator extends TestEvaluator{
    @Override
    protected Set<Object> evaluateSpecific(
            NodeTest nodeTest,
            Collection<Object> instanceIDs,
            Map<Object, List<Object>> multiSetPerInstance,
            Field fieldToCollect,
            Table tableOfFieldToCollect,
            boolean missingValueCorrespondsToSuccess) throws UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {

        // special kind of test: the instances for which the test succeeds are the instances for which there are values
        return multiSetPerInstance.keySet();
    }
}
