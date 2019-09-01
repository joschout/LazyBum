package dataset;

import java.io.Serializable;

public class FoldInfo implements Serializable {
    private static final long serialVersionUID = 7060860847793041350L;

    public double accuracy;
    public long totalTime;
    public long learningTime;
    public long predictionTime;

    public double timeSpentOnFeatureTableExtensionCalculations = 0.0;
    public double timeSpentOnFindingTheBestSplit = 0.0;

    public int nbOfDTNodes;

    public double majorityPredictionAcc = -1.0;



    public FoldInfo(){}

    public FoldInfo(double accuracy, long totalTime, long learningTime, long predictionTime, int nbOfDTNodes) {
        this.accuracy = accuracy;
        this.totalTime = totalTime;
        this.learningTime = learningTime;
        this.predictionTime = predictionTime;
        this.nbOfDTNodes = nbOfDTNodes;
    }

    public FoldInfo(double accuracy, long totalTime, long learningTime, long predictionTime, int nbOfDTNodes,
                    double timeSpentOnFeatureTableExtensionCalculations, double timeSpentOnFindingTheBestSplit) {
        this(accuracy, totalTime, learningTime, predictionTime, nbOfDTNodes);

        this.timeSpentOnFeatureTableExtensionCalculations = timeSpentOnFeatureTableExtensionCalculations;
        this.timeSpentOnFindingTheBestSplit = timeSpentOnFindingTheBestSplit;
    }


    public void setMajorityPredictionAcc(double acc){
        this.majorityPredictionAcc = acc;
    }
}
