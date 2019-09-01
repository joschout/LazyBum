package lazybum;

import graph.TraversalPath;

import java.util.*;

/**
 * Created by joschout.
 */
public class FeatureTableInfo {


    /**
     * The examples for which this feature table extension is defined;
     */
    private List<Object> exampleIDs;

    private final FeatureTableInfo parentFeatureTableJoinInfo;

    /**
     * The traversal paths used in creating the current feature table.
     */
    private final FTTraversalGraph traversalGraph;

    /**
     * The feature table extensions used in this feature table
     */
    private final Set<FeatureTableExtension> featureTableExtensions;


    public FeatureTableInfo(FeatureTableInfo parentFeatureTableJoinInfo,
                            FTTraversalGraph traversalGraph, Set<FeatureTableExtension> featureTableExtensions) {
        this.exampleIDs = null;
        this.parentFeatureTableJoinInfo = parentFeatureTableJoinInfo;
        this.traversalGraph = traversalGraph;
        this.featureTableExtensions = featureTableExtensions;
    }

    public FeatureTableInfo extend(Set<FeatureTableExtension> featureTableExtensions,
                       Map<TraversalPath, Set<TraversalPath>> traversalPathToExtensionMap ){
        FTTraversalGraph extendedGraph = this.traversalGraph.extend(traversalPathToExtensionMap);
        return new FeatureTableInfo(this, extendedGraph, featureTableExtensions);
    }

    public FTTraversalGraph getTraversalGraph() {
        if(traversalGraph == null){
            throw new IllegalStateException("Traversal graph is null." +
                    " This should only be the case for the feature table corresponding to the target table," +
                    " and for that table, I don't think we ever need to access this.");
        }
        return traversalGraph;
    }

    public Optional<FeatureTableInfo> getParentFeatureTableJoinInfo() {
        return Optional.ofNullable(parentFeatureTableJoinInfo);
    }

    /**
     * Get the feature table extensions from the last expansion
     * @return
     */
    public Set<FeatureTableExtension> getFeatureTableExtensions() {
        return featureTableExtensions;
    }

    public Set<FeatureTableExtension> getAllHierarchicalFeatureTableExtensions(){
        Set<FeatureTableExtension> allFeatureTableExtensions = new HashSet<>();
        getAllHierarchicalFeatureTableExtensionsRecursive(allFeatureTableExtensions);
        return allFeatureTableExtensions;
    }

    private void getAllHierarchicalFeatureTableExtensionsRecursive(Set<FeatureTableExtension> allFeatureTableExtensions){
        allFeatureTableExtensions.addAll(featureTableExtensions);
        if(parentFeatureTableJoinInfo != null){
            parentFeatureTableJoinInfo.getAllHierarchicalFeatureTableExtensionsRecursive(featureTableExtensions);
        }
    }

    public String toString(){
        return toString("");
    }

    public String toString(String indentation){
        String str = indentation + "has parent: " + (parentFeatureTableJoinInfo == null) + "\n";
        str += indentation + "nb of extensions: " + featureTableExtensions.size() + "\n";

        int extCount = 1;
        for (FeatureTableExtension featureTableExtension : featureTableExtensions) {
            String featureTableExtensionIndent = "\t" + extCount + ":\t";
            str += featureTableExtension.toString(featureTableExtensionIndent);
            extCount++;
        }
        return str;


    }


}
