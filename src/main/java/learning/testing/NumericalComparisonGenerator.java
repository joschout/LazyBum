package learning.testing;

import feature.featuretable.FeatureColumn;
import feature.tranform.NumericFeatureTransformationEnum;
import learning.split.NumericComparisonEnum;
import learning.split.NumericalSplitFinder;
import org.jooq.impl.DSL;

import java.util.Iterator;
import java.util.List;

/**
 * This is in a sense a finite state machine
 *
 * states:
 *  (boundaryValueIndex, numericalTestIndex):
 *      (0,0), (0,1), (0,2)
 *      (1,0), (1,1), (1,2)
 *      (2,0), (2,1), (2,2)
 *      (3,0), (3,1), (3,2)
 *      ...
 *
 * */
public class NumericalComparisonGenerator implements Iterator<NumericalComparison> {


    private List<Double> boundaryValues;
    private int currentBoundaryValueIndex;
    private int currentNumericalTestIndex;

    public NumericalComparisonGenerator(List<Number> featureValuesPerInstance){
        boundaryValues = NumericalSplitFinder.getBoundariesToTest(featureValuesPerInstance);

        currentBoundaryValueIndex = 0;
        currentNumericalTestIndex = 0;
    }

    public boolean hasNext() {
        return ! (currentBoundaryValueIndex  >= boundaryValues.size());
    }

    public NumericalComparison next() {
        NumericComparisonEnum currentNumericalTest = NumericComparisonEnum.values()[currentNumericalTestIndex];
        double currentBoundaryValue = boundaryValues.get(currentBoundaryValueIndex);

        NumericalComparison numericalComparison = new NumericalComparison(currentBoundaryValue, currentNumericalTest);

        proceedToNextState();
        return numericalComparison;

    }

    private void proceedToNextState(){
        if(currentNumericalTestIndex + 1 == NumericComparisonEnum.values().length){
            currentNumericalTestIndex = 0;
            currentBoundaryValueIndex ++;
        } else{
            currentNumericalTestIndex ++;
        }
    }


    public static void main(String[] args) {

        FeatureColumn<Number> featureColumn = new FeatureColumn<>(
                DSL.field("test_field"), NumericFeatureTransformationEnum.AVG);

        double[] featureValues = {-1.0, 0.0, 1.0};
        for (double featureValue : featureValues) {
            featureColumn.add(featureValue);
        }

        NumericalComparisonGenerator numericalComparisonGenerator = new NumericalComparisonGenerator(featureColumn.getFeatureValuePerInstance());

        while(numericalComparisonGenerator.hasNext()){
            NumericalComparison test = numericalComparisonGenerator.next();
            System.out.println(test.getBoundaryValue() + " " + test.getNumericComparisonOperator().toString());
        }
    }

    public List<Double> getBoundaryValues() {
        return boundaryValues;
    }
}
