package research;

import org.jooq.Field;

public class FieldInfo {
    public Field alias;
    public boolean addedAsPartOfThisTable;

    public FieldInfo(Field alias, boolean addedAsPartOfThisTable) {
        this.alias = alias;
        this.addedAsPartOfThisTable = addedAsPartOfThisTable;
    }

    @Override
    public String toString() {
        return alias.getQualifiedName() + ", added=" + addedAsPartOfThisTable;
    }
}
