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

public class DoubleColumnHandler extends ColumnFieldHandler<Double> {



    public static Double trueMissingValueEncoding = Double.NEGATIVE_INFINITY;


    public DoubleColumnHandler() {
        super();
    }

    @Override
    public List<FeatureColumn> toFeatureColumns(Table table, Field fieldToTransform) throws UnsupportedFeatureTransformationException {

        NumericMultiSetTransformer<Double> doubleMultisetTransformer = new NumericMultiSetTransformer<>();
        return doubleMultisetTransformer.buildFeatures(fieldToTransform, this);
        //        Map<NumericFeatureTransformationEnum, FeatureColumn> featureColumns = NumericMultiSetTransformer.buildFeatures(this);
//
//        return new ArrayList<FeatureColumn>(featureColumns.values());
    }

    @Override
    public Map<Object, Object> getFeatureValuesPerInstance(Map<Object, List<Double>> multiSetPerInstance, FieldTransformerEnumInterface transformation) throws UnsupportedFeatureTransformationException {

        NumericMultiSetTransformer<Double> numericMultiSetTransformer = new NumericMultiSetTransformer<>();
        return numericMultiSetTransformer.getFeatureValuesPerInstance(multiSetPerInstance, (NumericFeatureTransformationEnum) transformation);
    }

    @Override
    public Double getTrueMissingValueEncoding() {
        return trueMissingValueEncoding;
    }
}
