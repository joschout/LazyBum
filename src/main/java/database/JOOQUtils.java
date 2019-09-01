package database;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Table;
import org.jooq.UniqueKey;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JOOQUtils {

    public static Table getNeigborOfTableInForeignKey(Table table, ForeignKey foreignKey) {
        Table neighbor = foreignKey.getKey().getTable();
        if(neighbor.equals(table)){
            neighbor = foreignKey.getTable();
        }
        return neighbor;
    }

    public static void printOutForeignKeyInfo(ForeignKey foreignKey, PrintStream printStream){
        printStream.println("=== FK info ===");
        printStream.println(foreignKey.toString());
        printStream.println("------");
        printStream.println("\t.getFields(): " + foreignKey.getFields().toString());
        printStream.println("------");
        printStream.println("\t.getKey().getFields: " + foreignKey.getKey().getFields());
        printStream.println("===============\n");
    }
    
    public static void printOutFieldInfo(Field field){
        System.out.println(field);
                System.out.println("\t" + field.getName());
                System.out.println("\t" + field.getQualifiedName());
                System.out.println("\t" + field.getUnqualifiedName());
    }


    static void printTableForeignKeys(List<Table<?>> tables){
        for (Table table : tables) {
            System.out.println("\t\ttable: " + table.getName());
            List<ForeignKey<?, ?>> foreignKeys = table.getReferences();
            for (ForeignKey foreignKey : foreignKeys) {
                System.out.println(foreignKey.toString());
            }
        }
    }

    public static String listOfFieldsToCSVString(List<Field> fields){
        return fields.stream().map(Field::getName).collect(Collectors.joining(", "));
    }

    public static String tableInfo(Table table, String indent){
        String str = indent + table.getName() +"\n";
        str += indent + "\tfields:" + listOfFieldsToCSVString(Arrays.asList(table.fields()));
        return str;
    }

    public static String listOfFieldTypesToCSVString(List<Field> fields){
        return fields.stream().map(field -> field.getName() + "["+field.getType().getName()+"]").collect(Collectors.joining(", "));
    }


    public static boolean isFieldPartOfAKey(Table table, Field fieldOfTable){
        UniqueKey primaryKey = table.getPrimaryKey();
        if(primaryKey != null){
            List<Field> primaryKeyFields = primaryKey.getFields();
            if(primaryKeyFields.contains(fieldOfTable)){
                return true;
            }
        }

        List<ForeignKey> references = table.getReferences();
        for (ForeignKey reference : references) {
            if(reference.getFields().contains(fieldOfTable)){
                return true;
            }
            if(reference.getKey().getFields().contains(fieldOfTable)){
                return true;
            }
        }
        return false;
    }


    /**
     * A table is an associative table if
     * it only consists of two foreign keys.
     *
     * That is, it has exactly two foreign keys,
     * and all the fields of a table are part of one of the two keys.
     *
     * So it is not an associative table,
     * if it does not have two foreign keys
     * or it also has columns not part of those keys.
     *
     *
     * @param table
     * @return
     */
    public static boolean isTableAnAssociationTable(Table table){
        List<ForeignKey> references = table.getReferences();
        if(references.size()!= 2){
            return false;
        }
        // table has two foreign keys

        Field[] fields = table.fields();
        for (Field field : fields) {
            boolean isFieldPartOfForeignKey = false;
            for (ForeignKey reference : references) {
                if(reference.getFields().contains(field)){
                    isFieldPartOfForeignKey = true;
                    break;
                }
            }
            if(! isFieldPartOfForeignKey){
                return false;
            }
        }
        return true;
    }




}
