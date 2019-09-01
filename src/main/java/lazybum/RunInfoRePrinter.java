package lazybum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by joschout.
 */
public class RunInfoRePrinter {



    public RunInfoRePrinter() {
    }

    public static void main(String[] args) {

        List<String> dataSetNamesList = new ArrayList<>();

        dataSetNamesList.add("hepatitis_std");
//        dataSetNames.add("imdb_small");
        dataSetNamesList.add("uw_cse");


        Map<String, RunInfo> dataSetNameToRunInfoMap = new HashMap<>();
        for (String dataSetName : dataSetNamesList) {
            String runInfoJsonFileName = "output_onebmtable/" + dataSetName + "/runinfo10folds.json";
            RunInfo runInfo = RunInfo.readRunInfoFromJSON(runInfoJsonFileName);
            dataSetNameToRunInfoMap.put(dataSetName, runInfo);
        }

        RunInfoCSVStringBuilder runInfoCSVStringBuilder = new RunInfoCSVStringBuilder((String[]) dataSetNamesList.toArray(), dataSetNameToRunInfoMap);
        System.out.println(runInfoCSVStringBuilder.runInfosToCSV());

    }




    public String toCSVLines(String runInfoJsonFileName){
        RunInfo runInfo = RunInfo.readRunInfoFromJSON(runInfoJsonFileName);

        String[] dataSetNames = new String[1];
        dataSetNames[0] = runInfo.dataSetName;

        Map<String, RunInfo> dataSetNameToRunInfoMap = new HashMap<>();
        dataSetNameToRunInfoMap.put(runInfo.dataSetName, runInfo);

        RunInfoCSVStringBuilder runInfoCSVStringBuilder = new RunInfoCSVStringBuilder(dataSetNames, dataSetNameToRunInfoMap);
        return  runInfoCSVStringBuilder.runInfosToCSV();

    }
}
