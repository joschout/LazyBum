package feature.featuretable;

import org.jooq.Field;
import utils.PrettyPrinter;

import java.util.*;

@SuppressWarnings("Duplicates")
public class FeatureTableStringBuilder {

    private static final String columnSeparator = " | ";
    private static final String columnRowIntersection = "-+-";


    public static boolean SHORTEN = true;
    public static int MAX_NB_OF_EXAMPLES_TO_PRINT_WHEN_SHORTEN_IS_TRUE = 10;


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


    private static Map<FeatureColumn, Integer> getStringLengthForEachFieldColumn(List<FeatureColumn> featureColumnList){
        Map<FeatureColumn, Integer> columnToMaxStrLengthMap = new HashMap<>();


        for(FeatureColumn featureColumn: featureColumnList){

            Field field = featureColumn.getOriginalField();
            Optional optionalTransform = featureColumn.getTransformation();



            int longestStringLenghtForFieldColumn = Math.max(
                    field.getName().length(), featureColumn.maxStringLength());
            if(optionalTransform.isPresent()){
                longestStringLenghtForFieldColumn = Math.max(
                        longestStringLenghtForFieldColumn, optionalTransform.get().toString().length());
            }

            columnToMaxStrLengthMap.put(featureColumn, longestStringLenghtForFieldColumn);
        }
        return columnToMaxStrLengthMap;
    }


    private static String[] getHeadingAndRowSeparationLine(
            Field targetIDField, int longestStringLengthFirstColumn,
            List<FeatureColumn> featureColumnList,
            Map<FeatureColumn, Integer> columnToMaxStrLengthMap){

        String[] headingAndRowSeparationLine = new String[2];

        String heading = ""
                + columnSeparator
                + PrettyPrinter.padLeft(targetIDField.getName(), longestStringLengthFirstColumn)
                + columnSeparator;

        String rowSeparationLine = ""
                + columnRowIntersection
                + String.join("", Collections.nCopies(longestStringLengthFirstColumn, "-"))
                + columnRowIntersection;

        for(FeatureColumn featureColumn: featureColumnList){
            heading += PrettyPrinter.padLeft(featureColumn.getOriginalField().getName(), columnToMaxStrLengthMap.get(featureColumn));
            heading += columnSeparator;

            rowSeparationLine += String.join("", Collections.nCopies(columnToMaxStrLengthMap.get(featureColumn), "-"));
            rowSeparationLine += columnRowIntersection;

        }
        heading += "\n";
        rowSeparationLine += "\n";

        headingAndRowSeparationLine[0] = heading;
        headingAndRowSeparationLine[1] = rowSeparationLine;
        return  headingAndRowSeparationLine;
    }

    private static String getHeadingTransformationLine(
            int longestStringLengthFirstColumn, List<FeatureColumn> featureColumnList, Map<FeatureColumn, Integer> columnToMaxStrLengthMap) {
        String headingTransformationLine = ""
                + columnSeparator
                + PrettyPrinter.padLeft("", longestStringLengthFirstColumn)
                + columnSeparator;

        for(FeatureColumn featureColumn: featureColumnList){
            String optionalTransformString = "";
            Optional optionalTransform = featureColumn.getTransformation();
            if(optionalTransform.isPresent()){
                optionalTransformString = optionalTransform.get().toString();
            }
            headingTransformationLine += PrettyPrinter.padLeft(optionalTransformString, columnToMaxStrLengthMap.get(featureColumn));
            headingTransformationLine += columnSeparator;
        }

        headingTransformationLine += "\n";

        return  headingTransformationLine;
    }


    private static String buildLine(Object instanceID,
                                    List<FeatureColumn> featureColumnList,
                                    Map<Object, Integer> instanceToListIndexMap,
                                    int longestStringLengthFirstColumn,
                                    Map<FeatureColumn, Integer> columnToMaxStrLengthMap
    ){
        String line = columnSeparator + PrettyPrinter.padLeft( instanceID.toString(), longestStringLengthFirstColumn);
        line += columnSeparator;

        for(FeatureColumn featureColumn: featureColumnList){
            int instanceIndex = instanceToListIndexMap.get(instanceID);


            String featureValue = String.valueOf(featureColumn.getFeatureValueForInstance(instanceIndex));

            line += PrettyPrinter.padLeft(featureValue, columnToMaxStrLengthMap.get(featureColumn));
            line += columnSeparator;
        }

        line += "\n";
        return line;
    }


    public static String toString(FeatureTableHandler featureTableHandler){
        return toString(featureTableHandler, "");
    }

    public static String toString(FeatureTableHandler featureTableHandler, String indentation){
        Field targetIDField = featureTableHandler.getTargetIDField();
        Map<Object, Integer> instanceToListIndexMap = featureTableHandler.getInstanceToListIndexMap();
        List<FeatureColumn> featureColumnList = featureTableHandler.getFeatureColumnList();


        // get longest String in the first column:
        int longestStringLengthFirstColumn = getStringLengthFirstColumn(targetIDField, instanceToListIndexMap);

        // get max String length for each field
        Map<FeatureColumn, Integer> columnToMaxStrLengthMap = getStringLengthForEachFieldColumn(featureColumnList);

        String[] headingAndRowSeparationLine = getHeadingAndRowSeparationLine(
                targetIDField, longestStringLengthFirstColumn, featureColumnList, columnToMaxStrLengthMap);

        String heading = headingAndRowSeparationLine[0];
        String rowSeparationLine = headingAndRowSeparationLine[1];


        String headingTransformationLine = getHeadingTransformationLine(longestStringLengthFirstColumn, featureColumnList, columnToMaxStrLengthMap);

        String str = "\n" + indentation + "PARTIAL FEATURE TABLE:\n"
                + indentation + rowSeparationLine
                + indentation + heading
                + indentation + headingTransformationLine
                + indentation + rowSeparationLine;

        int countExamples = 0;
        for(Object instanceID: instanceToListIndexMap.keySet()){
            String line = buildLine(instanceID, featureColumnList, instanceToListIndexMap, longestStringLengthFirstColumn, columnToMaxStrLengthMap);
            str += indentation + line;

            countExamples++;
            if(SHORTEN && countExamples == MAX_NB_OF_EXAMPLES_TO_PRINT_WHEN_SHORTEN_IS_TRUE){
                str += indentation + "... (" + (instanceToListIndexMap.size() - countExamples) + " more rows)\n";
                break;
            }
        }
        str += indentation + rowSeparationLine;

        return str;
    }


}
