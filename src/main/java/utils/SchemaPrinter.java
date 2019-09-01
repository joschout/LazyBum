package utils;

import config.ImproperProgramConfigurationException;
import config.ProgramConfiguration;
import database.DatabaseConnection;
import database.JOOQDatabaseInteractor;
import graph.ForeignKeyEdge;
import graph.TableGraph;
import lazybum.RunInfo;
import org.jgrapht.Graph;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by joschout.
 */
public class SchemaPrinter {


    public static void main(String[] args) throws SQLException, IOException, ImproperProgramConfigurationException {
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

        dataSetNameToPropertiesPathsMap.put("home_credit_mod", "data/home_credit_mod/config.properties");


//        dataSetNameToPropertiesPathsMap.put("imdb_movielens", "data/imdb_movielens/config.properties");


        Map<String, RunInfo> dataSetNameToRunInfoMap = new HashMap<>();

        for (String dataSetName : dataSetNameToPropertiesPathsMap.keySet()) {
            String propertiesPath = dataSetNameToPropertiesPathsMap.get(dataSetName);

            System.out.println("Start of " + propertiesPath);
            printDatabaseSchemaAsGraph(dataSetName, propertiesPath);
            System.out.println("End of " + propertiesPath);
            System.out.println("========================================================");
        }
    }


    public static void printDatabaseSchemaAsGraph(String dataSetName, String propertiesPathStr) throws IOException, ImproperProgramConfigurationException, SQLException {

        ProgramConfiguration programConfiguration = new ProgramConfiguration(propertiesPathStr);
        Connection databaseConnection = DatabaseConnection.getDatabaseConnection(programConfiguration);

        JOOQDatabaseInteractor jooqDatabaseInteractor = new JOOQDatabaseInteractor(
                databaseConnection,
                programConfiguration
        );

        // create the relational graph. This is a DIRECTED graph
        Graph<String, ForeignKeyEdge> relationalGraph = TableGraph.createFrom(jooqDatabaseInteractor.getTables());
        System.out.println(TableGraph.asDOTString(relationalGraph));
    }
}
