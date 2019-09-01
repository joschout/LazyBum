package lazybum;

import database.JOOQDatabaseInteractor;
import dataset.TargetTableManager;
import graph.TraversalPath;
import org.jooq.Table;

import java.util.Map;

/**
 * Created by joschout.
 */
public interface TraversalGraphExtensionStrategy {

    public Map<TraversalPath, Table> getTraversalPathsToExtend(
            FTTraversalGraph traversalGraph, LazyBumTreeNode currentLazyBumTreeNode,
            JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager);
}
