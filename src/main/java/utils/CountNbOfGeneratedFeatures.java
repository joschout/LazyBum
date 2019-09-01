package utils;

import config.ImproperProgramConfigurationException;
import config.ProgramConfiguration;
import database.DatabaseConnection;
import database.JOOQDatabaseInteractor;
import feature.featuretable.persistance.FeatureColumnEncoder;
import org.jooq.Record1;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CountNbOfGeneratedFeatures
{

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
            runOneBM(propertiesPath);
            System.out.println("End of " + propertiesPath);
            System.out.println("========================================================");
            System.out.println("========================================================");
            System.out.println("========================================================");
        }

    }



    public static void runOneBM(String propertiesPath) {

//        String propertiesPath = args[0];

        Logger logger = Logger.getLogger("m");


        try {
            ProgramConfiguration programConfiguration = new ProgramConfiguration(propertiesPath);
            Connection databaseConnection = DatabaseConnection.getDatabaseConnection(programConfiguration);

            JOOQDatabaseInteractor jooqDatabaseInteractor = new JOOQDatabaseInteractor(
                    databaseConnection,
                    programConfiguration
            );


            Table featureEncodings = jooqDatabaseInteractor
                    .getTableRepresentationWithSchemaQualifiedName(
                            FeatureColumnEncoder.FEATURE_TABLE_FIELD_ENCODINGS_TABLE_NAME);

            Record1<Integer> integerRecord1 =

                    jooqDatabaseInteractor.getDslContext()
                            .selectDistinct(DSL.count())
                            .from(featureEncodings)
                            .fetchOne();
            System.out.println(propertiesPath + "\n\tnb of features in table:" + integerRecord1.toString());


        } catch (ImproperProgramConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
