package lazybum.main;

import config.ProgramConfiguration;
import database.JOOQDatabaseInteractor;
import dataset.Example;
import dataset.TargetTableManager;
import lazybum.LazyBumDecisionTree;
import lazybum.LazyBumTreeBuilder;
import lazybum.main.setup.SetupBuilder;
import lazybum.main.setup.SetupResultWrapper;
import learning.Prediction;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Created by joschout.
 */
public class LazyBumLearnSingleDecisionTreeOnTargetTable {


    public static void main(String[] args) throws IOException, ClassNotFoundException {


        Map<String, String> dataSetNameToPropertiesPathsMap = new LinkedHashMap<>();

        dataSetNameToPropertiesPathsMap.put("financial_mod_target", "data/financial_mod_target/config.properties");
        dataSetNameToPropertiesPathsMap.put("hepatitis_std_mod_target", "data/hepatitis_std_mod_target/config.properties");

        dataSetNameToPropertiesPathsMap.put("imdb_small_mod_target", "data/imdb_small_mod_target/config.properties");
        dataSetNameToPropertiesPathsMap.put("uw_cse_mod_target", "data/uw_cse_mod_target/config.properties");
        dataSetNameToPropertiesPathsMap.put("university_mod_target", "data/university_mod_target/config.properties");


        for (String dataSetName : dataSetNameToPropertiesPathsMap.keySet()) {
            String propertiesPath = dataSetNameToPropertiesPathsMap.get(dataSetName);

            System.out.println("Start of " + propertiesPath);
            buildSingleTree(dataSetName, propertiesPath);
            System.out.println("End of " + propertiesPath);
            System.out.println("========================================================");
            System.out.println("========================================================");
            System.out.println("========================================================");
        }
    }


    public static void buildSingleTree(String dataSetName, String propertiesPath) {

//        String dataSetName = "mushroom";
//        String propertiesPath = "data/mushroom/config.properties";

        Logger logger = Logger.getLogger("m");


        try {

            SetupBuilder setupBuilder = SetupBuilder.setupBuilderForTargetTable();
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
            LazyBumDecisionTree decisionTree = new LazyBumDecisionTree();
            decisionTree.fit(exampleList, treeBuilder);

            System.out.println(decisionTree.toString());

//            -------------------------------------------------------------------------------------------------------
            List<Object> allExampleIds = targetTableManager.getAllExampleInstanceIDs();
            double testSize = allExampleIds.size();

            Map<Object, Prediction> testSetInstanceIDSToPredictions = decisionTree.predictAsBatch(allExampleIds, jooqDatabaseInteractor, targetTableManager);


            int truePositiveCount = 0;
            for (Example example : exampleList) {
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
