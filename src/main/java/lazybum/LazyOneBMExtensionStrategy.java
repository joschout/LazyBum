package lazybum;

import database.JOOQDatabaseInteractor;
import dataset.TargetTableManager;
import graph.TraversalPath;
import org.jooq.Table;

import java.util.Map;

/**
 * Created by joschout.
 */
public class LazyOneBMExtensionStrategy implements TraversalGraphExtensionStrategy {
    @Override
    public Map<TraversalPath, Table> getTraversalPathsToExtend(FTTraversalGraph traversalGraph, LazyBumTreeNode currentLazyBumTreeNode, JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager) {

        // the traversal paths from the current traversal graph, with their end tables
        Map<TraversalPath, Table> traversalPathToEndingTableMap
                = traversalGraph.getTablesAtTheEndOfTraversalPaths(jooqDatabaseInteractor);
        return traversalPathToEndingTableMap;
    }
}
