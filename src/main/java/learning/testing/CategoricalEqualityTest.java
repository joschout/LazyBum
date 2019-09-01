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

public class CategoricalEqualityTest<T> extends NodeTest{

    public T comparisonValue;

    public CategoricalEqualityTest(NodeTest parentTest, TraversalPath traversalPath,
                              Field field, T comparisonValue) {
        super(parentTest, traversalPath, field);

        this.comparisonValue = comparisonValue;
    }

    @Override
    public boolean evaluate(Object featureValue) {
        return featureValue.equals(comparisonValue);
    }


    public String toString(String indentation){

        String str = this.getTraversalPath()
                .map(traversalPath -> traversalPath.toStringCompact(indentation)+ "\n")
                .orElse("");
        str += indentation + String.valueOf(unqualifiedFieldName) + " == " + String.valueOf(comparisonValue) +"?" ;
        return  str;
//                + indentation + field.getName() + " == " + comparisonValue.toString() +"?" ;
    }

    @Override
    public Set<Object> evaluateBatch(Collection<Object> instanceIDs,
                                     JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager,
                                     boolean missingValueCorrespondsToSuccess)
            throws UnsupportedFeatureTransformationException, UnsupportedFieldTypeException, InvalidKeyInfoException {

        CategoricalEqualityTestEvaluator<T> categoricalEqualityTestEvaluator = new CategoricalEqualityTestEvaluator<>();
        return categoricalEqualityTestEvaluator.evaluate2Batch(
                this, instanceIDs,
                jooqDatabaseInteractor, targetTableManager,
                missingValueCorrespondsToSuccess);
    }

//    @Override
//    public boolean evaluate(Object instanceID, JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager) throws InvalidKeyInfoException, UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {
//        CategoricalEqualityTestEvaluator<T> categoricalEqualityTestEvaluator = new CategoricalEqualityTestEvaluator<>();
//        return categoricalEqualityTestEvaluator.evaluate2(this, instanceID, jooqDatabaseInteractor, targetTableManager);
//    }

    @Override
    public String toString() {
        return toString("");
    }



}
