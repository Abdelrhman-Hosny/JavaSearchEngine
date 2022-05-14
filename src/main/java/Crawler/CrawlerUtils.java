package Crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static Constants.Constants.userAgent;

public class CrawlerUtils {

    public static HashSet<String> ReadInitialSeed(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        return new HashSet<>(lines);
    }

    public static String NormalizeUrl(String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        url = url.strip().toLowerCase();

        return url;
    }

    public static Elements GetAllLinks(String url) {
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

        return document.getElementsByTag("a");

    }

    public static void main(String[] args) {
        try {
            System.out.println(ReadInitialSeed("src/main/resources/InitialSeed.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

