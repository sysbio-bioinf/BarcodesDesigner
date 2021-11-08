package main.code.commandLineUse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class FileSaver {

    /**
     * Checks if the output path is accessible/exists and hence if the string can be saved to this path
     * @param outputPath The path to which the file should be saved
     * @param outString The string representing the content for the new file
     */
    public static void saveIfPossible(String outputPath, String outString) {
        List<String> lines = Arrays.stream(outString.split("\n")).toList();
        Path file = Paths.get(outputPath);
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("The output file with the path " + outputPath + " is not accessible " +
                    "or does not exist.");
        }
    }
}
