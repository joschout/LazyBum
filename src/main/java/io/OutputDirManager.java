package io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by joschout.
 */
public class OutputDirManager {


    private String outputRootDir;

    public OutputDirManager(String outputRootDir) {
        this.outputRootDir = outputRootDir;
    }

    public  String getOutputDirDataSetStr(String dataSetName){
        return outputRootDir
                + File.separator + dataSetName;
    }


    public String makeOutputDirDataSet(String dataSetName) throws IOException {
        String outputDirDataSetStr = getOutputDirDataSetStr(dataSetName);

        Path outputDirPath = Paths.get(outputDirDataSetStr);
        //create output directory if it does not yet exist
        if (!Files.exists(outputDirPath)) {
            Files.createDirectories(outputDirPath);
        }
        return outputDirDataSetStr;
    }

    public String getOutputFileNameStr(String dataSetName, String fileName) throws IOException {
        return makeOutputDirDataSet(dataSetName) + File.separator + fileName;
    }



}
