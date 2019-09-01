package onebm;

import org.apache.commons.math3.stat.StatUtils;

import java.io.*;
import java.util.*;

public class OneBMPropertiesPrinter {

    String[] dataSetNames;
    Map<String, List<Properties>> dataSetNameToPropertiesListMap;

    String csvSeparator = ",";


    public OneBMPropertiesPrinter(String[] dataSetNames, Map<String, List<Properties>> dataSetNameToPropertiesListMap) {
        this.dataSetNames = dataSetNames;
        this.dataSetNameToPropertiesListMap = dataSetNameToPropertiesListMap;
    }


    public static void main(String[] args) throws IOException {

        Map<String, String> dataSetNameToPropertiesPathsMap = new LinkedHashMap<>();

        dataSetNameToPropertiesPathsMap.put("cora", "data/cora/config.properties");
        dataSetNameToPropertiesPathsMap.put("facebook", "data/facebook/config.properties");
        dataSetNameToPropertiesPathsMap.put("genes", "data/genes/config.properties");
        dataSetNameToPropertiesPathsMap.put("hepatitis_std", "data/hepatitis_std/config.properties");
        dataSetNameToPropertiesPathsMap.put("imdb_small", "data/imdb_small/config.properties");
        dataSetNameToPropertiesPathsMap.put("uw_cse", "data/uw_cse/config.properties");
        dataSetNameToPropertiesPathsMap.put("webkb", "data/webkb/config.properties");

        Map<String, List<Properties>> datasetToPropertiesListMap = new HashMap<>();
        for (String dataSetName : dataSetNameToPropertiesPathsMap.keySet()) {
            List<Properties> propertiesList = new ArrayList<>();
            datasetToPropertiesListMap.put(dataSetName, propertiesList);
            // load the properties
            for (int i = 1; i <= OneBMMain.nbOfRuns; i++) {
                String propertiesRunFileName = OneBMMain.getPropertiesFileName(dataSetName, i);
                FileInputStream in = new FileInputStream(propertiesRunFileName);

                Properties properties = new Properties();
                properties.load(in);
                propertiesList.add(properties);
            }
        }

        String overviewCSVFileName = OneBMMain.outputRootDir + File.separator + "overview_onebmtablebuilding_run_times.csv";

        toOverviewCSVFile(datasetToPropertiesListMap, overviewCSVFileName);


    }

    public static void toOverviewCSVFile(Map<String, List<Properties>> datasetToPropertiesListMap, String overviewCSVFileName){

        String[] dataSetNames = datasetToPropertiesListMap.keySet().toArray(new String[0]);
        Arrays.sort(dataSetNames);

        OneBMPropertiesPrinter oneBMPropertiesPrinter = new OneBMPropertiesPrinter(dataSetNames, datasetToPropertiesListMap);
        String csvLines = oneBMPropertiesPrinter.runInfosToCSV();
        System.out.println(csvLines);



        try (BufferedWriter bufferedWriter= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(overviewCSVFileName), "utf-8"))){

            bufferedWriter.write(csvLines);
        } catch (IOException i) {
            i.printStackTrace();
        }


    }


    public String runInfosToCSV(){

        String csv = "";

        csv += getHeader();
        String avgTimeOneBMTableBuilding = "avg time onebm table build";
        csv += getLine(avgTimeOneBMTableBuilding, InfoEnum.DURATION_SEC);
        return csv;

    }



    private String getLine(String firstField, InfoEnum infoEnum){

        StringBuilder avgAccLine = new StringBuilder(firstField);
        for(String dataSetName: dataSetNames){
            List<Properties> propertiesList = dataSetNameToPropertiesListMap.get(dataSetName);

            double[] durations = new double[propertiesList.size()];
            for (int i = 0; i < propertiesList.size(); i++) {

                String durationSecString = propertiesList.get(i).getProperty(infoEnum.toString());
                durations[i] = Double.parseDouble(durationSecString);
            }
            double meanDuration = StatUtils.mean(durations);

            avgAccLine.append(csvSeparator).append(String.valueOf(meanDuration));
        }
        avgAccLine.append("\n");

        return avgAccLine.toString();
    }


    private String getHeader(){
        StringBuilder header = new StringBuilder();
        for(String dataSetName: dataSetNames){
            header.append(csvSeparator).append(dataSetName);
        }
        header.append("\n");
        return header.toString();
    }

}
