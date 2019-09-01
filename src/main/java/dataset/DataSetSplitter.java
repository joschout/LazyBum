package dataset;

import java.util.List;

public abstract class DataSetSplitter<T> {

    protected static Integer[] getIncreasingIndexes(int nbOfElements){
        Integer[] indexes = new Integer[nbOfElements];
        for (int i = 0; i < nbOfElements; i++) {
            indexes[i] = i;
        }
        return indexes;
    }


    public abstract List<T>[] split(List<T> instanceIDList) throws InvalidDataSplitException;

}
