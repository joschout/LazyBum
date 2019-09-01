package learning.split;

import dataset.Example;
import learning.testing.ExamplePartition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InformationGainCalculator extends SplitCriterionCalculator implements Serializable {

    private static final long serialVersionUID = -716928042700439627L;



    private final double threshold;
    private final double nbOfExamplesInTotalExampleSet;
    private final double entropyTotalExampleSet;
    private final Set<Object> possibleLabels;
//
//    public InformationGainCalculator(Set<Object> possibleLabels, List<Object> exampleLabels){
//        this(possibleLabels, exampleLabels, DEFAULT_THRESHOLD);
//    }

    public InformationGainCalculator(Set<Object> possibleLabels, List<Object> exampleLabels, double threshold) {
        this.possibleLabels = possibleLabels;
        this.entropyTotalExampleSet = entropy(exampleLabels);
        this.nbOfExamplesInTotalExampleSet = exampleLabels.size();
        this.threshold = threshold;
    }

    private double entropy(List<Object> exampleLabels){

        double nbOfExamples = exampleLabels.size();
        // entopy of the empty set is zero by definition
        if(nbOfExamples == 0.0){
            return 0.0;
        }

        double entropyValue = 0.0;

        // count the nb of times each label occurs in the set of examples
        Map<Object, Long> labelToLabelCountMap = exampleLabels.stream().collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        for(Object label: possibleLabels){
            long labelCount = labelToLabelCountMap.getOrDefault(label, 0L);

            if(labelCount != 0L){
                double labelFraction = labelCount / nbOfExamples;

                entropyValue -= labelFraction * Math.log(labelFraction) / Math.log(2);
            }
        }

        return entropyValue;
    }


    private double informationGainBinarySplit(List<Object> labelsOfSatisfyingExamples, List<Object> labelsOfUnsatisfyingExamples) {
        if(this.nbOfExamplesInTotalExampleSet == 0){
            return 0.0;
        }

        double infoGain = entropyTotalExampleSet;

        infoGain -= labelsOfSatisfyingExamples.size() / nbOfExamplesInTotalExampleSet * entropy(labelsOfSatisfyingExamples);
        infoGain -= labelsOfUnsatisfyingExamples.size() / nbOfExamplesInTotalExampleSet * entropy(labelsOfUnsatisfyingExamples);

        return infoGain;
    }


    private double informationGainArbitrarySplit(List<List<Object>> labelsPerSplit){
        if(this.nbOfExamplesInTotalExampleSet == 0){
            return 0.0;
        }

        double infoGain = entropyTotalExampleSet;

        for (List<Object> labelsOfOneSplitSubset : labelsPerSplit) {
            infoGain -= labelsOfOneSplitSubset.size() / nbOfExamplesInTotalExampleSet
                    * entropy(labelsOfOneSplitSubset);

        }
        return infoGain;
    }



    @Override
    public double calculate(List<Example> examplesSatisfyingTest, List<Example> examplesNotSatisfyingTest) {

        List<Object> labelsOfExamplesSatisfyingTest = examplesSatisfyingTest.stream().map(Example::getLabel).collect(Collectors.toList());
        List<Object> labelsOfExamplesNotSatisfyingTest = examplesNotSatisfyingTest.stream().map(Example::getLabel).collect(Collectors.toList());

        return informationGainBinarySplit(labelsOfExamplesSatisfyingTest, labelsOfExamplesNotSatisfyingTest);
    }

    public double calculateBasedOnThreeSubsets(ExamplePartition examplePartition){
        List<List<Object>> labelsPerSplitSubset = new ArrayList<>(3);

        List<Object> labelsOfExamplesSatisfyingTest = examplePartition.examplesSucceedingTest
                .stream().map(Example::getLabel).collect(Collectors.toList());
        List<Object> labelsOfExamplesNotSatisfyingTest = examplePartition.examplesFailingTest
                .stream().map(Example::getLabel).collect(Collectors.toList());
        List<Object> labelsOfExamplesNotEvaluatableInTest = examplePartition.examplesUnevaluatableByTest
                .stream().map(Example::getLabel).collect(Collectors.toList());

        labelsPerSplitSubset.add(labelsOfExamplesSatisfyingTest);
        labelsPerSplitSubset.add(labelsOfExamplesNotSatisfyingTest);
        labelsPerSplitSubset.add(labelsOfExamplesNotEvaluatableInTest);

        return informationGainArbitrarySplit(labelsPerSplitSubset);
    }

    public double calculateBasedOnBestInformationGain(ExamplePartition examplePartition){
        List<Object> succeedingTest = examplePartition.examplesSucceedingTest
                .stream().map(Example::getLabel).collect(Collectors.toList());
        List<Object> failingTest = examplePartition.examplesFailingTest
                .stream().map(Example::getLabel).collect(Collectors.toList());
        List<Object> unevaluatableOnTest = examplePartition.examplesUnevaluatableByTest
                .stream().map(Example::getLabel).collect(Collectors.toList());

        List<Object> succeedWithMissing = new ArrayList<>(succeedingTest.size() + unevaluatableOnTest.size());
        succeedWithMissing.addAll(succeedingTest);
        succeedWithMissing.addAll(unevaluatableOnTest);

        List<Object> failWithMissing = new ArrayList<>(failingTest.size() + unevaluatableOnTest.size());
        failWithMissing.addAll(failingTest);
        failWithMissing.addAll(unevaluatableOnTest);

        double igSucceedWithMissing = informationGainBinarySplit(succeedWithMissing, failingTest);
        double igFailWithMissing = informationGainBinarySplit(failWithMissing, succeedingTest);

        System.out.println("igSucceedWithMissing: " + igSucceedWithMissing + ", igFailWithMissing: " + igFailWithMissing);

        if(igSucceedWithMissing > igFailWithMissing){
            examplePartition.examplesSucceedingTest.addAll(
                    examplePartition.examplesUnevaluatableByTest
            );
            examplePartition.examplesUnevaluatableByTest.clear();
            // note: this is not sufficient. You need to store which way unevaluatable instances need to go in the internal node
            return igSucceedWithMissing;
        } else{
            examplePartition.examplesFailingTest.addAll(
                    examplePartition.examplesUnevaluatableByTest
            );
            examplePartition.examplesUnevaluatableByTest.clear();
            return igFailWithMissing;
        }

    }

    public double calculateBasedOnEvaluatableExamples(ExamplePartition examplePartition) {
        return calculate(examplePartition.examplesSucceedingTest, examplePartition.examplesFailingTest);
//        return calculate(examplePartition.examplesSucceedingTest, examplePartition.examplesFailingTest);
    }

    @Override
    public double calculate(ExamplePartition examplePartition) {
        return calculateBasedOnEvaluatableExamples(examplePartition);
//        return calculate(examplePartition.examplesSucceedingTest, examplePartition.examplesFailingTest);
    }

    @Override
    public double getThreshold() {
        return threshold;
    }

    @Override
    public SplitCriterionType getType() {
        return SplitCriterionType.INFORMATION_GAIN;
    }

}
