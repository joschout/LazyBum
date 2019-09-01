package feature.multisettable;

import com.google.common.collect.Sets;
import feature.columntype.ColumnFieldHandler;
import feature.featuretable.FeatureColumn;
import feature.tranform.ExistInMultiSetTransform;
import feature.tranform.UnsupportedFeatureTransformationException;
import org.jooq.Field;

import java.util.*;

public class CategoricalExistInMultiSetTransformer<T extends Comparable> {

    public List<FeatureColumn> buildFeatures(Field fieldToTransform, ColumnFieldHandler<? extends T> columnHandler) throws UnsupportedFeatureTransformationException {

        // gather all possible NON NULL values
        Set<T> possibleValues = new HashSet<>();
        for(List<? extends T> multiSetForOneInstance: columnHandler.getMultiSetPerInstance()){
            for(T value: multiSetForOneInstance){
                if(value != null){
                    possibleValues.add(value);
                }
            }
        }


        // initialize feature columns
        Map<T, FeatureColumn> featureColumns = new HashMap<>();
        for(T valueToCheckForExistence: possibleValues){
            ExistInMultiSetTransform<T> transform = new ExistInMultiSetTransform<>(fieldToTransform, valueToCheckForExistence);
            FeatureColumn<Boolean> featureColumn = new FeatureColumn<>(fieldToTransform, transform);
            featureColumns.put(valueToCheckForExistence, featureColumn);
        }

        // add booleans to feature columns
        // for each instance
        //   get
        for(List<? extends T> multiSetForOneInstance: columnHandler.getMultiSetPerInstance()){

            Set<T> valueSetForInstance = new HashSet<>();
            for(T value: multiSetForOneInstance){
                if(value != null){
                    valueSetForInstance.add(value);
                    FeatureColumn featureColumnForValue = featureColumns.get(value);
                    featureColumnForValue.add(true);
                }
            }

            Sets.SetView<T> featureColumnsFailingExistenceCheck = Sets.difference(featureColumns.keySet(), valueSetForInstance);

            for(T valueNotInMultiSet: featureColumnsFailingExistenceCheck){
                FeatureColumn featureColumnForValue = featureColumns.get(valueNotInMultiSet);
                featureColumnForValue.add(false);
            }
        }

        return new ArrayList<>(featureColumns.values());
    }


}
