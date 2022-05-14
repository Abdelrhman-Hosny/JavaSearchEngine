package Crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

import static Constants.Constants.*;


public class RobotsChecker {

    enum RobotPermissionTypes {
        WILDCARDS,
        STRAIGHT_MATCHES
    }

    public HashMap<String, HashMap<RobotPermissionTypes, HashSet<String>>> disallowed = new HashMap<>();
    public HashMap<String, HashMap<RobotPermissionTypes, HashSet<String>>> allowed = new HashMap<>();

    public boolean check(String url) {
        URL urlObject;

        try {
            urlObject = new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }

        try {
            // if site not visited before, check the robots.txt
            if (!allowed.containsKey(urlObject.getHost())) getRobotsTxtContent(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<RobotPermissionTypes, HashSet<String>> allowMap = allowed.get(urlObject.getHost());
        HashMap<RobotPermissionTypes, HashSet<String>> disallowMap = disallowed.get(urlObject.getHost());

        // Allowed : Straight matches
        HashSet<String> allowStraightMatches = allowMap.get(RobotPermissionTypes.STRAIGHT_MATCHES);
        for (String allowStraightMatch : allowStraightMatches) {
            if (url.contains(allowStraightMatch)) return true;
        }

        // Allowed : Wildcards
        HashSet<String> allowWildcards = allowMap.get(RobotPermissionTypes.WILDCARDS);
        // don't know for now
        for (String allowWildcard : allowWildcards) {
            continue;
        }


        // Disallowed : Straight matches
        HashSet<String> disallowStraightMatches = disallowMap.get(RobotPermissionTypes.STRAIGHT_MATCHES);
        for (String disallowStraightMatch : disallowStraightMatches) {
            if (url.contains(disallowStraightMatch)) return false;
        }

        // Disallowed : Wildcards
        HashSet<String> disallowWildcards = disallowMap.get(RobotPermissionTypes.WILDCARDS);
        // don't know for now
        for (String disallowWildcard : disallowWildcards) {
            continue;
        }

        return true;

    }

    public BufferedReader getRobotsFile(String incomingUrl) {
        URL url;

        try {
            url = new URL(incomingUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
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
            return null;
        }

        return in;
    }

    public void initializeMap(HashMap<String, HashMap<RobotPermissionTypes, HashSet<String>>> map, String host) {
        map.put(host, new HashMap<>());
        map.get(host).put(RobotPermissionTypes.WILDCARDS, new HashSet<>());
        map.get(host).put(RobotPermissionTypes.STRAIGHT_MATCHES, new HashSet<>());
    }

    public void getRobotsTxtContent(String incomingUrl) throws IOException {

        // if already checked, return
        URL incomingUrlObject = new URL(incomingUrl);
        String host = incomingUrlObject.getHost();

        if (allowed.containsKey(host) || disallowed.containsKey(host)) return;
        // create empty hash map in case of no robots.txt file
        // so that we don't visit it again

        initializeMap(disallowed, host);
        initializeMap(allowed, host);

        HashMap<RobotPermissionTypes, HashSet<String>> hostDisallowedMap = disallowed.get(host);
        HashMap<RobotPermissionTypes, HashSet<String>> hostAllowedMap = allowed.get(host);

        // get robots.txt url
        // BufferedReader outputs the content of the robots.txt file
        // line by line
        BufferedReader in = getRobotsFile(incomingUrl);
        if (in == null) return;

        String line;
        while ((line = in.readLine()) != null) {

            if (line.startsWith("User")) {
                // make sure that it is our agent or *
                String agent = line.split(":")[1].trim();

                if (agent.equals("*") || agent.equals(userAgent)) {
                    // If it is our agent or *, we check what rules should we set

                    while ((line = in.readLine()) != null) {
                        if (line.startsWith("Disallow:")) {
                            String disallowString = line.split(":")[1].trim();
                            String disallowedUrl = incomingUrlObject.getProtocol() + "://" + incomingUrlObject.getHost() + disallowString;
                            System.out.println("Disallow : " + disallowString);

                            // if it is a straight match, we add it to the straight hash set
                            if (true) {

                                hostDisallowedMap.get(RobotPermissionTypes.STRAIGHT_MATCHES).add(disallowedUrl);
                            } else {
                                hostDisallowedMap.get(RobotPermissionTypes.WILDCARDS).add(disallowedUrl);
                            }
                        } else if (line.startsWith("Allow:")) {
                            String allowString = line.split(":")[1].trim();
                            System.out.println("Allow : " + allowString);
                            String allowedUrl = incomingUrlObject.getProtocol() + "://" + incomingUrlObject.getHost() + allowString;
                            System.out.println("Disallow : " + allowString);

                            // if it is a straight match, we add it to the straight hash set
                            if (true) {

                                hostAllowedMap.get(RobotPermissionTypes.STRAIGHT_MATCHES).add(allowedUrl);
                            } else {
                                hostAllowedMap.get(RobotPermissionTypes.WILDCARDS).add(allowedUrl);
                            }
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
