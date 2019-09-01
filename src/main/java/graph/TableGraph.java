package graph;

import config.ImproperProgramConfigurationException;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphExporter;
import org.jooq.ForeignKey;
import org.jooq.Table;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

public class TableGraph {


    public static Graph<String, ForeignKeyEdge> createFrom(List<Table<?>> tableList ){

//        Graph<Table, ForeignKeyEdge> graph = new SimpleGraph<>(ForeignKeyEdge.class);
        Graph<String, ForeignKeyEdge> graph = new DefaultDirectedGraph<>(ForeignKeyEdge.class);

        // add the vertices
        for (Table table: tableList){
            graph.addVertex(table.getName());
        }

        //add the edges to create a linking structure
        for (Table table: tableList){

            // get the FKs of the current table
            List<ForeignKey<?,?>> foreignKeys = table.getReferences();
            for(ForeignKey foreignKey: foreignKeys){
                Table otherTable = foreignKey.getKey().getTable();

                // the unqualified table names
                String tableName = table.getName();
                String otherTableName = otherTable.getName();


                graph.addEdge(tableName, otherTableName, new ForeignKeyEdge(foreignKey));
//                graph.addEdge(table, otherTable, new ForeignKeyEdge(foreignKey));
            }
        }
        return graph;
    }


    public static Graph<String, ForeignKeyEdge> createFrom(Table targetTable){

//        Graph<Table, ForeignKeyEdge> graph = new SimpleGraph<>(ForeignKeyEdge.class);
        Graph<String, ForeignKeyEdge> graph = new DefaultDirectedGraph<>(ForeignKeyEdge.class);

        // add the vertices
        graph.addVertex(targetTable.getName());
        return graph;
    }


    public static String asDOTString(Graph graph){
        // use helper classes to define how vertices should be rendered,
        // adhering to the DOT language restrictions
        ComponentNameProvider<String> vertexIdProvider =
                new ComponentNameProvider<String>()
                {
                    public String getName(String table_as_vertex)
                    {
                        return table_as_vertex;
//                        return table_as_vertex.getName();
                    }
                };


        GraphExporter<String, ForeignKeyEdge> graphExporter = new DOTExporter<>(vertexIdProvider, null, null);
        Writer writer = new StringWriter();
        try {
            graphExporter.exportGraph(graph, writer);
        } catch (ExportException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    public static Table getTargetTable(Graph<Table, ForeignKeyEdge>graph, String targetTableName) throws ImproperProgramConfigurationException {
        return graph.vertexSet().stream()
                .filter(
                        table -> table.getName().equals(targetTableName))
                .findAny().orElseThrow(
                        () -> new ImproperProgramConfigurationException(
                                "Could not find table '" + targetTableName +"' in the relational graph:\n"
                                        + TableGraph.asDOTString(graph)));
    }

    public static String getTargetTableName(Graph<String, ForeignKeyEdge>graph, String targetTableName) throws ImproperProgramConfigurationException {
        return graph.vertexSet().stream()
                .filter(
                        tableName -> tableName.equals(targetTableName))
                .findAny().orElseThrow(
                        () -> new ImproperProgramConfigurationException(
                                "Could not find table '" + targetTableName +"' in the relational graph:\n"
                                        + TableGraph.asDOTString(graph)));
    }


}
