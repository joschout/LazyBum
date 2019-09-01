package lazybum;

import java.util.Set;

/**
 * Created by joschout.
 */
public class FeatureTableExtensionCounter {

    public int featureTableExtensionsInRoot = 0;

    public int extensionsCalculatedCounter = 0;
    public int extensionsAcceptedCounter = 0;

    
    
    public static FeatureTableExtensionCounter init(FeatureTableInfo rootFeatureTableInfo){
        FeatureTableExtensionCounter counter = new FeatureTableExtensionCounter();

        counter.featureTableExtensionsInRoot = rootFeatureTableInfo.getAllHierarchicalFeatureTableExtensions().size();
        counter.extensionsCalculatedCounter = counter.featureTableExtensionsInRoot;
        return counter;
    }

    public void countCalculatedExtensions(Set<FeatureTableExtension> newlyCalculatedFeatureTableExtensions){
        this.extensionsCalculatedCounter += newlyCalculatedFeatureTableExtensions.size();
    }

    public void countAcceptedExtensions(Set<FeatureTableExtension> newlyCalculatedFeatureTableExtensions){
        this.extensionsAcceptedCounter += newlyCalculatedFeatureTableExtensions.size();
    }

    public String statisticsToString(){
        return ("nb of table expansions in root feature table: " + featureTableExtensionsInRoot) + "\n" +
                "nb of extra table expansions used in feature tables: " + extensionsAcceptedCounter + "\n" +
                "total nb of table expansions calculated: " + extensionsCalculatedCounter + "\n";
    }
    
    
}
