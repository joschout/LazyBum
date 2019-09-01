package weka;

import config.ProgramConfiguration;
import config.ProgramConfigurationOption;
import database.DatabaseConnection;
import database.JOOQDatabaseInteractor;
import dataset.conversion.DatabaseToARFFConvertor;
import dataset.conversion.TableType;
import io.ConsoleAndFileWriter;
import io.OutputUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import utils.CurrentDate;
import utils.Timer;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instances;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Code from
 * https://www.programcreek.com/2013/01/a-simple-machine-learning-example-in-java/
 *
 */
public class WekaRunJ48 {


    public static class WekaRunJ48Settings{
        private TableType tableType;
        private String outputDir;
        private String foldDirRoot;
        private boolean removeTargetID;

        public WekaRunJ48Settings(TableType tableType, String rootDirString) throws IOException {
            this.tableType = tableType;


            this.outputDir = rootDirString + File.separator + CurrentDate.getCurrentDateAsString() + "_weka_" + tableType.toString();
            OutputUtils.createDirectoriesIfTheyDontExist(outputDir);

            this.foldDirRoot = "folds";
            this.removeTargetID = true;
        }

        public WekaRunJ48Settings(TableType tableType) throws IOException {
            this(tableType, "output");
        }
        public String getOverviewCSVFileName(){
            return outputDir + File.separator
                    + "overview_weka_j48_on_" + tableType.toString() + "_info.csv";
        }

        public TableType getTableType() {
            return tableType;
        }

        public String getOutputDir() {
            return outputDir;
        }

        public String getFoldDirRoot() {
            return foldDirRoot;
        }

        public boolean shouldRemoveTargetID() {
            return removeTargetID;
        }

        public void setOutputDir(String outputDir) {
            this.outputDir = outputDir;
        }
    }


    public static void main(String[] args) throws Exception {
//        run_converted_postgresql(args);
        run_converted_khiops(args);
    }



    public static void run_converted_postgresql(String[] args) throws Exception {

//        TableType tableType = TableType.ONEBM_TABLE;
////        String outputDir = "output_weka_mushroom_" + tableType.toString();
//        String outputDir = "output"+ File.separator + CurrentDate.getCurrentDateAsString() + "_weka_" + tableType.toString();
//        String overviewCSVFileName = outputDir + File.separator
//                + "overview_weka_j48_on_" + tableType.toString() + "_info.csv";
//        OutputUtils.createDirectoriesIfTheyDontExist(outputDir);
//        String foldDirRoot = "folds";
//        boolean removeTargetID = true;



        WekaRunJ48Settings wekaRunJ48Settings = new WekaRunJ48Settings(TableType.ONEBM_TABLE);
        Map<String, String> dataSetNameToPropertiesPathsMap = new LinkedHashMap<>();
//
//        dataSetNameToPropertiesPathsMap.put("mushroom", "data/mushroom/config.properties");
//        dataSetNameToPropertiesPathsMap.put("financial", "data/financial/config.properties");
//        dataSetNameToPropertiesPathsMap.put("hepatitis_std", "data/hepatitis_std/config.properties");
//        dataSetNameToPropertiesPathsMap.put("imdb_small", "data/imdb_small/config.properties");
//        dataSetNameToPropertiesPathsMap.put("university", "data/university/config.properties");
//        dataSetNameToPropertiesPathsMap.put("uw_cse", "data/uw_cse/config.properties");

//        dataSetNameToPropertiesPathsMap.put("financial_mod_target", "data/financial_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("hepatitis_std_mod_target", "data/hepatitis_std_mod_target/config.properties");
////
//        dataSetNameToPropertiesPathsMap.put("imdb_small_mod_target", "data/imdb_small_mod_target/config.properties");
        dataSetNameToPropertiesPathsMap.put("university_mod_target", "data/university_mod_target/config.properties");
        dataSetNameToPropertiesPathsMap.put("uw_cse_mod_target", "data/uw_cse_mod_target/config.properties");


        Map<String, Properties> datasetToPropertiesMap = new HashMap<>();

        for (String dataSetName : dataSetNameToPropertiesPathsMap.keySet()) {
            System.out.println("------");
            System.out.println("running " + dataSetName);

            String arffFileName = DatabaseToARFFConvertor.getARFFFileName(dataSetName, wekaRunJ48Settings.getTableType());
            String propertiesPath = dataSetNameToPropertiesPathsMap.get(dataSetName);
            ProgramConfiguration programConfiguration = new ProgramConfiguration(propertiesPath);

            Properties runProperties = runDataSet(
                    dataSetName, arffFileName,
                    programConfiguration,
                    wekaRunJ48Settings
            );
            datasetToPropertiesMap.put(dataSetName, runProperties);
            System.out.println("------");
        }

        WekaPropertiesPrinter wekaPropertiesPrinter = new WekaPropertiesPrinter(
                new ArrayList<>(dataSetNameToPropertiesPathsMap.keySet()),
                datasetToPropertiesMap);
        String csvLines = wekaPropertiesPrinter.runInfosToCSV();
        System.out.println(csvLines);

        try (BufferedWriter bufferedWriter= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(wekaRunJ48Settings.getOverviewCSVFileName()), "utf-8"))){

            bufferedWriter.write(csvLines);
        } catch (IOException i) {
            i.printStackTrace();
        }


    }

    public static void run_converted_khiops(String[] args) throws Exception {
        Map<String, String> dataSetNameToPropertiesPathsMap = new LinkedHashMap<>();
//        dataSetNameToPropertiesPathsMap.put("hepatitis_std_mod_target", "data/hepatitis_std_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("university_mod_target", "data/university_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("imdb_ijs_filtered_sample", "data/imdb_ijs_filtered_sample/config.properties");
        dataSetNameToPropertiesPathsMap.put("uw_cse_mod_target", "data/uw_cse_mod_target/config.properties");
        WekaRunJ48Settings wekaRunJ48Settings = new WekaRunJ48Settings(TableType.ONEBM_TABLE);

        String khiopsOutputDir = "output" + File.separator + CurrentDate.getCurrentDateAsString() + "_KHIOPS_weka_" + wekaRunJ48Settings.getTableType().toString();
        OutputUtils.createDirectoriesIfTheyDontExist(khiopsOutputDir);
        wekaRunJ48Settings.setOutputDir(khiopsOutputDir);


        Map<String, Properties> datasetToPropertiesMap = new HashMap<>();

        for (String dataSetName : dataSetNameToPropertiesPathsMap.keySet()) {
            System.out.println("------");
            System.out.println("running " + dataSetName);

            String arffFileName = "khiops/" + dataSetName + File.separator + "prop_table.arff";
            System.out.println(arffFileName);
            String propertiesPath = dataSetNameToPropertiesPathsMap.get(dataSetName);
            ProgramConfiguration programConfiguration = new ProgramConfiguration(propertiesPath);


            Properties runProperties = runDataSet(
                    dataSetName, arffFileName,
                    programConfiguration,
                    wekaRunJ48Settings.getOutputDir(),
                    wekaRunJ48Settings.getFoldDirRoot(),
                    wekaRunJ48Settings.removeTargetID
            );
            datasetToPropertiesMap.put(dataSetName, runProperties);
            System.out.println("------");
        }

        WekaPropertiesPrinter wekaPropertiesPrinter = new WekaPropertiesPrinter(
                new ArrayList<>(dataSetNameToPropertiesPathsMap.keySet()),
                datasetToPropertiesMap);
        String csvLines = wekaPropertiesPrinter.runInfosToCSV();
        System.out.println(csvLines);

        try (BufferedWriter bufferedWriter= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(wekaRunJ48Settings.getOverviewCSVFileName()), "utf-8"))){

            bufferedWriter.write(csvLines);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }



    public static Properties runDataSet(String dataSetName, ProgramConfiguration programConfiguration,
                                        WekaRunJ48Settings wekaRunJ48Settings) throws Exception {

        String arffFileName = DatabaseToARFFConvertor.getARFFFileName(dataSetName, wekaRunJ48Settings.getTableType());
        Properties runProperties = runDataSet(dataSetName, arffFileName,
                programConfiguration,
                wekaRunJ48Settings.getOutputDir(),
                wekaRunJ48Settings.getFoldDirRoot(),
                wekaRunJ48Settings.removeTargetID);
        return  runProperties;
    }



    public static Properties runDataSet(String dataSetName, String arffFileName,
                                        ProgramConfiguration programConfiguration,
                                        WekaRunJ48Settings wekaRunJ48Settings) throws Exception {
        return runDataSet(dataSetName, arffFileName, programConfiguration,
                wekaRunJ48Settings.getOutputDir(),
                wekaRunJ48Settings.getFoldDirRoot(),
                wekaRunJ48Settings.removeTargetID);
    }

    public static Properties runDataSet(String dataSetName, String arffFileName,
                                        ProgramConfiguration programConfiguration,
//                                        WekaRunJ48Settings wekaRunJ48Settings
                                        String outputDir,
                                        String foldDirRoot,
                                        boolean removeTargetID

    ) throws Exception {
        BufferedReader datafile = WekaUtils.readDataFile(arffFileName);
//        BufferedReader datafile = readDataFile("data/weather.txt");

        Path outputDirPath = Paths.get(outputDir);
        //create output directory if it does not yet exist
        if (!Files.exists(outputDirPath)) {
            Files.createDirectories(outputDirPath);
        }

        Connection databaseConnection = DatabaseConnection.getDatabaseConnection(programConfiguration);
        JOOQDatabaseInteractor jooqDatabaseInteractor = new JOOQDatabaseInteractor(
                databaseConnection,
                programConfiguration
        );

        String foldDir = foldDirRoot + File.separator + dataSetName;


        String targetIdString = programConfiguration.getConfigurationOption(ProgramConfigurationOption.TARGET_ID);
        String targetColumn = programConfiguration.getConfigurationOption(ProgramConfigurationOption.TARGET_COLUMN);

        Instances data = new Instances(datafile);
        data.setClass(data.attribute(targetColumn));

        Attribute targetIDAttribute = data.attribute(targetIdString);

        // Do 10-split cross validation
        WekaPredefinedFoldInstancesSplitter wekaPredefinedFoldInstancesSplitter = new WekaPredefinedFoldInstancesSplitter(
                foldDir, jooqDatabaseInteractor, programConfiguration,
                data, targetIDAttribute, removeTargetID);
        int nbOfFolds = wekaPredefinedFoldInstancesSplitter.getNbOfFolds();

        Instances[][] split = wekaPredefinedFoldInstancesSplitter.crossValidationPredefinedSplit();

        // Separate split into training and testing arrays
        Instances[] trainingSplits = split[0];
        Instances[] testingSplits = split[1];



//        Instances data = new Instances(datafile);
//        data.setClassIndex(data.numAttributes() - 1);
//
//        int nbOfFolds = 10;
//
//        // Do 10-split cross validation
//        Instances[][] split = WekaUtils.crossValidationSplit(data, nbOfFolds);
//
//        // Separate split into training and testing arrays
//        Instances[] trainingSplits = split[0];
//        Instances[] testingSplits = split[1];

        J48 j48 = new J48();

        // Use a set of classifiers
        Classifier[] models = {
               j48, // a decision tree
//                new PART(),
//                new DecisionTable(),//decision table majority classifier
//                new DecisionStump() //one-level decision tree
        };

        // Run for each model
//        for (int j = 0; j < models.length; j++) {

            // Collect every group of predictions for current model in a FastVector
        List<weka.classifiers.evaluation.Prediction> predictions = new ArrayList<>();


        List<Double> trainingTimes = new ArrayList<>();
        List<Double> testTimes = new ArrayList<>();
        double[] fold_accuracies = new double[trainingSplits.length];

        // For each training-testing split pair, train and test the classifier
        for (int i = 0; i < trainingSplits.length; i++) {

            String foldOutputFileStr = outputDir
                    + File.separator
                    +  "info_f" + i + ".txt";
            Path foldOutputFilePath = Paths.get(foldOutputFileStr);

            File foldOutputFile = foldOutputFilePath.toFile();
            ConsoleAndFileWriter foldOutputWriter = new ConsoleAndFileWriter(foldOutputFile);


            Classifier model = models[0];
//                Classifier model = models[j];
            Instances trainingSet = trainingSplits[i];
            Instances testingSet = testingSplits[i];

            Evaluation evaluation = new Evaluation(trainingSet);


            long trainTimeStart = System.nanoTime();
            model.buildClassifier(trainingSet);
            long trainTimeElapsed = System.nanoTime() - trainTimeStart;
            trainingTimes.add(trainTimeElapsed * Timer.convertNanoTimeToSeconds);


            long testTimeStart = System.nanoTime();
            evaluation.evaluateModel(model, testingSet);
            long testTimeElapsed = System.nanoTime() - testTimeStart;
            testTimes.add(testTimeElapsed * Timer.convertNanoTimeToSeconds);

            Evaluation validation = evaluation;

//                Evaluation validation = classify(models[j], trainingSplits[i], testingSplits[i]);

            double fold_accuracy = WekaUtils.calculateAccuracy(validation.predictions());
            fold_accuracies[i] = fold_accuracy;

            predictions.addAll(validation.predictions());

            // Uncomment to see the summary for each training-testing pair.
            foldOutputWriter.writeLine(models[0].toString());
            foldOutputWriter.writeLine("fold_acc: " + fold_accuracy);
            foldOutputWriter.writeLine("train time: " + trainTimeElapsed * Timer.convertNanoTimeToSeconds);
            foldOutputWriter.writeLine("test time: " + testTimeElapsed * Timer.convertNanoTimeToSeconds);
            foldOutputWriter.writeLine("total time: " + (trainTimeElapsed + testTimeElapsed) * Timer.convertNanoTimeToSeconds);

            foldOutputWriter.flush();
            foldOutputWriter.close();
        }


        String foldOverviewOutputFileStr = outputDir
                + File.separator
                +  "info_overview" + ".txt";
        Path foldOverviewOutputFilePath = Paths.get(foldOverviewOutputFileStr);
        File foldOverviewOutputFile = foldOverviewOutputFilePath.toFile();
        ConsoleAndFileWriter foldOverviewOutputWriter = new ConsoleAndFileWriter(foldOverviewOutputFile);


        double sumTrainTimes = trainingTimes.stream().reduce(0.0, Double::sum);
        double sumTestTimes = testTimes.stream().reduce(0.0, Double::sum);
        foldOverviewOutputWriter.writeLine(programConfiguration.toString());
        foldOverviewOutputWriter.writeLine("===");
        foldOverviewOutputWriter.writeLine("sum train times: " + sumTrainTimes);
        foldOverviewOutputWriter.writeLine("sum test times: " + sumTestTimes);
        foldOverviewOutputWriter.writeLine("");
        foldOverviewOutputWriter.writeLine("avg train time: " + sumTrainTimes / trainingSplits.length);
        foldOverviewOutputWriter.writeLine("avg test time: " + sumTestTimes / trainingSplits.length);
        foldOverviewOutputWriter.writeLine("avg total time: " + (sumTrainTimes + sumTestTimes) / trainingSplits.length);
        foldOverviewOutputWriter.writeLine("");
        foldOverviewOutputWriter.writeLine("training_times: "+ trainingTimes.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));


        double avgTestTime = sumTestTimes / nbOfFolds;
        double avgTrainingTime = sumTrainTimes / nbOfFolds;
        double avgTotalTime = (sumTrainTimes + sumTestTimes) / nbOfFolds;

        // Calculate overall accuracy of current classifier on all splits
        double accuracy = WekaUtils.calculateAccuracy(predictions);


        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
        for (double v : fold_accuracies) {
            descriptiveStatistics.addValue(v);
        }
        double accuracy_check = descriptiveStatistics.getMean();


        Properties properties = new Properties();
        properties.setProperty(WekaInfoEnum.ACC.toString(), String.valueOf(
                accuracy
        ));
        properties.setProperty(WekaInfoEnum.TRAINING_TIME_SEC.toString(), String.valueOf(avgTrainingTime));
        properties.setProperty(WekaInfoEnum.TEST_TIME_SEC.toString(), String.valueOf(avgTestTime));
        properties.setProperty(WekaInfoEnum.TOTAL_TIME_SEC.toString(), String.valueOf(avgTotalTime));


        // Print current classifier's name and accuracy in a complicated,
        // but nice-looking way.
        foldOverviewOutputWriter.writeLine("Accuracy of " + models[0].getClass().getSimpleName() + ": "
                + String.format("%.2f%%", accuracy * 100.0)
                + "\n---------------------------------");

        foldOverviewOutputWriter.writeLine("acc_check: " + accuracy_check);
        foldOverviewOutputWriter.writeLine("fold_acc:" + Arrays.stream(fold_accuracies)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(",")));

        foldOverviewOutputWriter.flush();
        foldOverviewOutputWriter.close();
        return properties;
//        }
    }




}