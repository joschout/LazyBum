package learning.testing;

import database.JOOQDatabaseInteractor;
import graph.InvalidKeyInfoException;
import dataset.TargetTableManager;
import feature.columntype.UnsupportedFieldTypeException;
import feature.tranform.FieldTransformerEnumInterface;
import feature.tranform.UnsupportedFeatureTransformationException;
import graph.TraversalPath;
import org.jooq.Field;

import java.util.Collection;
import java.util.Set;

public class NumericalFeatureTest extends NodeTest {

    private NumericalComparison numericalComparison;

//    public Field field;
    public FieldTransformerEnumInterface transformation;


    public NumericalFeatureTest(NodeTest parentTest,
                                NumericalComparison numericalComparison, TraversalPath traversalPath,
                                Field field, FieldTransformerEnumInterface transformation) {
        super(parentTest, traversalPath, field);
        this.numericalComparison = numericalComparison;
        this.transformation = transformation;
    }

    @Override
    public boolean evaluate(Object featureValue) {
        return numericalComparison.evaluate(featureValue);
    }


    public String toString(String indentation) {
        String str = this.getTraversalPath()
                .map(traversalPath -> traversalPath.toStringCompact(indentation)+ "\n")
                .orElse("");

        if (transformation != null) {
            str += indentation + transformation.toString() + " of " + unqualifiedFieldName + " " + numericalComparison.toString();
        } else {
            str += indentation + "identity of " + unqualifiedFieldName + " " + numericalComparison.toString();
        }
       return str;
    }

//    @Override
//    public boolean evaluate(Object instanceID, JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager) throws InvalidKeyInfoException, UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {
//        NumericalFeatureTestEvaluator numericalFeatureTestEvaluator = new NumericalFeatureTestEvaluator();
//        return numericalFeatureTestEvaluator.evaluate2(this, instanceID, jooqDatabaseInteractor, targetTableManager);
//    }

    @Override
    public Set<Object> evaluateBatch(
            Collection<Object> instanceIDs,
            JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager,
            boolean missingValueCorrespondsToSuccess
    ) throws UnsupportedFeatureTransformationException, UnsupportedFieldTypeException, InvalidKeyInfoException {
        NumericalFeatureTestEvaluator numericalFeatureTestEvaluator = new NumericalFeatureTestEvaluator();
        return numericalFeatureTestEvaluator
                .evaluate2Batch(this, instanceIDs, jooqDatabaseInteractor, targetTableManager, missingValueCorrespondsToSuccess);
    }

    @Override
    public String toString() {
        return toString("");
    }
}
