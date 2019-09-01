package database;

import config.ProgramConfiguration;
import config.ProgramConfigurationOption;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection getDatabaseConnection(String databaseURL, String databaseUser, String databasePassword) throws SQLException {
        Connection connection = DriverManager.getConnection(databaseURL, databaseUser,
                databasePassword);
        System.err.println("The connection is successfully obtained");
        return connection;
    }

    public static Connection getDatabaseConnection(ProgramConfiguration programConfiguration) throws SQLException {
        return getDatabaseConnection(
                programConfiguration.getConfigurationOption(ProgramConfigurationOption.DB_URL),
                programConfiguration.getConfigurationOption(ProgramConfigurationOption.DB_USER),
                programConfiguration.getConfigurationOption(ProgramConfigurationOption.DB_PASSWORD));
    }
}
