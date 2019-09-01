package graph;

import database.JOOQDatabaseInteractor;
import dataset.TargetTableManager;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jooq.*;
import org.jooq.impl.DSL;
import research.FieldController;
import utils.PrettyPrinter;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")

public class TraversalPathDataGatheringQueryBuilder {

    public static boolean VERBOSE = false;

    public static ImmutablePair<Field[], Select<Record>> buildQueryGatheringInfoForExamplesBatch(@Nullable Collection<Object> instanceIDs, JOOQDatabaseInteractor jooqDatabaseInteractor,
                                                                                                      TargetTableManager targetTableManager, TraversalPath traversalPath,
                                                                                                      @Nullable Field fieldToCollect) throws InvalidKeyInfoException {
        // BUILD THE NECESSARY QUERY
        // note: incorrect should the initial table not be the target table
        FieldController fieldController = FieldController.buildFromTraversalPath(traversalPath, jooqDatabaseInteractor, targetTableManager.getTargetTable());
        List<Condition> foreignKeyJoinConditionList = traversalPath.getJoinConditionList();

        Field[] selectionFields = getSelectionFields(fieldToCollect, targetTableManager.getTargetID(),
                traversalPath, jooqDatabaseInteractor);

        List<Table<?>> tablesToJoin = fieldController.getListOfTablesToJoin();

        Condition instanceIDCondition = getConditionOnInstancesIDs(instanceIDs, targetTableManager.getTargetID());
        Condition allConditions = DSL
                .and(foreignKeyJoinConditionList)
                .and(instanceIDCondition);

        Select<Record> resultQuery = jooqDatabaseInteractor.getDslContext()
                .select(selectionFields)
                .from(tablesToJoin)
                .where(allConditions);
        return new ImmutablePair<>(selectionFields, resultQuery);
    }

    /**
     * If the field to collect is null, select the targetID + all fields from the table at the end of the traversal path
     * elif the field is not null, select the targetID + the field to collect
     *
     *
     * @param fieldToCollect the field to collect, or null if all fields need to be collected
     * @param targetID field
     * @param traversalPath ends in the table from which we want to collect data
     * @param jooqDatabaseInteractor necessary for name resolution
     * @return the fields to select: always the targetid, and depending on whether fieldToCollect is null, fieldToCollect or all fields from the last table in the traversal path
     */
    private static Field[] getSelectionFields(@Nullable Field fieldToCollect, Field targetID, TraversalPath traversalPath,
                                       JOOQDatabaseInteractor jooqDatabaseInteractor){
        if (VERBOSE){System.out.println("... getting selection fields ...");}
        Field[] selectionFields;
        if(fieldToCollect == null) {
            if (VERBOSE){System.out.println("fieldToCollect is Null");}
            // get all fields from the last table
            TraversalStep lastStep = traversalPath.getLast();
            List<Field> filteredFieldsFromTableAtEndOfTraversalpath = lastStep.getNonFKDestinationFields(jooqDatabaseInteractor).collect(Collectors.toList());
            int nbOfSelectionFields = filteredFieldsFromTableAtEndOfTraversalpath.size() + 1;
            selectionFields = new Field[nbOfSelectionFields];

            selectionFields[0] = targetID;
            for (int i = 1; i < nbOfSelectionFields; i++) {
                selectionFields[i] = filteredFieldsFromTableAtEndOfTraversalpath.get(i-1);
            }
        } else{
            selectionFields = new Field[2];
            selectionFields[0] = targetID;
            selectionFields[1] = fieldToCollect;
        }
        return selectionFields;
    }

    private static Condition getConditionOnInstancesIDs(@Nullable Collection<Object> instanceIDs, Field targetID){
        Condition instanceIDCondition;
        if(instanceIDs != null){
            if(VERBOSE) {
                System.out.println(PrettyPrinter.collectionToCSVString(instanceIDs));
                System.out.println("nb of ids in IN condtion: " + instanceIDs.size());
                if(instanceIDs.size() == 1){
                    System.out.println("\tid"+ instanceIDs.stream().findFirst().get());
                }
            }
            instanceIDCondition = targetID.in(new HashSet<>(instanceIDs));
            if(VERBOSE){
                System.out.println("IN condition" + instanceIDCondition.toString());
            }
        } else{
            // if no instanceIDs are given, select them all
            instanceIDCondition = DSL.trueCondition();
        }
        return instanceIDCondition;
    }


    public static Field[] makeMultiSetTable(String multiSetTableName, @Nullable Collection<Object> instanceIDs, JOOQDatabaseInteractor jooqDatabaseInteractor,
                             TargetTableManager targetTableManager, TraversalPath traversalPath,
                             @Nullable Field fieldToCollect) throws InvalidKeyInfoException {
        ImmutablePair<Field[], Select<Record>> selectionFieldsAndresultQueryImmutablePair =
                buildQueryGatheringInfoForExamplesBatch(instanceIDs, jooqDatabaseInteractor, targetTableManager, traversalPath, fieldToCollect);

        Table multiSetTableRepr = jooqDatabaseInteractor.getTableRepresentationWithSchemaQualifiedName(multiSetTableName);


        jooqDatabaseInteractor.getDslContext()
                .createTable(multiSetTableRepr).as(
                        selectionFieldsAndresultQueryImmutablePair.getRight()
                 ).execute();
        return selectionFieldsAndresultQueryImmutablePair.getLeft();
    }

    public static Map<Object, List<Object>> selectFromTargetTable(
            @Nullable Collection<Object> instanceIDs, JOOQDatabaseInteractor jooqDatabaseInteractor,
            TargetTableManager targetTableManager, @Nullable Field fieldToCollect) {

        Table targetTable = targetTableManager.getTargetTable();

        Condition instanceIDCondition = getConditionOnInstancesIDs(instanceIDs, targetTableManager.getTargetID());

        Field[] selectionFields = {targetTableManager.getTargetID(), fieldToCollect};
        Select<Record> resultQuery = jooqDatabaseInteractor.getDslContext()
                .select(selectionFields)
                .from(targetTable)
                .where(instanceIDCondition);

        Result<Record> records = resultQuery.fetch();
        Map<Object, List<Object>> instanceToValueMap = new HashMap<>();
        for (Record record : records) {
            Object instanceID = record.get(targetTableManager.getTargetID());
            Object fieldValue = record.getValue(fieldToCollect);
            if(instanceToValueMap.containsKey(instanceID)){
                throw new UnsupportedOperationException("Expected only one value per instance, found multiple." +
                        " At least " + String.valueOf(instanceToValueMap.get(instanceID)) + " and " + String.valueOf(fieldValue));
            } else{
                List<Object> instanceList = new ArrayList<>(1);
                instanceList.add(fieldValue);
                instanceToValueMap.put(instanceID, instanceList);
            }
        }
        return instanceToValueMap;

    }

}
