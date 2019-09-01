package io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by joschout.
 */
public class OutputUtils {

    public static void createDirectoriesIfTheyDontExist(String filePath) throws IOException {

        Path outputDirPath = Paths.get(filePath);
        //create output directory if it does not yet exist
        if (!Files.exists(outputDirPath)) {
            Files.createDirectories(outputDirPath);
        }
    }
}
