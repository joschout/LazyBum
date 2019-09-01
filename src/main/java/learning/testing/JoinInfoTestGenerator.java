package learning.testing;

import dataset.Example;
import feature.columntype.UnsupportedFieldTypeException;
import feature.featuretable.FeatureColumn;
import feature.featuretable.FeatureTableHandler;
import graph.TraversalPath;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import research.JoinInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

//import org.apache.commons.lang3.tuple.Triple;

public class JoinInfoTestGenerator {


    public static Set<ImmutableTriple<NodeTest, JoinInfo, FeatureColumn>> generateTests(NodeTest parentTest,
                                                                                        List<JoinInfo>joinInfos,
                                                                                        List<Example> instances) throws UnsupportedFieldTypeException {
        Set<ImmutableTriple<NodeTest, JoinInfo, FeatureColumn>> tests = new HashSet<>();
        for (JoinInfo joinInfo : joinInfos) {

            FeatureTableHandler featureTableHandler = joinInfo.featureTableHandler;
            Optional<TraversalPath> optionalTraversalPath = joinInfo.getTraversalPath();
            TraversalPath traversalPath = optionalTraversalPath.orElse(null);

            for(FeatureColumn featureColumn: featureTableHandler.getFeatureColumnList()){

                System.out.println("FIX THE ADDED NULL as instaceToListIndexMap");
                TestGenerator testGenerator = TestGeneratorBuilder.buildFor(parentTest, traversalPath, featureColumn,
                        instances, null);

                testGenerator.forEachRemaining(nodeTest -> {
                    tests.add(new ImmutableTriple<>(nodeTest, joinInfo, featureColumn));
                });
            }
        }
        return tests;
    }

}
