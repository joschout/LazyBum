package lazybum.main;

import config.ProgramConfiguration;
import database.JOOQDatabaseInteractor;
import dataset.Example;
import dataset.TargetTableManager;
import lazybum.*;
import lazybum.main.setup.SetupBuilder;
import lazybum.main.setup.SetupResultWrapper;
import lazybum.main.setup.TraversalGraphExtensionStrategyEnum;
import learning.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

//@SuppressWarnings("Duplicates")
public class LazyBumLearnSingleDecisionTreeMain {


    public static void main(String[] args) throws IOException, ClassNotFoundException {


//        Class.forName("com.mysql.jdbc.Driver");

        Map<String, String> dataSetNameToPropertiesPathsMap = new LinkedHashMap<>();


        dataSetNameToPropertiesPathsMap.put("hepatitis_std_mod_target", "data/hepatitis_std_mod_target/config.properties");
        dataSetNameToPropertiesPathsMap.put("uw_cse_mod_target", "data/uw_cse_mod_target/config.properties");
        dataSetNameToPropertiesPathsMap.put("university_mod_target", "data/university_mod_target/config.properties");
        dataSetNameToPropertiesPathsMap.put("imdb_ijs_filtered_sample", "data/imdb_ijs_filtered_sample/config.properties");

        for (String dataSetName : dataSetNameToPropertiesPathsMap.keySet()) {
            String propertiesPath = dataSetNameToPropertiesPathsMap.get(dataSetName);

            System.out.println("Start of " + propertiesPath);
            runTest(dataSetName, propertiesPath);
            System.out.println("End of " + propertiesPath);
            System.out.println("========================================================");
            System.out.println("========================================================");
            System.out.println("========================================================");
        }
    }


    public static void runTest(String dataSetName, String propertiesPath) {


//        String propertiesPath = "data/hepatitis_std/config.properties";

        Logger logger = Logger.getLogger("m");


        try {

            SetupBuilder setupBuilder = SetupBuilder.setupBuilderForDatabase(TraversalGraphExtensionStrategyEnum.LAZY_ONEBM);
            Optional<SetupResultWrapper> optionalSetupInfo = setupBuilder.setup(dataSetName, propertiesPath);
            if (optionalSetupInfo.isEmpty()){
                throw new Exception("cannot start execution, setup info is empty");
            }
            SetupResultWrapper setupResultWrapper = optionalSetupInfo.get();

            ProgramConfiguration programConfiguration = setupResultWrapper.programConfiguration;
            TargetTableManager targetTableManager = setupResultWrapper.targetTableManager;
            LazyBumTreeBuilder treeBuilder = setupResultWrapper.getFreshLazyBumTreeBuilder();
            JOOQDatabaseInteractor jooqDatabaseInteractor = setupResultWrapper.jooqDatabaseInteractor;

            List<Example> exampleList = targetTableManager.getInstancesAsExamples();


//            exampleList = exampleList.subList(0,100);

            LazyBumDecisionTree decisionTree = new LazyBumDecisionTree();
            decisionTree.fit(exampleList, treeBuilder);

            System.out.println(decisionTree.toString());

            FeatureTableExtensionCounter featureTableExtensionCounter = treeBuilder.getFeatureTableExtensionCounter();
            System.out.println(featureTableExtensionCounter.statisticsToString());

//            -------------------------------------------------------------------------------------------------------
            List<Object> allExampleIds = targetTableManager.getAllExampleInstanceIDs();
            double testSize = allExampleIds.size();

            Map<Object, Prediction> testSetInstanceIDSToPredictions = decisionTree.predictAsBatch(allExampleIds, jooqDatabaseInteractor, targetTableManager);


            int truePositiveCount = 0;
            for (Example example: exampleList) {
                Prediction prediction = testSetInstanceIDSToPredictions.get(example.instanceID);
                Object predictedLabel = prediction.getValue();
                Object actualLabel = example.label;
                if (actualLabel.equals(predictedLabel)) {
                    truePositiveCount++;
                }
            }
            double accuracy = truePositiveCount / testSize;
            System.out.println("accuracy on total training set:" + accuracy);

            setupResultWrapper.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
