package prediction;

import config.ImproperProgramConfigurationException;
import config.ProgramConfiguration;
import config.ProgramConfigurationOption;
import database.DatabaseConnection;
import database.JOOQDatabaseInteractor;
import dataset.Example;
import dataset.FoldDataSetSplitter;
import dataset.InvalidDataSplitException;
import dataset.TargetTableManager;
import org.apache.commons.math3.stat.StatUtils;
import org.jooq.Table;
import dataset.ExampleIDFoldPersistor;
import dataset.InvalidFoldPersistence;
import prediction.MajorityPredictor;
import utils.PrettyPrinter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static utils.CleanUp.cleanUp;

@SuppressWarnings("Duplicates")
public class TenFoldCVMajorityClassPredictionMain {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        List<String> propertiesPaths = new ArrayList<>();

        propertiesPaths.add("data/cora/config.properties");
        propertiesPaths.add("data/facebook/config.properties");
        propertiesPaths.add("data/genes/config.properties");
        propertiesPaths.add("data/hepatitis_std/config.properties");
        propertiesPaths.add("data/imdb_small/config.properties");
        propertiesPaths.add("data/uw_cse/config.properties");
        propertiesPaths.add("data/webkb/config.properties");

        for (String propertiesPath : propertiesPaths) {
            System.out.println("Start of " + propertiesPath);
            runTest(propertiesPath);
            System.out.println("End of " + propertiesPath);
            System.out.println("========================================================");
            System.out.println("========================================================");
            System.out.println("========================================================");
        }

    }


    public static void runTest(String propertiesPathStr) throws IOException, ClassNotFoundException {

//        String propertiesPathStr = "data/webshop/config.properties";

        Logger logger = Logger.getLogger("m");

        BufferedWriter outputFileWriter = null;




        try {
            ProgramConfiguration programConfiguration = new ProgramConfiguration(propertiesPathStr);
            Connection databaseConnection = DatabaseConnection.getDatabaseConnection(programConfiguration);

            String outputDirStr = "output" + File.separator +
                    programConfiguration.getConfigurationOption(ProgramConfigurationOption.SCHEMA);
            Path outputDirPath = Paths.get(outputDirStr);
            //create output directory if it does not yet exist
            if (!Files.exists(outputDirPath)) {
                Files.createDirectories(outputDirPath);
            }

            JOOQDatabaseInteractor jooqDatabaseInteractor = new JOOQDatabaseInteractor(
                    databaseConnection,
                    programConfiguration
            );

            List<Table<?>> tables = jooqDatabaseInteractor.getTables();
            cleanUp(tables, jooqDatabaseInteractor);

            TargetTableManager targetTableManager = TargetTableManager.getInstanceManager(jooqDatabaseInteractor, programConfiguration);

            List<Example> allExamples = targetTableManager.getInstancesAsExamples();


            Set<Object> possibleLabels = targetTableManager.getPossibleLabels();


            FoldDataSetSplitter<Example> foldDataSetSplitter;
            ExampleIDFoldPersistor exampleIDFoldPersistor = new ExampleIDFoldPersistor();

            foldDataSetSplitter = exampleIDFoldPersistor.loadFoldDataSetSplitter(outputDirStr, allExamples);


            boolean noOverlappingFolds = foldDataSetSplitter.sanityCheck();
            if(! noOverlappingFolds){
                throw new InvalidDataSplitException("overlapping folds");
            }

            List[] folds = foldDataSetSplitter.getFolds();


            double[] accuracyPerFold = new double[folds.length];

            for (int i = 0; i < folds.length; i++) {

                System.out.println("--- start fold " + i + " of " + foldDataSetSplitter.getNbOfFolds() + "for " + propertiesPathStr + " ---");
                double accuracy = doFold(i, foldDataSetSplitter, possibleLabels,
                        outputDirStr,
                        programConfiguration, jooqDatabaseInteractor);

                accuracyPerFold[i] = accuracy;


                System.out.println("--- end fold " + i + " of " + foldDataSetSplitter.getNbOfFolds() + "for " + propertiesPathStr + " ---");
            }


            String outputFileStr = outputDirStr
                    + File.separator
                    +  "majority_prediction_average" + foldDataSetSplitter.getNbOfFolds() +"folds"+ ".txt";
            Path outputFilePath = Paths.get(outputFileStr);

            File outputFile = outputFilePath.toFile();
            outputFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));

            outputFileWriter.write(programConfiguration.toString());
            outputFileWriter.newLine();
            outputFileWriter.write("nb of possible labels: " + possibleLabels.size() + "\n");
            outputFileWriter.write("total nb of examples: " + allExamples.size() + "\n");
            outputFileWriter.write("training set size: " + (allExamples.size() - foldDataSetSplitter.getFoldSize()) + "\n");
            outputFileWriter.write("fold size (+-1): " + foldDataSetSplitter.getFoldSize() + "\n");
            outputFileWriter.write("all accuracies:\n");
            outputFileWriter.write(PrettyPrinter.doubleArrayToCSVString(accuracyPerFold));
            outputFileWriter.newLine();
            outputFileWriter.write("average accuracy: " + StatUtils.mean(accuracyPerFold));
            outputFileWriter.newLine();



            databaseConnection.close();

        } catch (SQLException | ImproperProgramConfigurationException
                 | InvalidDataSplitException | InvalidFoldPersistence e) {
            e.printStackTrace();
        } finally {
            if(outputFileWriter != null) {
                outputFileWriter.flush();
                outputFileWriter.close();
            }
        }
    }


    public static double doFold(int foldNb, FoldDataSetSplitter foldDataSetSplitter,
                                  Set<Object> possibleLabels,
                                  String outputDirStr, ProgramConfiguration programConfiguration,
                                  JOOQDatabaseInteractor jooqDatabaseInteractor) throws IOException {

        List<Example> validationExampleSet = foldDataSetSplitter.getValidationSet(foldNb);
        List<Example> trainingExampleSet = foldDataSetSplitter.getTrainingSet(foldNb);
       String outputFileStr = outputDirStr
                + File.separator
                + "majority_prediction" + "_f" + foldNb + ".txt";
        Path outputFilePath = Paths.get(outputFileStr);


        BufferedWriter outputFileWriter = null;

        File outputFile = outputFilePath.toFile();
        outputFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));

        outputFileWriter.write(programConfiguration.toString());
        outputFileWriter.newLine();


        MajorityPredictor majorityPredictor = new MajorityPredictor(trainingExampleSet);
        double accuracy = majorityPredictor.majorityPredictionAccuracy(validationExampleSet);

        outputFileWriter.write("training set size: " + trainingExampleSet.size() + "\n");
//            outputFileWriter.newLine();
        outputFileWriter.write("test set size: " + majorityPredictor.getTestSetSize() + "\n");
        outputFileWriter.write("possible labels: " + PrettyPrinter.listToCSVString(new ArrayList<>(possibleLabels)) + "\n");
        outputFileWriter.write("nb of possible labels: " + possibleLabels.size() + "\n");
        outputFileWriter.write("TPs: " + majorityPredictor.getTruePositiveCount() + "\n");
        outputFileWriter.write("Acc: " + accuracy + "\n");


        outputFileWriter.flush();
        outputFileWriter.close();
        //NOTE: CLEAN DATABASE
        List<Table<?>> tables = jooqDatabaseInteractor.getTables();
        cleanUp(tables, jooqDatabaseInteractor);
        return accuracy;
    }

}
