package graph;

import database.JOOQDatabaseInteractor;
import org.jooq.Condition;
import org.jooq.Table;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class EmptyTraversalPath extends TraversalPath {


    private static final long serialVersionUID = 8926419357982829825L;

    public Table root;

    public EmptyTraversalPath(Table root) {
        super(null, null);
        this.root = root;
    }

    public TraversalPath extend(TraversalStep step){
        if(step.getSourceName().equals(root.getName())){
            return makeInitialPath(step);
        } else{
            throw new UnsupportedOperationException("the root table does not have the same name as the source of the given traversal step");
        }
    }

    public int size(){
        return 0;
    }

    public int length(){
        return size();
    }

    public TraversalStep getFirst(){
        throw new UnsupportedOperationException("Empty path has no first step");
    }

    public TraversalStep getLast(){
        throw new UnsupportedOperationException("Empty path has no last step");
    }


    public String toString(String indent){
        return indent + "[" + this.root.getName() +"]" + "\n";
    }

    public String toStringCompact(String indent){
        return this.toString(indent);
    }

    public String toString(){
        return toString("");
    }

    public List<TraversalStep> getTraversalSteps(){
        return new ArrayList<>(0);
    }

    public List<Condition> getJoinConditionList() throws InvalidKeyInfoException {
        return new ArrayList<>(0);
    }

    public LinkedHashSet<Table> getTables(JOOQDatabaseInteractor jooqDatabaseInteractor){
        return new LinkedHashSet<>();
    }

}
