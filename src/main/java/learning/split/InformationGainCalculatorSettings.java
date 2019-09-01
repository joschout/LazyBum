package learning.split;

import dataset.Example;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InformationGainCalculatorSettings implements SplitCriterionCalculatorSettings {


    public static final double DEFAULT_THRESHOLD = 0.01;

    public Set<Object> possibleLabels;
    public double threshold;


    public InformationGainCalculatorSettings(Set<Object> possibleLabels) {
        this(possibleLabels, DEFAULT_THRESHOLD);
    }


    public InformationGainCalculatorSettings(Set<Object> possibleLabels, double threshold) {
        this.possibleLabels = possibleLabels;
        this.threshold = threshold;
    }


    @Override
    public SplitCriterionCalculator getSplitCriterionCalculator(List<Example> instances) {
        List<Object> exampleLabels = instances.stream().map(Example::getLabel).collect(Collectors.toList());
        return new InformationGainCalculator(possibleLabels, exampleLabels, threshold);

    }
}
