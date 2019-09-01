package feature.columntype;

import feature.featuretable.FeatureColumn;
import feature.multisettable.CategoricMultiSetTransformer;
import feature.tranform.CategoricalFeatureTransformationEnum;
import feature.tranform.FieldTransformerEnumInterface;
import feature.tranform.UnsupportedFeatureTransformationException;
import org.jooq.Field;
import org.jooq.Table;

import java.util.List;
import java.util.Map;

public class BooleanColumnHandler extends ColumnFieldHandler<Boolean> {


    public BooleanColumnHandler(boolean isPartOfAReference) {
        super(isPartOfAReference);
    }

    @Override
    public List<FeatureColumn> toFeatureColumns(Table table, Field fieldToTransform) throws UnsupportedFeatureTransformationException {
        CategoricMultiSetTransformer<Boolean> categoricMultiSetTransformer = new CategoricMultiSetTransformer<>();
        return categoricMultiSetTransformer.buildFeatures(fieldToTransform, this);

//        Map<CategoricalFeatureTransformationEnum, FeatureColumn> featureColumns = CategoricMultiSetTransformer.buildFeatures(fieldToTransform, this);
//
//        return new ArrayList<FeatureColumn>(featureColumns.values());
    }

    @Override
    public Map<Object, Object> getFeatureValuesPerInstance(Map<Object, List<Boolean>> multiSetPerInstance, FieldTransformerEnumInterface transformation) throws UnsupportedFeatureTransformationException {
        CategoricMultiSetTransformer<Boolean> categoricMultiSetTransformer = new CategoricMultiSetTransformer<>();
        return categoricMultiSetTransformer.getFeatureValuesPerInstance(multiSetPerInstance, (CategoricalFeatureTransformationEnum) transformation);
    }

    @Override
    public Boolean getTrueMissingValueEncoding() {
        return null;
    }

}
