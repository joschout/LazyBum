package feature.featuretable.persistance;

import database.JOOQDatabaseInteractor;
import feature.featuretable.FeatureTableHandler;
import org.jooq.Query;
import utils.NameGenerator;

public abstract class FeatureTablePersistance {


    public static NameGenerator nameGenerator = new NameGenerator("featuretable", 0);

    protected JOOQDatabaseInteractor jooqDatabaseInteractor;


    public FeatureTablePersistance(JOOQDatabaseInteractor jooqDatabaseInteractor){
        this.jooqDatabaseInteractor = jooqDatabaseInteractor;
    }


//    public abstract void storeFeatureTable(FeatureTableHandler featureTableHandler, TraversalPath traversalPath, String tableName);

    public abstract void storeFeatureTable(FeatureTableHandler featureTableHandler, String descriptionOfHowFeatureFieldsWereCollected);




//    public DSLContext dslContext;


//    /**
//     * Returns a list of fields for a partial feature table, that is:
//     *  - the first element represents the targetid field
//     *  - the other elements represent the fields to add
//     * @param featureTableHandler
//     * @return
//     */
//    protected List<Field<?>> createFieldObjectsForFeatureTable(FeatureTableHandler featureTableHandler, TraversalPath traversalPath,
//                                                               boolean includetargetIDColumn){
//        List<Field<?>> columnFields = new ArrayList<>();
//
//        // add the target id and the feature columns
//        if(includetargetIDColumn) {
//            columnFields.add(featureTableHandler.getTargetIDField());
//        }
//
//        // todo: move this to instance scope
//        FeatureColumnEncoder featureColumnEncoder = new FeatureColumnEncoder();
//        Map<Field, ImmutablePair<String, String>> fieldEncoding = featureColumnEncoder.encode(traversalPath, featureTableHandler);
//
//        featureTableHandler.getFeatureColumnList().forEach(featureColumn ->
//            {   Field field = featureColumn.getOriginalField();
//                ImmutablePair<String, String> encodingAndDescription = fieldEncoding.get(field);
//                String encodingStr = encodingAndDescription.getLeft();
//
//                columnFields.add(DSL.field(encodingStr, field.getDataType()));
//            });
//
//
//        for(FeatureColumn featureColumn: featureTableHandler.getFeatureColumnList()){
//            columnFields.add(featureColumn.getAsField());
//        }
//        return columnFields;
//    }

    public void printQuery(Query query){
        String sqlQuery = query.getSQL();
        System.out.println("-----------------");
        System.out.println("--- SQL query ---\n");
        System.out.println(sqlQuery);
        System.out.println("\n-----------------\n");
    }

}
