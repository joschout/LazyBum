package feature.featuretable.persistance;

import database.JOOQDatabaseInteractor;
import feature.featuretable.FeatureColumn;
import feature.featuretable.FeatureTableHandler;
import org.jooq.Field;
import org.jooq.InsertValuesStepN;
import org.jooq.Query;
import org.jooq.Table;

import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.constraint;

public class PartialFeatureTablePersistance extends FeatureTablePersistance{

    public static boolean VERBOSE = false;

    private FeatureColumnEncoder featureColumnEncoder;


    public PartialFeatureTablePersistance(JOOQDatabaseInteractor jooqDatabaseInteractor, FeatureColumnEncoder featureColumnEncoder) {
        super(jooqDatabaseInteractor);
        this.featureColumnEncoder = featureColumnEncoder;
    }

    public void storeFeatureTable(FeatureTableHandler featureTableHandler, String descriptionOfHowFeatureFieldsWereCollected,
                                  String tableName) {

        Table tableRepresentation = jooqDatabaseInteractor.getTableRepresentationWithSchemaQualifiedName(tableName);

        List<Field<?>> columnsToAdd = new ArrayList<>();
        // add the target identifier column
        columnsToAdd.add(featureTableHandler.getTargetIDField());

        // the other columns, encoded and the encoding persisted in the database



        List<Field<?>> encodedFields = featureColumnEncoder.encodeAndPersist(descriptionOfHowFeatureFieldsWereCollected, featureTableHandler, jooqDatabaseInteractor);
//        List<Field<?>> encodedFields = featureColumnEncoder.encodeAndPersist(traversalPath, featureTableHandler, jooqDatabaseInteractor);
        columnsToAdd.addAll(encodedFields);

//        List<Field<?>> columnFields = createFieldObjectsForFeatureTable(featureTableHandler, true);
        createTableInDatabase(featureTableHandler, tableName, tableRepresentation, columnsToAdd);
        insertDataIntoTable(featureTableHandler, tableName, tableRepresentation, columnsToAdd);
    }

    @Override
    public void storeFeatureTable(FeatureTableHandler featureTableHandler, String descriptionOfHowFeatureFieldsWereCollected){
        storeFeatureTable(featureTableHandler, descriptionOfHowFeatureFieldsWereCollected, FeatureTablePersistance.nameGenerator.getNewName());
    }


    private void createTableInDatabase(FeatureTableHandler featureTableHandler, String tableName, Table featureTable,
                                       List<Field<?>> columnFields){


        Query createTableQuery = jooqDatabaseInteractor.getDslContext()
                .createTable(featureTable)
                .columns(columnFields)
                .constraints(
                        constraint("PK_"+ tableName + "_" + featureTableHandler.getTargetIDField().getName())
                                .primaryKey(featureTableHandler.getTargetIDField().getName())
                );

        if(VERBOSE) {
            printQuery(createTableQuery);
        }
        createTableQuery.execute();
    }

    private void insertDataIntoTable(FeatureTableHandler featureTableHandler, String tableName, Table featureTable,
                                     List<Field<?>> columnFields){

        InsertValuesStepN partialInsertQuery =
                jooqDatabaseInteractor.getDslContext()
                        .insertInto(featureTable, columnFields);

        // reuse a fixed-size array to temporarily store the records
        Object[] values = new Object[columnFields.size()];

        // for each instance
        for(Object instanceID: featureTableHandler.getInstanceToListIndexMap().keySet()){
            int instanceIndex = featureTableHandler.getInstanceToListIndexMap().get(instanceID);

            // create the record
            values[0] = instanceID;
            int valueIndex = 1;
            for(FeatureColumn featureColumn: featureTableHandler.getFeatureColumnList()){
                values[valueIndex] = featureColumn.getFeatureValueForInstance(instanceIndex);

                valueIndex++;
            }

            // add the record
            /* TODO: this might be adding to much data at once,
                see also https://github.com/jOOQ/jOOQ/issues/6604
            */

            partialInsertQuery = partialInsertQuery
                    .values(values);

        }
        partialInsertQuery.execute();


    }


}
