package feature.multisettable;

import globalsettings.FieldToFeatureTranformationSettings;
import feature.columntype.ColumnFieldHandler;
import feature.featuretable.FeatureColumn;
import feature.tranform.CategoricalFeatureTransformationEnum;
import feature.tranform.UnsupportedFeatureTransformationException;
import org.apache.commons.math3.stat.Frequency;
import org.jooq.Field;

import java.util.*;

public class CategoricMultiSetTransformer<T extends Comparable> {

    public static boolean VERBOSE = false;

    private Frequency frequency;

    public CategoricMultiSetTransformer(){
        frequency = new Frequency();
    }

    public CategoricMultiSetTransformer(Collection<T> values){
        super();
        addAll(values);
    }

    public void addAll(Collection<T> values){
        for(T value: values){
            frequency.addValue(value);
        }
    }

    public void add(T value){
        frequency.addValue(value);
    }

    public int getCount(){
        return (int) frequency.getSumFreq();
    }

    public int getCountDistinct(){
        return frequency.getUniqueCount();
    }

    public T getMode() throws UnsupportedFeatureTransformationException {
        List<Comparable<?>> modes = frequency.getMode();
        if(modes.isEmpty()){
            throw new UnsupportedFeatureTransformationException("could not find a mode");
        }else{
            return (T) modes.get(0);

        }



    }

    private List<FeatureColumn> initFeatureColumnMap(Field fieldToTransform){

        List<FeatureColumn> featureColumns = new ArrayList<>();
        for(CategoricalFeatureTransformationEnum transformationEnum: FieldToFeatureTranformationSettings.categoricFeatureTransformations){
            featureColumns.add(new FeatureColumn<T>(fieldToTransform, transformationEnum));
        }
        return featureColumns;
    }


    public List<FeatureColumn> buildFeatures(Field fieldToTransform, ColumnFieldHandler<? extends T> columnHandler) throws UnsupportedFeatureTransformationException {
        List<? extends List<? extends T>> multiSetPerInstance = columnHandler.getMultiSetPerInstance();

        List<FeatureColumn> featureColumns = initFeatureColumnMap(fieldToTransform);

        for(List<? extends T> multiSetForOneInstance: multiSetPerInstance){
            CategoricMultiSetTransformer<T> transformerForOneInstance = new CategoricMultiSetTransformer<>();

            boolean containsNull = false;
            for (T value : multiSetForOneInstance) {// only add non-null values
                if(value == null){
                    containsNull = true;
                } else{
                    transformerForOneInstance.add(value);
                }
            }
            if(containsNull){
                if(VERBOSE){System.out.println("FOUND NULL IN FOLLOWING MULISET:\n" + multiSetForOneInstance.toString());}
            }

//            for(T value: multiSetForOneInstance){
//                transformer.add(value);
//            }

            for(FeatureColumn featureColumn: featureColumns){
                double featureValue = CategoricalFeatureTransformationEnum.get(transformerForOneInstance, (CategoricalFeatureTransformationEnum) featureColumn.getTransformation().get());
                featureColumn.add(featureValue);
            }
        }
        return featureColumns;
    }


    public Map<Object, Object> getFeatureValuesPerInstance(Map<Object, List<T>> multiSetPerInstance, CategoricalFeatureTransformationEnum transformationEnum) throws UnsupportedFeatureTransformationException {
        Map<Object, Object> featureValuePerInstance = new HashMap<>();

        for(Object instance: multiSetPerInstance.keySet()){
            List<? extends T> multiSetForOneInstance = multiSetPerInstance.get(instance);

            CategoricMultiSetTransformer<T> transformer = new CategoricMultiSetTransformer<>();

            boolean containsNull = false;
            for (T value : multiSetForOneInstance) {// only add non-null values
                if(value == null){
                    containsNull = true;
                } else{
                    transformer.add(value);
                }
            }
            if(containsNull){
                if(VERBOSE){System.out.println("FOUND NULL IN FOLLOWING MULISET:\n" + multiSetForOneInstance.toString());}
            }

//            for(T value: multiSetForOneInstance){
//                transformer.add(value);
//            }


            double featureValue = CategoricalFeatureTransformationEnum.get(transformer, transformationEnum);
            featureValuePerInstance.put(instance, featureValue);
        }
        return featureValuePerInstance;
    }


//
////    private static Map<CategoricalFeatureTransformationEnum, FeatureColumn> initFeatureColumnMap(){
////        Map<CategoricalFeatureTransformationEnum, FeatureColumn> featureColumns = new HashMap<>();
////        for(CategoricalFeatureTransformationEnum transformationEnum: FieldToFeatureTranformationSettings.categoricFeatureTransformations){
////            featureColumns.put(transformationEnum, new FeatureColumn<Double>(field, transformationEnum));
////        }
////        return featureColumns;
////    }
//
//    public static Map<CategoricalFeatureTransformationEnum, FeatureColumn> buildFeatures(StringColumnHandler stringColumnHandler) throws UnsupportedFeatureTransformationException {
//        List<List<String>> multiSetPerInstance = stringColumnHandler.getMultiSetPerInstance();
//
//        Map<CategoricalFeatureTransformationEnum, FeatureColumn> featureColumns = initFeatureColumnMap();
//
//        for(List<String> multiSetForOneInstance: multiSetPerInstance){
//            CategoricMultiSetTransformer transformer = new CategoricMultiSetTransformer();
//            transformer.addAll(multiSetForOneInstance);
//
//            for(CategoricalFeatureTransformationEnum transformation: FieldToFeatureTranformationSettings.categoricFeatureTransformations){
//                double featureValue = CategoricalFeatureTransformationEnum.get(transformer, transformation);
//                FeatureColumn featureColumn = featureColumns.get(transformation);
//                featureColumn.add(featureValue);
//            }
//        }
//        return featureColumns;
//
//    }
//
//
//    public static Map<CategoricalFeatureTransformationEnum, FeatureColumn> buildFeatures(BooleanColumnHandler booleanColumnHandler) throws UnsupportedFeatureTransformationException {
//        List<List<Boolean>> multiSetPerInstance = booleanColumnHandler.getMultiSetPerInstance();
//
//        Map<CategoricalFeatureTransformationEnum, FeatureColumn> featureColumns = initFeatureColumnMap();
//
//        for(List<Boolean> multiSetForOneInstance: multiSetPerInstance){
//            CategoricMultiSetTransformer transformer = new CategoricMultiSetTransformer();
//            transformer.addAll(multiSetForOneInstance);
//
//            for(CategoricalFeatureTransformationEnum transformation: FieldToFeatureTranformationSettings.categoricFeatureTransformations){
//                double featureValue = CategoricalFeatureTransformationEnum.get(transformer, transformation);
//                FeatureColumn featureColumn = featureColumns.get(transformation);
//                featureColumn.add(featureValue);
//            }
//        }
//        return featureColumns;
//
//    }


}
