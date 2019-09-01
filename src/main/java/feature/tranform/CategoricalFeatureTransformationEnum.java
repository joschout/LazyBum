package feature.tranform;

import feature.multisettable.CategoricMultiSetTransformer;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;

public enum CategoricalFeatureTransformationEnum implements FieldTransformerEnumInterface {
    COUNT (SQLDataType.INTEGER.nullable(true)),
    COUNT_DISTINCT(SQLDataType.INTEGER.nullable(true));

//    MODE();

    //, HIGH_CORRELATED_ITEMS(S);

//    COUNT (SQLDataType.INTEGER.nullable(false)),
//    COUNT_DISTINCT(SQLDataType.INTEGER.nullable(false));
//    //, HIGH_CORRELATED_ITEMS(S);
    
    private final DataType sqlDataType;

    public static double get(CategoricMultiSetTransformer categoricMultiSetTransformer, CategoricalFeatureTransformationEnum transform) throws UnsupportedFeatureTransformationException {
        if (CategoricalFeatureTransformationEnum.COUNT.equals(transform)) {
            return categoricMultiSetTransformer.getCount();
        } else if(CategoricalFeatureTransformationEnum.COUNT_DISTINCT.equals(transform)){
            return categoricMultiSetTransformer.getCountDistinct();
        }
//        else if(CategoricalFeatureTransformationEnum.HIGH_CORRELATED_ITEMS.equals(transform)){
//            throw new UnsupportedFeatureTransformationException(
//                    "The feature transformation " + CategoricalFeatureTransformationEnum.HIGH_CORRELATED_ITEMS.toString()
//                            + " is currently not supported");
//        }



        else {
            throw new UnsupportedFeatureTransformationException("The feature transformation " + transform.toString() + " is not supported or not available");
        }
    }

    CategoricalFeatureTransformationEnum(DataType dataType){
        this.sqlDataType = dataType;
    }

    public DataType getSqlDataType(){
        return sqlDataType;
    }

    @Override
    public DataType getSqlDataType(Field originalField) {
        if(this.equals(CategoricalFeatureTransformationEnum.COUNT)){
            return SQLDataType.INTEGER.nullable(true);
        } else if(this.equals(COUNT_DISTINCT)){
            return SQLDataType.INTEGER.nullable(true);
        }
//        else if(this.equals(MODE)){
//            return originalField.getDataType().nullable(true);
//        }
        else{
            throw new UnsupportedOperationException("could not find a data type for " + this.toString() + " on field " + originalField.getName());
        }
    }

    @Override
    public String safeToString(){
       return this.toString();
    }

}
