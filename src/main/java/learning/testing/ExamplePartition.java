package learning.testing;

import dataset.Example;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the partition of instances in a node split on a specific test.
 * Examples instances can
 * - succeed the test
 * - fail the test
 * - have a missing value, so the test cannot be applied to them.
 *
 * When calculating a split criterion, we choose to only use the examples that succeed and fail the test (i.e. the instances with missing values are not used for evaluating the split).
 *
 * But when splitting the node, the missing value instances need to be propagated. We choose to propagate them the the child node with the largest number of instances.
 *
 * Created by joschout.
 */
public class ExamplePartition {


    public final List<Example> examplesSucceedingTest;
    public final List<Example> examplesFailingTest;
    public final List<Example> examplesUnevaluatableByTest;

    public ExamplePartition() {
        examplesSucceedingTest = new ArrayList<>();
        examplesFailingTest = new ArrayList<>();
        examplesUnevaluatableByTest = new ArrayList<>();
    }



    public int getNbOfExamplesUnevaluatableByTest(){
        return examplesUnevaluatableByTest.size();
    }

    public double getFractionOfMissingValues(){
        return (double) examplesUnevaluatableByTest.size() /
                (examplesSucceedingTest.size() + examplesFailingTest.size());
    }



}
