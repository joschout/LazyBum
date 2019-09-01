package lazybum;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import config.ProgramConfiguration;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import dataset.FoldInfo;
import utils.PrettyPrinter;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RunInfo implements Serializable {

    private static final long serialVersionUID = 4224279348538230926L;
    public static double convertNanoTimeToSeconds = 1.0 / 1000_000_000;

    public String dataSetName;
    public ProgramConfiguration programConfiguration;

    public String propertiesPath;
    public int nbOfFolds;

    public Set<String> possibleLabelNames;
    public int totalNbOfExamples;
    public int foldSize;

    long startTime;
    long stopTime;

    public FoldInfo[] foldInfos;

    public RunInfo(){}

    public RunInfo(
            String dataSetName,
            ProgramConfiguration programConfiguration,
            String propertiesPath, int nbOfFolds,
            Set<Object> possibleLabels,
            int totalNbOfExamples,
            int foldSize,
            long startTime, long stopTime,
            FoldInfo[] foldInfos){
        this.dataSetName = dataSetName;
        this.programConfiguration = programConfiguration;

        this.propertiesPath = propertiesPath;
        this.nbOfFolds = nbOfFolds;

        this.possibleLabelNames = new HashSet<>(possibleLabels.size());
        possibleLabels.forEach(
                label -> this.possibleLabelNames.add(String.valueOf(label)));

        this.totalNbOfExamples = totalNbOfExamples;
        this.foldSize = foldSize;

        this.startTime = startTime;
        this.stopTime = stopTime;

        this.foldInfos = foldInfos;

    }


    public static RunInfo readRunInfoFromJSON(String runInfoJsonFileName){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String runInfoJson = "";

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(runInfoJsonFileName), "utf-8"))){
            String line;
            while ((line = bufferedReader.readLine()) != null){
                runInfoJson += line + "\n";
            }

        } catch (IOException i) {
            i.printStackTrace();
        }

        return gson.fromJson(runInfoJson, RunInfo.class);
    }


    public double getAvgAccPerFold(){
        double[] accuracyPerFold = Arrays.stream(foldInfos).mapToDouble(foldInfo -> foldInfo.accuracy).toArray();
        return StatUtils.mean(accuracyPerFold);
    }

    public double[] getAccOfEachFold() {
        return Arrays.stream(foldInfos).mapToDouble(foldInfo -> foldInfo.accuracy).toArray();
    }

    public double getAvgMajorityPredictionAccPerFold(){
        double[] majorityPredictionAccPerFold
                = Arrays.stream(foldInfos)
                .mapToDouble(
                        foldInfo -> foldInfo.majorityPredictionAcc).toArray();
        return StatUtils.mean(majorityPredictionAccPerFold);
    }


    public double getAvgTotalTimePerFoldInSec(){
        double[] totalTimePerFoldSec
                = Arrays.stream(foldInfos)
                .mapToDouble(
                        foldInfo -> foldInfo.totalTime * convertNanoTimeToSeconds).toArray();
        return StatUtils.mean(totalTimePerFoldSec);
    }

    public double getAvgLearningTimePerFoldInSec(){
        double[] learningTimePerFoldSec
                = Arrays.stream(foldInfos)
                .mapToDouble(
                        foldInfo -> foldInfo.learningTime * convertNanoTimeToSeconds).toArray();
        return StatUtils.mean(learningTimePerFoldSec);
    }

    public double[] getLearningTimeForEachFold(){
        return Arrays.stream(foldInfos)
                .mapToDouble(
                        foldInfo -> foldInfo.learningTime * convertNanoTimeToSeconds).toArray();
    }

    public double getAvgPredictionTimePerFoldInSec(){
        double[] predictionTimePerFoldSec
                = Arrays.stream(foldInfos)
                .mapToDouble(
                        foldInfo -> foldInfo.predictionTime * convertNanoTimeToSeconds).toArray();
        return StatUtils.mean(predictionTimePerFoldSec);
    }

    public double getAvgTimeSpentCalculatingSplit(){
        double[] timeSpentCalculatingSplitsPerFoldSec
                = Arrays.stream(foldInfos)
                .mapToDouble(
                        foldInfo -> foldInfo.timeSpentOnFindingTheBestSplit).toArray();
        return StatUtils.mean(timeSpentCalculatingSplitsPerFoldSec);
    }

    public double getAvgTimeSpentExtendingFeatureTable(){
        double[] timeSpentExtendingFeatureTablePerFoldSec
                = Arrays.stream(foldInfos)
                .mapToDouble(
                        foldInfo -> foldInfo.timeSpentOnFeatureTableExtensionCalculations).toArray();
        return StatUtils.mean(timeSpentExtendingFeatureTablePerFoldSec);
    }


    public ImmutableTriple<Integer, Integer, Integer> minMaxMedianNbOfDTNodes(){

        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
        Arrays.stream(foldInfos).forEach(
            foldInfo -> descriptiveStatistics.addValue((double) foldInfo.nbOfDTNodes));

        return new ImmutableTriple<>((int) descriptiveStatistics.getMin(), (int) descriptiveStatistics.getMax(), (int) descriptiveStatistics.getPercentile(50.0));
    }



    public double getMinNbOfNodes(){
        double[] nbOfNodes
                = Arrays.stream(foldInfos)
                .mapToDouble(
                        foldInfo -> foldInfo.nbOfDTNodes).toArray();
        return StatUtils.min(nbOfNodes);
    }

    public double getMaxNbOfNodes(){
        double[] nbOfNodes
                = Arrays.stream(foldInfos)
                .mapToDouble(
                        foldInfo -> foldInfo.nbOfDTNodes).toArray();
        return StatUtils.max(nbOfNodes);
    }

    public double getMedianNbOfNodes(){
        double[] nbOfNodes
                = Arrays.stream(foldInfos)
                .mapToDouble(
                        foldInfo -> foldInfo.nbOfDTNodes).toArray();
        return StatUtils.percentile(nbOfNodes, 50.0);
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(programConfiguration.toString() + "\n");

        builder.append("data set name:" + dataSetName + "\n");
        builder.append("nb of possible labels: " + possibleLabelNames.size() + "\n");
        builder.append("total nb of examples: " + totalNbOfExamples + "\n");
        builder.append("training set size: " + (totalNbOfExamples - foldSize) + "\n");
        builder.append("fold size (+-1): " + foldSize + "\n");
        builder.append("all accuracies:\n");

        double[] accuracyPerFold = Arrays.stream(foldInfos).mapToDouble(foldInfo -> foldInfo.accuracy).toArray();

        builder.append(PrettyPrinter.doubleArrayToCSVString(accuracyPerFold) + "\n");

        builder.append("average accuracy: " + StatUtils.mean(accuracyPerFold) + "\n");

        builder.append("total execution time (sec): " + (stopTime - startTime) * convertNanoTimeToSeconds + "\n");

        builder.append("avg total time per fold (sec): " + getAvgTotalTimePerFoldInSec() + "\n");
        builder.append("avg learning time per fold (sec): " + getAvgLearningTimePerFoldInSec() + "\n");
        builder.append("avg prediction time per fold (sec): " + getAvgPredictionTimePerFoldInSec() + "\n");

        builder.append("avg time spent calculating splits per fold (sec): " + getAvgTimeSpentCalculatingSplit() + "\n");
        builder.append("avg time spent extending feature table per fold (sec): " + getAvgTimeSpentExtendingFeatureTable() + "\n");

        ImmutableTriple<Integer, Integer, Integer> minMaxMedianNbOfDTNodes = minMaxMedianNbOfDTNodes();

        builder.append("min nb of tree nodes" + minMaxMedianNbOfDTNodes.getLeft() +  "\n");
        builder.append("max nb of tree nodes" + minMaxMedianNbOfDTNodes.getMiddle() + "\n");
        builder.append("median nb of tree nodes" + minMaxMedianNbOfDTNodes.getRight() + "\n");

        builder.append("accuracies_of_each_fold:" + Arrays.stream(getAccOfEachFold())
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(",")) + "\n");
        builder.append("learning_time_of_each_fold:" + Arrays.stream(getLearningTimeForEachFold())
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(",")) + "\n");

        return builder.toString();
    }
}
