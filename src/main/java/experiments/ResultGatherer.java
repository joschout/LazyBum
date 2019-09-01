package experiments;

import config.ProgramConfiguration;
import dataset.conversion.DatabaseToARFFConvertor;
import dataset.conversion.TableType;
import io.ConsoleAndFileWriter;
import lazybum.RunInfo;
import lazybum.main.LazyBumTenFoldCVDecisionTreePredictionMain;
import lazybum.main.setup.TraversalGraphExtensionStrategyEnum;
import onebm.InfoEnum;
import onebm.OneBMMain;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import utils.CurrentDate;
import weka.WekaInfoEnum;
import weka.WekaRunJ48;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by joschout.
 */
public class ResultGatherer {


    public static String rootDir = "output"+ File.separator + CurrentDate.getCurrentDateAsString() +"_gathered_results";

    public static int nb_of_runs = 1;


    private static String getRootDir(int iteration){
        return rootDir + '_' + iteration;
    }


    public static String getOutputDir(String dataSetName, int iteration) throws IOException {
        return getRootDir(iteration) + File.separator + dataSetName;
    }

    public static String getCSVFileStr(String dataSetName, int iteration) throws IOException {
        return getOutputDir(dataSetName, iteration)
                + File.separator
                +  "gathered_results"+ ".csv";
    }


    public static String getGatheredCSVFileStr(int iteration){
        return getRootDir(iteration) + File.separator + "gathered_results_multiple_datasets.csv";
    }

    public static void main(String[] args) throws Exception {


        System.out.println("START RESULT GATHERING...");

        Map<String, String> dataSetNameToPropertiesPathsMap = new LinkedHashMap<>();



        dataSetNameToPropertiesPathsMap.put("hepatitis_std_mod_target", "data/hepatitis_std_mod_target/config.properties");
        dataSetNameToPropertiesPathsMap.put("uw_cse_mod_target", "data/uw_cse_mod_target/config.properties");
        dataSetNameToPropertiesPathsMap.put("university_mod_target", "data/university_mod_target/config.properties");
        dataSetNameToPropertiesPathsMap.put("imdb_ijs_filtered_sample", "data/imdb_ijs_filtered_sample/config.properties");




        for (int i = 0; i < nb_of_runs; i++) {
            for (String dataSetName : dataSetNameToPropertiesPathsMap.keySet()) {
                String propertiesPath = dataSetNameToPropertiesPathsMap.get(dataSetName);
                runForSingleDataSet(dataSetName, propertiesPath, i);
            }

            // combine csv files?

            combineCSVFiles(dataSetNameToPropertiesPathsMap, i);
        }



    }



    public static void combineCSVFiles( Map<String, String> dataSetNameToPropertiesPathsMap, int iteration) throws IOException {
        Map<String, Iterator<CSVRecord>> dataSetNameToCSVRecordIteratorMap = new LinkedHashMap<>();

        CSVParser firstParser = null;

        for (String dataSetName : dataSetNameToPropertiesPathsMap.keySet()) {

            Reader reader = Files.newBufferedReader(Paths.get(getCSVFileStr(dataSetName, iteration)));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
            if(firstParser == null){
                firstParser = csvParser;
            }
            Iterator<CSVRecord> csvRecordIterator = csvParser.iterator();
            dataSetNameToCSVRecordIteratorMap.put(dataSetName, csvRecordIterator);
        }

        String[] header = new String[dataSetNameToCSVRecordIteratorMap.size() + 2];
        header[0] = "alg setting";
        header[1] = "measurement";
        int index = 2;
        for (String dataSetName : dataSetNameToCSVRecordIteratorMap.keySet()) {
            header[index] = dataSetName;
            index++;
        }

        Iterator<CSVRecord> firstIterator = firstParser.iterator();

        BufferedWriter writer = Files.newBufferedWriter(Paths.get(getGatheredCSVFileStr(iteration)));
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader(header));

        while(firstIterator.hasNext()){

            List<String> rowToWrite = new ArrayList<>();
            boolean rowNamesSet = false;

            for (String dataSetName : dataSetNameToCSVRecordIteratorMap.keySet()) {
                Iterator<CSVRecord> csvRecordIterator = dataSetNameToCSVRecordIteratorMap.get(dataSetName);


                CSVRecord record = csvRecordIterator.next();

                if(! rowNamesSet){
                    rowToWrite.add(record.get(0));
                    rowToWrite.add(record.get(1));
                    rowNamesSet = true;
                }
                rowToWrite.add(record.get(2));
            }
            csvPrinter.printRecord(rowToWrite.toArray());
        }
        csvPrinter.flush();
    }



    public static void runForSingleDataSet(String dataSetName, String propertiesPath, int iteration) throws Exception {



        String outputDirStr = getOutputDir(dataSetName, iteration);
        Path outputDirPath = Paths.get(outputDirStr);
        //create output directory if it does not yet exist
        if (!Files.exists(outputDirPath)) {
            Files.createDirectories(outputDirPath);
        }

        // test-based LazyBum
        // 10 fold CV

        Info infoTFCV_test_based = tenFoldCV_main_table(dataSetName, propertiesPath, TraversalGraphExtensionStrategyEnum.DTTEST_BASED, outputDirStr);
        Info infoTFCV_lazy_onebm = tenFoldCV_main_table(dataSetName, propertiesPath, TraversalGraphExtensionStrategyEnum.LAZY_ONEBM, outputDirStr);

        Properties properties = OneBMMain.runOneBM(dataSetName, propertiesPath);
        assert properties != null;
        String durationSecString =  properties.getProperty(InfoEnum.DURATION_SEC.toString());
        double onebm_table_build_s = Double.parseDouble(durationSecString);

        String onebm_nb_of_features_built_string = properties.getProperty(InfoEnum.NB_OF_FEATURES_BUILD.toString());
        int onebm_nb_of_features_built = Integer.parseInt(onebm_nb_of_features_built_string);



        // weka
        String arffOutputFilename_onebm_table = DatabaseToARFFConvertor.getARFFFileName(dataSetName, TableType.ONEBM_TABLE);
        DatabaseToARFFConvertor.convertDataSetToARFF(dataSetName, propertiesPath, TableType.ONEBM_TABLE, arffOutputFilename_onebm_table);

        String arffOutputFilename_target_table = DatabaseToARFFConvertor.getARFFFileName(dataSetName, TableType.TARGET_TABLE);
        DatabaseToARFFConvertor.convertDataSetToARFF(dataSetName, propertiesPath, TableType.TARGET_TABLE, arffOutputFilename_target_table);


        Info infoTFCV_weka_onebm_table = tenFoldCV_weka(dataSetName, arffOutputFilename_onebm_table, propertiesPath, TableType.ONEBM_TABLE, outputDirStr);
        Info infoTFCV_weka_target_table = tenFoldCV_weka(dataSetName, arffOutputFilename_target_table, propertiesPath, TableType.TARGET_TABLE, outputDirStr);



        String test_based_LazyBum = "test_based_LazyBum";
        String lazy_onebm_LazyBum = "lazy_onebm_LazyBum";

        String weka_on_target_table = "weka_on_target_table";
        String weka_on_OneBM_table = "weka_on_OneBM_table";
        String majority_prediction = "majority_prediction";

        String building_onebm_table = "building_onebm_table";
        String avg_building_onebm_table = "avg_building_onebm_table";

        String avg_acc_10fcv = "avg_acc_10fcv";
        String avg_train_time_s = "avg_train_time_s";

        String avg_train_time_plus_table_build_s = "avg_train_time_plus_table_build_s";


        String onebm_nb_of_features_built_output_string = "onebm_nb_of_non_target_features_built";


        String outputFileStr = getCSVFileStr(dataSetName, iteration);
        Path outputFilePath = Paths.get(outputFileStr);
        File outputFile = outputFilePath.toFile();

        ConsoleAndFileWriter foldOutputWriter = new ConsoleAndFileWriter(outputFile);
        foldOutputWriter.writeLine(test_based_LazyBum + "," + avg_acc_10fcv + "," + infoTFCV_test_based.avgAcc);
        foldOutputWriter.writeLine(lazy_onebm_LazyBum + "," + avg_acc_10fcv + "," + infoTFCV_lazy_onebm.avgAcc);
        foldOutputWriter.writeLine(weka_on_target_table + "," + avg_acc_10fcv + "," + infoTFCV_weka_target_table.avgAcc);
        foldOutputWriter.writeLine(weka_on_OneBM_table + "," + avg_acc_10fcv + "," + infoTFCV_weka_onebm_table.avgAcc);
        foldOutputWriter.writeLine(majority_prediction + "," + avg_acc_10fcv + "," + infoTFCV_test_based.avgMajorityAcc);

        foldOutputWriter.writeLine(test_based_LazyBum + "," + avg_train_time_s + "," + infoTFCV_test_based.learningTime_s);
        foldOutputWriter.writeLine(lazy_onebm_LazyBum + "," + avg_train_time_s + "," + infoTFCV_lazy_onebm.learningTime_s);

        foldOutputWriter.writeLine(building_onebm_table + "," + avg_building_onebm_table + "," + onebm_table_build_s);
        foldOutputWriter.writeLine(building_onebm_table + "," + onebm_nb_of_features_built_output_string + "," + onebm_nb_of_features_built);


        double avg_train_time_plus_table_build_s_weka_on_onebm_table = onebm_table_build_s + infoTFCV_weka_onebm_table.learningTime_s;

        foldOutputWriter.writeLine(weka_on_target_table + "," + avg_train_time_plus_table_build_s + "," + infoTFCV_weka_target_table.learningTime_s);
        foldOutputWriter.writeLine(weka_on_OneBM_table + "," + avg_train_time_plus_table_build_s + "," + avg_train_time_plus_table_build_s_weka_on_onebm_table);


        System.out.println("========================================================");
        System.out.println("========================================================");
        System.out.println("========================================================");


        foldOutputWriter.flush();
        foldOutputWriter.close();


    }




    private static Info tenFoldCV_main_table(String dataSetName, String propertiesPath, TraversalGraphExtensionStrategyEnum type,
        String outputDirStr
        ) throws IOException, ClassNotFoundException {


        LazyBumTenFoldCVDecisionTreePredictionMain.Settings settings = new LazyBumTenFoldCVDecisionTreePredictionMain.Settings(type, outputDirStr);

        RunInfo runInfo = LazyBumTenFoldCVDecisionTreePredictionMain.runTest(dataSetName, propertiesPath, settings);
        double avgAccPerFold = runInfo.getAvgAccPerFold();
        double avgMajorityPredictionAccPerFold = runInfo.getAvgMajorityPredictionAccPerFold();
        double avgLearningTimeInSec = runInfo.getAvgLearningTimePerFoldInSec();


//
//        double[] accPerFold = runInfo.getAccOfEachFold();
//        double[] learningTimePerFold = runInfo.getLearningTimeForEachFold();
//

        return new Info(avgLearningTimeInSec, avgAccPerFold, avgMajorityPredictionAccPerFold);
    }

    private static Info tenFoldCV_weka(String dataSetName, String arffFileName, String propertiesPath, TableType tableType, String outputDirStr) throws Exception {

        WekaRunJ48.WekaRunJ48Settings wekaRunJ48Settings = new WekaRunJ48.WekaRunJ48Settings(tableType, outputDirStr);

        ProgramConfiguration programConfiguration = new ProgramConfiguration(propertiesPath);
        Properties weka_run_properties = WekaRunJ48.runDataSet(dataSetName, arffFileName,
                programConfiguration, wekaRunJ48Settings);
        double avgAcc_weka = Double.parseDouble(weka_run_properties.getProperty(WekaInfoEnum.ACC.toString()));
        double avgLearningTimeSec_weka = Double.parseDouble(weka_run_properties.getProperty(WekaInfoEnum.TRAINING_TIME_SEC.toString()));

        return new Info(avgLearningTimeSec_weka, avgAcc_weka);
    }



    static class Info{
        double learningTime_s;
        double avgAcc;
        double avgMajorityAcc;
        int nbOfNonTargetFeaturesBuilt;


        Info(double learningTime_s, double avgAcc, double avgMajorityAcc) {
            this.learningTime_s = learningTime_s;
            this.avgAcc = avgAcc;
            this.avgMajorityAcc = avgMajorityAcc;
        }

        Info(double learningTime_s, double avgAcc) {
            this(learningTime_s, avgAcc, -1.0);
        }
        
    }

}
