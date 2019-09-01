package feature.tranform;

import feature.multisettable.NumericMultiSetTransformer;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;

public enum NumericFeatureTransformationEnum implements FieldTransformerEnumInterface {
    AVG(SQLDataType.DOUBLE.nullable(true)),
    STD(SQLDataType.DOUBLE.nullable(true)),
    VAR(SQLDataType.DOUBLE.nullable(true)),
    MAX(SQLDataType.DOUBLE.nullable(true)),
    MIN(SQLDataType.DOUBLE.nullable(true)),
    SUM(SQLDataType.DOUBLE.nullable(true)),
    COUNT(SQLDataType.DOUBLE.nullable(true));

//    AVG(SQLDataType.DOUBLE.nullable(false)),
//    STD(SQLDataType.DOUBLE.nullable(false)),
//    VAR(SQLDataType.DOUBLE.nullable(false)),
//    MAX(SQLDataType.DOUBLE.nullable(false)),
//    MIN(SQLDataType.DOUBLE.nullable(false)),
//    SUM(SQLDataType.DOUBLE.nullable(false)),
//    COUNT(SQLDataType.DOUBLE.nullable(false));


    private final DataType sqlDataType;


    public static Object get(NumericMultiSetTransformer numericMultiSetTransformer, NumericFeatureTransformationEnum transform) throws UnsupportedFeatureTransformationException {
        if (NumericFeatureTransformationEnum.AVG.equals(transform)) {
            return numericMultiSetTransformer.getMean();
        } else if(NumericFeatureTransformationEnum.STD.equals(transform)){
            return numericMultiSetTransformer.getStandardDeviation();
        } else if(NumericFeatureTransformationEnum.VAR.equals(transform)){
            return numericMultiSetTransformer.getVariance();
        } else if(NumericFeatureTransformationEnum.MAX.equals(transform)){
            return numericMultiSetTransformer.getMax();
        } else if(NumericFeatureTransformationEnum.MIN.equals(transform)){
            return numericMultiSetTransformer.getMin();
        } else if(NumericFeatureTransformationEnum.SUM.equals(transform)){
            return numericMultiSetTransformer.getSum();
        } else if(NumericFeatureTransformationEnum.COUNT.equals(transform)){
            return numericMultiSetTransformer.getCount();
        } else {
            throw new UnsupportedFeatureTransformationException("The feature transformation " + transform.toString() + " is not supported or not available");
        }

    }

    NumericFeatureTransformationEnum(DataType dataType){
        this.sqlDataType = dataType;
    }

    public DataType getSqlDataType(){
        return sqlDataType;
    }

    @Override
    public DataType getSqlDataType(Field originalField) {
        return sqlDataType;
    }

    @Override
    public String safeToString(){
        return this.toString();
    }


    public static void main(String[] args) {
        for (NumericFeatureTransformationEnum value: NumericFeatureTransformationEnum.values()){
            System.out.println(value);
        }
    }

}
