package lazybum;

import feature.featuretable.FeatureTableHandler;
import graph.TraversalPath;

import java.util.Optional;

/**
 * Created by joschout.
 */
public class FeatureTableExtension {

    /**
     * Name or this feature table extension
     * */
    private String name;

    /**
     * The traversal path extended for this table extension
     * */
    private TraversalPath extendedTraversalPath;

    /**
     * OPTIONAL: a data structure representing the feature table extension.
     * If the table is too big to be kept in memory, this should be null,
     * and the table should be read from disk when it is needed.
     */
    private FeatureTableHandler featureTableHandler;

    public FeatureTableExtension(String name, TraversalPath extendedTraversalPath, FeatureTableHandler featureTableHandler) {
        this.name = name;
        this.extendedTraversalPath = extendedTraversalPath;
        this.featureTableHandler = featureTableHandler;
    }

    public Optional<TraversalPath> getExtendedTraversalPath() {
        return Optional.ofNullable(extendedTraversalPath);
    }

    public FeatureTableHandler getFeatureTableHandler() {
        return featureTableHandler;
    }

    @Override
    public String toString() {
        return toString("");
    }

    public String toString(String indentation) {
        String str = indentation + "FeatureTableExtension of:\n";
        if(extendedTraversalPath != null){
            str += extendedTraversalPath.toStringCompact(indentation);
        } else{
            str += "no traversal path\n";
        }
        str += featureTableHandler.toString(indentation);
        return str;
    }

    public int getNbOfFeatures(){
        return featureTableHandler.getNbOfFeatureColumns();
    }
}
