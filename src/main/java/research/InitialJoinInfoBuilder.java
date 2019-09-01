package research;

import dataset.TargetTableManager;
import feature.columntype.UnsupportedFieldTypeException;
import feature.featuretable.FeatureTableHandler;
import feature.multisettable.MultiSetTableHandler;
import feature.multisettable.MultiSetTableTransformer;
import feature.tranform.UnsupportedFeatureTransformationException;
import org.jooq.*;

import java.util.*;
import java.util.stream.Collectors;

public class InitialJoinInfoBuilder {

    public static Set<JoinInfo> getInitialSingleJoinInfoSet(TargetTableManager targetTableManager, DSLContext dslContext) throws UnsupportedFeatureTransformationException, UnsupportedFieldTypeException {
        Table table = targetTableManager.getTargetTable();
        Field targetIDField = targetTableManager.getTargetID();
        Field targetLabel = targetTableManager.getTargetColumn();

        // get all target table fields but the targetID and targetLabel



        FieldController initialFieldController = FieldController.buildInitial(table);

        List<Field> fieldsToSelect = Arrays.stream(table.fields())
                .filter(field
                        -> !field.equals(targetIDField)
                        && !field.equals(targetLabel))
                .collect(Collectors.toList());

        // for the target table, no fields need to be aliased
        Map<Field, Field> fieldsToSelectAliasMap = new HashMap<>();
        for(Field fieldToSelect: fieldsToSelect){
            fieldsToSelectAliasMap.put(fieldToSelect, fieldToSelect);
        }
        MultiSetTableHandler multiSetTableHandler = new MultiSetTableHandler(targetIDField, fieldsToSelectAliasMap, table);

        /*
         * NOTE: it is weird that
         *    FieldController contains all fields (including the targetid and targetlabel),
         *   but MultiSetTableHandler contains all fields EXCEPT the targetid and targetlabel
         * */


        Result<Record> records = dslContext
                .select()
                .from(table).fetch();
//        MultiSetTableHandler multiSetTableHandler = new MultiSetTableHandler(targetIDField, fieldsToSelect);
        multiSetTableHandler.add(records);

        if(RefactoredJoinTableConstruction.VERBOSE) {
            System.out.println(multiSetTableHandler.toString());
        }
        FeatureTableHandler featureTableHandler = MultiSetTableTransformer.transform(multiSetTableHandler);
        if(RefactoredJoinTableConstruction.VERBOSE) {
            System.out.println(featureTableHandler.toString());
        }
        Set<JoinInfo> singleJoinInfoSet = new HashSet<>();
        singleJoinInfoSet.add(
                new UndroppableJoinInfo(null, table, featureTableHandler, initialFieldController));
        return singleJoinInfoSet;
    }

}
