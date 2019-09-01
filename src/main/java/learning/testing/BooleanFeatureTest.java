package learning.testing;

import database.JOOQDatabaseInteractor;
import graph.InvalidKeyInfoException;
import dataset.TargetTableManager;
import feature.columntype.UnsupportedFieldTypeException;
import feature.tranform.UnsupportedFeatureTransformationException;
import graph.TraversalPath;
import org.jooq.Field;

import java.util.Collection;
import java.util.Set;

public class BooleanFeatureTest  extends NodeTest {

    public BooleanFeatureTest(NodeTest parentTest, TraversalPath traversalPath,
                                Field field) {
        super(parentTest, traversalPath, field);
    }

    @Override
    public boolean evaluate(Object featureValue) {
        return (boolean) featureValue;
    }


    public String toString(String indentation){
        String str = this.getTraversalPath()
                .map(traversalPath -> traversalPath.toStringCompact(indentation)+ "\n")
                .orElse("");
        str += indentation + unqualifiedFieldName + " == true?" ;
        return str;
//                + indentation + field.getName() + " == true?" ;
    }

//    @Override
//    public boolean evaluate(Object instanceID, JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager) throws InvalidKeyInfoException, UnsupportedFeatureTransformationException {
//        BooleanFeatureTestEvaluator booleanFeatureTestEvaluator = new BooleanFeatureTestEvaluator();
//        return booleanFeatureTestEvaluator.evaluate2(this, instanceID, jooqDatabaseInteractor, targetTableManager);
//    }

    @Override
    public Set<Object> evaluateBatch(
            Collection<Object> instanceIDs,
            JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager,
            boolean missingValueCorrespondsToSuccess
    ) throws UnsupportedFeatureTransformationException, UnsupportedFieldTypeException, InvalidKeyInfoException {

        BooleanFeatureTestEvaluator booleanFeatureTestEvaluator = new BooleanFeatureTestEvaluator();
        return booleanFeatureTestEvaluator
                .evaluate2Batch(this, instanceIDs, jooqDatabaseInteractor, targetTableManager, missingValueCorrespondsToSuccess);
    }

    @Override
    public String toString() {
        return toString("");
    }
}
