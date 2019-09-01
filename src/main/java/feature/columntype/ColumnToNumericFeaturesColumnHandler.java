package feature.columntype;

import feature.featuretable.FeatureColumn;
import feature.multisettable.CategoricMultiSetTransformer;
import feature.multisettable.NumericMultiSetTransformer;
import feature.tranform.CategoricalFeatureTransformationEnum;
import feature.tranform.FieldTransformerEnumInterface;
import feature.tranform.NumericFeatureTransformationEnum;
import feature.tranform.UnsupportedFeatureTransformationException;
import org.jooq.Field;
import org.jooq.Table;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class ColumnToNumericFeaturesColumnHandler<T extends Number & Comparable> extends ColumnFieldHandler<T> {

    public static Double trueMissingValueEncodingDouble = Double.NEGATIVE_INFINITY;
    public static Integer trueMissingValueEncodingInteger = Integer.MIN_VALUE;
    public static BigDecimal trueMissingValueEncodingBigDecimal = BigDecimal.ZERO;
    public static BigInteger trueMissingValueEncodingBigInteger = BigInteger.ZERO;

    private Class<T> typeOfNumber;


//    public ColumnToNumericFeaturesColumnHandler(boolean isPartOfAReference) {
//        super(isPartOfAReference);
//    }

    public ColumnToNumericFeaturesColumnHandler(boolean isPartOfAReference, Class<T> typeOfNumber) {
        super(isPartOfAReference);
        this.typeOfNumber = typeOfNumber;
    }

    @Override
    public List<FeatureColumn> toFeatureColumns(Table table, Field fieldToTransform) throws UnsupportedFeatureTransformationException {
        if(this.isPartOfAReference){
            CategoricMultiSetTransformer<T> categoricMultiSetTransformer = new CategoricMultiSetTransformer<T>();
            return categoricMultiSetTransformer.buildFeatures(fieldToTransform, this);
        } else{
            NumericMultiSetTransformer<T> multiSetTransformer = new NumericMultiSetTransformer<T>();
            return multiSetTransformer.buildFeatures(fieldToTransform, this);
        }
    }

    @Override
    public Map<Object, Object> getFeatureValuesPerInstance(Map<Object, List<T>> multiSetPerInstance, FieldTransformerEnumInterface transformation) throws UnsupportedFeatureTransformationException {

        if(this.isPartOfAReference) {
            CategoricMultiSetTransformer<T> categoricMultiSetTransformer = new CategoricMultiSetTransformer<T>();
            return categoricMultiSetTransformer.getFeatureValuesPerInstance(multiSetPerInstance, (CategoricalFeatureTransformationEnum) transformation);
        } else{
            NumericMultiSetTransformer<T> multiSetTransformer = new NumericMultiSetTransformer<>();
            return multiSetTransformer.getFeatureValuesPerInstance(multiSetPerInstance, (NumericFeatureTransformationEnum) transformation);
        }
    }

    @Override
    public T getTrueMissingValueEncoding() {
        if(typeOfNumber == Double.class){
            return (T) trueMissingValueEncodingDouble;
        } else if(typeOfNumber == Integer.class){
            return (T) trueMissingValueEncodingInteger;
        } else if (typeOfNumber == BigDecimal.class){
            return (T) trueMissingValueEncodingBigDecimal;
        }else if (typeOfNumber == BigInteger.class){
            return (T) trueMissingValueEncodingBigInteger;
        } else{

            throw new RuntimeException("SHOULD NOT BE HERE");
        }
    }
}
