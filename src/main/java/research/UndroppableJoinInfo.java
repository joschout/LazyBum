package research;

import feature.featuretable.FeatureTableHandler;
import graph.TraversalPath;
import org.jooq.DSLContext;
import org.jooq.Table;

public class UndroppableJoinInfo extends JoinInfo {
    public UndroppableJoinInfo(TraversalPath traversalPath, Table joinTable, FeatureTableHandler featureTableHandler, FieldController fieldController) {
        super(traversalPath, joinTable, featureTableHandler, fieldController);
    }

    public void destruct(DSLContext dslContext){}

}
