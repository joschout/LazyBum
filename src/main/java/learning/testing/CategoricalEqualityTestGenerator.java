package learning.testing;

import feature.featuretable.FeatureColumn;
import graph.TraversalPath;
import org.jooq.Field;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CategoricalEqualityTestGenerator<T> extends TestGenerator {

    private NodeTest parentTest;
    private Field field;
    private TraversalPath traversalPath;

    private Iterator<T> possibleValueIterator;

    public CategoricalEqualityTestGenerator(NodeTest parentTest,
                                            TraversalPath traversalPath, FeatureColumn<T> categoricalFeatureColumn) {
        this.parentTest = parentTest;


        Set<T> featureValueSet = categoricalFeatureColumn.getFeatureValuePerInstance().stream().filter(Objects::nonNull).collect(Collectors.toSet());

        this.possibleValueIterator = featureValueSet.iterator();

        this.field = categoricalFeatureColumn.getOriginalField();
        this.traversalPath = traversalPath;

        if(VERBOSE){
        System.out.println("CategoricalEqualityTestGenerator for field " + field.getName() );
        }

    }

    @Override
    public boolean hasNext() {
        return possibleValueIterator.hasNext();
    }

    @Override
    public CategoricalEqualityTest next() {

        T nextValue = possibleValueIterator.next();
        return new CategoricalEqualityTest<T>(parentTest, traversalPath, field, nextValue);
    }
}