package dataset;

import config.ImproperProgramConfigurationException;
import config.ProgramConfiguration;
import config.ProgramConfigurationOption;
import database.DatabaseConnection;
import database.JOOQDatabaseInteractor;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TargetTableManager {

    private Table targetTable; // main table
    private Field targetID; // id of the instances
    private Field targetColumn; // column to predict

    private DSLContext dslContext;


    private Set<Object> possibleLabels;

    public TargetTableManager(Table targetTable, Field targetID, Field targetColumn, DSLContext dslContext) {
        this.targetTable = targetTable;
        this.targetID = targetID;
        this.targetColumn = targetColumn;

        this.dslContext = dslContext;
    }


    public static TargetTableManager getInstanceManager(JOOQDatabaseInteractor jooqDatabaseInteractor, ProgramConfiguration programConfiguration){

        // get the target table, based on its name
        Table targetTable = jooqDatabaseInteractor.getTableByName(programConfiguration.getConfigurationOption(ProgramConfigurationOption.TARGET_TABLE));

        // get targetID from the target table
        Field targetIDFieldFromTable = targetTable.field(programConfiguration.getConfigurationOption(ProgramConfigurationOption.TARGET_ID));

        // get targetColumn from the target table
        Field targetColumnFieldFromTable = targetTable.field(programConfiguration.getConfigurationOption(ProgramConfigurationOption.TARGET_COLUMN));
        return new TargetTableManager(targetTable, targetIDFieldFromTable, targetColumnFieldFromTable, jooqDatabaseInteractor.getDslContext());
    }

    public List<Object>getAllExampleInstanceIDs(){
        return getAllExampleInstanceIDs(this.targetTable, this.targetID, this.dslContext);
    }


    public Set<Object> getPossibleLabels(){
        if(possibleLabels != null){
            return possibleLabels;
        } else{
          return collectPossibleLabels(this.targetTable, this.targetColumn, this.dslContext);
        }
    }

    public static Set<Object> collectPossibleLabels(Table<?> targetTable, Field targetColumn, DSLContext dslContext){
        Set<Object> possibleLabels = new HashSet<>();
        Result<Record> records = dslContext
                .selectDistinct(targetColumn)
                .from(targetTable).fetch();
        records.forEach(
                record -> {
                    Object targetColumnValue = record.get(targetColumn);
                    possibleLabels.add(targetColumnValue);
                });
        return possibleLabels;

    }

    public static List<Object> getAllExampleInstanceIDs(Table<?> mainTable, Field instanceIDField, DSLContext dslContext){
        List<Object> instanceIDs = new ArrayList<>();
        Result<Record> records = dslContext
                .select(instanceIDField)
                .from(mainTable).fetch();
        records.forEach(
                record -> {
                    Object instanceIDvalue = record.get(instanceIDField);
                    instanceIDs.add(instanceIDvalue);
                });


//        instanceIDs.forEach(
//                instanceID -> System.out.println(instanceID.toString())
//        );

        return instanceIDs;
    }


    public List<Example> getInstancesAsExamples(List<Object> instanceIDsToSelect){
        List<Example> exampleList = new ArrayList<>();
        Result<Record> records = dslContext
                .select(targetID, targetColumn)
                .from(targetTable)
                .where(targetID.in(instanceIDsToSelect)).fetch();
        records.forEach(
                record -> {
                    Object instanceIDvalue = record.get(targetID);
                    Object targetValue = record.get(targetColumn);
                    exampleList.add(new Example(instanceIDvalue, targetValue));
                });

        return exampleList;


    }

    public List<Example> getInstancesAsExamples(){
        if(this.possibleLabels == null){
            possibleLabels = new HashSet<>();
        }

        List<Example> exampleList = new ArrayList<>();
        Result<Record> records = dslContext
                .select(targetID, targetColumn)
                .from(targetTable).fetch();
        records.forEach(
                record -> {
                    Object instanceIDvalue = record.get(targetID);
                    Object targetValue = record.get(targetColumn);

                    possibleLabels.add(targetValue);

                    exampleList.add(new Example(instanceIDvalue, targetValue));
                });

        return exampleList;
    }


    public static List<Example> getAsExamples(Table<?> mainTable, Field instanceIDField, Field targetColumn, DSLContext dslContext) {
        List<Example> exampleList = new ArrayList<>();
        Result<Record> records = dslContext
                .select(instanceIDField, targetColumn)
                .from(mainTable).fetch();
        records.forEach(
                record -> {
                    Object instanceIDvalue = record.get(instanceIDField);
                    Object targetValue = record.get(targetColumn);
                    exampleList.add(new Example(instanceIDvalue, targetValue));
                });

        return exampleList;
    }

    public Result<Record> getTableResults(Table table, DSLContext dslContext){
        return dslContext
                .select()
                .from(table).fetch();

    }

    public Table getTargetTable() {
        return targetTable;
    }

    public Field getTargetID() {
        return targetID;
    }

    public Field getTargetColumn() {
        return targetColumn;
    }

    public DSLContext getDslContext() {
        return dslContext;
    }

    public static void main(String[] args) throws IOException, ImproperProgramConfigurationException, SQLException {

        String propertiesPath = args[0];

        ProgramConfiguration programConfiguration = new ProgramConfiguration(propertiesPath);
        Connection databaseConnection = DatabaseConnection.getDatabaseConnection(programConfiguration);

        JOOQDatabaseInteractor jooqDatabaseInteractor = new JOOQDatabaseInteractor(
                databaseConnection,
                programConfiguration
        );

        Schema schema = jooqDatabaseInteractor.getSchema();

        DSLContext dslContext = jooqDatabaseInteractor.getDslContext();
        // get the target table, based on its name
        Table targetTable = schema.getTable(
                programConfiguration.getConfigurationOption(ProgramConfigurationOption.TARGET_TABLE));

        // get targetIDField from the target table
        Field targetIDFieldFromTable = targetTable.field(programConfiguration.getConfigurationOption(ProgramConfigurationOption.TARGET_ID));


        Field targetIDField = DSL.field(
                programConfiguration.getConfigurationOption(ProgramConfigurationOption.TARGET_ID), targetIDFieldFromTable.getDataType());

        Field targetColumnFieldFromTable = targetTable.field(programConfiguration.getConfigurationOption(ProgramConfigurationOption.TARGET_COLUMN));

        TargetTableManager targetTableManager = new TargetTableManager(targetTable, targetIDField, targetColumnFieldFromTable, jooqDatabaseInteractor.getDslContext());
        targetTableManager.getAllExampleInstanceIDs(targetTable, targetIDField, dslContext);

    }

}
