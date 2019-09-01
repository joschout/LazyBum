package prediction;

import dataset.Example;
import learning.NodeInfo;
import learning.Prediction;

import java.util.List;

/**
 * Created by joschout.
 */
public class MajorityPredictor {



    private Prediction majorityPrediction;

    private double testSetSize = 0.0;
    private int truePositiveCount = 0;

    public MajorityPredictor(List<Example> trainingExampleSet) {
        NodeInfo majorityPredictor = new NodeInfo(trainingExampleSet);
        this.majorityPrediction = majorityPredictor.predict();
    }

    public double majorityPredictionAccuracy(List<Example> validationExampleSet ){
        Object predictedLabel = majorityPrediction.getValue();

//        prediction
        testSetSize = validationExampleSet.size();
        for (Example testExample : validationExampleSet) {
            Object actualLabel = testExample.label;
            if (actualLabel.equals(predictedLabel)) {
                truePositiveCount++;
            }
        }

        double accuracy = truePositiveCount / testSetSize;
//        System.out.println("majority prediction: " + accuracy);
        return accuracy;
    }

    public double getTestSetSize() {
        return testSetSize;
    }

    public int getTruePositiveCount() {
        return truePositiveCount;
    }
}
