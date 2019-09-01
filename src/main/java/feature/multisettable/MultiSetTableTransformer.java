package feature.multisettable;

import feature.tranform.UnsupportedFeatureTransformationException;
import feature.columntype.UnsupportedFieldTypeException;
import feature.columntype.ColumnFieldHandler;
import feature.featuretable.FeatureColumn;
import feature.featuretable.FeatureTableHandler;
import org.jooq.Field;

import java.util.List;
import java.util.Map;

public class MultiSetTableTransformer {



    public static FeatureTableHandler transform(MultiSetTableHandler multiSetTableHandler) throws UnsupportedFieldTypeException, UnsupportedFeatureTransformationException {

        FeatureTableHandler featureTableHandler = new FeatureTableHandler(
          multiSetTableHandler.getTargetIDField(),
          multiSetTableHandler.getInstanceToListIndexMap()
        );

        Map<Field, ColumnFieldHandler> fieldToFieldHandlerMap = multiSetTableHandler.getFieldToFieldHandlerMap();

        for(Field fieldToTransform: fieldToFieldHandlerMap.keySet()){
            ColumnFieldHandler<?> columnToTranform = fieldToFieldHandlerMap.get(fieldToTransform);
            if(columnToTranform.hasOneValuePerInstance()){
                FeatureColumn featureColumn = columnToTranform.toSingleFeatureColumn(fieldToTransform);
                featureTableHandler.add(featureColumn);
            }else{
                List<FeatureColumn> featureColumns = columnToTranform.toFeatureColumns(multiSetTableHandler.getOriginalTable(), fieldToTransform);
                featureTableHandler.addAll(featureColumns);
            }
        }

        return featureTableHandler;
    }

//    public Map<FieldTransformationType, FeatureColumn> convert(ColumnFieldHandler columnFieldHandler){
//
////        Map<FieldTransformationType, FeatureColumn> featureColumns = new HashMap<>();
//
//        List<FieldTransformer> fieldTransformers = columnFieldHandler.getTransformers();
//
//        columnFieldHandler.useTransformers(fieldTransformers);
//
//
//
//
//    }


}
