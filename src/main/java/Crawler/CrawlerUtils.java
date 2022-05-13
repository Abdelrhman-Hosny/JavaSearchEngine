package Crawler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

public class CrawlerUtils {

    public static HashSet<String> ReadInitialSeed(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        return new HashSet<>(lines);
    }

    public static void main(String[] args) {
        try {
            System.out.println(ReadInitialSeed("src/main/resources/InitialSeed.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

