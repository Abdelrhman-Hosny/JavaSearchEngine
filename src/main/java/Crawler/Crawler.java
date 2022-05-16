package Crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static Constants.Constants.*;

public class Crawler {


    public static void crawl() throws IOException {
        RobotsChecker robotsChecker = new RobotsChecker();
        HashSet<String> visited = new HashSet<>();
        HashSet<String> toVisit = CrawlerUtils.ReadInitialSeed(SEED_PATH);

        HashMap<Long, HashSet<String>> checksumPathMap = new HashMap<>();


        // while there are still links to visit
        // and the visited links haven't exceeded the limit
        while (!toVisit.isEmpty() && visited.size() < MAX_DOCS) {

            String url = toVisit.iterator().next();
            toVisit.remove(url);

            if (! visited.contains(url) && !url.endsWith("robots.txt")) {
                crawlURL(url, visited, toVisit, robotsChecker, checksumPathMap);
                visited.add(url);
            }

        }

        System.out.println("Visited: " + visited.size());
        for (  String url : visited) {
            System.out.println(url);
        }

    }
    public static void crawlURL(String url, HashSet<String> visited, HashSet<String> toVisit, RobotsChecker robotsChecker, HashMap<Long, HashSet<String>> checksumPathMap) {
        // reading HTML document from given URL

        // Check for Robots.txt
        if (! robotsChecker.check(url)) return;

        // get HTML Document
        Document doc = CrawlerUtils.getHTMLDocument(url);
        if (doc == null) return;
        // generate checksum
        long checksum = CrawlerUtils.hashHTML(doc);
        // check if checksum is already in the map
        if (checksumPathMap.containsKey(checksum)) {
            if (compareFiles(checksumPathMap.get(checksum), doc)) {
                // true means file already exists
                // ,so we add the url to visited and exit the crawl
                visited.add(url);
                return;
            }
            // if files are different we download the new file and add it the paths

            String filePath = DOWNLOAD_PATH + UUID.randomUUID() + ".html";
            if (!CrawlerUtils.saveHTMLDocument(doc, filePath)) {
                // if file not added successfully, we add the url to visited and exit the crawl
                visited.add(url);
                return;
            }
            checksumPathMap.get(checksum).add(filePath);

        } else {
            // add checksum and url to map
            HashSet<String> paths = new HashSet<>();
            String filePath = DOWNLOAD_PATH + UUID.randomUUID() + ".html";
            if (!CrawlerUtils.saveHTMLDocument(doc, filePath)) {
                // if file not added successfully, we add the url to visited and exit the crawl
                visited.add(url);
                return;
            }

            paths.add(filePath);
            checksumPathMap.put(checksum, paths);

        }

        // get all links from document
        Elements links = doc.getElementsByTag("a");

        HashSet<String> newLinks = new HashSet<>();

        for (Element link : links) {

            String relative_link = link.attr("href").toLowerCase();
            if (relative_link.startsWith("#") || relative_link.isEmpty())
                continue;

            String abs_link = CrawlerUtils.NormalizeUrl(link.attr("abs:href").toLowerCase());
            if (!abs_link.startsWith("http")) continue;

            // check if link already visited or forbidden by robots.txt
            if (!visited.contains(abs_link) && !url.equals(abs_link) && !toVisit.contains(abs_link)) {
                newLinks.add(abs_link);
//                System.out.println(abs_link);
            }

            if (newLinks.size() == MAX_LINKS_PER_DOC) break;
        }

        // add new links to toVisit
        toVisit.addAll(newLinks);
    }

    private static boolean compareFiles(HashSet<String> paths, Document doc) {
        // return true if file already exists
        // false if it is a new file

        Document doc1 = CrawlerUtils.removeUselessHtmlTags(doc);
        for (String path : paths) {
            Document doc2;
            try {
                File input = new File(path);
                doc2 = CrawlerUtils.removeUselessHtmlTags(Jsoup.parse(input, "UTF-8", ""));

            } catch (IOException e) {
                System.out.println("Error reading file");
                e.printStackTrace();
                continue;
            }

            if (doc1 == doc2) return true;

        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        long startTime = System.nanoTime();
        crawl();
        long stopTime = System.nanoTime();
        long elapsedTime = stopTime - startTime;
        elapsedTime /= 1_000_000_000;
        System.out.println("Elapsed time: " + elapsedTime);
//        for (int i = 0; i < 10; i++) {
//            URL x = new URL("https://www.google.com");
//            x.openConnection();
//            System.out.println(x.getHost());
//        }
    }
}
