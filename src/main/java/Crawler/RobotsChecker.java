package Crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class RobotsChecker {

    public static String userAgent = "Crawler3505";

    public boolean check(String url) {
        return true;
    }

    public void getRobotsTxtContent(String incomingUrl) throws IOException {

        // get robots.txt url
        URL url;

        try {
            url = new URL(incomingUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        String robotsTxtUrl = url.getProtocol() + "://" + url.getHost() + "/robots.txt";

        // get the robots.txt content

        URL robotsTxtUrlObject;
        BufferedReader in;

        try {
            robotsTxtUrlObject = new URL(robotsTxtUrl);

            System.out.println(robotsTxtUrlObject);
            in = new BufferedReader(new InputStreamReader(robotsTxtUrlObject.openStream()));

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String line;

        while ((line = in.readLine()) != null) {

            if (line.startsWith("User")) {
                // make sure that it is our agent or *
                String agent = line.split(":")[1].trim();

                if (agent.equals("*") || agent.equals(userAgent)) {
                    // If it is our agent or *, we check what rules should we set

                    while ((line = in.readLine()) != null) {
                        if (line.startsWith("Disallow:")) {
                            String disallow = line.split(":")[1].trim();
                            System.out.println("Disallow : " + disallow);
                        } else if (line.startsWith("Allow:")) {
                            String allow = line.split(":")[1].trim();
                            System.out.println("Allow : " + allow);
                        } else if (line.startsWith("User")) {
                            // if there are robots for other agents, we ignore them
                            agent = line.split(":")[1].trim();
                            if (!(agent.equals("*") || agent.equals(userAgent)) ) break;
                        }

                    }

                }

            }
        }
    }


    public static void main(String[] args) {
        RobotsChecker robotsChecker = new RobotsChecker();

        try {
            robotsChecker.getRobotsTxtContent("https://stackoverflow.com/questions/20037659/get-domain-name-of-incoming-connection");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
