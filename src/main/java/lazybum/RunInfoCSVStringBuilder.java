package lazybum;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataset.FoldInfo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by joschout.
 */
public class RunInfoCSVStringBuilder {



    String[] dataSetNames;
    Map<String, RunInfo> dataSetNameToRunInfoMap;

    String csvSeparator = ",";


    public RunInfoCSVStringBuilder(String[] dataSetNames, Map<String, RunInfo> dataSetNameToRunInfoMap) {
        this.dataSetNames = dataSetNames;
        this.dataSetNameToRunInfoMap = dataSetNameToRunInfoMap;
    }

    public String runInfosToCSV(){

        String csv = "";

        csv += getHeader();

        RunInfoAccessor avgAccF = RunInfo::getAvgAccPerFold;
        String field = "avg acc (10 folds)";
        csv += getLine(field, avgAccF);

        RunInfoAccessor avgMajorityAccF = RunInfo::getAvgMajorityPredictionAccPerFold;
        String avgMajorityAccField = "avg major predict acc:";
        csv += getLine(avgMajorityAccField, avgMajorityAccF);

        RunInfoAccessor avgTotTimeSecF = RunInfo::getAvgTotalTimePerFoldInSec;
        RunInfoAccessor avgLearnTimeSecF = RunInfo::getAvgLearningTimePerFoldInSec;
        RunInfoAccessor avgPredictionTimeSecF = RunInfo::getAvgPredictionTimePerFoldInSec;


        RunInfoAccessor minNbOfDTNodesF = RunInfo::getMinNbOfNodes;
        RunInfoAccessor maxNbOfDTNodesF = RunInfo::getMaxNbOfNodes;
        RunInfoAccessor medianNbOfDTNodesF = RunInfo::getMedianNbOfNodes;

        String avgTotTimeField = "avg tot time/fold (s)";
        String avgLearnTimeField = "avg learn time/fold (s)";
        String avgPredictionTimeField = "avg predict time/fold (s)";

        String minNbOfDTNodesField = "min nb of DT nodes";
        String maxNbOfDTNodesField ="max nb of DT nodes";
        String medianNbOfDTNodesField = "median nb of DT nodes";

        csv += getLine(avgTotTimeField, avgTotTimeSecF);
        csv += getLine(avgLearnTimeField, avgLearnTimeSecF);
        csv += getLine(avgPredictionTimeField, avgPredictionTimeSecF);

        csv += getLine(minNbOfDTNodesField, minNbOfDTNodesF);
        csv += getLine(maxNbOfDTNodesField, maxNbOfDTNodesF);
        csv += getLine(medianNbOfDTNodesField, medianNbOfDTNodesF);

        return csv;

    }


    private String getHeader(){
        StringBuilder header = new StringBuilder();
        for(String dataSetName: dataSetNames){
            header.append(csvSeparator).append(dataSetName);
        }
        header.append("\n");
        return header.toString();
    }


    private String getLine(String firstField, RunInfoAccessor runInfoAccessFunc){

        StringBuilder avgAccLine = new StringBuilder(firstField);
        for(String dataSetName: dataSetNames){
            RunInfo dataSetRunInfo = dataSetNameToRunInfoMap.get(dataSetName);
            Object info  = runInfoAccessFunc.get(dataSetRunInfo);
            if(dataSetRunInfo != null){
                avgAccLine.append(csvSeparator).append(String.valueOf(info));
            } else{
                avgAccLine.append(csvSeparator);
            }
        }
        avgAccLine.append("\n");

        return avgAccLine.toString();
    }


//    private String getAvgAccLine(){
//        StringBuilder avgAccLine = new StringBuilder("avg accuracy (over 10 folds)");
//        for(String dataSetName: dataSetNames){
//            RunInfo dataSetRunInfo = dataSetNameToRunInfoMap.get(dataSetName);
//
//            if(dataSetRunInfo != null){
//                avgAccLine.append(csvSeparator).append(dataSetRunInfo.getAvgAccPerFold());
//            } else{
//                avgAccLine.append(csvSeparator);
//            }
//        }
//        return avgAccLine.toString();
//    }


    private interface RunInfoAccessor{
        public Object get(RunInfo runInfo);
    }

    public static void main(String[] args) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String runInfoJsonFileName = "output/hepatitis_std/runinfo10folds.json";

        String runInfoJson = "";

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(runInfoJsonFileName), "utf-8"))){
            String line;
            while ((line = bufferedReader.readLine()) != null){
                runInfoJson += line + "\n";
            }

        } catch (IOException i) {
            i.printStackTrace();
        }

        RunInfo runInfo = gson.fromJson(runInfoJson, RunInfo.class);

        String[] dataSetNames = new String[1];
        dataSetNames[0] = runInfo.dataSetName;

        Map<String, RunInfo> dataSetNameToRunInfoMap = new HashMap<>();
        dataSetNameToRunInfoMap.put(runInfo.dataSetName, runInfo);

        RunInfoCSVStringBuilder runInfoCSVStringBuilder = new RunInfoCSVStringBuilder(dataSetNames, dataSetNameToRunInfoMap);
        System.out.println(runInfoCSVStringBuilder.runInfosToCSV());


    }

    public static void main2(String[] args) {

        String dataSetName = "dataset";
        String[] dataSetNameArray = new String[1];
        dataSetNameArray[0] = dataSetName;

        RunInfo runInfoMock = new RunInfo();

        FoldInfo[] foldInfoArray = new FoldInfo[2];
        FoldInfo foldInfo1 = new FoldInfo();
        FoldInfo foldInfo2 = new FoldInfo();
        foldInfo1.accuracy = 0.25;
        foldInfo2.accuracy = 0.75;

        foldInfo1.totalTime = 25L;
        foldInfo2.totalTime = 75L;

        foldInfo1.predictionTime = 25L;
        foldInfo2.predictionTime = 75L;

        foldInfo1.learningTime = 25L;
        foldInfo2.learningTime = 75L;

        foldInfoArray[0] = foldInfo1;
        foldInfoArray[1] = foldInfo2;

        runInfoMock.foldInfos = foldInfoArray;

        Map<String, RunInfo> dataSetNameToRunInfoMap = new HashMap<>();
        dataSetNameToRunInfoMap.put(dataSetName, runInfoMock);

        RunInfoCSVStringBuilder runInfoCSVStringBuilder = new RunInfoCSVStringBuilder(dataSetNameArray, dataSetNameToRunInfoMap);
        String accLine = runInfoCSVStringBuilder.runInfosToCSV();
        System.out.println(accLine);
    }


}
