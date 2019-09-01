package dataset.conversion;

import config.ProgramConfiguration;
import config.ProgramConfigurationOption;
import io.OutputUtils;
import onebm.OneBMMain;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.experiment.InstanceQuery;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by joschout.
 */
public class DatabaseToARFFConvertor {


    public static final String onebmTableName = OneBMMain.oneBMTableName;
    public static final String arffRootDir = "arff";
    public static String DATABASEUTILS_PROPERTIES = "DatabaseUtils.props";


    public static void main(String[] args) throws Exception {
        Map<String, String> dataSetNameToPropertiesPathsMap = new LinkedHashMap<>();

        TableType typeOfTableToConvert = TableType.ONEBM_TABLE;


        dataSetNameToPropertiesPathsMap.put("financial_mod_target", "data/financial_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("hepatitis_std_mod_target", "data/hepatitis_std_mod_target/config.properties");
//
//        dataSetNameToPropertiesPathsMap.put("imdb_small_mod_target", "data/imdb_small_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("uw_cse_mod_target", "data/uw_cse_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("university_mod_target", "data/university_mod_target/config.properties");


        for (String dataSetName : dataSetNameToPropertiesPathsMap.keySet()) {
            String propertiesPath = dataSetNameToPropertiesPathsMap.get(dataSetName);

            String arffOutputFilename = getARFFFileName(dataSetName, typeOfTableToConvert);
            convertDataSetToARFF(dataSetName, propertiesPath, typeOfTableToConvert, arffOutputFilename);
        }

    }




    public static String getARFFFileName(String dataSetName, TableType tableType){
        return  arffRootDir + File.separator + dataSetName + File.separator + tableType.getARFFFileName();
    }




    public static void convertDataSetToARFF(String dataSetName, String propertiesPath, TableType typeOfTableToConvert, String arffOutputFilename) throws Exception {
        ProgramConfiguration programConfiguration = new ProgramConfiguration(propertiesPath);
        convertDataSetToARFF(dataSetName, programConfiguration, typeOfTableToConvert, arffOutputFilename);
    }

    public static void convertDataSetToARFF(String dataSetName, ProgramConfiguration programConfiguration,
                TableType typeOfTableToConvert, String arffOutputFilename) throws Exception {


        String schemaName = programConfiguration.getConfigurationOption(ProgramConfigurationOption.SCHEMA);

        String tableName;
        if(typeOfTableToConvert == TableType.ONEBM_TABLE){
            tableName = onebmTableName;
        } else if(typeOfTableToConvert == TableType.TARGET_TABLE){
            tableName = programConfiguration.getConfigurationOption(ProgramConfigurationOption.TARGET_TABLE);
        } else{
            throw new Exception("type of table to convert to arff is unknown");
        }

        String username = programConfiguration.getConfigurationOption(ProgramConfigurationOption.DB_USER);
        String password = programConfiguration.getConfigurationOption(ProgramConfigurationOption.DB_PASSWORD);
        String url = programConfiguration.getConfigurationOption(ProgramConfigurationOption.DB_URL);

        convertDatabaseTableToARFF(dataSetName, schemaName, tableName, username, password,
                url, arffOutputFilename);

    }
    public static void convertDatabaseTableToARFF(String databaseName, String schemaName, String tableName, String username, String password,
                                                  String databaseURL, String outputFilePath){

        try {
            //file reader of database utilities properties
            File propertiesFile = new File(DATABASEUTILS_PROPERTIES);

            InstanceQuery query = new InstanceQuery();
            query.setCustomPropsFile(propertiesFile);
            query.setUsername(username);
            query.setPassword(password);

            String queryString = String.format("select * from %s.%s", schemaName, tableName);
            query.setQuery(queryString);

            query.setDatabaseURL(databaseURL);

            // You can declare that your data set is sparse
            // query.setSparseData(true);

            Instances data = query.retrieveInstances();

            for (int i = 0; i < data.numAttributes(); i++) {
                Attribute attribute = data.attribute(i);
                System.out.println(attribute.toString());
            }

            System.out.println(data.relationName());

            OutputUtils.createDirectoriesIfTheyDontExist(outputFilePath);

            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(new File(outputFilePath));
            saver.writeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
