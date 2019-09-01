package utils;

import java.util.Collection;
import java.util.List;

public class PrettyPrinter {
    /**
     * Pad string s with spaces for a total string length of n
     *
     * NOTE: possible error if length of string s > n ?
     * @param s
     * @param n
     * @return
     */
    public static String padLeft(String s, int n) {
        if(n > 0){
            return String.format("%-" + n + "s", s);
        }
        else{
            return s;
        }
    }

    public static String doubleArrayToCSVString(double[] array){

        String str = "";
        int count = 0;
        for (Object o : array) {
            str += String.valueOf(o);
            count ++;
            if(count != array.length){
                str += ", ";
            }
        }
        return str;
    }
    public static String arrayToCSVString(Object[] array){

        String str = "";
        int count = 0;
        for (Object o : array) {
            str += String.valueOf(o);
            count ++;
            if(count != array.length){
                str += ", ";
            }
        }
        return str;
    }

    public static String collectionToCSVString(Collection collection){

        String str = "";
        int count = 0;
        for (Object o : collection) {
            str += String.valueOf(o);
            count ++;
            if(count != collection.size()){
                str += ", ";
            }
        }
        return str;
    }



    public static String listToCSVString(List list){

        String str = "";
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            if(obj == null){
                str += "null";
            } else{
                str += obj.toString();
            }
            if(i != list.size() - 1){
                str +=", ";
            }
        }
        return str;
    }


//    public static String formatAsTable(List<List<String>> rows)
//    {
//        int[] maxLengths = new int[rows.get(0).size()];
//        for (List<String> row : rows)
//        {
//            for (int i = 0; i < row.size(); i++)
//            {
//                maxLengths[i] = Math.max(maxLengths[i], row.get(i).length());
//            }
//        }
//
//        StringBuilder formatBuilder = new StringBuilder();
//        for (int maxLength : maxLengths)
//        {
//            formatBuilder.append("%-").append(maxLength + 2).append("s");
//        }
//        String format = formatBuilder.toString();
//
//        StringBuilder result = new StringBuilder();
//        for (List<String> row : rows)
//        {
//            result.append(String.format(format, row.toArray(new String[0]))).append("\n");
//        }
//        return result.toString();
//    }


}
