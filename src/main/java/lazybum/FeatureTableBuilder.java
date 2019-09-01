package lazybum;

import database.JOOQDatabaseInteractor;
import graph.InvalidKeyInfoException;
import dataset.TargetTableManager;
import feature.columntype.UnsupportedFieldTypeException;
import feature.featuretable.FeatureTableHandler;
import feature.featuretable.persistance.CompleteFeatureTablePersistance;
import feature.featuretable.persistance.FeatureColumnEncoder;
import feature.multisettable.MultiSetTableHandler;
import feature.multisettable.MultiSetTableTransformer;
import feature.tranform.UnsupportedFeatureTransformationException;
import graph.GraphTraversalException;
import graph.TraversalPath;
import org.jooq.*;

import java.util.*;
import java.util.stream.Collectors;


public class FeatureTableBuilder {

    public static boolean VERBOSE = false;

    private final JOOQDatabaseInteractor jooqDatabaseInteractor;
    private DSLContext dslContext;
    private TargetTableManager targetTableManager;

    private FeatureColumnEncoder featureColumnEncoder;
    private TraversalPathExtender traversalPathExtender;

    private TraversalGraphExtensionStrategy traversalGraphExtensionStrategy;

    public static String ROOT_FEATURE_TABLE_NAME = "featuretable_root";

    public int nbOfCallsToExtendFeatureTable = 0;


    public FeatureTableBuilder(JOOQDatabaseInteractor jooqDatabaseInteractor,
                               TraversalPathExtender traversalPathExtender,
                               TraversalGraphExtensionStrategy traversalGraphExtensionStrategy,
                               TargetTableManager targetTableManager,
                               FeatureColumnEncoder featureColumnEncoder
                               ) {
        this.jooqDatabaseInteractor = jooqDatabaseInteractor;
        this.dslContext = jooqDatabaseInteractor.getDslContext();
        this.targetTableManager = targetTableManager;

        this.traversalPathExtender = traversalPathExtender;
        this.traversalGraphExtensionStrategy = traversalGraphExtensionStrategy;

        this.featureColumnEncoder = featureColumnEncoder;

    }

    private FeatureTableExtension buildFeatureTableExtensionForTargetTable() throws UnsupportedFieldTypeException, UnsupportedFeatureTransformationException {
        Table targetTable = targetTableManager.getTargetTable();
        Field targetIDField = targetTableManager.getTargetID();
        Field targetLabel = targetTableManager.getTargetColumn();

        List<Field> fieldsToSelect = Arrays.stream(targetTable.fields())
                .filter(field
                        -> !field.equals(targetIDField)
                        && !field.equals(targetLabel))
                .collect(Collectors.toList());

        Result<Record> records = dslContext
                .select()
                .from(targetTable).fetch();
        Map<Field, Field> fieldsToSelectAliasMap = new HashMap<>();
        for(Field fieldToSelect: fieldsToSelect){
            fieldsToSelectAliasMap.put(fieldToSelect, fieldToSelect);
        }
        MultiSetTableHandler targetTableMultiSetTableHandler = new MultiSetTableHandler(targetIDField, fieldsToSelectAliasMap, targetTable);

        /*
         * NOTE: it is weird that
         *    FieldController contains all fields (including the targetid and targetlabel),
         *   but MultiSetTableHandler contains all fields EXCEPT the targetid and targetlabel
         * */


        targetTableMultiSetTableHandler.add(records);
        FeatureTableHandler targetTableFeatureTableHandler = MultiSetTableTransformer.transform(targetTableMultiSetTableHandler);
        if(VERBOSE) {
            System.out.println("Feature table extracted from target table: ");
            System.out.println(targetTableFeatureTableHandler.toString());
        }
        FeatureTableExtension featureTableExtensionOfTargetTable
                = new FeatureTableExtension(ROOT_FEATURE_TABLE_NAME, null, targetTableFeatureTableHandler);

        //-------------------------------------------------------------------------------------------


        // store the features from the target table
        // persist the partial feature table
//        PartialFeatureTablePersistance partialFeatureTablePersistance
//                = new PartialFeatureTablePersistance(jooqDatabaseInteractor, featureColumnEncoder);
//        String descriptionOfHowFeatureFieldsWereCollected = targetTable.getName();
//        partialFeatureTablePersistance
//                .storeFeatureTable(targetTableFeatureTableHandler, descriptionOfHowFeatureFieldsWereCollected, ROOT_FEATURE_TABLE_NAME);



        CompleteFeatureTablePersistance featureTablePersistance = new CompleteFeatureTablePersistance(jooqDatabaseInteractor,
                ROOT_FEATURE_TABLE_NAME, this.featureColumnEncoder);
        String descriptionOfHowFeatureFieldsWereCollected = targetTable.getName();
        featureTablePersistance.storeFeatureTable(targetTableFeatureTableHandler, descriptionOfHowFeatureFieldsWereCollected);


        return featureTableExtensionOfTargetTable;
    }


    public FeatureTableInfo buildFeatureTableForRootNode() throws UnsupportedFieldTypeException, UnsupportedFeatureTransformationException, GraphTraversalException, InvalidKeyInfoException {

        traversalPathExtender.resetFeatureColumnCounter();

        FeatureTableExtension targetTableFeatureTableExtension = buildFeatureTableExtensionForTargetTable();
        //-------------------------------------------------------------------------------------------
        Table targetTable = targetTableManager.getTargetTable();
        /*
         * Feature table extensions from immediate neighbors of the target table.
         * */
        Set<FeatureTableExtension> featureTableExtensions = traversalPathExtender.extend(targetTable, null);
        featureTableExtensions.add(targetTableFeatureTableExtension);


        return new FeatureTableInfo(
                null,
                new FTTraversalGraph(
                        featureTableExtensions.stream()
                                .map(FeatureTableExtension::getExtendedTraversalPath)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toSet())
                ),
                featureTableExtensions
        );

    }

    /**
     * Extend the current feature table
     * heuristic: possible tables to extend the current_feature_table are those that:
     *   1. are used in a feature test of an ancestor node
     *   2. still have (?unjoined) neighbors to join on.
     *
     * choice: which table to find neigbors for?
     *   all the tables found in the heuristic, or only a subset?
     *
     * current table = a join TREE  (assumption: forward joins)
     *                   root node = target node
     *                   leaf nodes = nodes for which the neighbors have not been used
     * NOTE: the join TREE is different for each dt node
     * possibility: pass the set of all tables that might still be extendable as an argument
     *
     * */
    public Optional<FeatureTableInfo> extendFeatureTable(FeatureTableInfo currentFeatureTableInfo, LazyBumTreeNode currentLazyBumTreeNode)
            throws GraphTraversalException, InvalidKeyInfoException, UnsupportedFieldTypeException, UnsupportedFeatureTransformationException {

        nbOfCallsToExtendFeatureTable++;

        Map<TraversalPath, Table> traversalPathsToExtend
                = traversalGraphExtensionStrategy
                    .getTraversalPathsToExtend(currentFeatureTableInfo.getTraversalGraph(), currentLazyBumTreeNode,
                            jooqDatabaseInteractor, targetTableManager);

        Set<FeatureTableExtension> featureTableExtensions = new HashSet<>();
        Map<TraversalPath, Set<TraversalPath>> traversalPathExtensions = new HashMap<>();

        for (TraversalPath traversalPath: traversalPathsToExtend.keySet()) {
            Table endOfPath = traversalPathsToExtend.get(traversalPath);

            Set<FeatureTableExtension> possibleFeatureTableExtensions = traversalPathExtender.extend(endOfPath, traversalPath);

            if(! possibleFeatureTableExtensions.isEmpty()){
                // if there are traversal path extensions
                // add the feature table extension for each TraversalPath extension to the collection
                featureTableExtensions.addAll(possibleFeatureTableExtensions);

                // save which traversal paths are extended
                traversalPathExtensions.put(
                        traversalPath,
                        possibleFeatureTableExtensions.stream()
                                .map(FeatureTableExtension::getExtendedTraversalPath)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toSet())
                );
            }
        }

        /*
         * If there are no feature table extensions, return Empty.
         */
        if(featureTableExtensions.isEmpty()){
            return Optional.empty();
        } else{
            FeatureTableInfo extendedFeatureTableInfo
                    = currentFeatureTableInfo.extend(featureTableExtensions, traversalPathExtensions);
            return Optional.of(extendedFeatureTableInfo);
        }
    }

    public int getNbOfFeaturesBuilt(){return traversalPathExtender.getTotalNbOfFeatureColumnsConstructed();}

}
