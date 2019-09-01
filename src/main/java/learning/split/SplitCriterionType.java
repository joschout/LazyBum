package learning.split;

public enum SplitCriterionType {
    INFORMATION_GAIN;




//
//    public static SplitCriterionCalculator getSplitCriterionCalculator(List<Example> instances,
//                                                                       SplitCriterionCalculatorSettings splitCriterionInfo) throws InvalidSplitCriterionException {
//        if(splitCriterionInfo.getSplitCriterionType() == SplitCriterionType.INFORMATION_GAIN){
//
//            List<Object> exampleLabels = instances.stream().map(Example::getLabel).collect(Collectors.toList());
//
//            if(splitCriterionInfo instanceof InformationGainCalculatorSettings){
//                Set<Object> possibleLabels = ((InformationGainCalculatorSettings) splitCriterionInfo).possibleLabels;
//                return new InformationGainCalculator(possibleLabels, exampleLabels);
//            }
//            throw new InvalidSplitCriterionException("");
//        }
//
//        throw new InvalidSplitCriterionException("");
//    }
//
//    public static SplitCriterionCalculatorSettings getSplitCriterionInfo(SplitCriterionType splitCriterionType, List<Example> instances) throws InvalidSplitCriterionException {
//
//        if(splitCriterionType == SplitCriterionType.INFORMATION_GAIN){
//            Set<Object> possibleLabels = instances.stream().map(Example::getLabel).collect(Collectors.toSet());
//            return new InformationGainCalculatorSettings(possibleLabels);
//        }
//
//        throw new InvalidSplitCriterionException("");
//    }
//
//

}
