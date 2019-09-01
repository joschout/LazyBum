package feature.featuretable;

import org.jooq.Field;

import java.util.*;

public class FeatureTableHandler {

    private Field targetIDField;
    private Map<Object, Integer> instanceToListIndexMap;


    private List<FeatureColumn> featureColumnList;

    public FeatureTableHandler(Field targetIDField, Map<Object, Integer> instanceToListIndexMap) {
        this.targetIDField = targetIDField;
        this.instanceToListIndexMap = instanceToListIndexMap;
        this.featureColumnList = new ArrayList<>();

    }


//    public void handleColumnFieldHandler(ColumnFieldHandler<?> columnFieldHandler) throws UnsupportedFeatureTransformationException {
//        List<? extends FeatureColumn> featureColumns = columnFieldHandler.toFeatureColumns(fieldToTransform);
//        featureColumnList.addAll(featureColumns);
//    }


    public void addAll(Collection<? extends FeatureColumn> featureColumns){
        featureColumnList.addAll(featureColumns);
    }

    public void add(FeatureColumn featureColumn){
        featureColumnList.add(featureColumn);
    }

    public Field getTargetIDField() {
        return targetIDField;
    }

    public Map<Object, Integer> getInstanceToListIndexMap() {
        return instanceToListIndexMap;
    }

    public List<FeatureColumn> getFeatureColumnList() {
        return featureColumnList;
    }

    @Override
    public String toString(){
        return FeatureTableStringBuilder.toString(this);
    }

    public String toString(String indentation){
        return FeatureTableStringBuilder.toString(this, indentation);
    }



    public int getNbOfFeatureColumns(){
        return featureColumnList.size();
    }
//    public FeatureTableHandler addOtherFeatureTable(FeatureTableHandler other) throws IllegalFeatureTableMergeException {
//        if(! other.targetIDField.equals(this.targetIDField)){
//            throw new IllegalFeatureTableMergeException("could not merge feature tables: different targetIDField:"
//                    + this.targetIDField.toString() + " vs " + other.targetIDField);
//        }
//
//        if(other.instanceToListIndexMap.keySet().size() != this.instanceToListIndexMap.keySet().size()){
//            throw new IllegalFeatureTableMergeException("could not merge feature tables: different number of instances");
//        }
//
//        for(Object instanceID: other.instanceToListIndexMap.keySet()){
//            if(! this.instanceToListIndexMap.keySet().contains(instanceID)){
//                throw new IllegalFeatureTableMergeException("could not merge feature tables: different instances");
//            }
//            int thisIndex = this.instanceToListIndexMap.get(instanceID);
//            int otherIndex = other.instanceToListIndexMap.get(instanceID);
//
//            if(thisIndex != otherIndex){
//                throw new IllegalFeatureTableMergeException("could not merge feature tables: different instance indexes");
//            }
//        }
//
//        // ALLOWED TO MERGE
//        addAll(other.getFeatureColumnList());
//        return this;
//    }
//
//    public class IllegalFeatureTableMergeException extends Exception{
//        public IllegalFeatureTableMergeException(String s) {
//            super(s);
//        }
//    }
//
//    public Optional<?> getFeatureValue(int featureColumnIndex, Object instanceID){
//        if(! instanceToListIndexMap.containsKey(instanceID)){
//            return Optional.empty();
//        } else {
//            FeatureColumn featureColumn = featureColumnList.get(featureColumnIndex);
//            int instanceIndex = instanceToListIndexMap.get(instanceID);
//            return Optional.of(featureColumn.getFeatureValueForInstance(instanceIndex));
//        }
//
//
//    }

}
