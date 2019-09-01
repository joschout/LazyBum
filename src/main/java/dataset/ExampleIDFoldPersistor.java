package dataset;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExampleIDFoldPersistor {


    public static final String FOLD_PREFIX = "fold";
    public static final String FOLD_INFO_PREFIX = "foldinfo";

    public static final String NB_OF_FOLDS_MARKER = "nbOfFolds";
    public static final String FOLD_SIZE_MARKER = "foldSize";

    private boolean shouldWriteOverExistingFolds;


    public ExampleIDFoldPersistor() {
        this.shouldWriteOverExistingFolds = false;
    }

    public void setShouldWriteOverExistingFolds(boolean shouldWriteOverExistingFolds) {
        this.shouldWriteOverExistingFolds = shouldWriteOverExistingFolds;
    }

    public void persist(FoldDataSetSplitter<Example> foldDataSetSplitter, String directoryPathStr) throws IOException {

        if(! isAllowedToWriteOverExistingFoldFiles(foldDataSetSplitter.getNbOfFolds(), directoryPathStr)){
            throw new IOException("Not allowed to write over exiting fold files");
        }

        List<Example>[] folds = foldDataSetSplitter.getFolds();

        persistFoldDataSetSplitterInfo(foldDataSetSplitter, directoryPathStr);
        for (int i = 0; i < folds.length; i++) {
            List<Example> examplesOfCurrentFold = folds[i];
            persistSingleFold(examplesOfCurrentFold, directoryPathStr, i);
        }

    }

    private String getFoldFileName(String directoryPathStr, int foldNr){
        return directoryPathStr + File.separator + FOLD_PREFIX + foldNr + ".txt";
    }

    private String getFoldInfoFilename(String directoryPathStr){
        return directoryPathStr + File.separator + FOLD_INFO_PREFIX  + ".txt";
    }



    private boolean isAllowedToWriteOverExistingFoldFiles(int nbOfFolds,  String directoryPathStr){
        // check if general file exists
        String foldInfoFileName = getFoldInfoFilename(directoryPathStr);
        File foldInfoFile = new File(foldInfoFileName);
        if(foldInfoFile.exists()){
            return false;
        }

        // check if specific fold file exists
        for (int i = 0; i < nbOfFolds; i++) {
            String foldFileName = getFoldFileName(directoryPathStr, i);
            File foldFile = new File(foldFileName);
            if(foldFile.exists()){
                return false;
            }
        }

        return true;
    }

    private void persistFoldDataSetSplitterInfo(FoldDataSetSplitter<Example> foldDataSetSplitter, String directoryPathStr){
        int nbOfFolds = foldDataSetSplitter.getNbOfFolds();
        int foldSize = foldDataSetSplitter.getFoldSize();

        String foldInfoFileName = getFoldInfoFilename(directoryPathStr);

        try (BufferedWriter bufferedWriter= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(foldInfoFileName), "utf-8"))){

            bufferedWriter.write(NB_OF_FOLDS_MARKER +":" + nbOfFolds + "\n");
            bufferedWriter.write(FOLD_SIZE_MARKER + ":" + foldSize);
            System.out.println("saved nb of folds and fold size to " + foldInfoFileName);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    private ImmutablePair<Integer, Integer> loadNbOfFoldsAndFoldSize(String directoryPathStr) throws InvalidFoldPersistence {
        String foldInfoFileName = getFoldInfoFilename(directoryPathStr);

        int nbOfFolds = 0;
        int foldSize = 0;

        boolean readNbOfFolds = false;
        boolean readFoldSize = false;

        try (BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(new FileInputStream(foldInfoFileName), "utf-8"))) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null){

                if(line.startsWith(NB_OF_FOLDS_MARKER)){
                    String[] parts = line.split(":");
                    nbOfFolds = Integer.parseInt(parts[1]);
                    readNbOfFolds = true;
                }

                if(line.startsWith(FOLD_SIZE_MARKER)){
                    String[] parts = line.split(":");
                    foldSize = Integer.parseInt(parts[1]);
                    readFoldSize = true;
                }
            }

            if(! readNbOfFolds){
                throw new InvalidFoldPersistence("could not find a line starting with " + NB_OF_FOLDS_MARKER +" in file " + foldInfoFileName);
            }
            if(! readFoldSize){
                throw new InvalidFoldPersistence("could not find a line starting with " + FOLD_SIZE_MARKER +" in file " + foldInfoFileName);
            }

            System.out.println("loaded nb of folds and fold size from " + foldInfoFileName);
        } catch (IOException i) {
            i.printStackTrace();
        }

        return new ImmutablePair<>(nbOfFolds, foldSize);

    }

    private void persistSingleFold(List<Example> examples, String directoryPathStr, int foldNr){

        String foldFileName = getFoldFileName(directoryPathStr, foldNr);

        try (BufferedWriter bufferedWriter= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(foldFileName), "utf-8"))){

            for (int i = 0; i < examples.size(); i++) {
                Example example = examples.get(i);
                bufferedWriter.write("" + example.instanceID);
                if(i != examples.size() - 1){
                    bufferedWriter.write("\n");
                }
            }
            System.out.println("Saved example ids of fold " + foldNr + " to " + foldFileName);
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
    
    
    private List<Example> loadSingleFold(List<Example> allExamples, String directoryPathStr, int foldNr){
        String foldFileName = getFoldFileName(directoryPathStr, foldNr);

        Set<String> instanceIDStringsOfFold = new HashSet<>();

        try (BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(new FileInputStream(foldFileName), "utf-8"))){
            String line = null;
            while ((line = bufferedReader.readLine()) != null){
                instanceIDStringsOfFold.add(line);
            }
            System.out.println("Loaded example ids of fold " + foldNr + " from " + foldFileName);
        } catch (IOException i) {
            i.printStackTrace();
        }

        return allExamples.stream()
                .filter(example -> instanceIDStringsOfFold.contains(example.instanceID.toString()))
                .collect(Collectors.toList());
    }
    
    

    public FoldDataSetSplitter<Example> loadFoldDataSetSplitter(String directoryPathStr, List<Example> allExamples) throws InvalidFoldPersistence {
        // load FoldInfo
        ImmutablePair<Integer, Integer> nbOfFoldsAndFoldSize = loadNbOfFoldsAndFoldSize(directoryPathStr);

        int nbOfFolds = nbOfFoldsAndFoldSize.getLeft();
        int foldSize = nbOfFoldsAndFoldSize.getRight();


        List[] folds = new List[nbOfFolds];
        for (int i = 0; i < nbOfFolds; i++) {
            List<Example> examplesForThisFold = loadSingleFold(allExamples, directoryPathStr, i);
            folds[i] = examplesForThisFold;
        }

        return FoldDataSetSplitter.load(nbOfFolds, foldSize, folds);
    }
}
