package lazybum;

import database.JOOQDatabaseInteractor;
import graph.ForeignKeyEdge;
import graph.TableGraph;
import graph.TraversalPath;
import graph.TraversalStep;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jooq.Table;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Each feature table is defined by
 * - the set of traversal paths used to create the feature table
 * - the examples to be featured in the table.
 *
 * This class is a wrapper around the traversal paths for one feature table.
 *
 * Created by joschout.
 */
public class FTTraversalGraph {


    /*
    * The traversal paths making up a feature table.
    * There will be overlap between the paths, which is fine, since they are a hierarchical data structure.
    * */
    private final Set<TraversalPath> traversalPaths;


    public FTTraversalGraph(Set<TraversalPath> traversalPaths) {
        this.traversalPaths = traversalPaths;
    }

    public Map<TraversalPath, Table> getTablesAtTheEndOfTraversalPaths(JOOQDatabaseInteractor jooqDatabaseInteractor ){
        return traversalPaths.stream()
                .collect(Collectors.toMap(
                            Function.identity(),
                            traversalPath -> traversalPath.getLastTable(jooqDatabaseInteractor)
                        )
                );
    }

    public FTTraversalGraph extend(Map<TraversalPath, Set<TraversalPath>> traversalPathToExtensionMap){
        Set<TraversalPath> traversalPathsExtendedGraph = new HashSet<>();

        for (TraversalPath oldTraversalPath : traversalPaths) {
            // if the old traversal path has been extended, save the extensions
            if(traversalPathToExtensionMap.containsKey(oldTraversalPath)){
                traversalPathsExtendedGraph.addAll(traversalPathToExtensionMap.get(oldTraversalPath));
            } else{ // save the old traversal path
                traversalPathsExtendedGraph.add(oldTraversalPath);
            }
        }
        return new FTTraversalGraph(traversalPathsExtendedGraph);
    }


    public String asDOTString(){
        System.out.println("WARNING: computationally heavy toString method");

        Graph<String, ForeignKeyEdge> graph = new DefaultDirectedGraph<>(ForeignKeyEdge.class);

        for(TraversalPath traversalPath: traversalPaths){
            for(TraversalStep step: traversalPath.getTraversalSteps()){
                String source = step.getSourceName();
                String destination = step.getDestinationName();

                if(! graph.containsVertex(source)){
                    graph.addVertex(source);
                }
                if(! graph.containsVertex(destination)){
                    graph.addVertex(destination);
                }

                if(! graph.containsEdge(source, destination)){
                    graph.addEdge(source, destination);
                }
            }
        }

        return TableGraph.asDOTString(graph);
    }


}
