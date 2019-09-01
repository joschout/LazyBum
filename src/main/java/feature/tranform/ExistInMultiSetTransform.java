package feature.tranform;

import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;

import java.io.Serializable;

public class ExistInMultiSetTransform<T> implements FieldTransformerEnumInterface, Serializable {
    private static final long serialVersionUID = 8911596867206684582L;

    private T valueToCheckForExistence;
    private DataType dataType;

    public ExistInMultiSetTransform(Field field, T valueToCheckForExistence){
        this.valueToCheckForExistence = valueToCheckForExistence;
//        this.dataType = field.getDataType();
        this.dataType = SQLDataType.BOOLEAN;
    }

    @Override
    public DataType getSqlDataType() {
        return this.dataType;
    }

    @Override
    public DataType getSqlDataType(Field originalField) {
        return this.dataType;
    }


    @Override
    public String toString() {
        return "\u2203 " + valueToCheckForExistence.toString();
    }


    @Override
    public String safeToString(){
        return "EXISTS_"+valueToCheckForExistence.toString();
    }


    public T getValueToCheckForExistence() {
        return valueToCheckForExistence;
    }
}
