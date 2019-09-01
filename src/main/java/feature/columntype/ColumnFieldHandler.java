package feature.columntype;

import feature.featuretable.FeatureColumn;
import feature.tranform.FieldTransformerEnumInterface;
import feature.tranform.UnsupportedFeatureTransformationException;
import org.jooq.Field;
import org.jooq.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ColumnFieldHandler<T> {


    public static boolean VERBOSE = false;

    protected List<List<T>> multiSetPerInstance;

//    protected Field field;

    protected boolean isPartOfAReference;

    ColumnFieldHandler(boolean isPartOfAReference){
        this.multiSetPerInstance = new ArrayList<>();
        this.isPartOfAReference = isPartOfAReference;
    }
    ColumnFieldHandler(){
        this(false);
        multiSetPerInstance = new ArrayList<>();
    }

    public void adaptForNewInstance(int indexOfNewInstance) {
        if(multiSetPerInstance.size() != indexOfNewInstance){
            throw new IllegalArgumentException("Incorrect bookkeeping of instances");
        }
        multiSetPerInstance.add(new ArrayList<>());
    }

    public void add(int indexOfInstance, Object value){
        List<T> multiSetForInstance = multiSetPerInstance.get(indexOfInstance);
        multiSetForInstance.add((T) value);
    }


    public int maxStringLength(){
        int max = 0;
        for(List<T> multiSet: multiSetPerInstance){
            int strLength = multiSet.toString().length();
            if(strLength > max){
                max = strLength;
            }
        }
        return max;
    }

    public List<T> getMultiSetForInstance(int instanceIndex){
        return multiSetPerInstance.get(instanceIndex);
    }

    public List<List<T>> getMultiSetPerInstance() {
        return multiSetPerInstance;
    }

    public abstract List<FeatureColumn> toFeatureColumns(Table table, Field fieldToTransform) throws UnsupportedFeatureTransformationException;

//    public List<FeatureColumn> toFeatureColumns2(Field fieldToTransform, Class<T> fieldType) throws UnsupportedFeatureTransformationException{
//        Class<?> type = field.getType();
//        if(fieldType.equals(Integer.class)){
//            ColumnFieldHandler<Integer> thisObj = (ColumnFieldHandler<Integer>) this;
//            NumericMultiSetTransformer<Integer> integerMultisetTransformer = new NumericMultiSetTransformer<>();
//            return integerMultisetTransformer.buildFeatures(field, thisObj);
//        } else if(fieldType.equals(String.class)){
//            return new StringColumnHandler();
//        } else if(fieldType.equals(Double.class)) {
//            ColumnFieldHandler<Double> thisObj = (ColumnFieldHandler<Double>) this;
//            NumericMultiSetTransformer<Double> doubleMultisetTransformer = new NumericMultiSetTransformer<>();
//            return doubleMultisetTransformer.buildFeatures(fieldToTransform, thisObj);
//            //        Map<NumericFeatureTransformationEnum, FeatureColumn> featureColumns = NumericMultiSetTransformer.buildFeatures(this);
////
//        } else if(fieldType.equals(Boolean.class)) {
//            return new BooleanColumnHandler();
//        } else if(fieldType.equals(Long.class)){
//            return new LongColumnHandler();
//        } else {
//            throw new UnsupportedFieldTypeException("No feature construction has been defined for columns of columntype: " + type.getName());
//        }
//    }
//
//    public Map<Object, Object> getFeatureValuesPerInstance2(Map<Object, List<T>> multiSetPerInstance, FieldTransformerEnumInterface transformation) throws UnsupportedFeatureTransformationException{
//        Class<?> type = field.getType();
//        System.out.println("field '" + field.getName() + "' of columntype: " + field.getType().getName());
//        if(type.equals(Integer.class)){
//
//            NumericMultiSetTransformer<Integer> numericMultiSetTransformer = new NumericMultiSetTransformer<>();
//            return numericMultiSetTransformer.getFeatureValuesPerInstance(multiSetPerInstance, (NumericFeatureTransformationEnum) transformation);
//        } else if(type.equals(String.class)){
//            return new StringColumnHandler();
//        } else if(type.equals(Double.class)) {
//            return new DoubleColumnHandler();
//        } else if(type.equals(Boolean.class)) {
//            return new BooleanColumnHandler();
//        } else if(type.equals(Long.class)){
//            return new LongColumnHandler();
//        } else {
//            throw new UnsupportedFieldTypeException("No feature construction has been defined for columns of columntype: " + type.getName());
//        }
//    }



    /**
     * Used in prediction
     *
     * @return
     */
    public abstract Map<Object, Object> getFeatureValuesPerInstance(Map<Object, List<T>> multiSetPerInstance, FieldTransformerEnumInterface transformation) throws UnsupportedFeatureTransformationException;


    /**
     * Used when each multiset contains exactly 1 value.
     * In this case, the values are used as features.
     * The featureColumn transformation is null.
     *
     * @param fieldToTransform
     * @return
     * @throws UnsupportedFeatureTransformationException
     */
    public FeatureColumn toSingleFeatureColumn(Field fieldToTransform) throws UnsupportedFeatureTransformationException {
        FeatureColumn<T> featureColumn = new FeatureColumn<T>(fieldToTransform, null);
        for (List<T> instanceMultiSet : getMultiSetPerInstance()) {
            if(instanceMultiSet.size() != 1){
                throw new UnsupportedFeatureTransformationException("Expected 1 value in each multiset, found (for a specific multiset): " + instanceMultiSet.size() + " values");
            }

            // NOTE: THIS GOES WRONG
            T featureValue = instanceMultiSet.get(0);
            if(featureValue == null){

                if(VERBOSE){
                    System.out.println("True MISSING VALUE used in one-to-one mapping, encoded as NULL");
                }
//                throw new RuntimeException("NULL FEATURE VALUE OCCURS");

                T missingValueEncoding = this.getTrueMissingValueEncoding();
                if(VERBOSE) {
                    System.out.println("\tUsing " + String.valueOf(missingValueEncoding) + " to encode missing value");
                }
                featureColumn.add(missingValueEncoding);

            } else{
            featureColumn.add(featureValue);
            }
        }
        return featureColumn;
    }

    public abstract T getTrueMissingValueEncoding();


//    public abstract T safeConvert(T singleFeatureValue);

    public boolean hasOneValuePerInstance(){
        for (List<T> instanceMultiSet : multiSetPerInstance) {
            if(instanceMultiSet.size() > 1){
                return false;
            }
        }
        return true;
    }

}
