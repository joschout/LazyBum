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

import java.sql.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by joschout.
 */
public class DateColumnHandler extends ColumnFieldHandler<Date> {


    public static Date trueMissingValueEncoding = new Date(0L);
//
//    private enum DateTransform{
//        DAY_OF_WEEK, DAY_OF_MONTH, MONTH, YEAR, EPOCH
//    }
//
//
//    public void foobar(){
//        List<DateTransform> dateTransforms = new ArrayList<>();
//        dateTransforms.add(DateTransform.DAY_OF_WEEK);
//        dateTransforms.add(DateTransform.YEAR);
//        dateTransforms.add(DateTransform.EPOCH);
//
//        List<List<Date>> multiSetPerInstance = this.multiSetPerInstance;
//
//    }
//
//
//
//    public Object transform(LocalDate date, DateTransform dateTransform) throws UnsupportedFeatureTransformationException {
//        if(dateTransform == DateTransform.DAY_OF_WEEK){
//            return date.getDayOfWeek();
//        } else if(dateTransform == DateTransform.DAY_OF_MONTH){
//            return date.getDayOfMonth();
//        } else if(dateTransform == DateTransform.MONTH){
//            return date.getMonth().getValue();
//        } else if(dateTransform == DateTransform.YEAR){
//            return date.getYear();
//        } else if (dateTransform == DateTransform.EPOCH){
//            ZoneId zoneId = DateMultiSetTransformer.zoneId;
//            return date.atStartOfDay(zoneId).toEpochSecond();
//        } else{
//            throw new UnsupportedFeatureTransformationException("Transforming dates using " + String.valueOf(dateTransform) + " is not supported");
//        }
//    }
//
//
//    public void transformToSupportedColumns(
//
//
//    )
//
    public DateColumnHandler(boolean isPartOfAReference) {
        super(isPartOfAReference);
    }

    @Override
    public List<FeatureColumn> toFeatureColumns(Table table, Field fieldToTransform) throws UnsupportedFeatureTransformationException {

        System.out.println("DATE TYPE COLUMNS NOT SUPPORTED, NOT GENERATING FEATURES FOR THEM");
        CategoricMultiSetTransformer<Date> categoricMultiSetTransformer = new CategoricMultiSetTransformer<>();
        List<FeatureColumn>  featureColumns = categoricMultiSetTransformer.buildFeatures(fieldToTransform, this);

        if(
//                nbOfValues <= FieldToFeatureTranformationSettings.MAX_NB_OF_CATEGORICAL_VALUES_FOR_EXIST_TESTS &&
                FieldToFeatureTranformationSettings.existenceTestCreationDecider.shouldCreateExistenceTestForField(table, fieldToTransform)
        ) {
            CategoricalExistInMultiSetTransformer<Date> categoricalEqualityTest = new CategoricalExistInMultiSetTransformer<>();
            List<FeatureColumn> existenceFeatureColumns = categoricalEqualityTest.buildFeatures(fieldToTransform, this);
            featureColumns.addAll(existenceFeatureColumns);
        }
        return featureColumns;



//        Map<CategoricalFeatureTransformationEnum, FeatureColumn> featureColumns = CategoricMultiSetTransformer.buildFeatures(this);
//
//        return new ArrayList<FeatureColumn>(featureColumns.values());
    }

    @Override
    public Map<Object, Object> getFeatureValuesPerInstance(Map<Object, List<Date>> multiSetPerInstance, FieldTransformerEnumInterface transformation) throws UnsupportedFeatureTransformationException {
        CategoricMultiSetTransformer<Date> categoricMultiSetTransformer = new CategoricMultiSetTransformer<>();
        return categoricMultiSetTransformer.getFeatureValuesPerInstance(multiSetPerInstance, (CategoricalFeatureTransformationEnum) transformation);
    }

    @Override
    public Date getTrueMissingValueEncoding() {
        return trueMissingValueEncoding;
    }


}
