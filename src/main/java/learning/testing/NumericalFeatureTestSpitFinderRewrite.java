package learning.testing;

import dataset.Example;
import feature.featuretable.FeatureColumn;
import feature.tranform.FieldTransformerEnumInterface;
import graph.TraversalPath;
import lazybum.unevaluatables.InformationGainRewrite;
import learning.split.NumericComparisonEnum;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jooq.Field;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by joschout.
 */
public class NumericalFeatureTestSpitFinderRewrite extends TestGenerator{


    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public NodeTest next() {
        try {
            if(this.nextTest != null){
                hasNext = false;
                return nextTest;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    class BestTestInfo{
        public final boolean shouldUnevaluatablesGoLeft;
        public final int indexOfBestFeatureValueToSplitOn;
        public final double bestScore;

        public BestTestInfo(int indexOfBestFeatureValueToSplitOn, double bestScore, boolean shouldUnevaluatablesGoLeft ) {
            this.shouldUnevaluatablesGoLeft = shouldUnevaluatablesGoLeft;
            this.indexOfBestFeatureValueToSplitOn = indexOfBestFeatureValueToSplitOn;
            this.bestScore = bestScore;
        }
    }

//    private Set<Object> possibleLabels;
    private List<Example> instances;
    private FeatureColumn<Number> featureColumn;
    private Map<Object, Integer> instanceToListIndexMap;
    private boolean hasNext = true;

    private NodeTest parentTest;
    private Field field;
    @Nullable
    private TraversalPath traversalPath;
    private FieldTransformerEnumInterface fieldTransformerEnum;



    private NodeTest nextTest;

    public NumericalFeatureTestSpitFinderRewrite(

            NodeTest parentTest,
            @Nullable TraversalPath traversalPath, FeatureColumn<Number> numericFeatureColumn,
            List<Example> instances, Map<Object, Integer> instanceToListIndexMap) {
        this.instances = instances;

        this.instanceToListIndexMap = instanceToListIndexMap;

        this.featureColumn = numericFeatureColumn;

        this.parentTest = parentTest;
        this.field = numericFeatureColumn.getOriginalField();
        this.traversalPath = traversalPath;

        Optional optionalTransform = numericFeatureColumn.getTransformation();
        if(optionalTransform.isPresent()){
            this.fieldTransformerEnum = numericFeatureColumn.getTransformation().get();
            if(VERBOSE){
                System.out.println("NumericalFeatureTestGenerator for field " + field.getName() + " using transform " + fieldTransformerEnum);
            }

        } else{
            this.fieldTransformerEnum = null;
            if(VERBOSE){
                System.out.println("NumericalFeatureTestGenerator for field " + field.getName() + " without transformation");
            }
        }


        try {
            nextTest = findBestTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(nextTest == null){
            hasNext = false;
        } else{
            hasNext = true;
        }



    }

    private NodeTest findBestTest() throws Exception {




        List<ImmutablePair<Example, Number>> evaluatableExampleAndFeatureValuePairList = new ArrayList<>();
        List<Example> examplesWithoutAFeatureValue = new ArrayList<>();

        Set<Object> possibleLabels = new HashSet<>();

        for(Example instance: instances){
            possibleLabels.add(instance.getLabel());

            if (instanceToListIndexMap.containsKey(instance.instanceID)) {
                int instanceIndex = instanceToListIndexMap.get(instance.instanceID);
                Object featureValue = featureColumn.getFeatureValueForInstance(instanceIndex);
                if(featureValue!= null) {
                    evaluatableExampleAndFeatureValuePairList.add(new ImmutablePair<>(instance, (Number)featureValue));
                }else{ // feature value is null
                    examplesWithoutAFeatureValue.add(instance);
                }
            } else{ // no feature value
                examplesWithoutAFeatureValue.add(instance);
            }
        }

        if(evaluatableExampleAndFeatureValuePairList.size() == 0){
            return null;
        }


        evaluatableExampleAndFeatureValuePairList.sort((left, right) -> {
            Double featureValueLeft = left.getRight().doubleValue();
            Double featureValueRight = right.getRight().doubleValue();
            return featureValueLeft.compareTo(featureValueRight);
        });




        Optional<BestTestInfo> infoAboutBestTestOptional = findBestNumericalSplit2(evaluatableExampleAndFeatureValuePairList, examplesWithoutAFeatureValue, possibleLabels);

        if(infoAboutBestTestOptional.isEmpty()){
            throw new Exception("THIS SHOULD NOT OCCUR");
        }

        BestTestInfo infoAboutBestTest = infoAboutBestTestOptional.get();




        int i = infoAboutBestTest.indexOfBestFeatureValueToSplitOn;
        double boundaryValue;
        if(evaluatableExampleAndFeatureValuePairList.size() > 1){
            try{
            boundaryValue= (
                evaluatableExampleAndFeatureValuePairList.get(i).getRight().doubleValue()
                        + evaluatableExampleAndFeatureValuePairList.get(i+1).getRight().doubleValue()
            )/2.0;}
            catch (IndexOutOfBoundsException e){
                System.out.println("evaluatables size; " + evaluatableExampleAndFeatureValuePairList.size() + " , index: " + i);
                System.out.println("instances size; " + instances.size() + " , index: " + i);
                throw e;
            }
        }else{
            try{
            boundaryValue = evaluatableExampleAndFeatureValuePairList.get(i).getRight().doubleValue();
            }
            catch (IndexOutOfBoundsException e){
                System.out.println("evaluatables size; " + evaluatableExampleAndFeatureValuePairList.size() + " , index: " + i);
                System.out.println("instances size; " + instances.size() + " , index: " + i);
                throw e;
            }
        }

        NumericalComparison numericalComparison = new NumericalComparison(boundaryValue, NumericComparisonEnum.LESS_THAN);

        NumericalFeatureTest numericalFeatureTest = new NumericalFeatureTest(parentTest, numericalComparison, traversalPath, field, fieldTransformerEnum);

        hasNext = false;

        return numericalFeatureTest;


    }

    /**
     *
     *
     *     orderedFeatureValues = { v1, v2, ..., vm } where vi <= vm
     *
     *     Use as tests the ordered feature values themselves (except for the last one):
     *          <= v1   --> {v1} , {v2, ..., vm}
     *          <= v2   --> {v1, v2} , {v3, ..., vm}
     *              ...
     *          <= vm-1  --> {v1, v2, ..., vm-1} , {vm}
     *
     *     Update the label frequencies as follows:
     *       initially:
     *          - left : all labels have absolute frequency 0
     *          - right : all labels have absolute frequency corresponding to the total set
     *       <= v1:
     *          - left: add label of v1 +1
     *          - right: subtract label of v1 -1
     *          - calculate entropy
     *          - if larger than previously, save this as the currently best test
     *
     * @param evaluatableExampleAndFeatureValuePairList
     * @param examplesWithoutAFeatureValue
     * @return
     * @throws Exception
     */
    public Optional<BestTestInfo> findBestNumericalSplit2(
            List<ImmutablePair<Example, Number>> evaluatableExampleAndFeatureValuePairList,
            List<Example> examplesWithoutAFeatureValue, Set<Object> possibleLabels) {

        int indexOfBestFeatureValueToSplitOn = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        boolean shouldUnevaluatablesGoLeft = false;

        Map<Object, AtomicInteger> leftSubsetLabelDistribution = possibleLabels.stream().collect(Collectors.toMap(
                Function.identity(), o -> new AtomicInteger()
        ));

        Map<Object, AtomicInteger> rightSubsetLabelDistribution = possibleLabels.stream().collect(Collectors.toMap(
                Function.identity(), o -> new AtomicInteger()));
        evaluatableExampleAndFeatureValuePairList.forEach(
                exampleDoubleImmutablePair ->
                        rightSubsetLabelDistribution.get(
                                exampleDoubleImmutablePair.getLeft().getClass())
        );

        Map<Object, AtomicInteger> examplesWithoutFeatureValueLabelDistribution = null;
        if(! examplesWithoutAFeatureValue.isEmpty()){
            Map<Object, AtomicInteger> examplesWithoutFeatureValueLabelDistribution2 = possibleLabels.stream().collect(
                    Collectors.toMap(
                            Function.identity(),
                            o -> new AtomicInteger()));
            examplesWithoutAFeatureValue.forEach(
                    example -> examplesWithoutFeatureValueLabelDistribution2.get(example.getLabel()).incrementAndGet()
            );
            examplesWithoutFeatureValueLabelDistribution = examplesWithoutFeatureValueLabelDistribution2;
        }




        int totalNbOfInstances = evaluatableExampleAndFeatureValuePairList.size();
        int nbOfTests = totalNbOfInstances - 1;

        int nbOfInstancesInLeftSubset = 1;

        for (int i = 0; i < nbOfTests; i++) {
            nbOfInstancesInLeftSubset ++;

            Object classLabel = evaluatableExampleAndFeatureValuePairList.get(i).getLeft().getLabel();
            leftSubsetLabelDistribution.get(classLabel).incrementAndGet();
            rightSubsetLabelDistribution.get(classLabel).decrementAndGet();
            ImmutablePair<Double,Boolean> igAndShouldUnevaluatablesGoLeft = InformationGainRewrite.score(
                    leftSubsetLabelDistribution, rightSubsetLabelDistribution,
                    nbOfInstancesInLeftSubset, totalNbOfInstances,
                    examplesWithoutFeatureValueLabelDistribution, examplesWithoutAFeatureValue.size());
//            double ig = informationGain(leftSubsetLabelDistribution, rightSubsetLabelDistribution, nbOfInstancesInLeftSubset, totalNbOfInstances);

            double ig = igAndShouldUnevaluatablesGoLeft.getLeft();
            if(ig > bestScore){
                indexOfBestFeatureValueToSplitOn = i;
                bestScore = ig;
                shouldUnevaluatablesGoLeft = igAndShouldUnevaluatablesGoLeft.getRight();
            }
        }

        if(indexOfBestFeatureValueToSplitOn == -1){
            // no good split found:
//            throw new Exception("Could not find a split. This should never occur. Even a bad split should be possible");
            return Optional.of(new BestTestInfo(0, 0.0, false));
        }
        return Optional.of(new BestTestInfo(indexOfBestFeatureValueToSplitOn, bestScore, shouldUnevaluatablesGoLeft));
    }



    public void findBestNumericalSplit(double[] orderedFeatureValues, Object[] classLabelsOrderedFeatureValues,  Set<Object> possibleLabels) throws Exception {
        if(orderedFeatureValues.length != classLabelsOrderedFeatureValues.length){
            throw new Exception("orderedFeatureValues and classLabelsOrderedFeatureValues should have the same length");
        }


        int indexOfBestFeatureValueToSplitOn = -1;
        double bestScore = Double.NEGATIVE_INFINITY;

        Map<Object, AtomicInteger> leftSubsetLabelDistribution = possibleLabels.stream().collect(Collectors.toMap(
                Function.identity(), o -> new AtomicInteger()
        ));


        Map<Object, AtomicInteger> rightSubsetLabelDistribution = possibleLabels.stream().collect(Collectors.toMap(
                Function.identity(), o -> new AtomicInteger()));
        for (Object classLabel : classLabelsOrderedFeatureValues) {
            rightSubsetLabelDistribution.get(classLabel).incrementAndGet();
        }


        /*
           orderedFeatureValues = { v1, v2, ..., vm } where vi <= vm

           Use as tests the ordered feature values themselves (except for the last one):
            * <= v1   --> {v1} , {v2, ..., vm}
            * <= v2   --> {v1, v2} , {v3, ..., vm}
            * ...
            * <= vm-1  --> {v1, v2, ..., vm-1} , {vm}

            Update the label frequencies as follows:
            * initially:
              - left : all labels have absolute frequency 0
              - right : all labels have absolute frequency corresponding to the total set
            * <= v1:
              - left: add label of v1 +1
              - right: subtract label of v1 -1
              - calculate entropy
              - if larger than previously, save this as the currently best test
         */

        int nbOfTests = orderedFeatureValues.length - 1;

        int nbOfInstancesInLeftSubset = 1;
        int totalNbOfInstances = orderedFeatureValues.length;
        for (int i = 0; i < nbOfTests; i++) {
            nbOfInstancesInLeftSubset ++;

            Object classLabel = classLabelsOrderedFeatureValues[i];
            leftSubsetLabelDistribution.get(classLabel).incrementAndGet();
            rightSubsetLabelDistribution.get(classLabel).decrementAndGet();
            double ig = informationGain(leftSubsetLabelDistribution, rightSubsetLabelDistribution, nbOfInstancesInLeftSubset, totalNbOfInstances);
            if(ig > bestScore){
                indexOfBestFeatureValueToSplitOn = i;
            }
        }

        if(indexOfBestFeatureValueToSplitOn == -1){
            // no good split found:
            throw new Exception("Could not find a split. This should never occur. Even a bad split should be possible");
        }

    }


    public double informationGain(Map<Object, AtomicInteger> leftSubsetAbsoluteLabelFrequencies,
                                  Map<Object, AtomicInteger> rightSubsetAbsoluteLabelFrequencies,
                                  int nbOfInstancesInLeftSubset, int totalNbOfInstances){

        double ig = 0.0;

        int nbOfInstancesInRightSubset = totalNbOfInstances - nbOfInstancesInLeftSubset;

        double entropyLeft = entropy(leftSubsetAbsoluteLabelFrequencies, nbOfInstancesInLeftSubset);
        double entropyRight = entropy(rightSubsetAbsoluteLabelFrequencies, nbOfInstancesInRightSubset);

        ig -= ((double)nbOfInstancesInLeftSubset) / totalNbOfInstances * entropyLeft;
        ig -= ((double)nbOfInstancesInRightSubset) / totalNbOfInstances * entropyRight;

        return ig;
    }

    public double entropy(Map<Object, AtomicInteger> absoluteLabelFrequencies, int nbOfInstances){
        double entr = 0.0;
        for (AtomicInteger aiAbsoluteLabelFrequency : absoluteLabelFrequencies.values()) {
            int absoluteClassLabelFrequency = aiAbsoluteLabelFrequency.get();
            double relativeClassLabelFrequency = ((double)absoluteClassLabelFrequency) / nbOfInstances;
            entr += relativeClassLabelFrequency *  Math.log(relativeClassLabelFrequency) / Math.log(2);
        }
        return entr;
    }


}
