package research;

import database.JOOQDatabaseInteractor;
import database.JOOQUtils;

import dataset.TargetTableManager;
import feature.columntype.UnsupportedFieldTypeException;
import feature.featuretable.FeatureTableHandler;
import feature.multisettable.MultiSetTableHandler;
import feature.multisettable.MultiSetTableTransformer;
import feature.tranform.UnsupportedFeatureTransformationException;
import graph.*;
import org.jooq.*;
import utils.NameGenerator;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")
public class RefactoredJoinTableConstruction {


    private final JOOQDatabaseInteractor jooqDatabaseInteractor;
    private DSLContext dslContext;
    private Schema schema;
    private RelationalGraphBFSHandler relationalGraphBFSHandler;
    private Field targetIDField;
    private TargetTableManager targetTableManager;

    private NameGenerator nameGenerator;

    public static boolean VERBOSE = false;

    public int totalNbOfFeaturesConstructed;

    public RefactoredJoinTableConstruction(RelationalGraphBFSHandler relationalGraphBFSHandler,
                                           JOOQDatabaseInteractor jooqDatabaseInteractor,
                                           NameGenerator nameGenerator, Field targetIDField,
                                           TargetTableManager targetTableManager) {
        this.jooqDatabaseInteractor = jooqDatabaseInteractor;
        this.relationalGraphBFSHandler = relationalGraphBFSHandler;
        this.dslContext = jooqDatabaseInteractor.getDslContext();
        this.nameGenerator = nameGenerator;
        this.schema = jooqDatabaseInteractor.getSchema();
        this.targetIDField = targetIDField;
        this.targetTableManager = targetTableManager;
        this.totalNbOfFeaturesConstructed = 0;
    }

// note: see also notes on creating conditions on fields (in dir doc)

    public Set<JoinInfo> oneStepJoinTableConstruction(@Nullable JoinInfo previousJoinInfo, List<Object> instanceIDsTrainingSet) throws GraphTraversalException, UnsupportedFieldTypeException, UnsupportedFeatureTransformationException {

        if(previousJoinInfo == null){
            return InitialJoinInfoBuilder.getInitialSingleJoinInfoSet(targetTableManager, this.dslContext);
        }

        Table currentJoinOfTables = previousJoinInfo.joinTable;
        Optional<TraversalPath> optionalParentTraversalPath = previousJoinInfo.getTraversalPath();
        FieldController currentFieldController = previousJoinInfo.fieldController;


        Table lastlyJoinedTable;
        int recursionDepth;
        if(optionalParentTraversalPath.isPresent()){
            recursionDepth = optionalParentTraversalPath.get().size();
            lastlyJoinedTable = optionalParentTraversalPath.get().getLastTable(jooqDatabaseInteractor);
        } else{
            recursionDepth = 0;
            lastlyJoinedTable = previousJoinInfo.joinTable;
        }

        if(VERBOSE){System.out.println("=== recursion depth: " + recursionDepth + "===");}

        //todo: add MAX_DEPTH for paths
        //todo: add redundant path removal
        //todo: lastly joined table is available from the traversalStep Stack

        //todo: also calculate features on the topmost table

        Set<JoinInfo> joinInfos = new HashSet<>();


        Set<ForeignKeyEdge> foreignKeysToBeChecked = relationalGraphBFSHandler.getDirectLinksToTablesWithHigherDepth(lastlyJoinedTable.getName());
        for (ForeignKeyEdge foreignKeyEdge: foreignKeysToBeChecked) {

            TraversalPath extendedTraveralPath = TraversalPath.extend(optionalParentTraversalPath.orElse(null), foreignKeyEdge, lastlyJoinedTable);
            JoinInfo joinInfo = createFeatureTableForPathExtension(currentJoinOfTables, extendedTraveralPath, currentFieldController,
                    instanceIDsTrainingSet);

            totalNbOfFeaturesConstructed += joinInfo.featureTableHandler.getNbOfFeatureColumns();

            joinInfos.add(joinInfo);
        }

        if(VERBOSE){
            System.out.println("=== END recursion depth: " + recursionDepth + "===");
            System.out.println("===============================");
        }
        return joinInfos;
    }


    /**
     * Creates a feature table for the extended traversal path.
     *
     *
     *
     *
     * @param currentJoinOfTables
     * @param extendedTraversalPath
     * @param currentFieldController
     * @param instanceIDsTrainingSet
     * @return
     * @throws UnsupportedFieldTypeException
     * @throws UnsupportedFeatureTransformationException
     */
    private JoinInfo createFeatureTableForPathExtension(Table currentJoinOfTables, TraversalPath extendedTraversalPath,
                                                        FieldController currentFieldController, List<Object> instanceIDsTrainingSet) throws UnsupportedFieldTypeException, UnsupportedFeatureTransformationException {

        TraversalStep lastTraversalStep = extendedTraversalPath.getLast();

        // create a new field controller
        FieldController newFieldController = FieldController.addFieldsOfNewTable(lastTraversalStep, currentFieldController, jooqDatabaseInteractor);
        Table tmpNewJoinOfTables = createTempJoinTable(currentJoinOfTables, lastTraversalStep, newFieldController, instanceIDsTrainingSet);
        Result<Record> records = dslContext
                .select()
                .from(tmpNewJoinOfTables).fetch();

        /*
         * calculate features on the newly joined table
         * */

        Set<String> namesOfNewlyAddedFieldsPossiblyAliased = newFieldController.getAliasedFieldsToAdd()
                .stream().map(Field::getName).collect(Collectors.toSet());
//
//        Set<String> namesOfNewlyAddedFields = aliasedTableFieldMap
//                .get(lastTraversalStep.getDestination()).values()
//                .stream().map(Field::getName).collect(Collectors.toSet());
//
        List<Field> newlyAddedFieldsPossiblyAliased = Arrays.stream(tmpNewJoinOfTables.fields())
                .filter(field -> namesOfNewlyAddedFieldsPossiblyAliased.contains(field.getName()))
                .collect(Collectors.toList());

        Map<Field, Field> fieldsAddedOriginalToAliasMap = newFieldController.getAddedOriginalFieldsToAliasMap();


        MultiSetTableHandler multiSetTableHandler = new MultiSetTableHandler(targetIDField, fieldsAddedOriginalToAliasMap, lastTraversalStep.getDestination(jooqDatabaseInteractor));
//        MultiSetTableHandler multiSetTableHandler = new MultiSetTableHandler(targetIDField, newlyAddedFieldsPossiblyAliased);
        multiSetTableHandler.add(records);
        if(VERBOSE) {
            System.out.println(multiSetTableHandler.toString());
        }
        FeatureTableHandler featureTableHandler = MultiSetTableTransformer.transform(multiSetTableHandler);
        if(VERBOSE) {
            System.out.println(featureTableHandler.toString());
        }
        return new JoinInfo(extendedTraversalPath, tmpNewJoinOfTables, featureTableHandler, newFieldController);
    }


    private Table createTempJoinTable(Table currentJoinOfTables, TraversalStep traversalStep,
                                      FieldController newFieldController,
                                      List<Object> instanceIDsTrainingSet
    ) {
        /*
         * 1. get the fields from the current join table
         *
         * */


        Table traversalStepDestination = traversalStep.getDestination(jooqDatabaseInteractor);

        // selection fields =
        // field of old join table
        // + added fields of newly joined table, PROPERLY ALIASED
//        List<Field> selectionFields = newFieldController.getPreviouslyAddedAliasedFields();
        List<Field> selectionFields = new ArrayList<>(Arrays.asList(currentJoinOfTables.fields()));
        selectionFields.addAll(newFieldController.getAliasedFieldsToAdd());
//        List<Field> selectionFields = getSelectionFieldsNewJoinOfTables(currentJoinOfTables, traversalStep);


        String tmpNewJoinTableName = nameGenerator.getNewName();
        Table tableRepresentationWithSchemaQualifiedName = jooqDatabaseInteractor.getTableRepresentationWithSchemaQualifiedName(tmpNewJoinTableName);
        if(VERBOSE) {
            System.out.println("New temp table name: " + tmpNewJoinTableName);
        }
        Condition joinCondition = newFieldController.getJoinCondition(currentJoinOfTables.getSchema().getName(), currentJoinOfTables.getName());
//        Condition joinCondition = createJoinCondition(traversalStep, newFieldController);
        if(VERBOSE) {
            System.out.println("--- join condition ---");
        }
        Condition instanceIDInTrainingSet =
                currentJoinOfTables.field(targetTableManager.getTargetID().getName())
                        .in(instanceIDsTrainingSet);


        Query query = this.dslContext
                .createTable(tableRepresentationWithSchemaQualifiedName).as(
                        dslContext
                                .select(selectionFields)
                                .from(currentJoinOfTables)
                                .join(traversalStepDestination).on(joinCondition)
//                                .fullOuterJoin(traversalStepDestination).on(joinCondition)
                                .where(instanceIDInTrainingSet)
                );

        String sqlQuery = query.getSQL();
        if(VERBOSE) {
            System.out.println("-----------------");
            System.out.println("--- SQL query ---\n");
            System.out.println(sqlQuery);
            System.out.println("\n-----------------\n");
        }
        // run the query
        query.execute();

        schema = jooqDatabaseInteractor.refreshSchema();
        Table tmpNewJoinTable = schema.getTable(tmpNewJoinTableName);
        if(VERBOSE) {
            System.out.println("check if created table actually exists: table info in schema:");
            System.out.println(JOOQUtils.tableInfo(tmpNewJoinTable, ""));
            System.out.println("field types: " + JOOQUtils.listOfFieldTypesToCSVString(Arrays.asList(tmpNewJoinTable.fields())));
        }
        return tmpNewJoinTable;

    }

}
