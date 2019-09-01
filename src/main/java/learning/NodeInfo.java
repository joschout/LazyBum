package learning;

import dataset.Example;

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeInfo implements Serializable {

    public static DecimalFormat df = new DecimalFormat("##.###");
    static {
        df.setRoundingMode(RoundingMode.CEILING);
    }

    protected Map<Object, Integer> labelCounts;
    protected int nbOfExamples;

    protected Prediction majorityLabel;

    public NodeInfo(){
    }

    public NodeInfo(List<Example> instances){
        this.labelCounts = Example.calculateLabelCounts(instances);

        Object maxLabel = Collections.max(labelCounts.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
        this.majorityLabel = new Prediction(maxLabel);

//        Object maxLabel = Collections.max(labelCounts.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
//        this.majorityLabel = new Prediction(maxLabel);
        this.nbOfExamples = instances.size();
    }


    public Prediction predict() {
        return this.majorityLabel;
    }



    public String toString(){
        return toString("");
    }

    public String toString(String indentation){

//        String str = "";
        List<String> parts = labelCounts.keySet().stream().map(o -> {
            int count = labelCounts.get(o);
            return o.toString() + ": " + count + "/" + nbOfExamples + " (" + df.format((double)count/nbOfExamples * 100.0) + "%)";
        }).collect(Collectors.toList());

        String str = indentation + "label freqs: " + String.join(", ", parts) + "\n";


//        for (Object o : labelCounts.keySet()) {
//            int count = labelCounts.get(o);
//
//            str += indentation + o.toString() + ": " + count + "/" + nbOfExamples + " (" + df.format((double)count/nbOfExamples * 100.0) + "%), ";
//        }
        return str;
    }


    public String toStringAsLeaf(){
        return "[" + majorityLabel.toString() + "] [" + labelCounts.get(majorityLabel.getValue()).toString() + "/" + nbOfExamples + "]\n";
    }

    public String toStringAsLeaf(String indentation){
        return this.toStringAsLeaf()
                + this.toString(indentation);
    }



}
