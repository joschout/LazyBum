package learning.testing;

import feature.featuretable.FeatureColumn;
import feature.tranform.FieldTransformerEnumInterface;
import graph.TraversalPath;
import org.jooq.Field;
import utils.PrettyPrinter;

import javax.annotation.Nullable;
import java.util.Optional;

public class NumericalFeatureTestGenerator extends TestGenerator {

    private NumericalComparisonGenerator numericalComparisonGenerator;

    private NodeTest parentTest;
    private Field field;
    @Nullable private TraversalPath traversalPath;
    private FieldTransformerEnumInterface fieldTransformerEnum;

    public NumericalFeatureTestGenerator(NodeTest parentTest,
                                         @Nullable TraversalPath traversalPath, FeatureColumn<Number> numericFeatureColumn) {
        this.parentTest = parentTest;

        numericalComparisonGenerator = new NumericalComparisonGenerator(
                numericFeatureColumn.getFeatureValuePerInstance());
        this.field = numericFeatureColumn.getOriginalField();
        this.traversalPath = traversalPath;

        Optional optionalTransform = numericFeatureColumn.getTransformation();
        if(optionalTransform.isPresent()){
            this.fieldTransformerEnum = numericFeatureColumn.getTransformation().get();
            if(VERBOSE){
                System.out.println("NumericalFeatureTestGenerator for field " + field.getName() + " using transform " + fieldTransformerEnum);
                System.out.println("\tusing boundary values: " + PrettyPrinter.listToCSVString(numericalComparisonGenerator.getBoundaryValues()));
            }

        } else{
            this.fieldTransformerEnum = null;
            if(VERBOSE){
                System.out.println("NumericalFeatureTestGenerator for field " + field.getName() + " without transformation");
                System.out.println("\tusing boundary values: " + PrettyPrinter.listToCSVString(numericalComparisonGenerator.getBoundaryValues()));
            }
        }


    }

    @Override
    public boolean hasNext() {
        return numericalComparisonGenerator.hasNext();
    }

    @Override
    public NumericalFeatureTest next() {
        NumericalComparison numericalComparison = numericalComparisonGenerator.next();
        return new NumericalFeatureTest(parentTest, numericalComparison, traversalPath, field, fieldTransformerEnum
                );
    }
}
