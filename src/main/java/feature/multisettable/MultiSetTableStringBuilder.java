package feature.multisettable;

import feature.columntype.ColumnFieldHandler;
import org.jooq.Field;
import utils.PrettyPrinter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("Duplicates")
public class MultiSetTableStringBuilder {


    private static final String columnSeparator = " | ";
    private static final String columnRowIntersection = "-+-";

    public static final boolean SHORTEN = true;
    public static final int MAX_NB_OF_EXAMPLES_TO_PRINT_WHEN_SHORTEN_IS_TRUE = 10;

    private static int getStringLengthFirstColumn(Field targetIDField, Map<Object, Integer> instanceToListIndexMap ){
        int longestStringLengthFirstColumn = targetIDField.getName().length();
        for(Object instanceID: instanceToListIndexMap.keySet()){
            int instanceIDStrLength = instanceID.toString().length();
            if( instanceIDStrLength > longestStringLengthFirstColumn){
                longestStringLengthFirstColumn = instanceIDStrLength;
            }
        }
        return longestStringLengthFirstColumn;
    }

    private static Map<Field, Integer> getStringLengthForEachFieldColumn(Map<Field, ColumnFieldHandler> fieldToFieldHandlerMap){
        Map<Field, Integer> columnToMaxStrLengthMap = new HashMap<>();
        for(Field field: fieldToFieldHandlerMap.keySet()){
            ColumnFieldHandler fieldHandler = fieldToFieldHandlerMap.get(field);

            int longestStringLenghtForFieldColumn = Math.max(
                    field.getName().length(),
                    fieldHandler.maxStringLength());
            columnToMaxStrLengthMap.put(field, longestStringLenghtForFieldColumn);
        }
        return columnToMaxStrLengthMap;
    }

    private static String[] getHeadingAndRowSeparationLine(
            Field targetIDField, int longestStringLengthFirstColumn,
            Map<Field, ColumnFieldHandler> fieldToFieldHandlerMap,
            Map<Field, Integer> columnToMaxStrLengthMap){

        String[] headingAndRowSeparationLine = new String[2];

        String heading = ""
                + columnSeparator
                + PrettyPrinter.padLeft(targetIDField.getName(), longestStringLengthFirstColumn)
                + columnSeparator;

        String rowSeparationLine = ""
                + columnRowIntersection
                + String.join("", Collections.nCopies(longestStringLengthFirstColumn, "-"))
                + columnRowIntersection;

        for(Field field: fieldToFieldHandlerMap.keySet()){
            heading += PrettyPrinter.padLeft(field.getName(), columnToMaxStrLengthMap.get(field));
            heading += columnSeparator;

            rowSeparationLine += String.join("", Collections.nCopies(columnToMaxStrLengthMap.get(field), "-"));
            rowSeparationLine += columnRowIntersection;

        }
        heading += "\n";
        rowSeparationLine += "\n";

        headingAndRowSeparationLine[0] = heading;
        headingAndRowSeparationLine[1] = rowSeparationLine;
        return  headingAndRowSeparationLine;
    }

    private static String buildLine(Object instanceID,
                                    Map<Field, ColumnFieldHandler> fieldToFieldHandlerMap,
                                    Map<Object, Integer> instanceToListIndexMap,
                                    int longestStringLengthFirstColumn,
                                    Map<Field, Integer> columnToMaxStrLengthMap
                                    ){
        String line = columnSeparator + PrettyPrinter.padLeft( instanceID.toString(), longestStringLengthFirstColumn);
        line += columnSeparator;

        for(Field field: fieldToFieldHandlerMap.keySet()){
            ColumnFieldHandler fieldHandler = fieldToFieldHandlerMap.get(field);
            int instanceIndex = instanceToListIndexMap.get(instanceID);
            String multiSetOfInstanceStr = fieldHandler.getMultiSetForInstance(instanceIndex).toString();

            line += PrettyPrinter.padLeft(multiSetOfInstanceStr, columnToMaxStrLengthMap.get(field));
            line += columnSeparator;
        }

        line += "\n";
        return line;
    }


    public static String toString(MultiSetTableHandler multiSetTableHandler){

        Field targetIDField = multiSetTableHandler.getTargetIDField();
        Map<Object, Integer> instanceToListIndexMap = multiSetTableHandler.getInstanceToListIndexMap();
        Map<Field, ColumnFieldHandler> fieldToFieldHandlerMap = multiSetTableHandler.getFieldToFieldHandlerMap();


        // get max String length for the first column:
        int longestStringLengthFirstColumn = getStringLengthFirstColumn(targetIDField, instanceToListIndexMap);


        // get max String length for each field
        Map<Field, Integer> columnToMaxStrLengthMap = getStringLengthForEachFieldColumn(fieldToFieldHandlerMap);


        String[] headingAndRowSeparationLine = getHeadingAndRowSeparationLine(
                targetIDField, longestStringLengthFirstColumn,
                fieldToFieldHandlerMap, columnToMaxStrLengthMap);

        String heading = headingAndRowSeparationLine[0];
        String rowSeparationLine = headingAndRowSeparationLine[1];


        String str = "\nEXTRACTED MULTISET TABLE:\n"
        + rowSeparationLine
        + heading
        + rowSeparationLine;

        int countExamples = 0;
        for(Object instanceID: instanceToListIndexMap.keySet()){
            String line = buildLine(instanceID, fieldToFieldHandlerMap,
                    instanceToListIndexMap, longestStringLengthFirstColumn, columnToMaxStrLengthMap);
            str += line;

            countExamples++;
            if(SHORTEN && countExamples == MAX_NB_OF_EXAMPLES_TO_PRINT_WHEN_SHORTEN_IS_TRUE){
                str += "... (" + (instanceToListIndexMap.size() - countExamples) + " more rows)\n";
                break;
            }
        }
        str += rowSeparationLine;

        return str;
    }

}
