package feature.columntype;

import feature.featuretable.FeatureColumn;
import feature.multisettable.CategoricMultiSetTransformer;
import feature.multisettable.CategoricalExistInMultiSetTransformer;
import feature.tranform.CategoricalFeatureTransformationEnum;
import feature.tranform.FieldTransformerEnumInterface;
import feature.tranform.UnsupportedFeatureTransformationException;
import globalsettings.FieldToFeatureTranformationSettings;
import org.jooq.Field;
import org.jooq.Table;

import java.util.List;
import java.util.Map;

public class StringColumnHandler extends ColumnFieldHandler<String> {

    public static String trueMissingValueEncoding = "missing";

    public StringColumnHandler(boolean isPartOfAReference) {
        super(isPartOfAReference);
    }

    @Override
    public List<FeatureColumn> toFeatureColumns(Table table, Field fieldToTransform) throws UnsupportedFeatureTransformationException {
        CategoricMultiSetTransformer<String> categoricMultiSetTransformer = new CategoricMultiSetTransformer<>();
        List<FeatureColumn>  featureColumns = categoricMultiSetTransformer.buildFeatures(fieldToTransform, this);

//        Frequency frequency = new Frequency();
//        List<List<String>> multiSetPerInstance = this.getMultiSetPerInstance();
//        for (List<String> strings : multiSetPerInstance) {
//            for (String string : strings) {
//                frequency.addValue(string);
//            }
//        }
//        int nbOfValues = frequency.getUniqueCount();
//        System.out.println("nb of unique values for field " + fieldToTransform.getName() + ": " + nbOfValues);
//        System.out.println("\tnb necessary for EXIST tests: " + FieldToFeatureTranformationSettings.MAX_NB_OF_CATEGORICAL_VALUES_FOR_EXIST_TESTS);

        if(
//                nbOfValues <= FieldToFeatureTranformationSettings.MAX_NB_OF_CATEGORICAL_VALUES_FOR_EXIST_TESTS &&
                FieldToFeatureTranformationSettings.existenceTestCreationDecider.shouldCreateExistenceTestForField(table, fieldToTransform)
            ) {
            CategoricalExistInMultiSetTransformer<String> categoricalEqualityTest = new CategoricalExistInMultiSetTransformer<>();
            List<FeatureColumn> existenceFeatureColumns = categoricalEqualityTest.buildFeatures(fieldToTransform, this);
            featureColumns.addAll(existenceFeatureColumns);
        }
        return featureColumns;



//        Map<CategoricalFeatureTransformationEnum, FeatureColumn> featureColumns = CategoricMultiSetTransformer.buildFeatures(this);
//
//        return new ArrayList<FeatureColumn>(featureColumns.values());
    }

    @Override
    public Map<Object, Object> getFeatureValuesPerInstance(Map<Object, List<String>> multiSetPerInstance, FieldTransformerEnumInterface transformation) throws UnsupportedFeatureTransformationException {
        CategoricMultiSetTransformer<String> categoricMultiSetTransformer = new CategoricMultiSetTransformer<>();
        return categoricMultiSetTransformer.getFeatureValuesPerInstance(multiSetPerInstance, (CategoricalFeatureTransformationEnum) transformation);
    }

    @Override
    public String getTrueMissingValueEncoding() {
        return trueMissingValueEncoding;
    }

}
