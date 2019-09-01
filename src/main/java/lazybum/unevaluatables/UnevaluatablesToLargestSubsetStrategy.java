package lazybum.unevaluatables;

import dataset.Example;
import learning.split.SplitCriterionCalculator;
import learning.testing.ExamplePartition;

import java.util.ArrayList;
import java.util.List;

public class UnevaluatablesToLargestSubsetStrategy implements UnevaluatableTestStrategy {

    private ExamplePartition examplePartition;

    public UnevaluatablesToLargestSubsetStrategy(ExamplePartition examplePartition) {
        this.examplePartition = examplePartition;
    }

    @Override
    public boolean shouldUnevaluatableTestCorrespondToSuccess() {
        return examplePartition.examplesSucceedingTest.size() > examplePartition.examplesFailingTest.size();
    }

    @Override
    public double score(SplitCriterionCalculator splitCriterionCalculator) {
        return splitCriterionCalculator.calculate(examplePartition.examplesSucceedingTest, examplePartition.examplesFailingTest);
    }

    /**
     * Get the examples propagated to the left child node. These always include the examples satisfying the test.
     * Should there be missing value examples AND should the number of examples succeeding the test be larger than the number of examples failing the test,
     * also propagate the missing value examples to the left child node.
     *
     * @return
     */
    @Override
    public List<Example> getExamplesLeftChild(){
        return getExamplesToPass(examplePartition.examplesSucceedingTest, examplePartition.examplesFailingTest, examplePartition.examplesUnevaluatableByTest);
    }

    /**
     * Get the examples propagated to right child node. These always include the examples failing the test.
     * Should there be missing value examples AND should the number of examples failing the test be larger than the number of examples succeeding the test,
     * also propagate the missing value examples to the right child node.
     *
     * @return
     */
    @Override
    public List<Example> getExamplesRightChild(){
        return getExamplesToPass(examplePartition.examplesFailingTest, examplePartition.examplesSucceedingTest, examplePartition.examplesUnevaluatableByTest);
    }


    private static List<Example> getExamplesToPass(List<Example> examplesCertainToPass, List<Example> examplesNotToPass, List<Example> missingValueExamples){
        if(missingValueExamples.isEmpty()
                || examplesCertainToPass.size() < examplesNotToPass.size()){
            return examplesCertainToPass;
        } else{
            System.out.println("Adding missing values to largest partition subset");
            int nbOfExamplesToPass = examplesCertainToPass.size() + missingValueExamples.size();
            List<Example> examplesToPassIncludingMissingValueExamples = new ArrayList<>(nbOfExamplesToPass);
            examplesToPassIncludingMissingValueExamples.addAll(examplesCertainToPass);
            examplesToPassIncludingMissingValueExamples.addAll(missingValueExamples);
            return examplesToPassIncludingMissingValueExamples;
        }
    }
}
