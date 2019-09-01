package learning.testing;


import database.JOOQDatabaseInteractor;
import graph.InvalidKeyInfoException;
import dataset.TargetTableManager;
import feature.columntype.UnsupportedFieldTypeException;
import feature.tranform.UnsupportedFeatureTransformationException;
import graph.TraversalPath;
import org.jooq.Field;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/*
*  Class representing a test in a decision tree node
*
*
* */
public abstract class NodeTest implements Serializable {

    private static final long serialVersionUID = 9087827585535875485L;

    private NodeTest parentTest;
    private TraversalPath traversalPath;
    public String unqualifiedFieldName;

    public NodeTest(NodeTest parentTest, TraversalPath traversalPath,
                    Field field) {
        this.parentTest = parentTest;
        this.traversalPath = traversalPath;
//        if(traversalPath != null && traversalPath.toStringCompact("").replace("\n","").equals("feat <-(id)--(id1)-- edges")
//            && field.getName().equals("id")
//        ){
//            System.out.println("warning");
//        }

        if(field!=null){
            this.unqualifiedFieldName = field.getName();
        } else{
            this.unqualifiedFieldName = null;
        }
    }

    public NodeTest() {
        this(null, null, null);
    }

    public Optional<NodeTest> getParentTest() {
        return Optional.ofNullable(parentTest);
    }

    public Optional<TraversalPath> getTraversalPath(){
        return Optional.ofNullable(traversalPath);
    }


    public abstract boolean evaluate(Object featureValue);

    public abstract String toString(String indentation);

//    public abstract boolean evaluate(Object instanceID, JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager) throws InvalidKeyInfoException, UnsupportedFeatureTransformationException, UnsupportedFieldTypeException;

    public abstract Set<Object> evaluateBatch(Collection<Object> instanceIDs,
                                              JOOQDatabaseInteractor jooqDatabaseInteractor, TargetTableManager targetTableManager,
                                              boolean missingValueCorrespondsToSuccess
                                              ) throws UnsupportedFeatureTransformationException, UnsupportedFieldTypeException, InvalidKeyInfoException;
}
