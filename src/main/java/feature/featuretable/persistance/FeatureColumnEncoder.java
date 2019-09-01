package feature.featuretable.persistance;

import database.JOOQDatabaseInteractor;
import feature.featuretable.FeatureColumn;
import feature.featuretable.FeatureTableHandler;
import feature.tranform.FieldTransformerEnumInterface;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.InsertValuesStepN;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import utils.NameGenerator;
import utils.PrettyPrinter;

import java.util.*;

/**
 * Responsible for assigning names to feature columns,
 * and saving a description of the feature column in a special table.
 */
public class FeatureColumnEncoder {

    public static boolean VERBOSE = false;

    public static String FEATURE_TABLE_FIELD_ENCODINGS_TABLE_NAME = "featuretable_field_encodings";
    public static final FeatureColumnEncoder DEFAULT_FEATURE_COLUMN_ENCODER = new FeatureColumnEncoder();

    public static int FIRST_FIELD_SUFFIX = 1; // used for the name generator
    public static final String FIELD_NAME_STEM = "f"; // used for the name generator

    public static final Field[] FIELDS = new Field[2];
    static{
        Field encoding = DSL.field("encoding", SQLDataType.INTEGER.nullable(false));
        Field description = DSL.field("description", SQLDataType.VARCHAR);
        FIELDS[0] = encoding;
        FIELDS[1] = description;
    }

    private NameGenerator nameGenerator;
//    private Map<Integer, String> featureColumnEncodingMap;

    public FeatureColumnEncoder(){
        this.nameGenerator = new NameGenerator(FIELD_NAME_STEM, FIRST_FIELD_SUFFIX);
    }


    public Map<FeatureColumn, ImmutablePair<Field, String>> encode(String descriptionOfHowFeatureFieldsWereCollected, FeatureTableHandler featureTableHandler){
//    public Map<FeatureColumn, ImmutablePair<Field, String>> encode(TraversalPath traversalPath, FeatureTableHandler featureTableHandler){

        Map<FeatureColumn, ImmutablePair<Field,String>> fieldToEncodingAndDescriptionMap = new HashMap<>();

        List<FeatureColumn> featureColumnList = featureTableHandler.getFeatureColumnList();

        for (FeatureColumn featureColumn : featureColumnList) {
            Field field = featureColumn.getOriginalField();

            String encodingStr = nameGenerator.getNewName();
//            System.out.println(encodingStr);

            DataType dataType;
            if(featureColumn.getTransformation().isPresent()){
                dataType = ((FieldTransformerEnumInterface) featureColumn.getTransformation().get()).getSqlDataType().nullable(true);
            } else{
                dataType = featureColumn.getOriginalField().getDataType().nullable(true);
            }

            Field encoding = DSL.field(encodingStr, dataType);

            String descriptionStr = "";
            Optional optionalTransformation = featureColumn.getTransformation();
            if (optionalTransformation.isPresent()) {
                FieldTransformerEnumInterface transformation = (FieldTransformerEnumInterface) optionalTransformation.get();
                String transformationStr = transformation.safeToString();
                descriptionStr = transformationStr + "(" + field.getName() + ")";
            } else {
                descriptionStr = field.getName();
            }
            descriptionStr += "<=" + descriptionOfHowFeatureFieldsWereCollected;

            fieldToEncodingAndDescriptionMap.put(featureColumn, new ImmutablePair<>(encoding, descriptionStr));
        }

        return fieldToEncodingAndDescriptionMap;
    }

    public boolean fieldEncodingTableExistsInDatabase(JOOQDatabaseInteractor jooqDatabaseInteractor){
        return jooqDatabaseInteractor.getTableByName(FEATURE_TABLE_FIELD_ENCODINGS_TABLE_NAME) != null;
    }

    public void createFeatureColumnMapTable(JOOQDatabaseInteractor jooqDatabaseInteractor){

        Table tableRepr = jooqDatabaseInteractor.getTableRepresentationWithSchemaQualifiedName(FEATURE_TABLE_FIELD_ENCODINGS_TABLE_NAME);
        jooqDatabaseInteractor.getDslContext()
                .createTable(tableRepr)
                .column(encodingField())
                .column(descriptionField())
                .constraints(
                        DSL.constraint("PK" + FEATURE_TABLE_FIELD_ENCODINGS_TABLE_NAME).primaryKey(encodingField())
                ).execute();
        if(VERBOSE){
            System.out.println("created feature table field encoding table");
        }
    }



    public void persistFieldEncodings(Collection<ImmutablePair<Field, String>> encodingAndDescriptionCollection,
                                 JOOQDatabaseInteractor jooqDatabaseInteractor){

        Table tableRepr = jooqDatabaseInteractor.getTableRepresentationWithSchemaQualifiedName(FEATURE_TABLE_FIELD_ENCODINGS_TABLE_NAME);

        InsertValuesStepN partialInsertQuery = jooqDatabaseInteractor.getDslContext().insertInto(tableRepr, FIELDS);


        for (ImmutablePair<Field, String> encodingAndDescription : encodingAndDescriptionCollection) {

            Object[] fieldValues = new Object[2];
//            String[] fieldValues = new String[2];

            String encoding = encodingAndDescription.getLeft().getName();

            fieldValues[0] = Integer.parseInt(encoding.substring(1));
            fieldValues[1] = encodingAndDescription.getRight();

            if(VERBOSE){
                System.out.println(PrettyPrinter.arrayToCSVString(fieldValues));
            }
            partialInsertQuery = partialInsertQuery.values(fieldValues);

//            Query updateTableQuery = jooqDatabaseInteractor.getDslContext()
//                    .update(tableRepr).set(
//                            row(FIELDS),
//                            row((Object[])fieldValues)
//                    );
//            updateTableQuery.execute();
        }

        partialInsertQuery.execute();

    }


    public List<Field<?>> encodeAndPersist(String descriptionOfHowFeatureFieldsWereCollected, FeatureTableHandler featureTableHandler,
                                           JOOQDatabaseInteractor jooqDatabaseInteractor) {
        List<Field<?>> fieldEncodings = new ArrayList<>();
        List<ImmutablePair<Field, String>> fieldEncodingsAndDescriptionsToAdd = new ArrayList<>();

        Map<FeatureColumn, ImmutablePair<Field, String>> fieldToEncodedFieldAndDescriptionMap =
                this.encode(descriptionOfHowFeatureFieldsWereCollected, featureTableHandler);

        // for each column encoding, add it to the list of columns to add for the feature table
        //     AND to the list column encodings to add to the field encoding table
        for(FeatureColumn featureColumn: featureTableHandler.getFeatureColumnList()){
            ImmutablePair<Field, String> encodingAndDescription = fieldToEncodedFieldAndDescriptionMap.get(featureColumn);

            // add to columns to add
            fieldEncodings.add(encodingAndDescription.getLeft());

            // add to rows in mapping
            fieldEncodingsAndDescriptionsToAdd.add(encodingAndDescription);
        }
        this.persistFieldEncodings(fieldEncodingsAndDescriptionsToAdd, jooqDatabaseInteractor);

        return fieldEncodings;
    }

    private Field encodingField(){
        return FIELDS[0];
    }

    private Field descriptionField(){
        return FIELDS[1];
    }


    public String exportAsCSV(JOOQDatabaseInteractor jooqDatabaseInteractor){

        Table tableRepr = jooqDatabaseInteractor.getTableRepresentationWithSchemaQualifiedName(FEATURE_TABLE_FIELD_ENCODINGS_TABLE_NAME);
        return jooqDatabaseInteractor.getDslContext()
                .selectFrom(tableRepr)
                .fetch()
                .formatCSV(',', "{null}");
    }


}
