package graph;

import com.google.common.base.Objects;
import org.jgrapht.graph.DefaultEdge;
import org.jooq.Condition;
import org.jooq.ForeignKey;
import org.jooq.Table;

import java.io.Serializable;

public class ForeignKeyEdge extends DefaultEdge implements Serializable {


    private static final long serialVersionUID = -4797720886781058884L;
    private ForeignKeyInfo foreignKey;
//    private ForeignKey foreignKey;

    public ForeignKeyEdge(ForeignKey foreignKey) {
        this.foreignKey = new ForeignKeyInfo(foreignKey);
    }


    public Table getForeignKeyTable(){
        return foreignKey.getTable();
    }

    public Table getPrimaryKeyTable(){
        return foreignKey.getKey().getTable();
    }

//    public List getForeignKeyFields(){
//        return foreignKey.getFields();
//    }
//
//    public List getPrimaryKeyFields(){
//        return foreignKey.getKey().getFields();
//    }

    public KeyInfo getKey(ForeignKeyTableEnum selector) {
        if(selector == ForeignKeyTableEnum.FOREIGN_KEY_TABLE){
            return foreignKey;
        } else {
            return foreignKey.getKey();
        }

    }

    public ForeignKeyInfo getForeignKey() {
        return foreignKey;
    }

    public Condition getJoinCondition() throws InvalidKeyInfoException {
        return foreignKey.getJoinCondition();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForeignKeyEdge that = (ForeignKeyEdge) o;
        return Objects.equal(foreignKey, that.foreignKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(foreignKey);
    }
}
