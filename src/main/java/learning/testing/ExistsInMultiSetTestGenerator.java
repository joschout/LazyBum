package learning.testing;

import feature.featuretable.FeatureColumn;
import feature.tranform.ExistInMultiSetTransform;
import graph.TraversalPath;
import org.jooq.Field;

public class ExistsInMultiSetTestGenerator extends TestGenerator {

    private NodeTest parentTest;
    private Field field;
    private TraversalPath traversalPath;
    private ExistInMultiSetTransform transform;

    private boolean hasNext;

    public ExistsInMultiSetTestGenerator(NodeTest parentTest,
                                       TraversalPath traversalPath, FeatureColumn booleanFeatureColumn) {
        this.parentTest = parentTest;
        this.field = booleanFeatureColumn.getOriginalField();
        this.traversalPath = traversalPath;
        this.hasNext = true;
        this.transform = (ExistInMultiSetTransform) booleanFeatureColumn.getTransformation().get();
//        System.out.println("BooleanFeatureTestGenerator for field " + field.getName());
    }

    @Override
    public boolean hasNext() {
        return this.hasNext;
    }

    @Override
    public ExistsInMultiSetTest next() {
        ExistsInMultiSetTest existsInMultiSetTest = new ExistsInMultiSetTest(
                parentTest, traversalPath, field, transform);
        this.hasNext = false;
        return existsInMultiSetTest;
    }
}
