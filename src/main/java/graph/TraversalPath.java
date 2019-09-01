package graph;

import com.google.common.base.Objects;
import database.JOOQDatabaseInteractor;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class TraversalPath implements Serializable {


    public static boolean VERBOSE = false;

    private static final long serialVersionUID = -2685850006480503969L;
    private TraversalPath parent;
    private TraversalStep step;



    protected TraversalPath(TraversalPath parent, TraversalStep step) {
        this.parent = parent;
        this.step = step;
    }

    public TraversalPath extend(TraversalStep step){
        return new TraversalPath(this, step);
    }

    public static TraversalPath extend(@Nullable TraversalPath parentTraversalPath, ForeignKeyEdge foreignKeyEdge, Table lastlyJoinedTable) throws GraphTraversalException {
        TraversalStep traversalStep = new TraversalStep(foreignKeyEdge, lastlyJoinedTable);

        if(parentTraversalPath == null) {
            return makeInitialPath(traversalStep);
        }else if(parentTraversalPath instanceof EmptyTraversalPath){
            return parentTraversalPath.extend(traversalStep);
        } else{
            if (VERBOSE){
                System.out.println("current traversal step: " + traversalStep.toString());
                System.out.println("new traversal path:");
            }
            TraversalPath extendedTraversalPath = parentTraversalPath.extend(traversalStep);
            if (VERBOSE){
                System.out.println(extendedTraversalPath.toString("\t"));
                System.out.println("----");
            }
            return extendedTraversalPath;
        }
    }


    public static TraversalPath makeInitialPath(TraversalStep step){
        return new TraversalPath(null, step);
    }

    public TraversalStep getFirst(){
        if(parent == null){
            return this.step;
        } else{
            return parent.getFirst();
        }
    }

    public TraversalStep getLast(){
        return this.step;
    }

    public int size(){
        if(parent == null){ // is this root
            return 1;
        } else{
            return 1 + parent.size();
        }
    }

    public Table getLastTable(JOOQDatabaseInteractor jooqDatabaseInteractor){
        return getLast().getDestination(jooqDatabaseInteractor);
    }


    public int length(){
        return this.size();
    }


    private String toStringRecursive(String indent){
        String str = "";
        if(parent != null){
            str += parent.toStringRecursive(indent) + ",\n";
        }
        str += indent + "\t" + step.toString();
        return str;
    }

    public String toString(String indent){
        return indent + "[" + "\n"
                + toStringRecursive(indent) + "\n"
                + indent +"]" + "\n";
    }

    public String toStringCompact(String indent){
        StringBuilder stringBuilder = new StringBuilder();
        toStringCompactRecursive(indent, stringBuilder);
        return stringBuilder.toString();
    }

    private void toStringCompactRecursive(String indent, StringBuilder stringBuilder){
        if(parent == null){
            stringBuilder.append(indent)
                    .append(step.getSourceName());
        }else{
            parent.toStringCompactRecursive(indent, stringBuilder);
        }

        String sourceKeyFieldsString = "("
                + step.getSourceKeyFields().stream().map(Field::getName).collect(Collectors.joining(","))
                + ")";
        String destinationKeyFieldsString = "("
                + step.getDestinationKeyFields().stream().map(Field::getName).collect(Collectors.joining(","))
                +")";

        if(step.getTraversalStepSource() == ForeignKeyTableEnum.PRIMARY_KEY_TABLE){
            stringBuilder.append( " <-"+ sourceKeyFieldsString + "--" + destinationKeyFieldsString+ "-- ");
        } else {
            stringBuilder.append( " --"+ sourceKeyFieldsString + "--" + destinationKeyFieldsString+ "-> ");
        }

        stringBuilder.append(step.getDestinationName());
    }

    public String toString(){
        return toString("");
    }


    public List<TraversalStep> getTraversalSteps(){
        List<TraversalStep> traversalSteps = new ArrayList<>();
        return getTraversalStepsRecursive(traversalSteps);

    }

    private List<TraversalStep> getTraversalStepsRecursive(List<TraversalStep> traversalSteps){
        if(parent == null){
            traversalSteps.add(step);
        } else {
            parent.getTraversalStepsRecursive(traversalSteps);
            traversalSteps.add(step);
        }
        return traversalSteps;
    }

    public List<Condition> getJoinConditionList() throws InvalidKeyInfoException {
        List<Condition> foreignKeyJoinConditionList = new ArrayList<>();
        for (TraversalStep traversalStep : this.getTraversalSteps()) {
            foreignKeyJoinConditionList.add(traversalStep.getJoinCondition());
        }
        return foreignKeyJoinConditionList;
    }

    private void getTablesRecursive(LinkedHashSet<Table> tablesOnPath, JOOQDatabaseInteractor jooqDatabaseInteractor){
        if(parent == null){
            tablesOnPath.add(step.getSource(jooqDatabaseInteractor));
            tablesOnPath.add(step.getDestination(jooqDatabaseInteractor));
        } else{
            getTablesRecursive(tablesOnPath, jooqDatabaseInteractor);
            tablesOnPath.add(step.getDestination(jooqDatabaseInteractor));
        }
    }

    public LinkedHashSet<Table> getTables(JOOQDatabaseInteractor jooqDatabaseInteractor){
        LinkedHashSet<Table> tablesOnPath = new LinkedHashSet<>();
        getTablesRecursive(tablesOnPath, jooqDatabaseInteractor);
        return tablesOnPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TraversalPath that = (TraversalPath) o;
        return Objects.equal(parent, that.parent) &&
                Objects.equal(step, that.step);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(parent, step);
    }
}
