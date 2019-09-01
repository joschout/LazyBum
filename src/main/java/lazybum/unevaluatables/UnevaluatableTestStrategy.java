package lazybum.unevaluatables;

import dataset.Example;
import learning.split.SplitCriterionCalculator;

import java.util.List;

public interface UnevaluatableTestStrategy {

    public boolean shouldUnevaluatableTestCorrespondToSuccess();

    public double score(SplitCriterionCalculator splitCriterionCalculator);

    public List<Example> getExamplesLeftChild();

    public List<Example> getExamplesRightChild();

}
