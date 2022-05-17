package Crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static Constants.Constants.userAgent;

public class CrawlerUtils {

    public static Document removeUselessHtmlTags(Document documentIn) {

        Document document = documentIn.clone();
        // Removes all the tags that don't contribute to the content of the page
        // (e.g. <head>, <script> , <header>, <footer>, <nav> )
        Elements headElements = document.getElementsByTag("head");
        for (Element headElement : headElements) headElement.remove();

        Elements navElements = document.select("nav");
        for (Element navElement : navElements)  navElement.remove();


        Elements headerElements = document.select("header");
        for (Element headerElement : headerElements)  headerElement.remove();

        Elements footerElements = document.select("footer");
        for (Element footerElement : footerElements)  footerElement.remove();

        return document;

    }

    public static long hashHTML(Document document) {
        Document cleanedDocument = removeUselessHtmlTags(document);

        String htmlText = cleanedDocument.text();

        // get crc32 for the html text
        Checksum checksum = new CRC32();
        checksum.update(htmlText.replaceAll("\\s+", "").getBytes());

        return checksum.getValue();

    }
    
    public static HashSet<String> ReadInitialSeed(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        lines.replaceAll(url -> NormalizeUrl(url));
        return new HashSet<>(lines);
    }


    public static ArrayList<HashSet<String>> ReadInitialSeed(String filePath, int numThreads) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        
        lines.replaceAll(url -> NormalizeUrl(url));


        ArrayList<HashSet<String>> seedArray = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {

            seedArray.add(new HashSet<>());
        }

        for (int i = 0; i < lines.size(); i++) {
            seedArray.get(i % numThreads).add(lines.get(i));
        }

        return seedArray;
    }


    public static String NormalizeUrl(String url) {
        // remove spaces and make it lowercase
        url = url.strip().toLowerCase();

        // remove the # at the end of the url
        if (url.contains("#")) {
            url = url.substring(0, url.indexOf("#"));
        }

        // remove trailing /
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        return url;
    }

    public static Document getHTMLDocument(String url) {
        Document document;
        try {
            Connection connection = Jsoup.connect(url).timeout(10000).maxBodySize(0).userAgent(userAgent);

            // get domain name of website

            Connection.Response response = connection.execute();

//            System.out.println(response.contentType());
            if (response.contentType() != null && !Objects.requireNonNull(response.contentType()).contains("text/html")) {
                return null;
            }

            document = connection.get();
        } catch (IOException e) {
            System.out.println("Error crawling : " + url);
            return null;
        }

        return document;
    }

    public static boolean saveHTMLDocument(Document document, String filePath) {
        try {
            FileWriter writer = new FileWriter(filePath);
            document.outputSettings(new Document.OutputSettings().prettyPrint(false));
            writer.write(document.outerHtml());
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        Document document;
        String url = "https://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java";
        try {
            Connection connection = Jsoup.connect(url).timeout(10000).maxBodySize(0).userAgent(userAgent);
            Connection.Response response = connection.execute();
            document = connection.get();
        } catch (IOException e) {
            System.out.println("Error: " );
            e.printStackTrace();
            return;
        }

        System.out.println(document.html());

//        Document cleanedDocument = removeUselessHtmlTags(document);

//        System.out.println(cleanedDocument.text().length());

//        System.out.println(document.text().length());
    }

    public static HashSet<String> loadVisited(String visitedFilePath) {
        HashSet<String> visited = new HashSet<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(visitedFilePath));
            String line;
            while ((line = reader.readLine()) != null) {
                visited.add(line.split(" ")[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return visited;
    }

    public static ArrayList<HashSet<String>> loadToVisit(String toVisitPath, int numThreads) {
        ArrayList<HashSet<String>> toVisit = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            toVisit.add(new HashSet<>());
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(toVisitPath));
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                toVisit.get(i % numThreads).add(line);
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return toVisit;
    }

    }


