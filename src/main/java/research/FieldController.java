package research;

import database.JOOQDatabaseInteractor;
import graph.TraversalPath;
import graph.TraversalStep;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Used for handling the fields off table generated from chain of joins along a traversal path.
 * For each such join chain, a linked list of FieldControllers is used, with the parent indicating the previously joined table
 *
 */
@SuppressWarnings("Duplicates")
public class FieldController {

    private FieldController previous;

    private Table joinedTable;
    private Map<Field,FieldInfo> fieldsAddedUsingAliasesMap; // maps the added field to its alias

//    private Condition joinCondition;


    private List<ImmutablePair<Field,Field>> joinConditionInfoList;



    private FieldController(FieldController previous, Table joinedTable, Map<Field, FieldInfo> fieldsAddedUsingAliasesMap,
                            List<ImmutablePair<Field,Field>> joinConditionInfoList) {
        this.previous = previous;
        this.joinedTable = joinedTable;
        this.fieldsAddedUsingAliasesMap = fieldsAddedUsingAliasesMap;
        this.joinConditionInfoList = joinConditionInfoList;
    }

    private FieldController extend(Table joinedTable, Map<Field, FieldInfo> fieldsAddedUsingAliasesMap, List<ImmutablePair<Field,Field>> joinConditionInfoList){
        return new FieldController(this, joinedTable, fieldsAddedUsingAliasesMap, joinConditionInfoList);
    }

    public static FieldController buildInitial(Table initialTable){

        Map<Field, FieldInfo> fieldToFieldInfoMap = new HashMap<>();
        for(Field field: initialTable.fields()){
//            fieldToFieldInfoMap.put(field, new FieldInfo(DSL.field(alias(field, initialTable)), true));
            fieldToFieldInfoMap.put(field, new FieldInfo(field, true));
        }
        return new FieldController(null, initialTable, fieldToFieldInfoMap, null);
    }

    public static Field alias(Field field, Table tableOfField){
        //todo: might be better to define this in JOOQUtils
        String alias = tableOfField.getName() + "_" + field.getName();
        return field.as(alias);
    }

//    private Condition createJoinCondition(TraversalStep traversalStep) throws UnimplementedFKColumnResolverException {
//
//        /*
//        TODO
//        * 3 cases:
//        * 1. the included columns of the lastly joined table contain the columns to join on at this step:
//        *   Problem: the columns are 'renamed':
//        *       * the columns are not part of the original table, so the foreign key constraint is not enough
//        *       * the columns might be aliased
//        *
//        *   Solution:
//        *
//        * 2. the included columns of the lastly joined table do NOT contain the columns to join on
//        *   example:
//        *     T1 contains FK to PK of T2
//        *     T1 contains FK to PK of T2
//        *     --> FK column of T is used in join table
//        *         joining T works, but the PK column is not added
//        *         joining T fails, because the PK column is not found
//        *
//        *
//        * 3. the included columns of the lastly joined table ONLY PARTIALLY contain the columns to join on
//        *
//        *
//        *
//        * */
//
//
//        List<Field> sourceKeyFields = traversalStep.getSourceKeyFields();
//        List<Field> destinationKeyFields = traversalStep.getDestinationKeyFields();
//
//        Table source = traversalStep.getSource();
//        Map<Field, Field> fieldsAddedForSourceAliasMap = aliasedTableFieldMap.get(source);
//        if(fieldsAddedForSourceAliasMap == null){
//            System.out.println("BOOBOOB NULL BOOBOOB");
//        }
//
//        Condition joinCondition = DSL.trueCondition();
//        for (int i = 0; i < sourceKeyFields.size(); i++) {
//
//            Field sourceKeyField = sourceKeyFields.get(i);
//            Field alias = fieldsAddedForSourceAliasMap.get(sourceKeyField);
//            if (alias == null){
//                String errStr = "The join columns in traversalStep "+ traversalStep.toString()
//                        + " are not part of the columns added for the last join table."
//                        + "\n\tNecessary columns: " + sourceKeyFields.stream().map(Field::getName).collect(Collectors.joining(","))
//                        + "\n\tAvailable columns: " + fieldsAddedForSourceAliasMap.keySet().stream().map(Field::getName).collect(Collectors.joining(","));
//
//                throw new UnimplementedFKColumnResolverException(errStr);
//            } else{
//                Field destinationKeyField = destinationKeyFields.get(i);
//                joinCondition = joinCondition.and(alias.eq(destinationKeyField));
//            }
//        }
//
//
//        return joinCondition;
//    }


    public static FieldController buildFromTraversalPath(TraversalPath traversalPath, JOOQDatabaseInteractor jooqDatabaseInteractor, Table initialTable) {

        FieldController fieldController = FieldController.buildInitial(initialTable);
        for (TraversalStep traversalStep : traversalPath.getTraversalSteps()) {
            fieldController = FieldController.addFieldsOfNewTable(traversalStep, fieldController, jooqDatabaseInteractor);
        }

        return fieldController;
    }


    public static FieldController addFieldsOfNewTable(TraversalStep traversalStep,
                                                      FieldController currentFieldController, JOOQDatabaseInteractor jooqDatabaseInteractor){
        Table traversalStepDestination = traversalStep.getDestination(jooqDatabaseInteractor);

        Map<Field, FieldInfo> fieldToFieldInfoMap = new HashMap<>();

        /*
        * The fields added in this join are all of the table's fields except those part of the foreign key
        * */

        List<Field> fieldsToAdd = traversalStep.getNonFKDestinationFields(jooqDatabaseInteractor).collect(Collectors.toList());

        for (Field field : fieldsToAdd) {
            Field aliasedField = alias(field, traversalStepDestination);
            fieldToFieldInfoMap.put(field, new FieldInfo(aliasedField, true));
        }

        /*
        * The fields not added to the join table are those part of the foreign key.
        * They need to be mapped to the corresponding fields in the join table
        * */
        List<Field> fieldsNotToAdd = traversalStep.getDestinationKeyFields();
        List<Field> fieldsNotToAddSources = traversalStep.getSourceKeyFields();


        List<ImmutablePair<Field, Field>> joinConditionInfoList = new ArrayList<>();

        Map<Field, FieldInfo> parentFieldsAddedUsingAliasesMap = currentFieldController.fieldsAddedUsingAliasesMap;
        for (int i = 0; i < fieldsNotToAdd.size(); i++) {
            Field destinationKeyField = fieldsNotToAdd.get(i);
            Field sourceKeyField = fieldsNotToAddSources.get(i);

            FieldInfo sourceKeyFieldInfo = parentFieldsAddedUsingAliasesMap.get(sourceKeyField);
            Field aliasOfSourceKeyField = sourceKeyFieldInfo.alias;

            fieldToFieldInfoMap.put(destinationKeyField, new FieldInfo(aliasOfSourceKeyField, false));

            joinConditionInfoList.add(new ImmutablePair<>(aliasOfSourceKeyField, destinationKeyField));

        }

        return currentFieldController.extend(traversalStepDestination, fieldToFieldInfoMap, joinConditionInfoList);
    }


    public List<Field> getAliasedFieldsToAdd(){
        List<Field> aliasedFieldsToAdd = new ArrayList<>();

        for(FieldInfo fieldInfo:fieldsAddedUsingAliasesMap.values()){
            if (fieldInfo.addedAsPartOfThisTable){
                aliasedFieldsToAdd.add(fieldInfo.alias);
            }
        }
        return aliasedFieldsToAdd;
    }


    public List<Field> getPreviouslyAddedAliasedFields() {
        List<Field> aliasedFieldsToAdd = new ArrayList<>();
        if (previous != null) {
            previous.getPreviouslyAddedAliasedFieldsRecursive(aliasedFieldsToAdd);
        }

        return aliasedFieldsToAdd;
    }

    private void getPreviouslyAddedAliasedFieldsRecursive(List<Field> aliasedFieldsToAdd){
        if(previous != null){
            getPreviouslyAddedAliasedFieldsRecursive(aliasedFieldsToAdd);
        }
        aliasedFieldsToAdd.addAll(getAliasedFieldsToAdd());
    }

    public Map<Field,Field> getAddedOriginalFieldsToAliasMap(){
        Map<Field, Field> fieldsAddedToOriginalToAliasMap = new HashMap<>();

        for(Field originalField:fieldsAddedUsingAliasesMap.keySet()){
            FieldInfo fieldInfo = fieldsAddedUsingAliasesMap.get(originalField);
            if(fieldInfo.addedAsPartOfThisTable){
                fieldsAddedToOriginalToAliasMap.put(originalField, fieldInfo.alias);
            }
        }
        return fieldsAddedToOriginalToAliasMap;
    }

    public Condition getJoinCondition() {
        if(joinConditionInfoList == null || joinConditionInfoList.isEmpty()){
            throw new NullPointerException("join condition is empty or null");
        }
        Condition joinCondition = DSL.trueCondition();
        for (ImmutablePair<Field, Field> aliasOfSourceKeyFieldToDestinationKeyField : joinConditionInfoList) {
            Field aliasOfSourceKeyField = aliasOfSourceKeyFieldToDestinationKeyField.getLeft();
            Field destinationKeyField = aliasOfSourceKeyFieldToDestinationKeyField.getRight();
            joinCondition = joinCondition.and(aliasOfSourceKeyField.eq(destinationKeyField));
        }
        return joinCondition;
    }

    public Condition getJoinCondition(String schemaName, String tableName){
        if(joinConditionInfoList == null || joinConditionInfoList.isEmpty()){
            throw new NullPointerException("join condition is empty or null");
        }
        Condition joinCondition = DSL.trueCondition();
        for (ImmutablePair<Field, Field> aliasOfSourceKeyFieldToDestinationKeyField : joinConditionInfoList) {
            Field aliasOfSourceKeyField = aliasOfSourceKeyFieldToDestinationKeyField.getLeft();
            Field destinationKeyField = aliasOfSourceKeyFieldToDestinationKeyField.getRight();

            String[] parts = {schemaName, tableName, aliasOfSourceKeyField.getName()};
            Field renamedSourceKeyField = DSL.field(DSL.name(parts));

            joinCondition = joinCondition.and(renamedSourceKeyField.eq(destinationKeyField));
        }
        return joinCondition;
    }



    public List<Table<?>> getListOfTablesToJoin(){

        List<Table<?>> tablesToJoin;
        if(previous != null){
            tablesToJoin = previous.getListOfTablesToJoin();
        } else{
            tablesToJoin = new LinkedList<Table<?>>();
        }
        tablesToJoin.add(joinedTable);
        return  tablesToJoin;
    }

}
