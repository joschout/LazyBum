package learning.split;

import java.io.Serializable;

public enum NumericComparisonEnum  implements Serializable {
    LESS_THAN;
//    EQUAL_TO,
//    GREATER_THAN;



    public static boolean compare(NumericComparisonEnum operator, double leftValue, double rightValue){
        switch (operator){
            case LESS_THAN:
                return leftValue < rightValue;
//            case EQUAL_TO:
//                return leftValue == rightValue;
//            case GREATER_THAN:
//                return  leftValue > rightValue;
        }
        return false;
    }

}
