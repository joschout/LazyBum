package feature.tranform;

//import org.jooq.impl.SQLDataType;

import org.jooq.DataType;
import org.jooq.Field;

public interface FieldTransformerEnumInterface {

    public DataType getSqlDataType();

    public DataType getSqlDataType(Field originalField);


    public String safeToString();


}
