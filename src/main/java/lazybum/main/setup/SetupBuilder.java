package lazybum.main.setup;

import config.ImproperProgramConfigurationException;
import config.ProgramConfiguration;
import config.ProgramConfigurationOption;
import database.DatabaseConnection;
import database.JOOQDatabaseInteractor;
import dataset.TargetTableManager;
import dataset.conversion.TableType;
import feature.featuretable.persistance.FeatureColumnEncoder;
import globalsettings.FieldToFeatureTranformationSettings;
import graph.ForeignKeyEdge;
import graph.RelationalGraphBFSHandler;
import graph.TableGraph;
import org.jgrapht.Graph;
import org.jooq.Table;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static utils.CleanUp.cleanUp;


public class SetupBuilder {

    public final TableType tableType;
    public final TraversalGraphExtensionStrategyEnum traversalGraphExtensionStrategyEnum;

    public final boolean learnFromOneTable;

    public boolean shouldPersistFeatureColumnExtensions = true;


    private SetupBuilder(TableType tableType, TraversalGraphExtensionStrategyEnum traversalGraphExtensionStrategyEnum, boolean learnFromOneTable) {
        this.tableType = tableType;
        this.traversalGraphExtensionStrategyEnum = traversalGraphExtensionStrategyEnum;
        this.learnFromOneTable = learnFromOneTable;
    }

    public static SetupBuilder setupBuilderForOneBMTable(){
        return new SetupBuilder(TableType.ONEBM_TABLE, TraversalGraphExtensionStrategyEnum.NEVER_EXTEND, true);
    }

    public static SetupBuilder setupBuilderForTargetTable(){
        return new SetupBuilder(TableType.TARGET_TABLE, TraversalGraphExtensionStrategyEnum.NEVER_EXTEND, true);
    }

    public static SetupBuilder setupBuilderForDatabase(TraversalGraphExtensionStrategyEnum traversalGraphExtensionStrategyEnum){
        return new SetupBuilder(TableType.TARGET_TABLE, traversalGraphExtensionStrategyEnum, false);
    }



    private Graph<String, ForeignKeyEdge> getRelationalGraph(JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager){
        if(learnFromOneTable){
            /**
             * NOTE: in this experiment, the only table in the relational graph is the target table or the onebm table
             */
            return TableGraph.createFrom(targetTableManager.getTargetTable());

        } else{
            return TableGraph.createFrom(jooqDatabaseInteractor.getTables());
        }
    }

    public Optional<SetupResultWrapper> setup(String dataSetName, String propertiesPath){

        SetupResultWrapper setupResultWrapper = null;
        try {
            ProgramConfiguration programConfiguration = new ProgramConfiguration(propertiesPath);
            Connection databaseConnection = DatabaseConnection.getDatabaseConnection(programConfiguration);

            JOOQDatabaseInteractor jooqDatabaseInteractor = new JOOQDatabaseInteractor(
                    databaseConnection,
                    programConfiguration
            );


            if(tableType == TableType.ONEBM_TABLE){
                //NOTE: THIS IS IMPORTANT
                programConfiguration.setConfigurationOption(ProgramConfigurationOption.TARGET_TABLE, "onebmtable");
            }



            double distinctnessThreshold = 0.2;
            int absoluteDistinctnessThreshold = 40;
            FieldToFeatureTranformationSettings.setExistenceTestCreationDeciderToCategoricFieldUniqueValueCountController(
                    jooqDatabaseInteractor, distinctnessThreshold, absoluteDistinctnessThreshold
            );

            List<Table<?>> tables = jooqDatabaseInteractor.getTables();
            cleanUp(tables, jooqDatabaseInteractor);

            TargetTableManager targetTableManager = TargetTableManager.getInstanceManager(jooqDatabaseInteractor, programConfiguration);

            // create the relational graph. This is a DIRECTED graph
            Graph<String, ForeignKeyEdge> relationalGraph = getRelationalGraph(jooqDatabaseInteractor, targetTableManager);
            System.out.println(TableGraph.asDOTString(relationalGraph));
            RelationalGraphBFSHandler<String> relationalGraphBFSHandler = new RelationalGraphBFSHandler<String>(relationalGraph, targetTableManager.getTargetTable().getName());

            Set<Object> possibleLabels = targetTableManager.getPossibleLabels();

            FeatureColumnEncoder featureColumnEncoder = FeatureColumnEncoder.DEFAULT_FEATURE_COLUMN_ENCODER;

            if(tableType == TableType.ONEBM_TABLE){
                //NOTE: THIS IS IMPORTANT
                FeatureColumnEncoder.FEATURE_TABLE_FIELD_ENCODINGS_TABLE_NAME = "featuretable_field_encodings_onebmtable";
            }


//
//
//            FeatureTableBuilder featureTableBuilder
//                    = new FeatureTableBuilder(
//                    jooqDatabaseInteractor,
//                    new TraversalPathExtender(
//                            relationalGraphBFSHandler, jooqDatabaseInteractor,
//                            multiSetNameGenerator, targetTableManager, featureColumnEncoder,
//                            true, shouldPersistFeatureColumnExtensions
//                    ),
//                    traversalGraphExtensionStrategy,
//                    targetTableManager,
//                    featureColumnEncoder);
//
//
//            LazyBumTreeBuilder treeBuilder = new LazyBumTreeBuilder(
//                    new LazyBumSplitter(
//                            new InformationGainCalculatorSettings(possibleLabels),
//                            jooqDatabaseInteractor
//                    ),
//                    new LeafBuilder(),
//                    new StopCriterion(),
//                    featureTableBuilder
//            );

            setupResultWrapper = new SetupResultWrapper(programConfiguration, databaseConnection, jooqDatabaseInteractor,
                    targetTableManager, possibleLabels, relationalGraphBFSHandler, featureColumnEncoder,
                    shouldPersistFeatureColumnExtensions, traversalGraphExtensionStrategyEnum);
        } catch (ImproperProgramConfigurationException | IOException | SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(setupResultWrapper);
    }

}
