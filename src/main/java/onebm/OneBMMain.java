package onebm;

import config.ProgramConfiguration;
import config.ProgramConfigurationOption;
import database.DatabaseConnection;
import database.JOOQDatabaseInteractor;
import dataset.TargetTableManager;
import dataset.conversion.DatabaseToARFFConvertor;
import dataset.conversion.TableType;
import feature.featuretable.persistance.CompleteFeatureTablePersistance;
import globalsettings.FieldToFeatureTranformationSettings;
import graph.ForeignKeyEdge;
import graph.RelationalGraphBFSHandler;
import graph.TableGraph;
import org.jgrapht.Graph;
import org.jooq.Table;
import research.RefactoredJoinTableConstruction;
import utils.CleanUp;
import utils.CurrentDate;
import utils.NameGenerator;
import utils.Timer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static utils.CleanUp.cleanUp;
import static utils.CleanUp.cleanUpSingleTableUnsafe;


public class OneBMMain {


    public static JOOQDatabaseInteractor jooqDatabaseInteractor;
    public static ProgramConfiguration programConfiguration;

    public static boolean cleanUpJoinTables = true;
    public static NameGenerator joinTableNameGenerator;

    public static final String oneBMTableName = "onebmtable";

    public static final String outputRootDir = "output"+ File.separator + CurrentDate.getCurrentDateAsString() +
            "_onebmtablebuilding_mod_target_tables";

    public static final String overviewFileNameStem = "overview_onebmtablebuilding_run_times.csv";

    public static final String propertiesFileNameStem = "runinfo";
    public static final String propertiesSuffix = ".properties";

    public static final int nbOfRuns = 1;

    public static Logger oneBMRunTimeLogger;

    static {
        LogManager.getLogManager().reset();
    }


    public static String getOutputDirDataSetStr(String dataSetName){
        return outputRootDir
                    + File.separator + dataSetName;
    }
    public static String getPropertiesFileName(String dataSetName, int runIndex){
        return getOutputDirDataSetStr(dataSetName) + File.separator +
                propertiesFileNameStem + "_" + runIndex + "of"+nbOfRuns + propertiesSuffix;
    }

    public static Logger getRunTimeLogger(){

        Logger logger = Logger.getLogger("MyLog");
        FileHandler fh;

        try {

            // This block configure the logger with handler and formatter
            fh = new FileHandler(outputRootDir + File.separator + "onebm_runtime_log.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            // the following statement is used to log any messages
            logger.info("My first log");

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logger;

    }


    public static void setVerbosities(){
        RefactoredJoinTableConstruction.VERBOSE = true;
    }


    public static void main(String[] args) throws IOException, ClassNotFoundException {

        Path rootOutputDirPath = Paths.get(outputRootDir);
        //create output directory if it does not yet exist
        if (!Files.exists(rootOutputDirPath)) {
            Files.createDirectories(rootOutputDirPath);
        }


        oneBMRunTimeLogger = getRunTimeLogger();

//        setVerbosities();


        Map<String, String> dataSetNameToPropertiesPathsMap = new LinkedHashMap<>();

//        dataSetNameToPropertiesPathsMap.put("cora", "data/cora/config.properties");
//        dataSetNameToPropertiesPathsMap.put("facebook", "data/facebook/config.properties");
//        dataSetNameToPropertiesPathsMap.put("genes", "data/genes/config.properties");
//        dataSetNameToPropertiesPathsMap.put("hepatitis_std", "data/hepatitis_std/config.properties");

//        dataSetNameToPropertiesPathsMap.put("imdb_small", "data/imdb_small/config.properties");
//        dataSetNameToPropertiesPathsMap.put("uw_cse", "data/uw_cse/config.properties");
//        dataSetNameToPropertiesPathsMap.put("webkb", "data/webkb/config.properties");

//        dataSetNameToPropertiesPathsMap.put("hepatitis_std", "data/hepatitis_std/config.properties");
//        dataSetNameToPropertiesPathsMap.put("imdb_small", "data/imdb_small/config.properties");
//        dataSetNameToPropertiesPathsMap.put("uw_cse", "data/uw_cse/config.properties");
//
        dataSetNameToPropertiesPathsMap.put("financial", "data/financial/config.properties");
//        dataSetNameToPropertiesPathsMap.put("university", "data/university/config.properties");

//        dataSetNameToPropertiesPathsMap.put("financial_mod_target", "data/financial_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("hepatitis_std_mod_target", "data/hepatitis_std_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("imdb_small_mod_target", "data/imdb_small_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("uw_cse_mod_target", "data/uw_cse_mod_target/config.properties");
//        dataSetNameToPropertiesPathsMap.put("university_mod_target", "data/university_mod_target/config.properties");


        Map<String, List<Properties>> datasetToPropertiesList = new HashMap<>();
        for (String dataSetName : dataSetNameToPropertiesPathsMap.keySet()) {
            List<Properties> propertiesList = new ArrayList<>();
            datasetToPropertiesList.put(dataSetName, propertiesList);
        }


        for (String dataSetName : dataSetNameToPropertiesPathsMap.keySet()) {
            System.out.println("========================================================");
            System.out.println("Start of " + dataSetName );
            List<Properties> dataSetPropertiesList = datasetToPropertiesList.get(dataSetName);

            String outputDirDataSetStr = getOutputDirDataSetStr(dataSetName);

            Path outputDirPath = Paths.get(outputDirDataSetStr);
            //create output directory if it does not yet exist
            if (!Files.exists(outputDirPath)) {
                Files.createDirectories(outputDirPath);
            }

            for (int i = 1; i <= nbOfRuns; i++) {


                String propertiesPath = dataSetNameToPropertiesPathsMap.get(dataSetName);
                System.out.println("Start of " + dataSetName + ", run " + i + " of " + nbOfRuns);

                try {

                    Properties properties = runOneBM(dataSetName, propertiesPath);
                    dataSetPropertiesList.add(properties);

                    String propertiesOutputFileName = getPropertiesFileName(dataSetName, i);
                    FileOutputStream out = new FileOutputStream(propertiesOutputFileName);
                    properties.store(out, "run info for " + dataSetName + " for run " + i + " of " + nbOfRuns);

                } catch (Exception e){
                    String message = "Catched exception for " + dataSetName + ", run " + i + "/" + nbOfRuns +"\n"
                            + e.toString();
                    oneBMRunTimeLogger.info(message);
                    throw e;
                }

                System.out.println("End of " + dataSetName + ", run " + i + " of " + nbOfRuns);
                System.out.println("--------------------------------------------------------");

            }
            System.out.println("========================================================");
            System.out.println("========================================================");

        }

        String overviewCSVFileName = outputRootDir + File.separator + overviewFileNameStem;

        OneBMPropertiesPrinter.toOverviewCSVFile(datasetToPropertiesList, overviewCSVFileName);

    }



    public static Properties runOneBM(String dataSetName, String propertiesPath) {

//        String propertiesPath = args[0];

        Logger logger = Logger.getLogger("m");


        try {
            OneBMMain.programConfiguration = new ProgramConfiguration(propertiesPath);
            Connection databaseConnection = DatabaseConnection.getDatabaseConnection(programConfiguration);

            OneBMMain.jooqDatabaseInteractor = new JOOQDatabaseInteractor(
                    databaseConnection,
                    programConfiguration
            );

            joinTableNameGenerator = new NameGenerator();

            List<Table<?>> tables = jooqDatabaseInteractor.getTables();
            cleanUp(tables, jooqDatabaseInteractor);

            // create the relational graph. This is a DIRECTED graph
            Graph<String, ForeignKeyEdge> relationalGraph = TableGraph.createFrom(jooqDatabaseInteractor.getTables());
            System.out.println(TableGraph.asDOTString(relationalGraph));

            TargetTableManager targetTableManager = TargetTableManager.getInstanceManager(jooqDatabaseInteractor, programConfiguration);


            double distinctnessThreshold = 0.2;
            int absoluteDistinctnessThreshold = 40;
            FieldToFeatureTranformationSettings.setExistenceTestCreationDeciderToCategoricFieldUniqueValueCountController(
                    jooqDatabaseInteractor, distinctnessThreshold, absoluteDistinctnessThreshold
            );

            RelationalGraphBFSHandler<String> relationalGraphBFSHandler = new RelationalGraphBFSHandler<String>(relationalGraph, targetTableManager.getTargetTable().getName());


            RefactoredJoinTableConstruction refactoredJoinTableConstruction = new RefactoredJoinTableConstruction(
                    relationalGraphBFSHandler, jooqDatabaseInteractor, joinTableNameGenerator, targetTableManager.getTargetID(),
                    targetTableManager
            );

            CompleteFeatureTablePersistance.GLOBAL_FEATURE_TABLE_NAME = oneBMTableName;
            cleanUpSingleTableUnsafe(oneBMTableName, jooqDatabaseInteractor);


            OneBMTableBuilder oneBMTableBuilder = new OneBMTableBuilder(refactoredJoinTableConstruction, jooqDatabaseInteractor);

            long startTimeOneBMTableBuilding = System.nanoTime();
            oneBMTableBuilder.build(targetTableManager);
            long stopTimeOneBMTableBuilding = System.nanoTime();

            double durationSec = (stopTimeOneBMTableBuilding - startTimeOneBMTableBuilding) * Timer.convertNanoTimeToSeconds;

            System.out.println("time onebm table build (s):" + durationSec);


            String outputDirStr = outputRootDir + File.separator +
                    programConfiguration.getConfigurationOption(ProgramConfigurationOption.SCHEMA);

            Path outputDirPath = Paths.get(outputDirStr);
            if (!Files.exists(outputDirPath)) {
                Files.createDirectories(outputDirPath);
            }

            String fieldEncodingCSVString =
                    oneBMTableBuilder.getFeatureColumnEncoder().exportAsCSV(jooqDatabaseInteractor);
            String fieldEncodingCSVFileStr =
                    outputDirStr + File.separator + "field_encodings.csv";
            System.out.println(fieldEncodingCSVFileStr);
            try (BufferedWriter fieldEncodingWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fieldEncodingCSVFileStr), "utf-8"))){
                fieldEncodingWriter.write(fieldEncodingCSVString);
            } catch (IOException i) {
                i.printStackTrace();
            }


            String arffOutputFileStr = outputDirStr
                    + File.separator
                    +  "onebmtable.arff";

            DatabaseToARFFConvertor.convertDataSetToARFF(dataSetName, programConfiguration, TableType.ONEBM_TABLE, arffOutputFileStr);

            System.out.println("ONEBM: nb of non-target table features constructed" + refactoredJoinTableConstruction.totalNbOfFeaturesConstructed);

            Properties properties = new Properties();
            properties.setProperty(InfoEnum.DURATION_SEC.toString(), Double.toString(durationSec));
            properties.setProperty(InfoEnum.NB_OF_FEATURES_BUILD.toString(), Integer.toString(refactoredJoinTableConstruction.totalNbOfFeaturesConstructed));
            return properties;


        } catch(
                Exception e
        ){
            e.printStackTrace();

            if(cleanUpJoinTables){
                System.out.println("CLEANING UP TABLES: " + joinTableNameGenerator.getAllGeneratedNames());
                for (String generatedTableName : joinTableNameGenerator.getAllGeneratedNames()) {
                    CleanUp.cleanUpSingleTable(generatedTableName, jooqDatabaseInteractor);
                }
            }

            e.printStackTrace();
        }
        return null;
    }
}
