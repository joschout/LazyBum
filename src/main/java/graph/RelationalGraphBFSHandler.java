package graph;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RelationalGraphBFSHandler<TVertex> {



    //relational graph
    Graph<TVertex, ForeignKeyEdge> graph;

    Map<TVertex, Integer> bfsDepths;

    /*
     * Get the tree subgraph necessary for forward-only traversal mode,
     * by giving each table a depth using breadth-first search
     * */
    public RelationalGraphBFSHandler(Graph<TVertex, ForeignKeyEdge> graph, TVertex targetTable){
        this.graph = graph;
        this.bfsDepths = getBreathFirstDepths(graph, targetTable);
    }


    public RelationalGraphBFSHandler(Graph<TVertex, ForeignKeyEdge> graph,
                                     Map<TVertex, Integer> bfsDepths) {
        this.graph = graph;
        this.bfsDepths = bfsDepths;
    }


    public Set<ForeignKeyEdge> getDirectLinksToTablesWithHigherDepth(TVertex table) {
        return getDirectLinksToTablesWithHigherDepthAsStream(table).collect(Collectors.toSet());

//        /*
//         * for the current table, get the tables connected to it using foreign keys
//         * AND check that their depth > depth of the current table
//         *
//         * */
//
//        Set<ForeignKeyEdge> foreignKeyEdgeSet = graph.edgesOf(table);
////        Set<org.jooq.ForeignKey> foreignKeysToBeChecked = new HashSet<>();
//        Set<ForeignKeyEdge> foreignKeyEdgesToBeChecked = new HashSet<>();
//        for (ForeignKeyEdge foreignKeyEdge : foreignKeyEdgeSet) {
//
//
//            Table neighbor = Graphs.getOppositeVertex(graph, foreignKeyEdge, table);
//            int neighborDepth = bfsDepths.get(neighbor);
//            if (neighborDepth <= bfsDepths.get(table)) {
//                continue;
//            } else {
////                org.jooq.ForeignKey foreignKey = foreignKeyEdge.foreignKey;
////                foreignKeysToBeChecked.add(foreignKey);
//                foreignKeyEdgesToBeChecked.add(foreignKeyEdge);
//            }
//        }
//
//        return foreignKeyEdgesToBeChecked;
////        return foreignKeysToBeChecked;
    }


    private boolean doesNeigbourHaveAnIncreasedDepth(TVertex table, ForeignKeyEdge edge){
        TVertex neighbor = Graphs.getOppositeVertex(graph, edge, table);
        return bfsDepths.get(neighbor) > bfsDepths.get(table);
    }


    public Stream<ForeignKeyEdge> getDirectLinksToTablesWithHigherDepthAsStream(TVertex table){
        return graph.edgesOf(table).stream() // for each foreign key edge
                .filter(edge -> doesNeigbourHaveAnIncreasedDepth(table, edge));
    }

    public Map<TVertex,Integer> getBreathFirstDepths(Graph<TVertex, ?> graph, TVertex targetTable){

        AsUndirectedGraph asUndirectedGraph = new AsUndirectedGraph(graph);
        BreadthFirstIterator breadthFirstIterator = new BreadthFirstIterator(asUndirectedGraph, targetTable);

        Map<TVertex, Integer> tableDepths = new HashMap<>();
        graph.vertexSet().forEach(
                table -> tableDepths.put(table, Integer.MAX_VALUE));
        tableDepths.put(targetTable, 0);

        while(breadthFirstIterator.hasNext()){

            TVertex nextTable = (TVertex) breadthFirstIterator.next();
            int depth = breadthFirstIterator.getDepth(nextTable);
            tableDepths.put(nextTable, depth);

//            System.out.println(depth);
//            System.out.println(nextTable.getName());
        }
        return tableDepths;
    }
}
