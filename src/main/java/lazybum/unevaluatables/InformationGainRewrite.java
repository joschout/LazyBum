package lazybum.unevaluatables;

import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by joschout.
 */
public class InformationGainRewrite {

    public static ImmutablePair<Double,Boolean> score(Map<Object, AtomicInteger> leftSubsetAbsoluteLabelFrequencies,
                                      Map<Object, AtomicInteger> rightSubsetAbsoluteLabelFrequencies,
                                      int nbOfInstancesInLeftSubset, int totalNbOfInstancesWithFeatureValue,
                                      @Nullable Map<Object, AtomicInteger> examplesWithoutFeatureValueLabelDistribution,
                                      int nbOfExamplesWithoutFeatureValue
                        ){



        int nbOfInstancesInRightSubset = totalNbOfInstancesWithFeatureValue - nbOfInstancesInLeftSubset;

        if(examplesWithoutFeatureValueLabelDistribution == null){

            double ig = informationGain(
                    leftSubsetAbsoluteLabelFrequencies, rightSubsetAbsoluteLabelFrequencies,
                    nbOfInstancesInLeftSubset, nbOfInstancesInRightSubset
            );
            return new ImmutablePair<>(ig, false);
        } else{

            double scoreLeftWithUnevaluatables = informationGainWithUnevaluatables(
                    leftSubsetAbsoluteLabelFrequencies, rightSubsetAbsoluteLabelFrequencies,
                    nbOfInstancesInLeftSubset, nbOfInstancesInRightSubset,
                    examplesWithoutFeatureValueLabelDistribution, nbOfExamplesWithoutFeatureValue
            );
            double scoreRightWithUnevaluatables = informationGainWithUnevaluatables(
                    rightSubsetAbsoluteLabelFrequencies, leftSubsetAbsoluteLabelFrequencies,
                    nbOfInstancesInRightSubset, nbOfInstancesInLeftSubset,
                    examplesWithoutFeatureValueLabelDistribution, nbOfExamplesWithoutFeatureValue
            );

            boolean shouldUnevaluatableTestCorrespondToSuccess = (scoreLeftWithUnevaluatables > scoreRightWithUnevaluatables);
            if(shouldUnevaluatableTestCorrespondToSuccess){
                return new ImmutablePair<>(scoreLeftWithUnevaluatables, shouldUnevaluatableTestCorrespondToSuccess);
            } else{
                return new ImmutablePair<>(scoreRightWithUnevaluatables, shouldUnevaluatableTestCorrespondToSuccess);
            }
        }

    }



    public static double informationGainWithUnevaluatables(Map<Object, AtomicInteger> subsetAbsoluteLabelFrequenciesToBeExtended,
                                         Map<Object, AtomicInteger> otherSubsetAbsoluteLabelFrequencies,
                                         int nbOfInstancesInSubsetToBeExtended, int nbOfInstancesInOtherSubset,
                                         Map<Object, AtomicInteger> examplesWithoutFeatureValueLabelDistribution,
                                         int nbOfInstancesWithoutFeatureValue){

        double ig = 0.0;


        int nbOfInstancesInSetToBeExtendedTogetherWithUnevaluatables = nbOfInstancesInSubsetToBeExtended + nbOfInstancesWithoutFeatureValue;
        int totalNbOfInstances = nbOfInstancesInSetToBeExtendedTogetherWithUnevaluatables + nbOfInstancesWithoutFeatureValue;


        double entropyExtendedSubsetWithUnevalutabales = entropyWithUnevaluatables(
                subsetAbsoluteLabelFrequenciesToBeExtended, nbOfInstancesInSubsetToBeExtended,
                examplesWithoutFeatureValueLabelDistribution, nbOfInstancesWithoutFeatureValue);
        double entropyOther = entropy(otherSubsetAbsoluteLabelFrequencies, nbOfInstancesInOtherSubset);

        ig -= ((double)nbOfInstancesInSetToBeExtendedTogetherWithUnevaluatables ) / totalNbOfInstances * entropyExtendedSubsetWithUnevalutabales;
        ig -= ((double)nbOfInstancesInOtherSubset) / totalNbOfInstances * entropyOther;

        return ig;
    }
//
//    public static double informationGainLeftWithUnevaluatables(Map<Object, AtomicInteger> leftSubsetAbsoluteLabelFrequencies,
//                                         Map<Object, AtomicInteger> rightSubsetAbsoluteLabelFrequencies,
//                                         int nbOfInstancesInLeftSubset, int totalNbOfInstancesWithFeatureValue,
//                                         Map<Object, AtomicInteger> examplesWithoutFeatureValueLabelDistribution,
//                                         int nbOfExamplesWithoutFeatureValue){
//
//        double igLeftWithUnevaluatables = 0.0;
//
//
//        int nbOfInstancesLefWithUnevaluatables = nbOfInstancesInLeftSubset + nbOfExamplesWithoutFeatureValue;
//        int nbOfInstancesInRightSubset = totalNbOfInstancesWithFeatureValue - nbOfInstancesInLeftSubset;
//        int totalNbOfInstances = totalNbOfInstancesWithFeatureValue + nbOfExamplesWithoutFeatureValue;
//
//
//        double entropyLeftWithUnevalutabales = entropyWithUnevaluatables(
//                leftSubsetAbsoluteLabelFrequencies, nbOfInstancesInLeftSubset,
//                examplesWithoutFeatureValueLabelDistribution, nbOfExamplesWithoutFeatureValue);
//        double entropyRight = entropy(rightSubsetAbsoluteLabelFrequencies, nbOfInstancesInRightSubset);
//
//        igLeftWithUnevaluatables -= ((double)nbOfInstancesLefWithUnevaluatables ) / totalNbOfInstances * entropyLeftWithUnevalutabales;
//        igLeftWithUnevaluatables -= ((double)nbOfInstancesInRightSubset) / totalNbOfInstances * entropyRight;
//
//        return igLeftWithUnevaluatables;
//    }




    public static double informationGain(Map<Object, AtomicInteger> leftSubsetAbsoluteLabelFrequencies,
                                  Map<Object, AtomicInteger> rightSubsetAbsoluteLabelFrequencies,
                                  int nbOfInstancesInLeftSubset, int nbOfInstancesInRightSubset){



        double ig = 0.0;

        int totalNbOfInstances = nbOfInstancesInLeftSubset + nbOfInstancesInRightSubset;

        double entropyLeft = entropy(leftSubsetAbsoluteLabelFrequencies, nbOfInstancesInLeftSubset);
        double entropyRight = entropy(rightSubsetAbsoluteLabelFrequencies, nbOfInstancesInRightSubset);

        ig -= ((double)nbOfInstancesInLeftSubset) / totalNbOfInstances * entropyLeft;
        ig -= ((double)nbOfInstancesInRightSubset) / totalNbOfInstances * entropyRight;

        return ig;
    }

    public static double entropy(Map<Object, AtomicInteger> absoluteLabelFrequencies, int nbOfInstances){
        if(nbOfInstances == 0){
            return 0.0;
        }
        double entr = 0.0;
        for (AtomicInteger aiAbsoluteLabelFrequency : absoluteLabelFrequencies.values()) {
            int absoluteClassLabelFrequency = aiAbsoluteLabelFrequency.get();
            double relativeClassLabelFrequency = ((double)absoluteClassLabelFrequency) / nbOfInstances;
            entr += relativeClassLabelFrequency *  Math.log(relativeClassLabelFrequency) / Math.log(2);
        }
        return entr;
    }

    public static double entropyWithUnevaluatables(
            Map<Object, AtomicInteger> absoluteLabelFrequenciesInstancesWithFeatureValues, int nbOfInstancesWithFeatureValues,
            Map<Object, AtomicInteger> absoluteLabelFrequenciesUnevaluatables, int nbOfUnevaluatables){
        int nbOfInstances = nbOfInstancesWithFeatureValues + nbOfUnevaluatables;

        double entr = 0.0;
        for (Object classLabel : absoluteLabelFrequenciesInstancesWithFeatureValues.keySet()) {
            int absoluteClassLabelFrequency =
                    absoluteLabelFrequenciesInstancesWithFeatureValues.get(classLabel).get()
                    + absoluteLabelFrequenciesUnevaluatables.get(classLabel).get();

            double relativeClassLabelFrequency = ((double)absoluteClassLabelFrequency) / nbOfInstances;
            entr += relativeClassLabelFrequency *  Math.log(relativeClassLabelFrequency) / Math.log(2);
        }
        return entr;
    }



}
