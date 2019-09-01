package lazybum;

import database.JOOQDatabaseInteractor;
import dataset.TargetTableManager;
import graph.TraversalPath;
import org.jooq.Table;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by joschout.
 */
public class ExtendEndingTablesUsedInDTTestStrategy implements TraversalGraphExtensionStrategy {

    /**
     * Get the traversal paths to extend.
     * These are the traversal paths from the traversal graph
     *   that end in a table used in a test in the current decision tree branch.
     *
     * @param traversalGraph
     * @param currentLazyBumTreeNode
     * @return
     */
    @Override
    public Map<TraversalPath, Table> getTraversalPathsToExtend(
            FTTraversalGraph traversalGraph, LazyBumTreeNode currentLazyBumTreeNode,
            JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager){

        // the traversal paths from the current traversal graph, with their end tables
        Map<TraversalPath, Table> traversalPathToEndingTableMap
                = traversalGraph.getTablesAtTheEndOfTraversalPaths(jooqDatabaseInteractor);
        // get the tables used for features in the current decision tree branch
        Set<Table> tablesUsedInAncestorTests = currentLazyBumTreeNode.getAncestorTests().stream()
                .map(
                        /*
                         * The table from a test is
                         *  - if the there is a traversal path, the end of that path
                         *  - else, the target table
                         * */
                        ancestorTest
                                -> ancestorTest.getTraversalPath()
                                .map(traversalPath -> traversalPath.getLastTable(jooqDatabaseInteractor))
                                .orElse(targetTableManager.getTargetTable())
                )
                .collect(Collectors.toSet());

        Map<TraversalPath, Table> traversalPathsToFindNeighborsFor = new HashMap<>();
        for (TraversalPath traversalPath : traversalPathToEndingTableMap.keySet()) {
            Table endOfPath = traversalPathToEndingTableMap.get(traversalPath);
            if(tablesUsedInAncestorTests.contains(endOfPath)){
                traversalPathsToFindNeighborsFor.put(traversalPath, endOfPath);
            }
        }
        return traversalPathsToFindNeighborsFor;
    }
}
