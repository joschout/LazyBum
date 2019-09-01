package graph;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphExporter;

import java.io.StringWriter;
import java.io.Writer;

public class SimpleJGraphTExample {

    public static void main(String[] args) {
//        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String target = "target";
        String purchase = "purchase";
        String product = "product";
        String truck = "truck";
        String car = "car";

        graph.addVertex(target);
        graph.addVertex(purchase);
//        graph.addVertex(product);
//        graph.addVertex(truck);
//        graph.addVertex(car);

        graph.addEdge(target, purchase);
        graph.addEdge(purchase, target);


        String start = graph.vertexSet().stream().filter(
                str -> str.equals("target")).findAny().get();
        System.out.println(start);


        // use helper classes to define how vertices should be rendered,
        // adhering to the DOT language restrictions
        ComponentNameProvider<String> vertexIdProvider =
                new ComponentNameProvider<String>()
                {
                    public String getName(String string)
                    {
                        return string;
                    }
                };


        GraphExporter<String, DefaultEdge> graphExporter = new DOTExporter<>(vertexIdProvider, null, null);
        Writer writer = new StringWriter();
        try {
            graphExporter.exportGraph(graph, writer);
        } catch (ExportException e) {
            e.printStackTrace();
        }
        System.out.println(writer.toString());


    }

}
