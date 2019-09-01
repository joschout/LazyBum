package dataset;

import utils.PrettyPrinter;

import java.util.*;

public class FoldDataSetSplitter<T> extends DataSetSplitter<T>{


    private Random random;
    public static int DEFAULT_NB_OF_FOLDS = 10;
    private int nbOfFolds;
    private int foldSize;
    private List<T>[] folds;


    public FoldDataSetSplitter(int nbOfFolds, List<T> instanceList) throws InvalidDataSplitException {
        this.nbOfFolds = nbOfFolds;
        this.random = new Random();
        this.foldSize = instanceList.size() / nbOfFolds;
        split(instanceList);
    }


    private FoldDataSetSplitter(){
        this.random = new Random();
    }
//    public FoldDataSetSplitter(int nbOfFolds){
//        this.nbOfFolds = nbOfFolds;
//        this.random = new Random();
//    }


//    public FoldDataSetSplitter(){
//        this(DEFAULT_NB_OF_FOLDS);
//    }

    public List getValidationSet(int foldNb){
        return this.folds[foldNb];
    }


    public List getTrainingSet(int foldNb){
        List trainingExampleSet = new ArrayList<>();
        for (int j = 0; j < folds.length; j++) {
            if(foldNb != j){
                trainingExampleSet.addAll(folds[j]);
            }
        }
        return trainingExampleSet;
    }

    @Override
    public List<T>[] split(List<T> instanceList) throws InvalidDataSplitException {

        int nbOfInstances = instanceList.size();
        if(nbOfInstances < nbOfFolds){
            throw new InvalidDataSplitException("Cannot split " + nbOfInstances + " into " + nbOfFolds + " folds");
        }

        // get an array representing the indexes on instanceIDList
        Integer[] indexes = getIncreasingIndexes(nbOfInstances);

        // note: this list is a view on the array
        List<Integer> indexesAslist = Arrays.asList(indexes);

        // shuffle the indexes randomly
        Collections.shuffle(indexesAslist, random);

        List[] indexesPerFold = new List[nbOfFolds];

        // https://stackoverflow.com/questions/32305683/10-fold-cross-validation-with-sample-size-that-is-not-a-factor-of-10
        // In general, if you have n samples and n_folds folds, you want to do what scikit-learn does:
        //  The first n % n_folds folds have size n // n_folds + 1, other folds have size n // n_folds.

        int nbOfFoldsWithOneMore = nbOfInstances % nbOfFolds;
//        int foldSize = nbOfInstances / nbOfFolds;


        int startIndex = 0;
        for (int i = 0; i < nbOfFoldsWithOneMore ; i++) {

            indexesPerFold[i] = indexesAslist.subList(startIndex, startIndex + (foldSize + 1));
            startIndex = startIndex + (foldSize + 1);
//            if(i < nbOfFoldsWithOneMore){
//                indexesPerFold[i] = indexesAslist.subList(i, i + foldSize + 1);
//            } else{
//                indexesPerFold[i] = indexesAslist.subList(i, i + foldSize);
//            }
        }

        for(int i = nbOfFoldsWithOneMore; i < nbOfFolds; i++){
            indexesPerFold[i] = indexesAslist.subList(startIndex, startIndex +  foldSize);
            startIndex = startIndex + foldSize;
        }


        this.folds = new List[nbOfFolds];
        for (int i = 0; i < nbOfFolds ; i++) {
            List<T> instancesOfFold = new ArrayList<T>();
            this.folds[i] = instancesOfFold;
            for(Object indexOfFold: indexesPerFold[i]){
                T instance = instanceList.get((int) indexOfFold);
                instancesOfFold.add(instance);
            }
        }
        return this.folds;
    }


    public boolean sanityCheck(){
        for (int i = 0; i < folds.length; i++) {
            for (int j = 0; j < folds.length; j++) {
                if(i != j){
                    for (Object o : folds[i]) {
                        if(folds[j].contains(o)){
                            System.out.println("sanity check fails, object " + String.valueOf(o) + " occurs in 2 folds");
                            return false;
                        }
                    }
                }
            }
        }

        System.out.println("sanity check succeeds");
        return true;
    }

    public List[] getFolds() {
        return folds;
    }

    public int getFoldSize() {
        return foldSize;
    }

    public int getNbOfFolds() {
        return nbOfFolds;
    }


    public static FoldDataSetSplitter<Example> load(int nbOfFolds,int foldSize, List<Example>[] folds){

        FoldDataSetSplitter<Example> foldDataSetSplitter = new FoldDataSetSplitter();
        foldDataSetSplitter.foldSize = foldSize;
        foldDataSetSplitter.nbOfFolds = nbOfFolds;
        foldDataSetSplitter.folds = folds;
        return foldDataSetSplitter;

    }


    public static void main(String[] args) throws InvalidDataSplitException {
        String[] instances = new String[16];
        instances[0] = "ex0";
        instances[1] = "ex1";
        instances[2] = "ex2";
        instances[3] = "ex3";
        instances[4] = "ex4";

        instances[5] = "ex5";
        instances[6] = "ex6";
        instances[7] = "ex7";
        instances[8] = "ex8";
        instances[9] = "ex9";

        instances[10] = "ex10";
        instances[11] = "ex11";
        instances[12] = "ex12";
        instances[13] = "ex13";
        instances[14] = "ex14";

        instances[15] = "ex15";


        FoldDataSetSplitter foldDataSetSplitter = new FoldDataSetSplitter(10, Arrays.asList((Object[]) instances));

        foldDataSetSplitter.sanityCheck();

        List[] folds = foldDataSetSplitter.getFolds();

        for (int i = 0; i < folds.length; i++) {
            System.out.println("Fold " + i);
            System.out.println("nbOfElem: " + folds[i].size());
            System.out.println(PrettyPrinter.listToCSVString(folds[i]));
            System.out.println("-----");
        }
    }
}
