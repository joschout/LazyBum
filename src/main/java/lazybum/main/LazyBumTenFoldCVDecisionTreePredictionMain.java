package lazybum.main;

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
import io.OutputUtils;
import lazybum.*;
import lazybum.main.setup.SetupBuilder;
import lazybum.main.setup.SetupResultWrapper;
import lazybum.main.setup.TraversalGraphExtensionStrategyEnum;
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

@SuppressWarnings("Duplicates" )
public class LazyBumTenFoldCVDecisionTreePredictionMain {

    public static double convertNanoTimeToSeconds = 1.0 / 1000_000_000;

    public static class Settings{

        private boolean shouldRelearn;
        private int nbOfFolds;

        private boolean useFoldsOnFile;
        private TraversalGraphExtensionStrategyEnum traversalGraphExtensionStrategyEnum;
        private String foldDir;

        private String rootDirString;
        private boolean verbose;


        public Settings(TraversalGraphExtensionStrategyEnum traversalGraphExtensionStrategyEnum, String rootDirString){

            this.verbose = false;

            this.shouldRelearn = true;
            this.nbOfFolds = 10;
            this.useFoldsOnFile = true;
            this.foldDir = "folds";

            this.traversalGraphExtensionStrategyEnum = traversalGraphExtensionStrategyEnum;

            this.rootDirString = rootDirString + File.separator + CurrentDate.getCurrentDateAsString() +"_lazybum_"
                    + traversalGraphExtensionStrategyEnum.toString().toLowerCase()
                    +"_10fcv_mod_target_tables"
            ;

        }


        private Settings(TraversalGraphExtensionStrategyEnum traversalGraphExtensionStrategyEnum) {
            this(traversalGraphExtensionStrategyEnum,
                    "output"
                    );
        }

        String getRootDir(){
            return rootDirString;
        }

        String getOverviewCSVFileName(){
            return getRootDir() + File.separator + "overview_datasets_10fCV_lazybumdts.csv";
        }


        boolean shouldRelearn() {
            return shouldRelearn;
        }

        int getNbOfFolds() {
            return nbOfFolds;
        }

        boolean useFoldsOnFile() {
            return useFoldsOnFile;
        }

        TraversalGraphExtensionStrategyEnum getTraversalGraphExtensionStrategyEnum() {
            return traversalGraphExtensionStrategyEnum;
        }

        String getFoldDir() {
            return foldDir;
        }
    }



    public static void main(String[] args) throws IOException, ClassNotFoundException {


        Settings settings = new Settings(TraversalGraphExtensionStrategyEnum.LAZY_ONEBM);


        Map<String, String> dataSetNameToPropertiesPathsMap = new LinkedHashMap<>();




        dataSetNameToPropertiesPathsMap.put("hepatitis_std_mod_target", "data/hepatitis_std_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("uw_cse_mod_target", "data/uw_cse_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("university_mod_target", "data/university_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("imdb_ijs_filtered_sample", "data/imdb_ijs_filtered_sample/config.properties");




        Map<String, RunInfo> dataSetNameToRunInfoMap = new HashMap<>();

        for (String dataSetName : dataSetNameToPropertiesPathsMap.keySet()) {
            String propertiesPath = dataSetNameToPropertiesPathsMap.get(dataSetName);

            System.out.println("Start of " + propertiesPath);
            RunInfo runInfo = runTest(dataSetName, propertiesPath, settings);
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

        String overviewCSVFileName = settings.getOverviewCSVFileName();

        try (BufferedWriter bufferedWriter= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(overviewCSVFileName), "utf-8"))){

            bufferedWriter.write(runInfoCSVStringBuilder.runInfosToCSV());
        } catch (IOException i) {
            i.printStackTrace();
        }
        // note: get the csv line here by extracting the infor from the runinfo objects
        // note: the runinfo objects are saved to Json, so there is no need to rerun the tests to get the information in the correct format
    }

    public static RunInfo runTest(String dataSetName, String propertiesPathStr, Settings settings) throws IOException, ClassNotFoundException {
        long startTime = System.nanoTime();

        Logger logger = Logger.getLogger("m");

        BufferedWriter outputFileWriter = null;

        try {
            SetupBuilder setupBuilder = SetupBuilder.setupBuilderForDatabase(
                    settings.getTraversalGraphExtensionStrategyEnum());
            Optional<SetupResultWrapper> optionalSetupInfo = setupBuilder.setup(dataSetName, propertiesPathStr);
            if (optionalSetupInfo.isEmpty()){
                throw new Exception("cannot start execution, setup info is empty");
            }
            SetupResultWrapper setupResultWrapper = optionalSetupInfo.get();

            ProgramConfiguration programConfiguration = setupResultWrapper.programConfiguration;
            TargetTableManager targetTableManager = setupResultWrapper.targetTableManager;
            JOOQDatabaseInteractor jooqDatabaseInteractor = setupResultWrapper.jooqDatabaseInteractor;
            Connection databaseConnection = setupResultWrapper.connection;
            Set<Object> possibleLabels = setupResultWrapper.possibleLabels;


            String outputDirStr = settings.getRootDir() + File.separator +
                    programConfiguration.getConfigurationOption(ProgramConfigurationOption.SCHEMA);
            Path outputDirPath = Paths.get(outputDirStr);
            //create output directory if it does not yet exist
            if (!Files.exists(outputDirPath)) {
                Files.createDirectories(outputDirPath);
            }

            List<Example> allExamples = targetTableManager.getInstancesAsExamples();


            String foldDirStr = settings.getFoldDir() + File.separator +
                    programConfiguration.getConfigurationOption(ProgramConfigurationOption.SCHEMA);
            OutputUtils.createDirectoriesIfTheyDontExist(foldDirStr);

            FoldDataSetSplitter<Example> foldDataSetSplitter;
            ExampleIDFoldPersistor exampleIDFoldPersistor = new ExampleIDFoldPersistor();
            if(!settings.useFoldsOnFile()) {
                 foldDataSetSplitter = new FoldDataSetSplitter<Example>(settings.getNbOfFolds(), allExamples);
                 exampleIDFoldPersistor.persist(foldDataSetSplitter, foldDirStr);
            } else{
                foldDataSetSplitter = exampleIDFoldPersistor.loadFoldDataSetSplitter(foldDirStr, allExamples);
            }

            boolean noOverlappingFolds = foldDataSetSplitter.sanityCheck();
            if(! noOverlappingFolds){
                throw new InvalidDataSplitException("overlapping folds");
            }

            List[] folds = foldDataSetSplitter.getFolds();

            FoldInfo[] foldInfos = new FoldInfo[folds.length];

            for (int i = 0; i < folds.length; i++) {

                System.out.println("--- start fold " + i + " of " + settings.getNbOfFolds() + "for " + propertiesPathStr + " ---");
                foldInfos[i] = doFold(i, foldDataSetSplitter, possibleLabels,

                        // NOTE: changed this!
                        setupResultWrapper.getFreshLazyBumTreeBuilder(),

                        outputDirStr,
                        programConfiguration, jooqDatabaseInteractor, targetTableManager,
                        settings.shouldRelearn(),
                        settings.verbose);
                System.out.println("--- end fold " + i + " of " + settings.getNbOfFolds() + "for " + propertiesPathStr + " ---");
            }



            long stopTime = System.nanoTime();

            String outputFileStr = outputDirStr
                    + File.separator
                    +  "info_average" + settings.getNbOfFolds() +"folds"+ ".txt";
            Path outputFilePath = Paths.get(outputFileStr);


            RunInfo runInfo = new RunInfo(
                    dataSetName, programConfiguration,
                    propertiesPathStr, settings.getNbOfFolds(),
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
                    + "runinfo" + settings.getNbOfFolds() + "folds.json";
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
                                JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager,
                                  boolean shouldRelearn, boolean verbose) throws NoSplitInfoFoundException, InvalidTreeBuilderException, GraphTraversalException, UnsupportedFieldTypeException, InvalidSplitCriterionException, UnsupportedFeatureTransformationException, IOException, ClassNotFoundException, InvalidKeyInfoException, InvalidTreeException {

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

            LazyBumDecisionTreeIO.writeDecisionTreeRootNodeToFile(decisionTree, decisionTreeFileStr);

        } else {
            if(Files.notExists(Paths.get(decisionTreeFileStr))){
                throw new FileNotFoundException(decisionTreeFileStr);
            }
            decisionTree = LazyBumDecisionTreeIO.readDecisionTreeFromRootNodeFile(decisionTreeFileStr);
        }
        if(verbose){
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
        outputFileWriter.write("majority prediction acc: " + majorityPredictionAccuracy + "\n");
        outputFileWriter.write("total time (sec): " + totalTimeForFold * convertNanoTimeToSeconds + "\n");
        outputFileWriter.write("learning time (sec): " + totalTimeForLearning * convertNanoTimeToSeconds + "\n");
        outputFileWriter.write("prediction time (sec): " + totalTimeForPrediction * convertNanoTimeToSeconds +"\n");

        double timeSpentOnFeatureTableExtensionCalculations = treeBuilder.getTimeSpentOnFeatureTableExtensionCalculations();
        double timeSpentOnFindingTheBestSplit = treeBuilder.getTimeSpentOnFindingTheBestSplit();

        outputFileWriter.write("time spent on finding split (sec): " + timeSpentOnFindingTheBestSplit + "\n");
        outputFileWriter.write("time spent on extending tables (sec): " + timeSpentOnFeatureTableExtensionCalculations + "\n");
        outputFileWriter.write("LazyBum total nb of non-target feature columns built: "+  treeBuilder.getTotalNbOfFeaturesBuilt() + "\n");


        outputFileWriter.flush();
        outputFileWriter.close();
        //NOTE: CLEAN DATABASE


        System.out.println("LazyBum total nb of non-target feature columns built: "+  treeBuilder.getTotalNbOfFeaturesBuilt());


        List<Table<?>> tables = jooqDatabaseInteractor.getTables();
        cleanUp(tables, jooqDatabaseInteractor);

        int nbOfNodesInTree = LazyBumTreeNode.countNbOfNodes(decisionTree.getTreeRoot());

        FoldInfo foldInfo = new FoldInfo(accuracy, totalTimeForFold, totalTimeForLearning, totalTimeForPrediction, nbOfNodesInTree,
                timeSpentOnFeatureTableExtensionCalculations, timeSpentOnFindingTheBestSplit);

        foldInfo.setMajorityPredictionAcc(majorityPredictionAccuracy);
        return foldInfo;
    }


}
