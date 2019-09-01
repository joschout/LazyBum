package learning.split;

import dataset.Example;
import learning.testing.ExamplePartition;

import java.io.Serializable;
import java.util.List;

public abstract class SplitCriterionCalculator implements Serializable {

    private static final long serialVersionUID = -2520318535044698658L;

    public double calculate(List<List<Example>> splittedExampleInstances){
        if(splittedExampleInstances.size() != 2){
            throw new IllegalArgumentException("Did not receive a list containing exactly two lists of example instances");
        }
        return calculate(splittedExampleInstances.get(0), splittedExampleInstances.get(1));

    }

    public abstract double calculate(List<Example> examplesSatisfyingTest, List<Example> examplesNotSatisfyingTest);

    public abstract double calculate(ExamplePartition examplePartition);

    public abstract double getThreshold();

    public abstract SplitCriterionType getType();

}

