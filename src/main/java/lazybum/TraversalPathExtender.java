package lazybum;

import database.JOOQDatabaseInteractor;
import database.JOOQUtils;
import graph.InvalidKeyInfoException;
import dataset.TargetTableManager;
import feature.columntype.UnsupportedFieldTypeException;
import feature.featuretable.FeatureTableHandler;
import feature.featuretable.persistance.FeatureColumnEncoder;
import feature.featuretable.persistance.FeatureTablePersistance;
import feature.featuretable.persistance.PartialFeatureTablePersistance;
import feature.multisettable.MultiSetTableHandler;
import feature.multisettable.MultiSetTableTransformer;
import feature.tranform.UnsupportedFeatureTransformationException;
import graph.*;
import org.jooq.*;
import utils.NameGenerator;
import utils.PrettyPrinter;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsible for creating the feature table extensions obtained by extending the end of a traversal path.
 *
 * Created by joschout.
 */
public class TraversalPathExtender {

    public static boolean VERBOSE = false;

    private final JOOQDatabaseInteractor jooqDatabaseInteractor;
    private DSLContext dslContext;
    private RelationalGraphBFSHandler relationalGraphBFSHandler;
    private Field targetIDField;
    private TargetTableManager targetTableManager;

    private NameGenerator multiSetTableNameGenerator;
    private FeatureColumnEncoder featureColumnEncoder;
    private NameGenerator featureTableNameGenerator;

    // skip recalculation of feature table extensions
    private boolean cacheFeatureTableExtensionsForPreviouslySeenTraversalPaths;
    private Map<TraversalPath, Set<FeatureTableExtension>> traversalPathsSeenBeforeToFeatureTableExtensionsMap;

    private boolean shouldPersistFeatureColumnExtensions;

    private int totalNbOfFeatureColumnsConstructed;


    public TraversalPathExtender(RelationalGraphBFSHandler relationalGraphBFSHandler,
                                 JOOQDatabaseInteractor jooqDatabaseInteractor,
                                 NameGenerator multiSetNameGenerator,
                                 TargetTableManager targetTableManager,
                                 FeatureColumnEncoder featureColumnEncoder,
                                 boolean useSpeedUpTrick,
                                 boolean shouldPersistFeatureColumnExtensions
                                 ) {
        this.jooqDatabaseInteractor = jooqDatabaseInteractor;
        this.relationalGraphBFSHandler = relationalGraphBFSHandler;
        this.dslContext = jooqDatabaseInteractor.getDslContext();
        this.multiSetTableNameGenerator = multiSetNameGenerator;
        this.targetIDField = targetTableManager.getTargetID();
        this.targetTableManager = targetTableManager;

        this.featureColumnEncoder = featureColumnEncoder;
        this.featureTableNameGenerator = FeatureTablePersistance.nameGenerator;

        this.cacheFeatureTableExtensionsForPreviouslySeenTraversalPaths = useSpeedUpTrick;
        if(useSpeedUpTrick) {
            traversalPathsSeenBeforeToFeatureTableExtensionsMap = new HashMap<>();
        }

        this.shouldPersistFeatureColumnExtensions = shouldPersistFeatureColumnExtensions;

        totalNbOfFeatureColumnsConstructed = 0;


    }

    /**
     * Creates the feature table extensions by extending a single traversal path
     * @param endOfPath
     * @param traversalPath
     * @return
     * @throws InvalidKeyInfoException
     * @throws UnsupportedFieldTypeException
     * @throws GraphTraversalException
     * @throws UnsupportedFeatureTransformationException
     */
    public Set<FeatureTableExtension> extend(Table endOfPath, @Nullable TraversalPath traversalPath)
            throws InvalidKeyInfoException, UnsupportedFieldTypeException, GraphTraversalException, UnsupportedFeatureTransformationException {


        if(cacheFeatureTableExtensionsForPreviouslySeenTraversalPaths) {
            if(traversalPathsSeenBeforeToFeatureTableExtensionsMap.containsKey(traversalPath)){
                if(VERBOSE){System.out.println("SEEN THIS TRAVERSAL PATH BEFORE --> REUSE FEATURE TABLE EXTENSIONS");}
                return traversalPathsSeenBeforeToFeatureTableExtensionsMap.get(traversalPath);
            }else{
                if(VERBOSE){System.out.println("EXTENDING " + (traversalPath == null ? "empty path" : traversalPath.toString()));}
            }
        }

        Set<FeatureTableExtension> featureTableExtensions = new HashSet<>();

        // get the neigboring tables
        Set<ForeignKeyEdge> foreignKeysToBeChecked
                = relationalGraphBFSHandler.getDirectLinksToTablesWithHigherDepth(endOfPath.getName());
        for (ForeignKeyEdge foreignKeyEdge: foreignKeysToBeChecked) {

            // extend the current traversal path with the foreign key
            TraversalPath extendedTraveralPath =
                    TraversalPath.extend(traversalPath, foreignKeyEdge, endOfPath);

            extendedTraveralPath = handleAssociationTableAtEndOfTraversalPath(extendedTraveralPath);


            // create the multiset table corresponding with the path extension
            MultiSetTableHandler multiSetTableHandler = getMultiSetTableHandler(extendedTraveralPath);

            // create the partial feature table corresponding with the multiset table
            FeatureTableHandler featureTableHandler = MultiSetTableTransformer.transform(multiSetTableHandler);

            // represent the partial feature table as a feature table extension
            String featureTableName = this.featureTableNameGenerator.getNewName();
            FeatureTableExtension featureTableExtension = new FeatureTableExtension(featureTableName, extendedTraveralPath, featureTableHandler);
            featureTableExtensions.add(featureTableExtension);

            totalNbOfFeatureColumnsConstructed += featureTableHandler.getNbOfFeatureColumns();


            if(shouldPersistFeatureColumnExtensions){
                // persist the partial feature table
                PartialFeatureTablePersistance partialFeatureTablePersistance
                        = new PartialFeatureTablePersistance(jooqDatabaseInteractor, featureColumnEncoder);
                partialFeatureTablePersistance
                        .storeFeatureTable(featureTableHandler, extendedTraveralPath.toStringCompact(""), featureTableName);
            }
        }

        if(cacheFeatureTableExtensionsForPreviouslySeenTraversalPaths){
            traversalPathsSeenBeforeToFeatureTableExtensionsMap.put(traversalPath, featureTableExtensions);
        }

        return featureTableExtensions;
    }



    private MultiSetTableHandler getMultiSetTableHandler(TraversalPath extendedTraveralPath) throws InvalidKeyInfoException, GraphTraversalException, UnsupportedFieldTypeException {

        String multiSetTableName = multiSetTableNameGenerator.getNewName();

        /* note: very inefficient with respect to which instance ids are kept,
         *       as no instance ids are filtered
         */
        // note: first field is the target field
        Field[] selectionFields = TraversalPathDataGatheringQueryBuilder
                .makeMultiSetTable(multiSetTableName, null, jooqDatabaseInteractor, targetTableManager,
                        extendedTraveralPath, null);


        if(VERBOSE){
            System.out.println("traversal path:\n" + extendedTraveralPath.toString("\t"));
            System.out.println("selection fields:\n\t" + PrettyPrinter.arrayToCSVString(selectionFields));
        }

        Map<Field, Field> fieldsToSelectFromMultiSetAliasMap = new HashMap<>();
        for (int i = 1; i < selectionFields.length; i++) {
            Field selectionField = selectionFields[i];
//        }
//
//        for (Field selectionField : selectionFields) {
            fieldsToSelectFromMultiSetAliasMap.put(selectionField, selectionField);
        }
        MultiSetTableHandler multiSetTableHandler =
                new MultiSetTableHandler(
                        targetIDField, fieldsToSelectFromMultiSetAliasMap,
                        extendedTraveralPath.getLastTable(jooqDatabaseInteractor));
        jooqDatabaseInteractor.refreshSchema();
        Table multiSetTable = jooqDatabaseInteractor.getTableRepresentationWithSchemaQualifiedName(multiSetTableName);

        Result<Record> results = dslContext
                .select()
                .from(multiSetTable)
                .fetch();
        multiSetTableHandler.add(results);
        return multiSetTableHandler;
    }


    private TraversalPath handleAssociationTableAtEndOfTraversalPath(TraversalPath extendedTraveralPath) throws GraphTraversalException {
        Table possiblyAnAssociationTable = extendedTraveralPath.getLastTable(jooqDatabaseInteractor);

        /*
         * Check whether the table at the end of the extended path is an associative table, encoding a many-to-many relationship
         *
         * */
        boolean isTableAnAssociationTable = JOOQUtils.isTableAnAssociationTable(possiblyAnAssociationTable);
        if(isTableAnAssociationTable) {

            Set<ForeignKeyEdge> foreignKeysOfAssociationTable
                    = relationalGraphBFSHandler.getDirectLinksToTablesWithHigherDepth(possiblyAnAssociationTable.getName());
            if (foreignKeysOfAssociationTable.size() != 1) {
                System.out.println("table " + possiblyAnAssociationTable.getName() + " is an association table," +
                        " but does not connect to a table with a higher depth");
                return extendedTraveralPath;
            } else {
                ForeignKeyEdge foreignKeyEdgeOfAssociationTable
                        = foreignKeysOfAssociationTable.stream().findFirst().get();
                extendedTraveralPath =
                        TraversalPath.extend(
                                extendedTraveralPath, foreignKeyEdgeOfAssociationTable,
                                extendedTraveralPath.getLastTable(jooqDatabaseInteractor));



                List<ForeignKey> references = possiblyAnAssociationTable.getReferences();
                String str = "table " + possiblyAnAssociationTable.getName() + " is an association table with foreign keys:\n";
                for (ForeignKey reference : references) {
                    List<Field> fkTableFields = reference.getFields();
                    String fkString =
                            reference.getTable().getName() +"("
                                    + fkTableFields.stream().map(Field::getName).collect(Collectors.joining(","))
                                    + ")";

                    List<Field> pkTableFields = reference.getKey().getFields();
                    String pkString =
                            reference.getKey().getTable().getName() + "("
                                    + pkTableFields.stream().map(Field::getName).collect(Collectors.joining(","))
                                    +")";


                    str += "\t" +fkString + "--" + pkString + "\n";
                }
                System.out.println(str);

            }
        }

        return extendedTraveralPath;
    }

    public void resetFeatureColumnCounter(){
        totalNbOfFeatureColumnsConstructed = 0;
    }


    public int getTotalNbOfFeatureColumnsConstructed() {
        return totalNbOfFeatureColumnsConstructed;
    }
}
