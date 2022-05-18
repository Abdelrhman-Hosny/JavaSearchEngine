package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static Constants.Constants.*;

public class Utils {

    public HashMap<String, HashSet<String>> cleanPageDegreeFile(String filePath) throws IOException {

        HashMap<String, HashSet<String>> pageDegreeMap = new HashMap<>();

        HashSet<String> visited = new HashSet<>();

        List<String> lines = Files.readAllLines(Paths.get(CRAWLER_PROGRESS_PATH + VISITED_SAVE_FILE));

        lines.replaceAll(line -> line.split("\t")[0]);

        visited.addAll(lines);
        BufferedReader br = Files.newBufferedReader(Paths.get(filePath));

        String line;
        while ((line = br.readLine()) != null) {
            String[] split = line.split("\t");
            if (split.length <= 1 && !visited.contains(split[0])) continue;

            String page = split[0];

            HashSet<String> outLinks = new HashSet<>();
            for (int i = 1; i < split.length; i++) {
               if (!visited.contains(split[i])) continue;

                outLinks.add(split[i]);

            }

            pageDegreeMap.put(page, outLinks);

        }

        return pageDegreeMap;
    }

    HashMap<String, Integer> getInCount(HashMap<String, HashSet<String>> pageDegreeMap) {
        HashMap<String, Integer> inCount = new HashMap<>();
        for (String currentPage : pageDegreeMap.keySet()) {

            for (String loopingPage : pageDegreeMap.keySet()) {
                if (loopingPage.equals(currentPage)) continue;
                if (pageDegreeMap.get(loopingPage).contains(currentPage)) {
                    if (inCount.containsKey(currentPage)) {
                        inCount.put(currentPage, inCount.get(currentPage) + 1);
                    } else {
                        inCount.put(currentPage, 1);
                    }
                }
            }
        }
        return inCount;
    }

    public static void main(String[] args) {
        try {
            Utils utils = new Utils();
            HashMap<String, HashSet<String>> x = utils.cleanPageDegreeFile(CRAWLER_PROGRESS_PATH + PAGE_DEGREE_SAVE_FILE);
            System.out.println(x.size());
            for (String s : x.keySet()) {
                if (x.get(s).size() != 0) {
                    System.out.println(s + " " + x.get(s));
                }
            }
            System.out.println("Count in\n\n");
            System.out.println(utils.getInCount(x));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

