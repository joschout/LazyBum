package dataset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Example {

    public Object instanceID;
    public Object label;

    public Object getLabel(){
        return label;
    }

    public Example(Object instanceID, Object label){
        this.instanceID = instanceID;
        this.label = label;
    }

    public static Map<Object, Integer> calculateLabelCounts(List<Example> examples){
        Map<Object, Integer> labelCounts = new HashMap<>();

        for (Example example : examples) {
            Object label = example.label;
            labelCounts.put(label, labelCounts.getOrDefault(label,0) + 1);
        }
        return labelCounts;
    }

    public static Map<Object, Double> calculateLabelFrequencies(List<Example> examples){
        Map<Object, Integer> labelCounts = calculateLabelCounts(examples);
        Map<Object, Double> labelFrequencies = new HashMap<>();

        // normalize
        for(Object label: labelCounts.keySet()){
            labelFrequencies.put(label, (double) (labelCounts.get(label) / examples.size()));
        }
        return labelFrequencies;
    }

    @Override
    public String toString() {
        return "ex(id="+instanceID.toString()+ ", label="+label.toString()+")";
    }


    public static List<Object> getInstanceIDs(List<Example> examples){
        return examples.stream().map(example -> example.instanceID).collect(Collectors.toList());
    }
}
