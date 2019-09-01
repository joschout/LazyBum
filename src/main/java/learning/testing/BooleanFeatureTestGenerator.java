package learning.testing;

import feature.featuretable.FeatureColumn;
import graph.TraversalPath;
import org.jooq.Field;

public class BooleanFeatureTestGenerator extends TestGenerator {

    private NodeTest parentTest;
    private Field field;
    private TraversalPath traversalPath;

    private boolean hasNext;

    public BooleanFeatureTestGenerator(NodeTest parentTest,
                                         TraversalPath traversalPath, FeatureColumn<Boolean> booleanFeatureColumn) {
        this.parentTest = parentTest;
        this.field = booleanFeatureColumn.getOriginalField();
        this.traversalPath = traversalPath;
        this.hasNext = true;
//        System.out.println("BooleanFeatureTestGenerator for field " + field.getName());
    }

    @Override
    public boolean hasNext() {
        return this.hasNext;
    }

    @Override
    public BooleanFeatureTest next() {
        BooleanFeatureTest booleanFeatureTest = new BooleanFeatureTest(parentTest, traversalPath, field);
        this.hasNext = false;
        return booleanFeatureTest;
    }
}
