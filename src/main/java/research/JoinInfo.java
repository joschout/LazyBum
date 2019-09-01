package research;

import feature.featuretable.FeatureTableHandler;
import graph.TraversalPath;
import org.jooq.DSLContext;
import org.jooq.Table;

import java.util.Optional;

public class JoinInfo {

     private TraversalPath traversalPath;
     public Table joinTable;
     public FeatureTableHandler featureTableHandler;
     public FieldController fieldController;

    public JoinInfo(TraversalPath traversalPath, Table joinTable, FeatureTableHandler featureTableHandler, FieldController fieldController) {
        this.traversalPath = traversalPath;
        this.joinTable = joinTable;
        this.featureTableHandler = featureTableHandler;
        this.fieldController = fieldController;
    }

    public void destruct(DSLContext dslContext){
        System.out.println("Dropping join table:" + joinTable.getName());
        dslContext.dropTable(joinTable).execute();
    }


    public Optional<TraversalPath> getTraversalPath(){
        return Optional.ofNullable(traversalPath);
    }

}
