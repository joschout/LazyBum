package learning.split;

import java.util.*;
import java.util.stream.Collectors;

public class NumericalSplitFinder {

    public static void main(String[] args) {

        int minFeatureValue = -2;
        int maxFeatureValue = 2;

        Random random = new Random();

        int nb0fInt = 5;

        List<Number> unsortedFeatureValues = new ArrayList<>();

        for (int i = 0; i < nb0fInt; i++) {
            int nextRandomNumber = random.nextInt(maxFeatureValue +1 - minFeatureValue) + minFeatureValue;
            unsortedFeatureValues.add(nextRandomNumber);
        }

        System.out.println(unsortedFeatureValues);

        List<Double> boundaryValues = getBoundariesToTest(unsortedFeatureValues);
        System.out.println(boundaryValues);


    }



    public static List<Double> getBoundariesToTest(List<Number> featureValues){

//        if(featureValues.contains(null)){
//            System.out.println("NULL INPUT SHOULD NOT HAPPEN");
//        }
        Set<Number> featureValuesWithoutNull = featureValues.stream().filter(Objects::nonNull).collect(Collectors.toSet());


        Set<Number> sortedFeatureValueSet = new TreeSet<>(featureValuesWithoutNull);
        List<Double> boundaryValues = new ArrayList<>();

        Number previousValue = null;
        for (Number featureValue : sortedFeatureValueSet) {
            if(previousValue == null) { // if this the first feature value, do nothing
                previousValue = featureValue;
            } else {
                double middle = (previousValue.doubleValue() + featureValue.doubleValue()) / 2.0;
                boundaryValues.add(middle);
                previousValue = featureValue;
            }
        }
        return boundaryValues;
    }








}
