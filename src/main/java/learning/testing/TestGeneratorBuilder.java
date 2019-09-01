package learning.testing;

import dataset.Example;
import feature.columntype.UnsupportedFieldTypeException;
import feature.featuretable.FeatureColumn;
import feature.tranform.FieldTransformerEnumInterface;
import graph.TraversalPath;
import org.jooq.DataType;
import org.jooq.impl.SQLDataType;

import javax.annotation.Nullable;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TestGeneratorBuilder {

    public static TestGenerator buildFor(NodeTest parentTest, @Nullable TraversalPath traversalPath, FeatureColumn featureColumn,
                                         List<Example> instances, Map<Object, Integer> instanceToListIndexMap) throws UnsupportedFieldTypeException {


        Optional<FieldTransformerEnumInterface> featureTransform = featureColumn.getTransformation();


        DataType dataType;
        if(featureTransform.isPresent()) {
            FieldTransformerEnumInterface featureTransformerOEnumInterface = featureTransform.get();
            dataType = featureTransformerOEnumInterface.getSqlDataType();
            if(dataType.getSQLType() == SQLDataType.BOOLEAN.getSQLType()){
                return new ExistsInMultiSetTestGenerator(parentTest, traversalPath, featureColumn);
            }


        } else {
            dataType = featureColumn.getOriginalField().getDataType();
            if(dataType == null){
                throw new NullPointerException("DATATYPE IS NULL; NOT SUPPORTED");
            }
        }

        if(dataType.getSQLType() == SQLDataType.DOUBLE.getSQLType() ||
            dataType.getSQLType() == SQLDataType.INTEGER.getSQLType() ||
            dataType.getSQLType() == SQLDataType.BIGINT.getSQLType() ||
            dataType.getSQLType() == SQLDataType.NUMERIC.getSQLType()
        ) {
            return new NumericalFeatureTestSpitFinderRewrite(parentTest, traversalPath, featureColumn,
                    instances, instanceToListIndexMap);

//            return new NumericalFeatureTestGenerator(parentTest, traversalPath, featureColumn);

        }
        else if (dataType.getSQLType() == SQLDataType.BOOLEAN.getSQLType()){
            return new BooleanFeatureTestGenerator(parentTest, traversalPath, featureColumn);
        }
        else if(dataType.getSQLType() == SQLDataType.VARCHAR.getSQLType()){
            return new CategoricalEqualityTestGenerator<String>(parentTest, traversalPath, featureColumn);
        }
        else{
            return new CategoricalEqualityTestGenerator<Date>(parentTest, traversalPath, featureColumn);
        }
    }

}
