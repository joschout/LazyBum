package feature.columntype;

import database.JOOQUtils;
import org.jooq.Field;
import org.jooq.Table;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;

public class FieldTypeResolver {

    public static boolean VERBOSE = false;

    public static ColumnFieldHandler resolveColumn(Field field, Table originalTable) throws UnsupportedFieldTypeException {
        Class<?> type = field.getType();
        if(VERBOSE) {
            System.out.println("field '" + field.getName() + "' of columntype: " + field.getType().getName());
        }
        boolean isPartOfAReference = JOOQUtils.isFieldPartOfAKey(originalTable, field);

        if(VERBOSE){
            if(isPartOfAReference){
                System.out.println("\t" + field.getName() + " in table " + originalTable.getName() + " is part of a reference");
            } else{
                System.out.println("\t" + field.getName() + " in table " + originalTable.getName() + " is NOT part of a reference");
            }
        }


        if(type.equals(Integer.class)){
            return new ColumnToNumericFeaturesColumnHandler<Integer>(isPartOfAReference, Integer.class);
//            return new IntegerColumnHandler();
        } else if(type.equals(String.class)){
            return new StringColumnHandler(JOOQUtils.isFieldPartOfAKey(originalTable, field));
        } else if(type.equals(Double.class)) {
            return new ColumnToNumericFeaturesColumnHandler<Double>(isPartOfAReference, Double.class);
//            return new DoubleColumnHandler();
        } else if(type.equals(Boolean.class)) {
            return new BooleanColumnHandler(isPartOfAReference);
        } else if(type.equals(Long.class)){
            return new LongColumnHandler(isPartOfAReference);
        } else if(type.equals(BigDecimal.class)){
            return new ColumnToNumericFeaturesColumnHandler<BigDecimal>(isPartOfAReference, BigDecimal.class);
        }else if(type.equals(BigInteger.class)){
            return new ColumnToNumericFeaturesColumnHandler<BigInteger>(isPartOfAReference, BigInteger.class);
        }
        else if(type.equals(Date.class)){
            return new DateColumnHandler(isPartOfAReference);
        }


        else {
            throw new UnsupportedFieldTypeException("No feature construction has been defined for columns of columntype: " + type.getName());
        }
    }
}
