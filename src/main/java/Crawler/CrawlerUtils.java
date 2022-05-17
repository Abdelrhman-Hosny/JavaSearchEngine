package Crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.Doc;
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

        int seedsPerThread = lines.size() / numThreads;

        ArrayList<HashSet<String>> seedArray = new ArrayList<>();
        // all except last
        for (int i = 0; i < numThreads - 1; i++) {
            HashSet<String> currentSet = new HashSet<>();

            for (int j = i * seedsPerThread; j < (i + 1) * seedsPerThread; j++) {
                currentSet.add(lines.get(i));
            }

            seedArray.add(currentSet);
        }

        // last thread
        HashSet<String> lastSet = new HashSet<>();
        for (int i = (numThreads - 1) * seedsPerThread; i < lines.size(); i++) {
            lastSet.add(lines.get(i));
        }

        seedArray.add(lastSet);

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
            System.out.println("Error: " );
            e.printStackTrace();
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

}

