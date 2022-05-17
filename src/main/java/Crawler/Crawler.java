package Crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static Constants.Constants.*;

public class Crawler {

    HashMap<Long, HashSet<String>> checksumPathMap;
    HashSet<String> globalVisited, globalToVisit;
    int numThreads;
    String seedPath;
    RobotsChecker robotsChecker;

    Crawler(int numThreads, String seedPath) {
        globalVisited = new HashSet<>();
        globalToVisit = new HashSet<>();
        this.numThreads = numThreads;
        this.seedPath = seedPath;
        robotsChecker = new RobotsChecker();

        checksumPathMap = new HashMap<>();
        // check for state (checkpoints)

        // split seeds into numThreads starting seeds.
        ArrayList<HashSet<String>> seedArray;
        try {
            seedArray = CrawlerUtils.ReadInitialSeed(seedPath, numThreads);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Can't read seeds");
            return;
        }

        // create threads
        Thread[] threadArr = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            // for each thread we pass the
            // 1, seeds for the current

            HashSet<String> localToVisit = seedArray.get(i);
            threadArr[i] = new Thread(
                    new CrawlerInstance(localToVisit, globalToVisit, globalVisited, robotsChecker, checksumPathMap));

            threadArr[i].start();
        }

        // wait for all threads to finish
        for (int i = 0; i < numThreads; i++) {
            try {
                threadArr[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Visited: " + globalVisited.size());
        for (String url : globalVisited) {
            System.out.println(url);
        }

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

            if (doc1 == doc2)
                return true;

        }
        return false;
    }

    private class CrawlerInstance implements Runnable {
        HashSet<String> localToVisit, globalToVisit, globalVisited;
        RobotsChecker robotsChecker;
        HashMap<Long, HashSet<String>> checksumPathMap;

        CrawlerInstance(HashSet<String> localToVisit, HashSet<String> globalToVisit,
                HashSet<String> globalVisited, RobotsChecker robotsChecker,
                HashMap<Long, HashSet<String>> checksumPathMap) {

            this.localToVisit = localToVisit;
            this.globalToVisit = globalToVisit;
            this.globalVisited = globalVisited;
            this.robotsChecker = robotsChecker;
            this.checksumPathMap = checksumPathMap;
        }

        @Override
        public void run() {

            // while there are still links to visit
            // and the visited links haven't exceeded the limit
            System.out.println("Thread started");
            System.out.println("To visit : " + localToVisit);
            while (!localToVisit.isEmpty() && globalVisited.size() < MAX_DOCS) {

                String url = localToVisit.iterator().next();
                synchronized(globalToVisit) {
                    localToVisit.remove(url);
                    globalToVisit.remove(url);
                }

                if (!globalVisited.contains(url) && !url.endsWith("robots.txt")) {
                    crawlURL(url);
                    globalVisited.add(url);
                }

            }

        }

        public void crawlURL(String url) {
            // reading HTML document from given URL

            // Check for Robots.txt
            synchronized(robotsChecker) {
                if (!robotsChecker.check(url))
                    return;
            }

            // get HTML Document
            Document doc = CrawlerUtils.getHTMLDocument(url);
            if (doc == null)
                return;
            // generate checksum
            long checksum = CrawlerUtils.hashHTML(doc);
            // check if checksum is already in the map
            if (checksumPathMap.containsKey(checksum)) {
                if (compareFiles(checksumPathMap.get(checksum), doc)) {
                    // true means file already exists
                    // ,so we add the url to visited and exit the crawl
                    globalVisited.add(url);
                    return;
                }
                // if files are different we download the new file and add it the paths

                String filePath = DOWNLOAD_PATH + UUID.randomUUID() + ".html";
                if (!CrawlerUtils.saveHTMLDocument(doc, filePath)) {
                    // if file not added successfully, we add the url to visited and exit the crawl
                    globalVisited.add(url);
                    return;
                }
                checksumPathMap.get(checksum).add(filePath);

            } else {
                // add checksum and url to map
                HashSet<String> paths = new HashSet<>();
                String filePath = DOWNLOAD_PATH + UUID.randomUUID() + ".html";
                if (!CrawlerUtils.saveHTMLDocument(doc, filePath)) {
                    // if file not added successfully, we add the url to visited and exit the crawl
                    globalVisited.add(url);
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
                if (!abs_link.startsWith("http"))
                    continue;

                // check if link already visited or forbidden by robots.txt
                if (!globalVisited.contains(abs_link) && !url.equals(abs_link) && !globalToVisit.contains(abs_link)) {
                    localToVisit.add(abs_link);
                    globalToVisit.add(abs_link);
                }

            }

        }

    }

    public static void main(String[] args) throws IOException {
        long startTime = System.nanoTime();
        Crawler crawler = new Crawler(4, SEED_PATH);
        long stopTime = System.nanoTime();
        long elapsedTime = stopTime - startTime;
        elapsedTime /= 1_000_000_000;
        System.out.println("Elapsed time: " + elapsedTime);
        // for (int i = 0; i < 10; i++) {
        // URL x = new URL("https://www.google.com");
        // x.openConnection();
        // System.out.println(x.getHost());
        // }
    }
}
