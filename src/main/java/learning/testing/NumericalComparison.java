package learning.testing;

import learning.split.NumericComparisonEnum;

import java.io.Serializable;

public class NumericalComparison<T extends Number> implements Serializable {

    private static final long serialVersionUID = -5099693329059648016L;
    private double boundaryValue;
    private NumericComparisonEnum numericComparisonOperator;


    public NumericalComparison(double boundaryValue, NumericComparisonEnum numericComparisonOperator) {
        this.boundaryValue = boundaryValue;
        this.numericComparisonOperator = numericComparisonOperator;
    }

    public double getBoundaryValue() {
        return boundaryValue;
    }

    public NumericComparisonEnum getNumericComparisonOperator() {
        return numericComparisonOperator;
    }

    public boolean evaluate(Object featureValue){
        if(featureValue == null){
            throw new UnsupportedOperationException("this method should never be called on a null");
        }
        return NumericComparisonEnum.compare(numericComparisonOperator, ((T) featureValue).doubleValue(), boundaryValue);
    }

    public String toString(){
        return "" + numericComparisonOperator.toString() + " " + boundaryValue;
    }


}
