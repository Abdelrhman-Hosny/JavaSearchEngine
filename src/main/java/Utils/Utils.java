package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static Constants.Constants.*;

public class Utils {

    public HashMap<String, HashSet<String>> cleanPageDegreeFile(String filePath) throws IOException {

        HashMap<String, HashSet<String>> pageDegreeMap = new HashMap<>();

        List<String> lines = Files.readAllLines(Paths.get(CRAWLER_PROGRESS_PATH + VISITED_SAVE_FILE));

        lines.replaceAll(line -> line.split("\t")[0]);

        HashSet<String> visited = new HashSet<>(lines);
        // HashSet<String> visited = new HashSet<String>(List.of(new String[]{"1", "2",
        // "3", "4", "5"}));

        BufferedReader br = Files.newBufferedReader(Paths.get(filePath));

        String line;

        while ((line = br.readLine()) != null) {

            String[] split = line.split("\t");
            // System.out.println(split[0]);

            if (split.length <= 1 && !visited.contains(split[0]))
                continue;

            String page = split[0];

            HashSet<String> outLinks = new HashSet<>();
            for (int i = 1; i < split.length; i++) {
                if (!visited.contains(split[i]))
                    continue;
                // if(page.equals(split[i]))
                // continue;
                outLinks.add(split[i]);

            }
            if (outLinks.size() == 0) {
                System.out.println("adding sink url" + page);

                for (String x : visited) {
                    // if(x.equals(page))
                    // continue;
                    outLinks.add(x);

                }
            }
            pageDegreeMap.put(page, outLinks);

        }

        return pageDegreeMap;
    }

    public HashMap<String, HashSet<String>> getInPageMap(HashMap<String, HashSet<String>> pageDegreeMap) {
        HashMap<String, HashSet<String>> inPageMap = new HashMap<>();
        for (String currentPage : pageDegreeMap.keySet()) {

            HashSet<String> inLinks = new HashSet<>();

            for (String loopingPage : pageDegreeMap.keySet()) {
                // if (loopingPage.equals(currentPage)) continue;
                if (pageDegreeMap.get(loopingPage).contains(currentPage)) {
                    inLinks.add(loopingPage);
                }
            }

            inPageMap.put(currentPage, inLinks);
        }

        return inPageMap;
    }

    public static void main(String[] args) {
        System.out.println("hello");
    }
}
