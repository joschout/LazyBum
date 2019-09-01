package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by joschout.
 */
public class ConsoleAndFileWriter {

    PrintWriter consoleWriter;
    PrintWriter fileWriter;

    public ConsoleAndFileWriter(File file) throws FileNotFoundException {

        consoleWriter = new PrintWriter(System.out);
        fileWriter = new PrintWriter(file);
    }

    public void writeLine(String string) {
        consoleWriter.write(string + "\n");
        fileWriter.write(string + "\n");
    }


    public void close() {
//            consoleWriter.close();
        fileWriter.close();
    }

    public void flush() {
        consoleWriter.flush();
        fileWriter.flush();
    }


}
