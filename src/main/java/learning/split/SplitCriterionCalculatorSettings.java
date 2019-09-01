package learning.split;

import dataset.Example;

import java.util.List;

public interface SplitCriterionCalculatorSettings {

    public SplitCriterionCalculator getSplitCriterionCalculator(List<Example> instances);

}
