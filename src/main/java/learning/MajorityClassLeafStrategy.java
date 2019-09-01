package learning;

import dataset.Example;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MajorityClassLeafStrategy extends LeafNodeInfo {

//    private Map<Object, Double> labelFrequencies;
//    private Map<Object, Integer> labelCounts;
    private Prediction majorityLabel;
    // nb of examples in this leaf
//    private int nbOfExamples;


    public MajorityClassLeafStrategy(){}

    public MajorityClassLeafStrategy(List<Example> instances){
        super(instances);

        Object maxLabel = Collections.max(labelCounts.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
        this.majorityLabel = new Prediction(maxLabel);

    }

    @Override
    public Prediction predict() {
        return this.majorityLabel;
    }

    public String toString(){
        return "[" + majorityLabel.toString() + "] [" + labelCounts.get(majorityLabel.getValue()).toString() + "/" + nbOfExamples + "]\n";
    }

    public String toString(String indentation){
        return this.toString()
                + super.toString(indentation);
    }





}
