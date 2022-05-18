package Crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static Constants.Constants.*;

public class Crawler {

    HashMap<Long, HashSet<String>> checksumPathMap;
    HashSet<String> globalVisited, globalToVisit, globalVisitedRuined;
    int numThreads;
    String seedPath;
    RobotsChecker robotsChecker;
    public final String VisitedFilePath = CRAWLER_PROGRESS_PATH + VISITED_SAVE_FILE;
    public final String toVisitPath = CRAWLER_PROGRESS_PATH + TO_VISIT_SAVE_FILE;
    public final String pageDegreeFilePath = CRAWLER_PROGRESS_PATH + PAGE_DEGREE_SAVE_FILE;

    Crawler(int numThreads, String seedPath, boolean loadOldProgress) {
        this.numThreads = numThreads;
        this.seedPath = seedPath;
        globalVisitedRuined = new HashSet<>();
        robotsChecker = new RobotsChecker(loadOldProgress);

        checksumPathMap = new HashMap<>();
        // check for state (checkpoints)

        ArrayList<HashSet<String>> seedArray;
        if (loadOldProgress) {
            globalVisited = CrawlerUtils.loadVisited(VisitedFilePath);
            seedArray = CrawlerUtils.loadToVisit(toVisitPath, numThreads);
            globalToVisit = seedArray.get(0);
            for (int i = 1; i < numThreads; i++) {
                globalToVisit.addAll(seedArray.get(i));
            }

        } else {
            // initialize variables

            globalToVisit = new HashSet<>();
            globalVisited = new HashSet<>();
            // delete old progress
            File file = new File(VisitedFilePath);
            if (file.exists()) {
                file.delete();
            }
            file = new File(toVisitPath);
            if (file.exists()) {
                file.delete();
            }
            file = new File(pageDegreeFilePath);
            if (file.exists()) {
                file.delete();
            }
            try {
                seedArray = CrawlerUtils.ReadInitialSeed(seedPath, numThreads);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Can't read seeds");
                return;
            }

        }
        // split seeds into numThreads starting seeds.

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
        HashSet<String> localToVisit;
        final HashSet<String> globalToVisit;
        HashSet<String> globalVisited;
        final RobotsChecker robotsChecker;
        final HashMap<Long, HashSet<String>> checksumPathMap;

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
                synchronized (globalToVisit) {
                    localToVisit.remove(url);
                    globalToVisit.remove(url);
                }

                if (!globalVisited.contains(url) && !globalVisitedRuined.contains(url) && !url.endsWith("robots.txt")) {
                    try {

                        crawlURL(url);
                    } catch (Exception e) {
                        System.out.println("Error crawling url: " + url);
                    }
                }

            }

        }

        public void crawlURL(String url) throws IOException {
            // reading HTML document from given URL

            // Check for Robots.txt
            synchronized (robotsChecker) {
                if (!robotsChecker.check(url))
                    return;
            }

            // get HTML Document
            Document doc = CrawlerUtils.getHTMLDocument(url);
            if (doc == null) {
                globalVisitedRuined.add(url);
                return;
            }
            // generate checksum
            long checksum = CrawlerUtils.hashHTML(doc);
            // check if checksum is already in the map
            synchronized (checksumPathMap) {
                if (checksumPathMap.containsKey(checksum)) {
                    if (compareFiles(checksumPathMap.get(checksum), doc)) {
                        // true means file already exists
                        // ,so we add the url to visited and exit the crawl
                        globalVisitedRuined.add(url);
                        return;
                    }
                    // if files are different we download the new file and add it the paths

                    String filePath = DOWNLOAD_PATH + UUID.randomUUID() + ".html";
                    if (!CrawlerUtils.saveHTMLDocument(doc, filePath)) {
                        // if file not added successfully, we add the url to visited and exit the crawl
                        globalVisitedRuined.add(url);
                        return;
                    }
                    // write to file
                    FileWriter fw;
                    try {
                        fw = new FileWriter(VisitedFilePath, true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(url + "\t" + filePath + "\n");
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    checksumPathMap.get(checksum).add(filePath);
                    globalVisited.add(url);

                } else {
                    // add checksum and url to map
                    HashSet<String> paths = new HashSet<>();
                    String filePath = DOWNLOAD_PATH + UUID.randomUUID() + ".html";
                    if (!CrawlerUtils.saveHTMLDocument(doc, filePath)) {
                        // if file not added successfully, we add the url to visited and exit the crawl
                        globalVisitedRuined.add(url);
                        return;
                    }

                    FileWriter fw;
                    try {
                        fw = new FileWriter(VisitedFilePath, true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(url + "\t" + filePath + "\n");
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    paths.add(filePath);
                    checksumPathMap.put(checksum, paths);

                    globalVisited.add(url);
                }
            }

            // get all links from document
            Elements links = doc.getElementsByTag("a");
            FileWriter toVisitWriter, pageDegreeWriter;
            BufferedWriter toVisitBufferedWriter, pageDegreeBufferedWriter;
            try {
                toVisitWriter = new FileWriter(toVisitPath, true);
                toVisitBufferedWriter = new BufferedWriter(toVisitWriter);
            } catch (IOException e) {
                e.printStackTrace();
                toVisitBufferedWriter = null;
            }

            try {

                pageDegreeWriter = new FileWriter(pageDegreeFilePath, true);
                pageDegreeBufferedWriter = new BufferedWriter(pageDegreeWriter);
            } catch (IOException e) {

                pageDegreeBufferedWriter = null;
            }

            synchronized (globalToVisit) {
                if (pageDegreeBufferedWriter != null)
                    pageDegreeBufferedWriter.write(url + "\t");
            }

            for (Element link : links) {

                String relative_link = link.attr("href").toLowerCase();
                if (relative_link.startsWith("#") || relative_link.isEmpty())
                    continue;

                String abs_link = CrawlerUtils.NormalizeUrl(link.attr("abs:href").toLowerCase());
                if (!abs_link.startsWith("http"))
                    continue;

                synchronized (globalToVisit) {
                    if (pageDegreeBufferedWriter != null)
                        pageDegreeBufferedWriter.write(abs_link + "\t");

                    // check if link already visited or forbidden by robots.txt
                    if (!globalVisitedRuined.contains(abs_link) && !globalVisited.contains(abs_link)
                            && !url.equals(abs_link) && !globalToVisit.contains(abs_link)) {
                        localToVisit.add(abs_link);
                        globalToVisit.add(abs_link);
                        if (toVisitBufferedWriter != null)
                            toVisitBufferedWriter.write(abs_link + "\n");
                    }

                }
            }
            if (pageDegreeBufferedWriter != null) {
                synchronized (globalToVisit) {
                    pageDegreeBufferedWriter.write("\n");
                }
                pageDegreeBufferedWriter.close();
            }

            if (toVisitBufferedWriter != null)
                toVisitBufferedWriter.close();

        }

    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        new Crawler(4, SEED_PATH, false);
        long stopTime = System.nanoTime();
        long elapsedTime = stopTime - startTime;
        elapsedTime /= 1_000_000_000;
        System.out.println("Elapsed time: " + elapsedTime);
    }
}
