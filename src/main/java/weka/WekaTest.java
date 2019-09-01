package weka;

import config.ProgramConfiguration;
import config.ProgramConfigurationOption;
import database.DatabaseConnection;
import database.JOOQDatabaseInteractor;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

import java.io.BufferedReader;
import java.sql.Connection;


public class WekaTest {


    public static void main(String[] args) throws Exception {

        String programConfigurationPath = "data/financial/config.properties";
        String foldDir = "folds/financial";
        BufferedReader datafile = WekaUtils.readDataFile("test_creation.arff");

        boolean removeTargetIdColumn = true;

        ProgramConfiguration programConfiguration = new ProgramConfiguration(programConfigurationPath);
        Connection databaseConnection = DatabaseConnection.getDatabaseConnection(programConfiguration);
        JOOQDatabaseInteractor jooqDatabaseInteractor = new JOOQDatabaseInteractor(
                databaseConnection,
                programConfiguration
        );

        String targetIdString = programConfiguration.getConfigurationOption(ProgramConfigurationOption.TARGET_ID);
        String targetColumn = programConfiguration.getConfigurationOption(ProgramConfigurationOption.TARGET_COLUMN);

        Instances data = new Instances(datafile);
        data.setClass(data.attribute(targetColumn));

        Attribute targetIDAttribute = data.attribute(targetIdString);

        // Do 10-split cross validation
        WekaPredefinedFoldInstancesSplitter wekaPredefinedFoldInstancesSplitter = new WekaPredefinedFoldInstancesSplitter(
                foldDir, jooqDatabaseInteractor, programConfiguration,
                data, targetIDAttribute, removeTargetIdColumn);
        Instances[][] split = wekaPredefinedFoldInstancesSplitter.crossValidationPredefinedSplit();

        // Separate split into training and testing arrays
        Instances[] trainingSplits = split[0];
        Instances[] testingSplits = split[1];

        // Use a set of classifiers
        Classifier[] models = {
                new J48(), // a decision tree
//                new PART(),
//                new DecisionTable(),//decision table majority classifier
//                new DecisionStump() //one-level decision tree
        };

        // Run for each model
        for (int j = 0; j < models.length; j++) {

            // Collect every group of predictions for current model in a FastVector
            FastVector predictions = new FastVector();

            // For each training-testing split pair, train and test the classifier
            for (int i = 0; i < trainingSplits.length; i++) {
                Evaluation validation = WekaUtils.classify(models[j], trainingSplits[i], testingSplits[i]);

                predictions.appendElements(validation.predictions());

                // Uncomment to see the summary for each training-testing pair.
                System.out.println(models[j].toString());
            }

            // Calculate overall accuracy of current classifier on all splits
            double accuracy = WekaUtils.calculateAccuracy(predictions);

            // Print current classifier's name and accuracy in a complicated,
            // but nice-looking way.
            System.out.println("Accuracy of " + models[j].getClass().getSimpleName() + ": "
                    + String.format("%.2f%%", accuracy)
                    + "\n---------------------------------");
        }

    }
}