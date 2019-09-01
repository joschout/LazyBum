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

import java.util.List;
import java.util.Map;

public class LongColumnHandler extends ColumnFieldHandler<Long> {

    public static Long trueMissingValueEncoding = Long.MIN_VALUE;

    public LongColumnHandler(boolean isPartOfAReference) {
        super(isPartOfAReference);
    }

    @Override
    public List<FeatureColumn> toFeatureColumns(Table table, Field fieldToTransform) throws UnsupportedFeatureTransformationException {

        if(this.isPartOfAReference){
            CategoricMultiSetTransformer<Long> categoricMultiSetTransformer = new CategoricMultiSetTransformer<>();
            return categoricMultiSetTransformer.buildFeatures(fieldToTransform, this);
        } else{
            NumericMultiSetTransformer<Long> multiSetTransformer = new NumericMultiSetTransformer<>();
            return multiSetTransformer.buildFeatures(fieldToTransform, this);
        }

    }

    @Override
    public Map<Object, Object> getFeatureValuesPerInstance(Map<Object, List<Long>> multiSetPerInstance, FieldTransformerEnumInterface transformation) throws UnsupportedFeatureTransformationException {
        if(this.isPartOfAReference) {
            CategoricMultiSetTransformer<Long> categoricMultiSetTransformer = new CategoricMultiSetTransformer<>();
            return categoricMultiSetTransformer.getFeatureValuesPerInstance(multiSetPerInstance, (CategoricalFeatureTransformationEnum) transformation);
        } else{
            NumericMultiSetTransformer<Long> multiSetTransformer = new NumericMultiSetTransformer<>();
            return multiSetTransformer.getFeatureValuesPerInstance(multiSetPerInstance, (NumericFeatureTransformationEnum) transformation);
        }
    }

    @Override
    public Long getTrueMissingValueEncoding() {
        return trueMissingValueEncoding;
    }


//    public FeatureColumn toSingleFeatureColumn(Field fieldToTransform) throws UnsupportedFeatureTransformationException {
//        FeatureColumn<Long> featureColumn = new FeatureColumn<Long>(fieldToTransform, null);
//        for (List<Long> instanceMultiSet : getMultiSetPerInstance()) {
//            if(instanceMultiSet.size() != 1){
//                throw new UnsupportedFeatureTransformationException("Expected 1 value in each multiset, found (for a specific multiset): " + instanceMultiSet.size() + " values");
//            }
//            featureColumn.add(instanceMultiSet.get(0));
//        }
//        return featureColumn;
//    }

}
