package feature.columntype;

import feature.featuretable.FeatureColumn;
import feature.multisettable.NumericMultiSetTransformer;
import feature.tranform.FieldTransformerEnumInterface;
import feature.tranform.NumericFeatureTransformationEnum;
import feature.tranform.UnsupportedFeatureTransformationException;
import org.jooq.Field;
import org.jooq.Table;

import java.util.List;
import java.util.Map;

public class IntegerColumnHandler extends ColumnFieldHandler<Integer> {

    public static Integer trueMissingValueEncoding = Integer.MIN_VALUE;

    public IntegerColumnHandler() {
        super();
    }

    @Override
    public List<FeatureColumn> toFeatureColumns(Table table, Field fieldToTransform) throws UnsupportedFeatureTransformationException {
        NumericMultiSetTransformer<Integer> integerMultisetTransformer = new NumericMultiSetTransformer<>();
        return integerMultisetTransformer.buildFeatures(fieldToTransform, this);
//
//        Map<NumericFeatureTransformationEnum, FeatureColumn> featureColumns = NumericMultiSetTransformer.buildFeatures(fieldToTransform, this);
//
        
//        return new ArrayList<FeatureColumn>(featureColumns.values());
    }
    @Override
    public Map<Object, Object> getFeatureValuesPerInstance(Map<Object, List<Integer>> multiSetPerInstance, FieldTransformerEnumInterface transformation) throws UnsupportedFeatureTransformationException {

        NumericMultiSetTransformer<Integer> numericMultiSetTransformer = new NumericMultiSetTransformer<>();
        return numericMultiSetTransformer.getFeatureValuesPerInstance(multiSetPerInstance, (NumericFeatureTransformationEnum) transformation);
    }

    @Override
    public Integer getTrueMissingValueEncoding() {
        return trueMissingValueEncoding;
    }

}
