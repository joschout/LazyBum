package feature.multisettable;

import feature.columntype.ColumnFieldHandler;
import feature.columntype.FieldTypeResolver;
import feature.columntype.UnsupportedFieldTypeException;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;

import java.util.HashMap;
import java.util.Map;

public class MultiSetTableHandler {

    public static boolean VERBOSE = false;

    private Table originalTable;

    private Field targetIDField;
    final private Map<Object, Integer> instanceToListIndexMap;
    private Map<Field, ColumnFieldHandler> fieldToFieldHandlerMap; // possibly aliased field to ColumnFieldHandler

    private Map<Field, Field> originalToAliasMap;

//    /**
//     *
//     * @param targetIDField
//     * @param fieldsAddedToTable possibly renamed fields
//     * @throws UnsupportedFieldTypeException
//     */
//    public MultiSetTableHandler(Field targetIDField, List<Field> fieldsAddedToTable) throws UnsupportedFieldTypeException {
//
//        this.targetIDField = targetIDField;
//        this.fieldToFieldHandlerMap = new HashMap<>();
//        this.instanceToListIndexMap = new HashMap<>();
//
//        for(Field field: fieldsAddedToTable){
//            ColumnFieldHandler fieldHandler = FieldTypeResolver.resolveColumn(field);
//            fieldToFieldHandlerMap.put(field, fieldHandler);
//        }
//    }


    public MultiSetTableHandler(Field targetIDField, Map<Field,Field> fieldsAddedToTableOriginalToAliasMap, Table originalTable) throws UnsupportedFieldTypeException {

        this.originalTable = originalTable;

        this.targetIDField = targetIDField;
        this.fieldToFieldHandlerMap = new HashMap<>();
        this.instanceToListIndexMap = new HashMap<>();

        this.originalToAliasMap = fieldsAddedToTableOriginalToAliasMap;

        for(Field original: fieldsAddedToTableOriginalToAliasMap.keySet()){
//            Field alias = fieldsAddedToTableOriginalToAliasMap.get(original);
            ColumnFieldHandler fieldHandler = FieldTypeResolver.resolveColumn(original, originalTable);
            fieldToFieldHandlerMap.put(original, fieldHandler);
        }
    }



    private void updateFieldHandlersForNewInstance(int indexOfNewInstance){
        for(Field field: fieldToFieldHandlerMap.keySet()){
            ColumnFieldHandler fieldHandler = fieldToFieldHandlerMap.get(field);
            fieldHandler.adaptForNewInstance(indexOfNewInstance);
        }
    }


    private void handleRecord(Record record){
        Object targetId = record.getValue(targetIDField);

        if( ! instanceToListIndexMap.keySet().contains(targetId)){
            int newIndex = instanceToListIndexMap.size();
            instanceToListIndexMap.put(targetId, newIndex);
            updateFieldHandlersForNewInstance(newIndex);
        }

        int indexForInstance = instanceToListIndexMap.get(targetId);
        for(Field field: fieldToFieldHandlerMap.keySet()){
            ColumnFieldHandler fieldHandler = fieldToFieldHandlerMap.get(field);
            Field alias = originalToAliasMap.get(field);
            Object value = record.getValue(alias);
            if(VERBOSE && value == null){
                System.out.println("True missing value, added to multi-set");
            }
            fieldHandler.add(indexForInstance, value);
        }

    }

    public void add(Result<Record> results) {

        /*
         * TODO: if memory gets to full:
         *   - SELECT only on the targetID and the feature column
         *     & calculate only features on this
         *
         * */

        for (Record record : results) {
            handleRecord(record);
        }
    }


//    public FeatureTableHandler toFeatureTableHandler() throws UnsupportedFieldTypeException, UnsupportedFeatureTransformationException {
//        FeatureTableHandler featureTableHandler = new FeatureTableHandler(targetIDField, instanceToListIndexMap);
//        for(ColumnFieldHandler columnFieldHandler: fieldToFieldHandlerMap.values()){
//            featureTableHandler.handleColumnFieldHandler(columnFieldHandler);
//        }
//
//        return featureTableHandler;
//    }

    public String toString(){
        return MultiSetTableStringBuilder.toString(this);
    }


    public Field getTargetIDField() {
        return targetIDField;
    }

    public Map<Object, Integer> getInstanceToListIndexMap() {
        return instanceToListIndexMap;
    }

    public Map<Field, ColumnFieldHandler> getFieldToFieldHandlerMap() {
        return fieldToFieldHandlerMap;
    }


    public Table getOriginalTable() {
        return originalTable;
    }
}
