package lazybum.unevaluatables;

import dataset.Example;
import learning.split.SplitCriterionCalculator;
import learning.testing.ExamplePartition;

import java.util.ArrayList;
import java.util.List;
@SuppressWarnings("Duplicates")
public class HighestScoreIncreaseStrategy implements UnevaluatableTestStrategy{


    public static boolean VERBOSE = false;

    private ExamplePartition examplePartition;

    private List<Example> examplesLeftChild = null;
    private List<Example> examplesRightChild = null;
    private boolean shouldUnevaluatableTestCorrespondToSuccess;

    public HighestScoreIncreaseStrategy(ExamplePartition examplePartition) {
        this.examplePartition = examplePartition;
    }

    public double score(SplitCriterionCalculator splitCriterionCalculator){
        if(examplePartition.examplesUnevaluatableByTest.isEmpty()){
            examplesLeftChild = examplePartition.examplesSucceedingTest;
            examplesRightChild = examplePartition.examplesFailingTest;

            return splitCriterionCalculator.calculate(examplePartition.examplesSucceedingTest, examplePartition.examplesFailingTest);
        } else {
            List<Example> succeedWithMissing = new ArrayList<>(
                    examplePartition.examplesSucceedingTest.size() + examplePartition.examplesUnevaluatableByTest.size());
            succeedWithMissing.addAll(examplePartition.examplesSucceedingTest);
            succeedWithMissing.addAll(examplePartition.examplesUnevaluatableByTest);

            List<Example> failWithMissing = new ArrayList<>(
                    examplePartition.examplesFailingTest.size() + examplePartition.examplesUnevaluatableByTest.size());
            failWithMissing.addAll(examplePartition.examplesFailingTest);
            failWithMissing.addAll(examplePartition.examplesUnevaluatableByTest);

            double scoreSucceedWithMissing = splitCriterionCalculator.calculate(succeedWithMissing, examplePartition.examplesFailingTest);
            double scoreFailWithMissing = splitCriterionCalculator.calculate(failWithMissing, examplePartition.examplesSucceedingTest);

            if(VERBOSE) {
                System.out.println("scoreSucceedWithMissing: " + scoreSucceedWithMissing + ", scoreFailWithMissing: " + scoreFailWithMissing);
            }

            if (scoreSucceedWithMissing > scoreFailWithMissing) {
                examplesLeftChild = succeedWithMissing;
                examplesRightChild = examplePartition.examplesFailingTest;

                shouldUnevaluatableTestCorrespondToSuccess = true;

                return scoreSucceedWithMissing;
            } else {
                examplesLeftChild = examplePartition.examplesSucceedingTest;
                examplesRightChild = failWithMissing;

                shouldUnevaluatableTestCorrespondToSuccess = false;

                return scoreFailWithMissing;
            }
        }
    }



    @Override
    public boolean shouldUnevaluatableTestCorrespondToSuccess() {
        if(examplesLeftChild == null || examplesRightChild == null){
            throw new IllegalStateException("The examples to go to the left and right children have not been correctly instantiated");
        }
        return shouldUnevaluatableTestCorrespondToSuccess;
    }

    public List<Example> getExamplesLeftChild(){
        if(examplesLeftChild == null){
            throw new IllegalStateException("The list of examples to go to the left child has not been correctly instantiated");
        }
        return examplesLeftChild;
    }

    public List<Example> getExamplesRightChild(){
        if(examplesRightChild == null){
            throw new IllegalStateException("The list of examples to go to the right child has not been correctly instantiated");
        }
        return examplesRightChild;
    }


}
