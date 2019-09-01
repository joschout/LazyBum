package feature.featuretable.persistance;

import database.JOOQDatabaseInteractor;
import dataset.Example;
import dataset.TargetTableManager;
import feature.featuretable.FeatureColumn;
import feature.featuretable.FeatureTableHandler;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.row;

public class CompleteFeatureTablePersistance extends FeatureTablePersistance {

    public static String GLOBAL_FEATURE_TABLE_NAME = "featuretable_global";

    private String globalFeatureTableName;
    private FeatureColumnEncoder featureColumnEncoder;

    public CompleteFeatureTablePersistance(JOOQDatabaseInteractor jooqDatabaseInteractor, String globalFeatureTableName, FeatureColumnEncoder featureColumnEncoder) {
        super(jooqDatabaseInteractor);
        this.globalFeatureTableName = globalFeatureTableName;
        this.featureColumnEncoder = featureColumnEncoder;
    }

    public CompleteFeatureTablePersistance(JOOQDatabaseInteractor jooqDatabaseInteractor, String globalFeatureTableName) {
        this(jooqDatabaseInteractor, globalFeatureTableName, new FeatureColumnEncoder());
    }

    public CompleteFeatureTablePersistance(JOOQDatabaseInteractor jooqDatabaseInteractor) {
        this(jooqDatabaseInteractor, CompleteFeatureTablePersistance.GLOBAL_FEATURE_TABLE_NAME);
    }

    @Override
    public void storeFeatureTable(FeatureTableHandler featureTableHandler, String descriptionOfHowFeatureFieldsWereCollected) {
        // 1. check if there already exists a global feature table

        if(! globalFeatureTableExistsInDatabase()){

            if(! featureColumnEncoder.fieldEncodingTableExistsInDatabase(jooqDatabaseInteractor)){
                featureColumnEncoder.createFeatureColumnMapTable(jooqDatabaseInteractor);
            }

            PartialFeatureTablePersistance partialFeatureTablePersistance = new PartialFeatureTablePersistance(jooqDatabaseInteractor, featureColumnEncoder);
            partialFeatureTablePersistance.storeFeatureTable(featureTableHandler, descriptionOfHowFeatureFieldsWereCollected, globalFeatureTableName);


        } else{

//            List<Field<?>> columnsToAdd = createFieldObjectsForFeatureTable(featureTableHandler, false);


            List<Field<?>> columnsToAdd = featureColumnEncoder.encodeAndPersist(descriptionOfHowFeatureFieldsWereCollected, featureTableHandler, jooqDatabaseInteractor);
            addColumnsToGlobalFeatureTable(columnsToAdd);
            updateTableWithData(featureTableHandler, columnsToAdd);
        }
    }


    private void updateTableWithData(FeatureTableHandler featureTableHandler,
                                     List<Field<?>> columnFields) {

        Table globalFeatureTable = jooqDatabaseInteractor.getTableByName(globalFeatureTableName);

        List<FeatureColumn> featureColumnList = featureTableHandler.getFeatureColumnList();
        Object[] fieldValues = new Object[featureColumnList.size()];

        boolean printedQueryOnce = false;

        // for each instance
        for(Object instanceID: featureTableHandler.getInstanceToListIndexMap().keySet()){
            int instanceIndex = featureTableHandler.getInstanceToListIndexMap().get(instanceID);

            Condition rowIDCondition = DSL.field(featureTableHandler.getTargetIDField().getName()).eq(instanceID);

            for (int i = 0; i < featureColumnList.size(); i++) {

                Object featureValue = featureColumnList.get(i).getFeatureValueForInstance(instanceIndex);
                if(featureValue == null) {
                    fieldValues[i] = DSL.value(null, columnFields.get(i).getType());
                } else{
                    fieldValues[i] = featureValue;
                }
            }
            Query updateTableQuery = jooqDatabaseInteractor.getDslContext()
                    .update(globalFeatureTable)
                    .set(
                        row(columnFields),
                        row(fieldValues) )
                    .where(rowIDCondition);

            if(! printedQueryOnce){
                System.out.println(updateTableQuery.getSQL());
                printedQueryOnce = true;
            }

            try{
//            printQuery(updateTableQuery);
             updateTableQuery.execute();
            } catch(Exception e){
                System.out.println("something went wrong here");
                System.out.println(e.getMessage());
                System.out.println(columnFields.stream().map(Field::getName).collect(Collectors.joining(",")));
                System.out.println(Arrays.stream(fieldValues).map(Objects::toString).collect(Collectors.joining(",")));



                throw e;
            }

        }
    }


    /**
     * Use SQL to add the provided fields to the table
     *
     * @param columnsToAdd
     */
    private void addColumnsToGlobalFeatureTable(List<Field<?>> columnsToAdd) {

        Table globalFeatureTable = jooqDatabaseInteractor.getTableByName(globalFeatureTableName);

        for (Field nonTargetColumnField: columnsToAdd ){
            jooqDatabaseInteractor.getDslContext()
                    .alterTable(globalFeatureTable).addColumn(nonTargetColumnField).execute();
        }

        jooqDatabaseInteractor.refreshSchema();
    }

    private boolean globalFeatureTableExistsInDatabase(){
        return jooqDatabaseInteractor.getTableByName(this.globalFeatureTableName) != null;
    }


    public void addLabelColumn(List<Example> exampleList, TargetTableManager targetTableManager){
        Table globalFeatureTable = jooqDatabaseInteractor.getTableByName(globalFeatureTableName);

        // 1. insert new column in table

        Field originalTargetLabelField = targetTableManager.getTargetColumn();
        Field targetLabelField = DSL.field(originalTargetLabelField.getName(), originalTargetLabelField.getDataType().nullable(true));

        jooqDatabaseInteractor.getDslContext()
                .alterTable(globalFeatureTable)
                .addColumn(targetLabelField)
                .execute();

        // refresh, just in case
        jooqDatabaseInteractor.refreshSchema();
        globalFeatureTable = jooqDatabaseInteractor.getTableByName(globalFeatureTableName);


        // 2. add labels to the table
        Field targetIDField = targetTableManager.getTargetID();

        Field[] columnFields = new Field[1];
        columnFields[0] = targetLabelField;

        for (Example example : exampleList) {

            Object[] fieldValues = {example.label};
            Condition rowIDCondition = DSL.field(targetIDField.getName()).eq(example.instanceID);


            jooqDatabaseInteractor.getDslContext()
                .update(globalFeatureTable)
                .set(
                        row(columnFields),
                        row(fieldValues) )
                .where(rowIDCondition)
                .execute();
        }
    }


    public FeatureColumnEncoder getFeatureColumnEncoder() {
        return featureColumnEncoder;
    }
}
