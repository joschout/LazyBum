package feature.multisettable;

import feature.columntype.ColumnToNumericFeaturesColumnHandler;
import globalsettings.FieldToFeatureTranformationSettings;
import feature.columntype.ColumnFieldHandler;
import feature.featuretable.FeatureColumn;
import feature.tranform.NumericFeatureTransformationEnum;
import feature.tranform.UnsupportedFeatureTransformationException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jooq.Field;

import java.util.*;

public class NumericMultiSetTransformer<T extends Number> {

    public static boolean VERBOSE = false;

    private DescriptiveStatistics descriptiveStatistics;

    public NumericMultiSetTransformer(){
        descriptiveStatistics = new DescriptiveStatistics();
    }

    public NumericMultiSetTransformer(Collection<Double> values){
        this();
        for(double value: values){
            descriptiveStatistics.addValue(value);
        }
    }

    public void add(T value){
        descriptiveStatistics.addValue(value.doubleValue());
    }

//    public void add(Integer value){
//        descriptiveStatistics.addValue((double) value);
//    }
//
//    public void add(double value){
//        descriptiveStatistics.addValue(value);
//    }


    public double getMean(){
        return descriptiveStatistics.getMean();
    }

    public double getStandardDeviation(){
        return descriptiveStatistics.getStandardDeviation();
    }

    public double getMax(){
        return descriptiveStatistics.getMax();
    }

    public double getMin(){
        return descriptiveStatistics.getMin();
    }

    public double getVariance(){
        return descriptiveStatistics.getVariance();
    }

    public double getSum(){
        return descriptiveStatistics.getSum();
    }

    public double getCount(){
        return (double) descriptiveStatistics.getN();
    }



    public List<FeatureColumn> buildFeatures(Field fieldToTransform, ColumnFieldHandler<? extends T> columnHandler) throws UnsupportedFeatureTransformationException {
        List<? extends List<? extends T>> multiSetPerInstance = columnHandler.getMultiSetPerInstance();

        List<FeatureColumn> featureColumns = initFeatureColumnMap(fieldToTransform);

        for(List<? extends T> multiSetForOneInstance: multiSetPerInstance){
            NumericMultiSetTransformer<Number> transformer = new NumericMultiSetTransformer<>();


            boolean containsNull = false;
            for (T value : multiSetForOneInstance) {// only add non-null values
                if(value == null){
                    containsNull = true;
                } else{
                    transformer.add(value);
                }
            }


            boolean containsNonNullElements = false;
            if(containsNull){
                if(VERBOSE){System.out.println("Found missing value (encoded as NULL) in following multi-set:\n" + multiSetForOneInstance.toString());}

                for (T t : multiSetForOneInstance) {
                    if(t != null){
                        containsNonNullElements = true;
                        break;
                    }
                }
                if(! containsNonNullElements){
                    if(VERBOSE){System.out.println("\t multi-set contains ONLY NULL elements (missing values)");}
                }

            }

//            for(T value: multiSetForOneInstance){
//                transformer.add(value);
//

            for(FeatureColumn featureColumn: featureColumns){
                Object featureValue = NumericFeatureTransformationEnum.get(transformer, (NumericFeatureTransformationEnum) featureColumn.getTransformation().get());
                if(featureValue == null){
                    throw new RuntimeException("NULL NUMERIC FEATURE VALUE occurs after transformation. This should not be the case");
                }
                if (Double.isNaN((double)featureValue)) {
                    System.out.println("Got NaN out of feature value calculation - NaNaNaNaNaNaNaNaNaNaNaNaNaNaNaNa BATMAN");
                    System.out.println("Use true missing value encoding instead");

                    featureColumn.add(ColumnToNumericFeaturesColumnHandler.trueMissingValueEncodingDouble);

                } else{
                    featureColumn.add(featureValue);
                }
            }
        }
        return featureColumns;
    }



    public Map<Object, Object> getFeatureValuesPerInstance(Map<Object, List<T>> multiSetPerInstance, NumericFeatureTransformationEnum transformationEnum) throws UnsupportedFeatureTransformationException {
        Map<Object, Object> featureValuePerInstance = new HashMap<>();

        for(Object instance: multiSetPerInstance.keySet()){
            List<? extends T> multiSetForOneInstance = multiSetPerInstance.get(instance);

            NumericMultiSetTransformer<T> transformer = new NumericMultiSetTransformer<>();

            boolean containsNull = false;
            for (T value : multiSetForOneInstance) {// only add non-null values
                if(value == null){
                    containsNull = true;
                } else{
                    transformer.add(value);
                }
            }
            if(containsNull){
                if(VERBOSE){System.out.println("Found true missing value (encoded as NULL) in following multi-set:\n" + multiSetForOneInstance.toString());}
            }

//            for(T value: multiSetForOneInstance){
//                transformer.add(value);
//            }


            Object featureValue = NumericFeatureTransformationEnum.get(transformer, transformationEnum);
            if(featureValue == null){
                throw new RuntimeException("NULL NUMERIC FEATURE VALUE occurs after transformation. This should not be the case");
            }
            if (Double.isNaN((double)featureValue)) {
                System.out.println("Got NaN out of feature value calculation - NaNaNaNaNaNaNaNaNaNaNaNaNaNaNaNa BATMAN");
                System.out.println("Use true missing value encoding instead");

                featureValuePerInstance.put(instance, ColumnToNumericFeaturesColumnHandler.trueMissingValueEncodingDouble);

            } else{
                featureValuePerInstance.put(instance, featureValue);
            }
        }
        return featureValuePerInstance;
    }


//    public static Map<NumericFeatureTransformationEnum, FeatureColumn> buildFeatures(Field fieldToTransform, IntegerColumnHandler integerColumnHandler) throws UnsupportedFeatureTransformationException {
//        List<List<Integer>> multiSetPerInstance = integerColumnHandler.getMultiSetPerInstance();
//
//        List<FeatureColumn> featureColumns = initFeatureColumnMap(fieldToTransform);
//
//        for(List<Integer> multiSetForOneInstance: multiSetPerInstance){
//            NumericMultiSetTransformer<Integer> transformer = new NumericMultiSetTransformer<>();
//            for(int value: multiSetForOneInstance){
//                transformer.add(value);
//            }
//
//            for(FeatureColumn featureColumn: featureColumns){
//                double featureValue = NumericFeatureTransformationEnum.get(transformer, (NumericFeatureTransformationEnum) featureColumn.getTransformation());
//                featureColumn.add(featureValue);
//            }
//        }
//        return featureColumns;
//
//    }

    private List<FeatureColumn> initFeatureColumnMap(Field fieldToTransform){

        List<FeatureColumn> featureColumns = new ArrayList<>();
        for(NumericFeatureTransformationEnum transformationEnum: FieldToFeatureTranformationSettings.numericFeatureTransformations){
            featureColumns.add(new FeatureColumn<T>(fieldToTransform, transformationEnum));
        }
        return featureColumns;
    }

//
//    public static Map<NumericFeatureTransformationEnum, FeatureColumn> buildFeatures(DoubleColumnHandler doubleColumnHandler) throws UnsupportedFeatureTransformationException {
//        List<List<Double>> multiSetPerInstance = doubleColumnHandler.getMultiSetPerInstance();
//
//        Map<NumericFeatureTransformationEnum, FeatureColumn> featureColumns = initFeatureColumnMap();
//        for(List<Double> multiSetForOneInstance: multiSetPerInstance){
//
//            NumericMultiSetTransformer<Comparable<Double>> transformer = new NumericMultiSetTransformer<Comparable<Double>>();
//            for(double value: multiSetForOneInstance){
//                transformer.add(value);
//            }
//
//
//            for(NumericFeatureTransformationEnum transformation: FieldToFeatureTranformationSettings.numericFeatureTransformations){
//                double featureValue = NumericFeatureTransformationEnum.get(transformer, transformation);
//                FeatureColumn featureColumn = featureColumns.get(transformation);
//                featureColumn.add(featureValue);
//            }
//        }
//        return featureColumns;
//
//    }
}
