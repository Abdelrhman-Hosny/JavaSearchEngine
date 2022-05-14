package Crawler;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

import static Constants.Constants.*;

public class Crawler {


    public static void crawl() throws IOException {
        RobotsChecker robotsChecker = new RobotsChecker();
        HashSet<String> visited = new HashSet<>();
        HashSet<String> toVisit = CrawlerUtils.ReadInitialSeed(SEED_PATH);

        // while there are still links to visit
        // and the visited links haven't exceeded the limit
        while (!toVisit.isEmpty() && visited.size() < MAX_DOCS) {

            String url = toVisit.iterator().next();
            toVisit.remove(url);

            url = CrawlerUtils.NormalizeUrl(url);
            if (! visited.contains(url) && !url.endsWith("robots.txt")) {
                crawlURL(url, visited, toVisit, robotsChecker);
                visited.add(url);
            }

        }

        System.out.println("Visited: " + visited.size());
        for (  String url : visited) {
            System.out.println(url);
        }

    }
    public static void crawlURL(String url, HashSet<String> visited, HashSet<String> toVisit, RobotsChecker robotsChecker){
        // reading HTML document from given URL

        // Check for Robots.txt
        if (! robotsChecker.check(url)) return;

        // getting all links from document
        Elements links = CrawlerUtils.GetAllLinks(url);
        if (links == null) return;
//        System.out.println("Links: " + links.size());

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

    public static void main(String[] args) throws IOException {
        crawl();
//        for (int i = 0; i < 10; i++) {
//            URL x = new URL("https://www.google.com");
//            x.openConnection();
//            System.out.println(x.getHost());
//        }
    }
}
