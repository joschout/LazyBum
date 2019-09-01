package learning;

import dataset.Example;
import learning.split.SplitCriterionCalculator;

import java.util.List;

public class InternalNodeInfo extends NodeInfo{


    private static final long serialVersionUID = 6839556447354335130L;
    public double score;
    public SplitCriterionCalculator splitCriterionCalculator;


    protected int nbOfUnevaluatableInstances;
    private boolean unevaluatableInstanceCorrespondsToSuccess;

    public InternalNodeInfo(List<Example> instances, double score, SplitCriterionCalculator splitCriterionCalculator,
                            int nbOfUnevaluatableInstances, boolean unevaluatableInstanceCorrespondsToSuccess) {
        super(instances);
        this.score = score;
        this.splitCriterionCalculator = splitCriterionCalculator;
        this.nbOfUnevaluatableInstances = nbOfUnevaluatableInstances;
        this.unevaluatableInstanceCorrespondsToSuccess = unevaluatableInstanceCorrespondsToSuccess;
    }


    public String toString(){
        return toString("");
    }

    public String toString(String indentation){
        String str = indentation + NodeInfo.df.format(score) +
                " (=" + splitCriterionCalculator.getType() + ", thr=" + NodeInfo.df.format(splitCriterionCalculator.getThreshold()) +")\n";
        str += indentation + "MVs: " + nbOfUnevaluatableInstances + "/" + super.nbOfExamples + " (" + df.format((double) nbOfUnevaluatableInstances /nbOfExamples * 100.0) + "%)";

        if(nbOfUnevaluatableInstances > 0){
            if(unevaluatableInstanceCorrespondsToSuccess){
                str += "\t to yes";
            } else{
                str += "\t to no";
            }
        }
        str += "\n";

        str += super.toString(indentation);
        return str;
    }

    public boolean shouldMissingValueCorrespondToSuccess(){
        return unevaluatableInstanceCorrespondsToSuccess;
    }
}
