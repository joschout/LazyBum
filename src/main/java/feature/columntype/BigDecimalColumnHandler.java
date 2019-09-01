package feature.columntype;

import feature.featuretable.FeatureColumn;
import feature.multisettable.NumericMultiSetTransformer;
import feature.tranform.FieldTransformerEnumInterface;
import feature.tranform.NumericFeatureTransformationEnum;
import feature.tranform.UnsupportedFeatureTransformationException;
import org.jooq.Field;
import org.jooq.Table;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class BigDecimalColumnHandler extends ColumnFieldHandler<BigDecimal> {

    public static BigDecimal trueMissingValueEncoding = BigDecimal.ZERO;

    @Override
    public List<FeatureColumn> toFeatureColumns(Table table, Field fieldToTransform) throws UnsupportedFeatureTransformationException {
        NumericMultiSetTransformer<BigDecimal> multiSetTransformer = new NumericMultiSetTransformer<>();
        return multiSetTransformer.buildFeatures(fieldToTransform, this);
    }

    @Override
    public Map<Object, Object> getFeatureValuesPerInstance(Map<Object, List<BigDecimal>> multiSetPerInstance, FieldTransformerEnumInterface transformation) throws UnsupportedFeatureTransformationException {
        NumericMultiSetTransformer<BigDecimal> numericMultiSetTransformer = new NumericMultiSetTransformer<>();
        return numericMultiSetTransformer.getFeatureValuesPerInstance(multiSetPerInstance, (NumericFeatureTransformationEnum) transformation);
    }

    @Override
    public BigDecimal getTrueMissingValueEncoding() {
        return trueMissingValueEncoding;
    }
}
