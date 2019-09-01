package lazybum;

import database.JOOQDatabaseInteractor;
import dataset.TargetTableManager;
import graph.TraversalPath;
import org.jooq.Table;

import java.util.HashMap;
import java.util.Map;

public class NeverExtendStrategy implements TraversalGraphExtensionStrategy {


    @Override
    public Map<TraversalPath, Table> getTraversalPathsToExtend(FTTraversalGraph traversalGraph, LazyBumTreeNode currentLazyBumTreeNode, JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager) {
        return new HashMap<>(0);
    }
}
