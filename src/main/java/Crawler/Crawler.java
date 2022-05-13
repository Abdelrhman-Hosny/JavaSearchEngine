package Crawler;

import Crawler.CrawlerUtils.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;

public class Crawler {

    public static String userAgent = "Crawler3505";
    public static Integer MAX_LINKS_PER_DOC = 3, MAX_DOCS = 10;

    public static void crawl() throws IOException {
        HashSet<String> visited = new HashSet<>();
        HashSet<String> toVisit = CrawlerUtils.ReadInitialSeed("src/main/resources/InitialSeed.txt");

        // while there are still links to visit
        // and the visited links haven't exceeded the limit
        while (!toVisit.isEmpty() && visited.size() < MAX_DOCS) {

            String url = toVisit.iterator().next();
            toVisit.remove(url);
            if (! visited.contains(url)) {
                crawlURL(url, visited, toVisit);
                visited.add(url);
            }

        }

        for (  String url : visited) {
            System.out.println(url);
        }

    }
    public static void crawlURL(String url, HashSet<String> visited, HashSet<String> toVisit) {
        // reading HTML document from given URL
        Document document;
        try {
            Connection connection = Jsoup.connect(url).timeout(10000).maxBodySize(0).userAgent(userAgent);

            // get domain name of website

            Connection.Response response = connection.execute();

//            System.out.println(response.contentType());
            if (response.contentType() != null && !Objects.requireNonNull(response.contentType()).contains("text/html")) {
                return;
            }

            document = connection.get();
        } catch (IOException e) {
            System.out.println("Error: " );
            e.printStackTrace();
            return;
        }

        // Check for Robots.txt

        // getting all links from document
        Elements links = document.getElementsByTag("a");

//        System.out.println("Links: " + links.size());

        HashSet<String> newLinks = new HashSet<>();

        for (Element link : links) {

            String relative_link = link.attr("href").toLowerCase();
            if (relative_link.startsWith("#") || relative_link.isEmpty())
                continue;

            String abs_link = link.attr("abs:href").toLowerCase();
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

    public static void main(String[] args) throws IOException {
        crawl();
    }
}
