package learning.split;

import dataset.Example;
import feature.featuretable.FeatureColumn;
import lazybum.unevaluatables.UnevaluatableTestStrategy;
import learning.testing.ExamplePartition;
import learning.testing.NodeTest;
import research.JoinInfo;

import java.util.List;

/**
 * Contains the information about a split using a test on a set of training examples.
 */
public class SplitInfo {

    public NodeTest test;

//    public List<Example> leftExamples;
//    public List<Example> rightExamples;
    public ExamplePartition examplePartition;
    public UnevaluatableTestStrategy unevaluatableTestStrategy;

    public double score;
    public SplitCriterionCalculator splitCriterionCalculator;


//    public double scoreThreshold;
//    public SplitCriterionType splitCriterionType;


    public JoinInfo joinInfo;
    public FeatureColumn featureColumn;


    public SplitInfo(NodeTest test,
                     ExamplePartition examplePartition, UnevaluatableTestStrategy unevaluatableTestStrategy,
                     double score, SplitCriterionCalculator splitCriterionCalculator,
//                     double score, double scoreThreshold, SplitCriterionType splitCriterionType,
                     JoinInfo joinInfo, FeatureColumn featureColumn
                     ) {
        this.test = test;
        this.examplePartition = examplePartition;
        this.unevaluatableTestStrategy = unevaluatableTestStrategy;
        this.score = score;
        this.splitCriterionCalculator = splitCriterionCalculator;
//        this.scoreThreshold = scoreThreshold;
//        this.splitCriterionType = splitCriterionType;

        this.joinInfo = joinInfo;
        this.featureColumn = featureColumn;

    }

    public boolean hasPassingScore() {
        return this.score >= this.splitCriterionCalculator.getThreshold();
//        return this.score >= this.scoreThreshold;
    }

    public List<Example> getLeftExamples() {
        return unevaluatableTestStrategy.getExamplesLeftChild();
    }

    public List<Example> getRightExamples() {
        return unevaluatableTestStrategy.getExamplesRightChild();
    }
}
