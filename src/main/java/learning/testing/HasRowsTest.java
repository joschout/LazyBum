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

/**
 * Created by joschout.
 */
public class HasRowsTest extends NodeTest {


    public Set<Object> instancesHavingRows; // SHOULD ONLY BE INSTANTIATED DURING TREE BUILDING, NOT DURING PREDICTION

    public HasRowsTest(NodeTest parentTest, TraversalPath traversalPath, Field field, Set<Object> instancesHavingRows) {
        super(parentTest, traversalPath, field);
        this.instancesHavingRows = instancesHavingRows;
    }

    @Override
    public boolean evaluate(Object featureValue) {
        throw new UnsupportedOperationException("this method should never be called");
    }

    @Override
    public String toString(String indentation) {
        String str = this.getTraversalPath()
                .map(traversalPath -> traversalPath.toStringCompact(indentation)+ "\n")
                .orElse("");

        str += indentation + "has rows along path";
        return str;
    }

    @Override
    public Set<Object> evaluateBatch(
            Collection<Object> instanceIDs,
            JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager,
            boolean missingValueCorrespondsToSuccess) throws UnsupportedFeatureTransformationException, UnsupportedFieldTypeException, InvalidKeyInfoException {
        HasRowsTestEvaluator hasRowsTestEvaluator = new HasRowsTestEvaluator();
        return hasRowsTestEvaluator.evaluate2Batch(this, instanceIDs,
                jooqDatabaseInteractor, targetTableManager, missingValueCorrespondsToSuccess);
    }
}
