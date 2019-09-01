package lazybum;

import database.JOOQDatabaseInteractor;
import dataset.Example;
import feature.columntype.UnsupportedFieldTypeException;
import feature.featuretable.FeatureColumn;
import feature.featuretable.FeatureTableHandler;
import graph.TraversalPath;
import learning.testing.HasRowsTest;
import learning.testing.NodeTest;
import learning.testing.TestGenerator;
import learning.testing.TestGeneratorBuilder;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.jooq.Field;
import org.jooq.Table;

import java.util.*;

/**
 * Created by joschout.
 */
@SuppressWarnings("Duplicates")
public class FTTestGenerator {


//    /**
//     * Generate tests based on the current feature table.
//     * NOTE: 2 ways to handle memory-wise:
//     *  1. in-memory: since we recently calculated the feature tables, they are still in-memory.
//     *                 Use these structures to calculate the tests to split on.
//     *  2. from disk:
//     *      since we write out the feature table, we can just as well re-read it to use for our feature calculation
//     *
//     * @return
//     * @throws UnsupportedFieldTypeException
//     */

    public static Set<ImmutableTriple<NodeTest, Map<Object, Integer>, FeatureColumn>> generateTests(
            Set<FeatureTableExtension> featureTableExtensionsToCheck,
            JOOQDatabaseInteractor jooqDatabaseInteractor,
            List<Example> currentInstances
            ) throws UnsupportedFieldTypeException {

        Set<ImmutableTriple<NodeTest, Map<Object, Integer>, FeatureColumn>> tests = new HashSet<>();

        // get the complete feature table
        for(FeatureTableExtension featureTableExtension: featureTableExtensionsToCheck){
            FeatureTableHandler featureTableHandler = featureTableExtension.getFeatureTableHandler();

            Map<Object, Integer> instanceToListIndexMap = featureTableHandler.getInstanceToListIndexMap();
            Optional<TraversalPath> optionalTraversalPath = featureTableExtension.getExtendedTraversalPath();
            // note: unsafe, might get you into trouble later on
            TraversalPath traversalPath = optionalTraversalPath.orElse(null);

            if (traversalPath != null) {
                Set<Object> instancesHavingRows = featureTableHandler.getInstanceToListIndexMap().keySet();
                HasRowsTest hasRowsTest = new HasRowsTest(null, traversalPath, traversalPath.getLast().getDestinationKeyFields().get(0), instancesHavingRows);
                tests.add(new ImmutableTriple<>(hasRowsTest, null, null));
            }

            for(FeatureColumn featureColumn: featureTableHandler.getFeatureColumnList()){

                if(traversalPath != null) {
                    Table lastTable = traversalPath.getLastTable(jooqDatabaseInteractor);
                    Field originalField = featureColumn.getOriginalField();
                    Field field =
                            lastTable.field(featureColumn.getOriginalField().getName());
                    if (field == null) {
                        System.out.println(featureTableExtension.toString());
                        throw new IllegalArgumentException("field " +originalField.getName() +" should not be null");
                    }
                }

                TestGenerator testGenerator = TestGeneratorBuilder.buildFor(null, traversalPath, featureColumn,
                        currentInstances, instanceToListIndexMap);
                testGenerator.forEachRemaining(
                        test -> tests.add(new ImmutableTriple<NodeTest, Map<Object, Integer>, FeatureColumn>(test, instanceToListIndexMap, featureColumn))
                );
            }
        }
        return tests;
    }

}
