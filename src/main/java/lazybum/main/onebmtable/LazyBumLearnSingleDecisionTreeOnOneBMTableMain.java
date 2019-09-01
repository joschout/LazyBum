package lazybum.main.onebmtable;

import config.ProgramConfiguration;
import database.JOOQDatabaseInteractor;
import dataset.Example;
import dataset.TargetTableManager;
import lazybum.LazyBumDecisionTree;
import lazybum.LazyBumTreeBuilder;
import lazybum.main.setup.SetupBuilder;
import lazybum.main.setup.SetupResultWrapper;
import learning.Prediction;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@SuppressWarnings("Duplicates")
public class LazyBumLearnSingleDecisionTreeOnOneBMTableMain {


    public static void main(String[] args) {

        String dataSetName = "university";
        String propertiesPath = "data/university/config.properties";

        Logger logger = Logger.getLogger("m");


        try {
            SetupBuilder setupBuilder = SetupBuilder.setupBuilderForOneBMTable();
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
