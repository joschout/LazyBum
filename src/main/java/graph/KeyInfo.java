package graph;

import org.jooq.Field;
import org.jooq.Key;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class KeyInfo implements Serializable {


    private static final long serialVersionUID = 8369325996675059256L;
    protected String keyName;
    protected String[] qualifiedTableNameParts;
    protected String[] unqualifiedFieldNames;

    public String getName() {
        return keyName;
    }

    public String getQualifiedTableName(){
        return String.join(".", qualifiedTableNameParts);
    }

    public String getUnqualifiedTableName(){
        return qualifiedTableNameParts[qualifiedTableNameParts.length - 1];
    }

    public String[] getUnqualifiedFieldNames(){
        return unqualifiedFieldNames;
    }


    public Table getTable(){
        return DSL.table(DSL.name(qualifiedTableNameParts));
    }


    public List<Field> getFields(){
        List<Field> fields = new ArrayList<>(unqualifiedFieldNames.length);
        for (String unqualifiedFieldName : unqualifiedFieldNames) {
            String[] fieldParts = Arrays.copyOf(qualifiedTableNameParts, qualifiedTableNameParts.length + 1);
            fieldParts[qualifiedTableNameParts.length] = unqualifiedFieldName;
            fields.add(
                    DSL.field(DSL.name(fieldParts))
            );
        }
        return fields;
    }

    protected KeyInfo(){
    }

    protected KeyInfo(Key key){

        this.keyName = key.getName();
        this.qualifiedTableNameParts = key.getTable().getQualifiedName().getName();

        List<TableField> fields = (List<TableField>) key.getFields();
        this.unqualifiedFieldNames = new String[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            this.unqualifiedFieldNames[i] = fields.get(i).getName();
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyInfo keyInfo = (KeyInfo) o;
        return Objects.equals(keyName, keyInfo.keyName) &&
                Arrays.equals(qualifiedTableNameParts, keyInfo.qualifiedTableNameParts) &&
                Arrays.equals(unqualifiedFieldNames, keyInfo.unqualifiedFieldNames);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(keyName);
        result = 31 * result + Arrays.hashCode(qualifiedTableNameParts);
        result = 31 * result + Arrays.hashCode(unqualifiedFieldNames);
        return result;
    }

    protected KeyInfo(String keyName, String[] qualifiedTableNameParts, String[] unqualifiedFieldNames) {
        this.keyName = keyName;
        this.qualifiedTableNameParts = qualifiedTableNameParts;
        this.unqualifiedFieldNames = unqualifiedFieldNames;
    }
}
