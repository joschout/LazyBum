package learning.testing;

import database.JOOQDatabaseInteractor;
import graph.InvalidKeyInfoException;
import dataset.TargetTableManager;
import feature.columntype.UnsupportedFieldTypeException;
import feature.tranform.ExistInMultiSetTransform;
import feature.tranform.UnsupportedFeatureTransformationException;
import graph.TraversalPath;
import org.jooq.Field;

import java.util.Collection;
import java.util.Set;

public class ExistsInMultiSetTest extends NodeTest {


    private ExistInMultiSetTransform transformation;

    public ExistsInMultiSetTest(NodeTest parentTest, TraversalPath traversalPath, Field field,
                                ExistInMultiSetTransform transform) {
        super(parentTest, traversalPath, field);
        this.transformation = transform;
    }

    @Override
    public boolean evaluate(Object featureValue) {
//        System.out.println("NOT IMPLEMENTED IN ExistsInMultiSetTest");
        return (boolean) featureValue;
    }

    @Override
    public String toString(String indentation) {
        String str = this.getTraversalPath().map(traversalPath -> traversalPath.toStringCompact(indentation)+ "\n").orElse("");
        str += indentation + transformation.toString() + " for " + unqualifiedFieldName;
        return str;
    }

    @Override
    public Set<Object> evaluateBatch(
            Collection<Object> instanceIDs,
            JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager,
            boolean missingValueCorrespondsToSuccess) throws UnsupportedFeatureTransformationException, UnsupportedFieldTypeException, InvalidKeyInfoException {
        ExistsInMultiSetTestEvaluator existsInMultiSetTestEvaluator = new ExistsInMultiSetTestEvaluator();
        return existsInMultiSetTestEvaluator
                .evaluate2Batch(
                        this, instanceIDs, jooqDatabaseInteractor, targetTableManager, missingValueCorrespondsToSuccess);
    }

    public ExistInMultiSetTransform getTransformation() {
        return transformation;
    }
}
