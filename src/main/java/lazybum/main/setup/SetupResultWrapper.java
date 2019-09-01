package lazybum.main.setup;

import config.ProgramConfiguration;
import database.JOOQDatabaseInteractor;
import dataset.TargetTableManager;
import feature.featuretable.persistance.FeatureColumnEncoder;
import graph.RelationalGraphBFSHandler;
import lazybum.*;
import learning.LeafBuilder;
import learning.StopCriterion;
import learning.split.InformationGainCalculatorSettings;
import utils.NameGenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

public class SetupResultWrapper {
    public final ProgramConfiguration programConfiguration;
    public final Connection connection;
    public final JOOQDatabaseInteractor jooqDatabaseInteractor;
    public final TargetTableManager targetTableManager;
//    public final LazyBumTreeBuilder treeBuilder;
    public final Set<Object> possibleLabels;





    private final RelationalGraphBFSHandler<String> relationalGraphBFSHandler;
    private final FeatureColumnEncoder featureColumnEncoder;
    private final boolean shouldPersistFeatureColumnExtensions;

    private final TraversalGraphExtensionStrategyEnum traversalGraphExtensionStrategyEnum;


    public SetupResultWrapper(ProgramConfiguration programConfiguration, Connection connection,
                              JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager,
                              Set<Object> possibleLabels,

                              RelationalGraphBFSHandler<String> relationalGraphBFSHandler,
                              FeatureColumnEncoder featureColumnEncoder,
                              boolean shouldPersistFeatureColumnExtensions,
                              TraversalGraphExtensionStrategyEnum traversalGraphExtensionStrategyEnum


                              ) {
        this.programConfiguration = programConfiguration;
        this.connection = connection;
        this.jooqDatabaseInteractor = jooqDatabaseInteractor;
        this.targetTableManager = targetTableManager;
//        this.treeBuilder = treeBuilder;
        this.possibleLabels = possibleLabels;


        this.relationalGraphBFSHandler = relationalGraphBFSHandler;
        this.featureColumnEncoder = featureColumnEncoder;
        this.shouldPersistFeatureColumnExtensions = shouldPersistFeatureColumnExtensions;
        this.traversalGraphExtensionStrategyEnum = traversalGraphExtensionStrategyEnum;
    }


    public LazyBumTreeBuilder getFreshLazyBumTreeBuilder(){

        NameGenerator multiSetNameGenerator = new NameGenerator();

        TraversalGraphExtensionStrategy traversalGraphExtensionStrategy
                = traversalGraphExtensionStrategyEnum.getStrategy();

        FeatureTableBuilder featureTableBuilder
                = new FeatureTableBuilder(
                jooqDatabaseInteractor,
                new TraversalPathExtender(
                        relationalGraphBFSHandler, jooqDatabaseInteractor,
                        multiSetNameGenerator, targetTableManager, featureColumnEncoder,
                        true, shouldPersistFeatureColumnExtensions
                ),
                traversalGraphExtensionStrategy,
                targetTableManager,
                featureColumnEncoder);

        return new LazyBumTreeBuilder(
                new LazyBumSplitter(
                        new InformationGainCalculatorSettings(possibleLabels),
                        jooqDatabaseInteractor
                ),
                new LeafBuilder(),
                new StopCriterion(),
                featureTableBuilder
        );

    }


    public void close(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
