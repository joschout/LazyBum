package lazybum.main.onebmtable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import config.ProgramConfiguration;
import config.ProgramConfigurationOption;
import database.JOOQDatabaseInteractor;

import graph.InvalidKeyInfoException;
import dataset.Example;
import dataset.FoldDataSetSplitter;
import dataset.InvalidDataSplitException;
import dataset.TargetTableManager;
import feature.columntype.UnsupportedFieldTypeException;
import feature.tranform.UnsupportedFeatureTransformationException;
import graph.GraphTraversalException;
import io.LazyBumDecisionTreeIO;
import lazybum.*;
import lazybum.main.setup.SetupBuilder;
import lazybum.main.setup.SetupResultWrapper;
import learning.InvalidTreeBuilderException;
import learning.InvalidTreeException;
import learning.Prediction;
import learning.split.InvalidSplitCriterionException;
import learning.split.NoSplitInfoFoundException;
import org.jooq.Table;
import dataset.ExampleIDFoldPersistor;
import dataset.FoldInfo;
import prediction.MajorityPredictor;
import utils.CurrentDate;
import utils.PrettyPrinter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static utils.CleanUp.cleanUp;

/**
 * Created by joschout.
 */
@SuppressWarnings("Duplicates")
public class TenFoldCVDecisionTreePredictionOnOneBMTableMain {

    public static boolean shouldRelearn = true;
    public static double convertNanoTimeToSeconds = 1.0 / 1000_000_000;
    public static int nbOfFolds = 10;

    public static boolean useFoldsOnFile = true;


    public static String rootDir = "output"+ File.separator + CurrentDate.getCurrentDateAsString()
            + "_run_lazybum_10cv_onebmtable";


    public static void main(String[] args) throws IOException, ClassNotFoundException {


        String foldRootDir = "folds";

        Map<String, String> dataSetNameToPropertiesPathsMap = new LinkedHashMap<>();

//        dataSetNameToPropertiesPathsMap.put("cora", "data/cora/config.properties");
//        dataSetNameToPropertiesPathsMap.put("facebook", "data/facebook/config.properties");
//        dataSetNameToPropertiesPathsMap.put("genes", "data/genes/config.properties");
//        dataSetNameToPropertiesPathsMap.put("hepatitis_std", "data/hepatitis_std/config.properties");
//        dataSetNameToPropertiesPathsMap.put("imdb_small", "data/imdb_small/config.properties");
//        dataSetNameToPropertiesPathsMap.put("uw_cse", "data/uw_cse/config.properties");
//        dataSetNameToPropertiesPathsMap.put("webkb", "data/webkb/config.properties");
//
//        dataSetNameToPropertiesPathsMap.put("financial", "data/financial/config.properties");
//        dataSetNameToPropertiesPathsMap.put("university", "data/university/config.properties");
//        dataSetNameToPropertiesPathsMap.put("imdb_movielens", "data/imdb_movielens/config.properties");


//        dataSetNameToPropertiesPathsMap.put("hepatitis_std", "data/hepatitis_std/config.properties");
//        dataSetNameToPropertiesPathsMap.put("imdb_small", "data/imdb_small/config.properties");
//        dataSetNameToPropertiesPathsMap.put("uw_cse", "data/uw_cse/config.properties");
////
//        dataSetNameToPropertiesPathsMap.put("financial", "data/financial/config.properties");
//        dataSetNameToPropertiesPathsMap.put("university", "data/university/config.properties");

        dataSetNameToPropertiesPathsMap.put("financial_mod_target", "data/financial_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("hepatitis_std_mod_target", "data/hepatitis_std_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("imdb_small_mod_target", "data/imdb_small_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("uw_cse_mod_target", "data/uw_cse_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("university_mod_target", "data/university_mod_target/config.properties");




//        dataSetNameToPropertiesPathsMap.put("imdb_movielens", "data/imdb_movielens/config.properties");


        Map<String, RunInfo> dataSetNameToRunInfoMap = new HashMap<>();

        for (String dataSetName : dataSetNameToPropertiesPathsMap.keySet()) {
            String propertiesPath = dataSetNameToPropertiesPathsMap.get(dataSetName);

            System.out.println("Start of " + propertiesPath);
            RunInfo runInfo = runTest(dataSetName, propertiesPath, foldRootDir);
            dataSetNameToRunInfoMap.put(dataSetName, runInfo);
            System.out.println("End of " + propertiesPath);
            System.out.println("========================================================");
            System.out.println("========================================================");
            System.out.println("========================================================");
        }



//        String[] dataSetNames = new String[dataSetNameToPropertiesPathsMap.keySet().size()];


        String[] dataSetNames = dataSetNameToPropertiesPathsMap.keySet().toArray(new String[0]);
        Arrays.sort(dataSetNames);

        RunInfoCSVStringBuilder runInfoCSVStringBuilder = new RunInfoCSVStringBuilder(dataSetNames, dataSetNameToRunInfoMap);

        String overviewCSVFileName = rootDir + File.separator + "overview_datasets_10fCV_onebmtable.csv";

        try (BufferedWriter bufferedWriter= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(overviewCSVFileName), "utf-8"))){

            bufferedWriter.write(runInfoCSVStringBuilder.runInfosToCSV());
        } catch (IOException i) {
            i.printStackTrace();
        }



        // note: get the csv line here by extracting the infor from the runinfo objects
        // note: the runinfo objects are saved to Json, so there is no need to rerun the tests to get the information in the correct format



    }

    public static RunInfo runTest(String dataSetName, String propertiesPathStr, String foldRootDir) throws IOException, ClassNotFoundException {
        long startTime = System.nanoTime();
//        String propertiesPathStr = "data/webshop/config.properties";

        Logger logger = Logger.getLogger("m");

        BufferedWriter outputFileWriter = null;

        try {

            SetupBuilder setupBuilder = SetupBuilder.setupBuilderForOneBMTable();
            Optional<SetupResultWrapper> optionalSetupInfo = setupBuilder.setup(dataSetName, propertiesPathStr);
            if (optionalSetupInfo.isEmpty()){
                throw new Exception("cannot start execution, setup info is empty");
            }
            SetupResultWrapper setupResultWrapper = optionalSetupInfo.get();

            ProgramConfiguration programConfiguration = setupResultWrapper.programConfiguration;
            TargetTableManager targetTableManager = setupResultWrapper.targetTableManager;
//            LazyBumTreeBuilder treeBuilder = setupResultWrapper.treeBuilder;

//            treeBuilder.getStopCriterion().setMaxDepth(5);


            JOOQDatabaseInteractor jooqDatabaseInteractor = setupResultWrapper.jooqDatabaseInteractor;
            Connection databaseConnection = setupResultWrapper.connection;
            Set<Object> possibleLabels = setupResultWrapper.possibleLabels;

            String outputDirStr = rootDir + File.separator +
                    programConfiguration.getConfigurationOption(ProgramConfigurationOption.SCHEMA);
            String foldDir = foldRootDir + File.separator + dataSetName;

            Path outputDirPath = Paths.get(outputDirStr);
            //create output directory if it does not yet exist
            if (!Files.exists(outputDirPath)) {
                Files.createDirectories(outputDirPath);
            }

            List<Example> allExamples = targetTableManager.getInstancesAsExamples();
            
            FoldDataSetSplitter<Example> foldDataSetSplitter;
            ExampleIDFoldPersistor exampleIDFoldPersistor = new ExampleIDFoldPersistor();
            if(!useFoldsOnFile) {
                foldDataSetSplitter = new FoldDataSetSplitter<Example>(nbOfFolds, allExamples);
                exampleIDFoldPersistor.persist(foldDataSetSplitter, outputDirStr);
            } else{
                foldDataSetSplitter = exampleIDFoldPersistor.loadFoldDataSetSplitter(foldDir, allExamples);
            }

            boolean noOverlappingFolds = foldDataSetSplitter.sanityCheck();
            if(! noOverlappingFolds){
                throw new InvalidDataSplitException("overlapping folds");
            }

            List[] folds = foldDataSetSplitter.getFolds();

            FoldInfo[] foldInfos = new FoldInfo[folds.length];

            for (int i = 0; i < folds.length; i++) {

                System.out.println("--- start fold " + i + " of " + nbOfFolds + "for " + propertiesPathStr + " ---");
                foldInfos[i] = doFold(i, foldDataSetSplitter, possibleLabels,
                        setupResultWrapper.getFreshLazyBumTreeBuilder(), outputDirStr,
                        programConfiguration, jooqDatabaseInteractor, targetTableManager);
                System.out.println("--- end fold " + i + " of " + nbOfFolds + "for " + propertiesPathStr + " ---");
            }



            long stopTime = System.nanoTime();

            String outputFileStr = outputDirStr
                    + File.separator
                    +  "info_average" + nbOfFolds +"folds"+ ".txt";
            Path outputFilePath = Paths.get(outputFileStr);


            RunInfo runInfo = new RunInfo(
                    dataSetName, programConfiguration,
                    propertiesPathStr, nbOfFolds,
                    possibleLabels, allExamples.size(),
                    foldDataSetSplitter.getFoldSize(),
                    startTime, stopTime,
                    foldInfos);

            File outputFile = outputFilePath.toFile();
            outputFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));

            outputFileWriter.write(runInfo.toString());
            outputFileWriter.flush();
            outputFileWriter.close();



            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String runInfoJson =  gson.toJson(runInfo);
            String runInfoJsonFileStr = outputDirStr
                    + File.separator
                    + "runinfo" + nbOfFolds + "folds.json";
            Path runInfoJsonFilePath = Paths.get(runInfoJsonFileStr);
            File runInfoJsonFile = runInfoJsonFilePath.toFile();
            outputFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(runInfoJsonFile), "utf-8"));
            outputFileWriter.write(runInfoJson);


            setupResultWrapper.close();

            return runInfo;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(outputFileWriter != null) {
                outputFileWriter.flush();
                outputFileWriter.close();
            }
        }
        return null;
    }


    public static FoldInfo doFold(int foldNb, FoldDataSetSplitter foldDataSetSplitter,
                                  Set<Object> possibleLabels, LazyBumTreeBuilder treeBuilder,
                                  String outputDirStr, ProgramConfiguration programConfiguration,
                                  JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager) throws NoSplitInfoFoundException, InvalidTreeBuilderException, GraphTraversalException, UnsupportedFieldTypeException, InvalidSplitCriterionException, UnsupportedFeatureTransformationException, IOException, ClassNotFoundException, InvalidKeyInfoException, InvalidTreeException {

        long startTimeForFold = System.nanoTime();
        List<Example> validationExampleSet = foldDataSetSplitter.getValidationSet(foldNb);
        List<Example> trainingExampleSet = foldDataSetSplitter.getTrainingSet(foldNb);
        List<Object> instanceIDsValidationSet = validationExampleSet.stream().map(example -> example.instanceID).collect(Collectors.toList());

        LazyBumDecisionTree decisionTree;


        String decisionTreeFileStr = outputDirStr
                + File.separator + "decisionTreeRoot" + "_f" + foldNb + ".ser";


        long startTimeForLearning = System.nanoTime();
        if (shouldRelearn) {
            decisionTree = new LazyBumDecisionTree();
            decisionTree.fit(trainingExampleSet, treeBuilder);
            System.out.println(decisionTree.toString());

            LazyBumDecisionTreeIO.writeDecisionTreeRootNodeToFile(decisionTree, decisionTreeFileStr);

        } else {
            if(Files.notExists(Paths.get(decisionTreeFileStr))){
                throw new FileNotFoundException(decisionTreeFileStr);
            }
            decisionTree = LazyBumDecisionTreeIO.readDecisionTreeFromRootNodeFile(decisionTreeFileStr);
            System.out.println(decisionTree.toString());
        }
        long stopTimeForLearning = System.nanoTime();
        long totalTimeForLearning = stopTimeForLearning - startTimeForLearning;


        String outputFileStr = outputDirStr
                + File.separator
                + "info" + "_f" + foldNb + ".txt";
        Path outputFilePath = Paths.get(outputFileStr);


        BufferedWriter outputFileWriter = null;

        File outputFile = outputFilePath.toFile();
        outputFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));

        outputFileWriter.write(programConfiguration.toString());
        outputFileWriter.newLine();
        outputFileWriter.write(decisionTree.toString());
        outputFileWriter.newLine();

//        prediction
        long startTimeForPrediction = System.nanoTime();

        Map<Object, Prediction> testSetInstanceIDSToPredictions = decisionTree.predictAsBatch(instanceIDsValidationSet, jooqDatabaseInteractor, targetTableManager);
        int truePositiveCount = 0;
        double testSetSize = validationExampleSet.size();


        for (Example testExample : validationExampleSet) {
            Prediction prediction = testSetInstanceIDSToPredictions.get(testExample.instanceID);
            Object predictedLabel = prediction.getValue();
            Object actualLabel = testExample.label;
            if (actualLabel.equals(predictedLabel)) {
                truePositiveCount++;
            }
        }

        long stopTimeForPrediction = System.nanoTime();
        long totalTimeForPrediction = stopTimeForPrediction - startTimeForPrediction;

        long stopTimeForFold = System.nanoTime();
        long totalTimeForFold = stopTimeForFold - startTimeForFold;
        double accuracy = truePositiveCount / testSetSize;

        MajorityPredictor majorityPredictor = new MajorityPredictor(trainingExampleSet);
        double majorityPredictionAccuracy = majorityPredictor.majorityPredictionAccuracy(validationExampleSet);

        outputFileWriter.write("training set size: " + trainingExampleSet.size() + "\n");
//            outputFileWriter.newLine();
        outputFileWriter.write("test set size: " + testSetSize + "\n");
        outputFileWriter.write("possible labels: " + PrettyPrinter.listToCSVString(new ArrayList<>(possibleLabels)) + "\n");
        outputFileWriter.write("nb of possible labels: " + possibleLabels.size() + "\n");
        outputFileWriter.write("TPs: " + truePositiveCount + "\n");
        outputFileWriter.write("Acc: " + accuracy + "\n");
        outputFileWriter.write("total time (sec): " + totalTimeForFold * convertNanoTimeToSeconds + "\n");
        outputFileWriter.write("learning time (sec): " + totalTimeForLearning * convertNanoTimeToSeconds + "\n");
        outputFileWriter.write("prediction time (sec): " + totalTimeForPrediction * convertNanoTimeToSeconds +"\n");


        outputFileWriter.flush();
        outputFileWriter.close();
        //NOTE: CLEAN DATABASE
        List<Table<?>> tables = jooqDatabaseInteractor.getTables();
        cleanUp(tables, jooqDatabaseInteractor);

        int nbOfNodesInTree = LazyBumTreeNode.countNbOfNodes(decisionTree.getTreeRoot());

        FoldInfo foldInfo = new FoldInfo(accuracy, totalTimeForFold, totalTimeForLearning, totalTimeForPrediction, nbOfNodesInTree);

        foldInfo.setMajorityPredictionAcc(majorityPredictionAccuracy);
        return foldInfo;
    }



}
