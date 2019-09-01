package weka;


import java.util.List;
import java.util.Map;
import java.util.Properties;
/**
 * Created by joschout.
 */

public class WekaPropertiesPrinter {

    List<String> dataSetNames;
    Map<String, Properties> dataSetNameToPropertiesMap;

    String csvSeparator = ",";


    public WekaPropertiesPrinter(List<String> dataSetNames, Map<String, Properties> dataSetNameToPropertiesMap) {
        this.dataSetNames = dataSetNames;
        this.dataSetNameToPropertiesMap = dataSetNameToPropertiesMap;
    }




    public String runInfosToCSV(){

        String csv = "";

        csv += getHeader();

        String avgTestTime = "avg pred time (s)";
        String avgTrainingTime = "avg train time (s)";
        String avgTotalTime = "avg total time (s)";


        String avgAcc = "avg acc";
        
        csv += getLine(avgAcc, WekaInfoEnum.ACC);
        csv += getLine(avgTrainingTime, WekaInfoEnum.TRAINING_TIME_SEC);
        csv += getLine(avgTestTime, WekaInfoEnum.TEST_TIME_SEC);
        csv += getLine(avgTotalTime, WekaInfoEnum.TOTAL_TIME_SEC);

        return csv;

    }



    private String getLine(String firstField, WekaInfoEnum infoEnum){

        StringBuilder line = new StringBuilder(firstField);
        for(String dataSetName: dataSetNames){
            Properties properties= dataSetNameToPropertiesMap.get(dataSetName);

            String valueStr = properties.getProperty(infoEnum.toString());
            
            line.append(csvSeparator).append(String.valueOf(valueStr));
        }
        line.append("\n");

        return line.toString();
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
