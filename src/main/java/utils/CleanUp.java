package utils;

import config.ImproperProgramConfigurationException;
import config.ProgramConfiguration;
import config.ProgramConfigurationOption;
import database.DatabaseConnection;
import database.JOOQDatabaseInteractor;
import org.jooq.Table;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CleanUp {

    public static void main(String[] args) throws IOException, ImproperProgramConfigurationException, SQLException {
//        String propertiesPath = args[0];

        String propertiesPath = "data/hepatitis_std_mod_target/config.properties";

        ProgramConfiguration programConfiguration = new ProgramConfiguration(propertiesPath);
        Connection databaseConnection = DatabaseConnection.getDatabaseConnection(programConfiguration);

        JOOQDatabaseInteractor jooqDatabaseInteractor = new JOOQDatabaseInteractor(
                databaseConnection,
                programConfiguration
        );


        System.out.println("CLEANING UP SCHEMA " + programConfiguration.getConfigurationOption(ProgramConfigurationOption.SCHEMA));

        List<Table<?>> tables = jooqDatabaseInteractor.getTables();

        cleanUp(tables, jooqDatabaseInteractor);
    }

    private static boolean shouldBeDropped(String tableName) {
        return tableName.startsWith("name") || tableName.startsWith("featuretable");
    }

//    private static boolean shouldBeDropped(Table table) {
//        return shouldBeDropped(table.getName());
//    }


    public static void cleanUp(Iterable<Table<?>> tables, JOOQDatabaseInteractor jooqDatabaseInteractor){
        for(Table table: tables){
            cleanUpSingleTable(table.getName(), jooqDatabaseInteractor);
        }
    }

    public static void cleanUpSingleTableUnsafe(String tableName, JOOQDatabaseInteractor jooqDatabaseInteractor){
        try {
            Table tableWithSchemaQualified = jooqDatabaseInteractor.getTableRepresentationWithSchemaQualifiedName(tableName);
            if(tableWithSchemaQualified != null){
                jooqDatabaseInteractor.getDslContext().dropTable(tableWithSchemaQualified).execute();
            }
        } catch (org.jooq.exception.DataAccessException exception){
            exception.printStackTrace();
        }
    }

    public static void cleanUpSingleTable(String tableName, JOOQDatabaseInteractor jooqDatabaseInteractor){
        if (shouldBeDropped(tableName)) {
            cleanUpSingleTableUnsafe(tableName, jooqDatabaseInteractor);
        }
    }
}
