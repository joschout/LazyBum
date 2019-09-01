package dataset;

import java.util.*;

public class TwoPartDataSetSplitter extends DataSetSplitter {


    private Random random;
    private double splitFraction = 0.75;

    private List leftInstances;
    private List rightInstances;

    public TwoPartDataSetSplitter(){
        random = new Random();
    }

    public TwoPartDataSetSplitter(List instanceIDList) throws InvalidDataSplitException {
        this();
        split(instanceIDList);
    }


    public List[] split(List instanceIDList) throws InvalidDataSplitException {

        int nbOfInstances = instanceIDList.size();
        if(nbOfInstances < 2){
            throw new InvalidDataSplitException("Cannot split " + nbOfInstances + " into 2 sets");
        }

        // get an array representing the indexes on instanceIDList
        Integer[] indexes = getIncreasingIndexes(nbOfInstances);

        // note: this list is a view on the array
        List<Integer> indexesAslist = Arrays.asList(indexes);

        Collections.shuffle(indexesAslist, random);

        // use the first splitFraction indexes for the left list
        int splitBoundary = (int) (splitFraction * nbOfInstances);

        // note: leftList and rightList are views of the larger list and become invalid if the total list is updated
        List<Integer> leftList = indexesAslist.subList(0, splitBoundary);
        List<Integer> rightList = indexesAslist.subList(splitBoundary, nbOfInstances);
        System.out.println(leftList);
        System.out.println(rightList);

        List leftInstances = new ArrayList<>();
        List rightInstances = new ArrayList<>();
        for(int leftIndex: leftList){
            leftInstances.add(instanceIDList.get(leftIndex));
        }
        for(int rightIndex: rightList){
            rightInstances.add(instanceIDList.get(rightIndex));
        }

        List[] leftAndRight = new List[2];
        leftAndRight[0] = leftInstances;
        leftAndRight[1] = rightInstances;

        this.leftInstances = leftInstances;
        this.rightInstances = rightInstances;
        System.out.println(leftInstances);
        System.out.println(rightInstances);
        return leftAndRight;
    }

    public List getLeftInstances() {
        return leftInstances;
    }

    public List getRightInstances() {
        return rightInstances;
    }

    public static void main(String[] args) throws InvalidDataSplitException {

        String[] instances = new String[5];
        instances[0] = "ex0";
        instances[1] = "ex1";
        instances[2] = "ex2";
        instances[3] = "ex3";
        instances[4] = "ex4";

        TwoPartDataSetSplitter twoPartDataSetSplitter = new TwoPartDataSetSplitter();
        List[] leftAndRight = twoPartDataSetSplitter.split(Arrays.asList((Object[]) instances));
        System.out.println(leftAndRight);
    }



}
