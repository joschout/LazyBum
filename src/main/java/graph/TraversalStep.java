package graph;

import com.google.common.base.Objects;
import database.JOOQDatabaseInteractor;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This encodes links from a depth-first graph perspective,
 * as an abstraction over the real foreign key relation direction.
 */
public class TraversalStep implements Serializable {

    private static final long serialVersionUID = -4993184741239919564L;
    private ForeignKeyEdge foreignKeyEdge;
    private ForeignKeyTableEnum traversalStepSource;


    private transient Table destinatonTableCache = null;

    public TraversalStep(ForeignKeyEdge foreignKeyEdge, Table stepOrigin) throws GraphTraversalException {

        // foreign key table is the origin:
        //     we step in the direction of the foreign key relationship
        if(foreignKeyEdge.getForeignKeyTable().equals(stepOrigin)){
            traversalStepSource = ForeignKeyTableEnum.FOREIGN_KEY_TABLE;
        }

        // PRIMARY key table is the origin:
        //     we step in AGAINST the direction of the foreign key relationship
        else if (foreignKeyEdge.getPrimaryKeyTable().equals(stepOrigin)){
            traversalStepSource = ForeignKeyTableEnum.PRIMARY_KEY_TABLE;
        } else{
            throw new GraphTraversalException("Neither of the two tables in the foreign key relationship corresponds to the current table node in the graph traversal");
        }

        this.foreignKeyEdge = foreignKeyEdge;
    }



    private KeyInfo getSourceKey(){
        return foreignKeyEdge.getKey(traversalStepSource);
    }

    private KeyInfo getDestinationKey(){
        return foreignKeyEdge.getKey(ForeignKeyTableEnum.other(traversalStepSource));
    }

    public Table getSource(JOOQDatabaseInteractor jooqDatabaseInteractor){
        return jooqDatabaseInteractor.getTableByName(getSourceKey().getTable().getName());
    }

    public String getSourceName(){
        return getSourceKey().getTable().getName();
    }

    public String getDestinationName(){
        return getDestinationKey().getTable().getName();
    }

    public Table getDestination(JOOQDatabaseInteractor jooqDatabaseInteractor){
        if(destinatonTableCache != null){
            return destinatonTableCache;
        }
        else{
            Table destinationTable = jooqDatabaseInteractor.getTableByName(getDestinationKey().getTable().getName());
            destinatonTableCache = destinationTable;
            return destinationTable;
        }
    }

    public List<Field> getSourceKeyFields(){
        return getSourceKey().getFields();
    }

    public List<Field> getDestinationKeyFields(){
        return getDestinationKey().getFields();
    }

    public Stream<Field> getNonFKDestinationFields(JOOQDatabaseInteractor jooqDatabaseInteractor){

        List<Field> destinationKeyFields = getDestinationKeyFields();

        return Arrays.stream(getDestination(jooqDatabaseInteractor).fields())
                .filter(field -> ! destinationKeyFields.contains(field));
    }


    public ForeignKeyTableEnum getTraversalStepSource() {
        return traversalStepSource;
    }

    public ForeignKeyEdge getForeignKeyEdge() {
        return foreignKeyEdge;
    }

    public Condition getJoinCondition() throws InvalidKeyInfoException {
        return foreignKeyEdge.getJoinCondition();
    }


    public String toString(){

        String sourceKeyFieldsString = "("
                + getSourceKeyFields().stream().map(Field::getName).collect(Collectors.joining(","))
                + ")";

        String destinationKeyFieldsString = "("
                + getDestinationKeyFields().stream().map(Field::getName).collect(Collectors.joining(","))
                +")";

        String foreignKeyDirectionStr;
        if(traversalStepSource == ForeignKeyTableEnum.PRIMARY_KEY_TABLE){
            foreignKeyDirectionStr = " --PK---FK--> ";
        } else {
            foreignKeyDirectionStr = " --FK---PK--> ";
        }


        return getSourceName() + ":" + sourceKeyFieldsString
                + foreignKeyDirectionStr
                + getDestinationName() + ":" + destinationKeyFieldsString;

    }

    public String toStringCompact(){

        String sourceKeyFieldsString = "("
                + getSourceKeyFields().stream().map(Field::getName).collect(Collectors.joining(","))
                + ")";

        String destinationKeyFieldsString = "("
                + getDestinationKeyFields().stream().map(Field::getName).collect(Collectors.joining(","))
                +")";

        String foreignKeyDirectionStr;
        if(traversalStepSource == ForeignKeyTableEnum.PRIMARY_KEY_TABLE){
            foreignKeyDirectionStr = " <-- ";
        } else {
            foreignKeyDirectionStr = " --> ";
        }


        return getSourceName() + ":" + sourceKeyFieldsString
                + foreignKeyDirectionStr
                + getDestinationName() + ":" + destinationKeyFieldsString;

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TraversalStep that = (TraversalStep) o;
        return Objects.equal(foreignKeyEdge, that.foreignKeyEdge) &&
                traversalStepSource == that.traversalStepSource;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(foreignKeyEdge, traversalStepSource);
    }
}
