package globalsettings;

import database.JOOQDatabaseInteractor;
import dataset.CategoricFieldUniqueValueCountController;
import feature.tranform.CategoricalFeatureTransformationEnum;
import feature.tranform.NumericFeatureTransformationEnum;
import learning.testing.DefaultExistenceTestCreationDecider;
import learning.testing.ExistenceTestCreationDecider;

import java.util.ArrayList;
import java.util.List;

public class FieldToFeatureTranformationSettings {

    public static List<NumericFeatureTransformationEnum> numericFeatureTransformations;

    public static List<CategoricalFeatureTransformationEnum> categoricFeatureTransformations;

    public static ExistenceTestCreationDecider existenceTestCreationDecider;

//    public static boolean TEST_EXIST_IN_MULTISET = false;
//
//    public static long MAX_NB_OF_CATEGORICAL_VALUES_FOR_EXIST_TESTS = 7;


    static{
        existenceTestCreationDecider = new DefaultExistenceTestCreationDecider(false);


        numericFeatureTransformations = new ArrayList<>();
        categoricFeatureTransformations = new ArrayList<>();

        numericFeatureTransformations.add(NumericFeatureTransformationEnum.AVG);
        numericFeatureTransformations.add(NumericFeatureTransformationEnum.COUNT);
        numericFeatureTransformations.add(NumericFeatureTransformationEnum.STD);
        numericFeatureTransformations.add(NumericFeatureTransformationEnum.SUM);
        numericFeatureTransformations.add(NumericFeatureTransformationEnum.VAR);
        numericFeatureTransformations.add(NumericFeatureTransformationEnum.MAX);
        numericFeatureTransformations.add(NumericFeatureTransformationEnum.MIN);


        categoricFeatureTransformations.add(CategoricalFeatureTransformationEnum.COUNT);
        categoricFeatureTransformations.add(CategoricalFeatureTransformationEnum.COUNT_DISTINCT);

    }


    public static void setExistenceTestCreationDeciderToCategoricFieldUniqueValueCountController(
            JOOQDatabaseInteractor jooqDatabaseInteractor,
            double distinctnessThreshold, int absoluteDistinctnessThreshold){
        FieldToFeatureTranformationSettings.existenceTestCreationDecider
                = new CategoricFieldUniqueValueCountController(
                jooqDatabaseInteractor, distinctnessThreshold, absoluteDistinctnessThreshold);
    }

}
