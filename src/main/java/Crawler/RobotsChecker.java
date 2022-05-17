package Crawler;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Constants.Constants.*;


public class RobotsChecker {

    enum RobotPermissionTypes {
        WILDCARDS,
        STRAIGHT_MATCHES
    }


    public HashMap<String, HashMap<RobotPermissionTypes, HashSet<String>>> disallowed = new HashMap<>();
    public HashMap<String, HashMap<RobotPermissionTypes, HashSet<String>>> allowed = new HashMap<>();

    public final String RobotsFilePath = CRAWLER_PROGRESS_PATH + ROBOTS_SAVE_FILE;
    RobotsChecker() {

    }

    RobotsChecker(boolean LoadFromProgress) {
        Path of = Path.of(RobotsFilePath);
        if (LoadFromProgress) {
            List<String> lines = null;
            try {
                lines = Files.readAllLines(of);
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert lines != null;
            for (String line : lines) {
                try {
                    getRobotsTxtContent(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            File file = new File(RobotsFilePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public static boolean matchRegex(String pattern, String text) {
        Pattern re = Pattern.compile(pattern);
        Matcher matcher = re.matcher(text);
        return matcher.matches();
    }


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
            System.out.println("Error while checking " + url + " for robots.txt");
        }

        HashMap<RobotPermissionTypes, HashSet<String>> allowMap = allowed.get(urlObject.getHost());
        HashMap<RobotPermissionTypes, HashSet<String>> disallowMap = disallowed.get(urlObject.getHost());

        String urlPath = urlObject.getPath();
        // Allowed : Straight matches
        HashSet<String> allowStraightMatches = allowMap.get(RobotPermissionTypes.STRAIGHT_MATCHES);
        for (String allowStraightMatch : allowStraightMatches) {
            if (urlPath.startsWith(allowStraightMatch)) return true;
        }

        // Allowed : Wildcards
        HashSet<String> allowWildcards = allowMap.get(RobotPermissionTypes.WILDCARDS);

        for (String allowWildcard : allowWildcards) {
            if (matchRegex(allowWildcard, urlPath)) return true;
        }


        // Disallowed : Straight matches
        HashSet<String> disallowStraightMatches = disallowMap.get(RobotPermissionTypes.STRAIGHT_MATCHES);
        for (String disallowStraightMatch : disallowStraightMatches) {
            if (urlPath.startsWith(disallowStraightMatch)) return false;
        }

        // Disallowed : Wildcards
        HashSet<String> disallowWildcards = disallowMap.get(RobotPermissionTypes.WILDCARDS);

        for (String disallowWildcard : disallowWildcards) {

            if (matchRegex(disallowWildcard, urlPath)) return false;
        }

        return true;

    }

    public BufferedReader getRobotsFile(String incomingUrl) {
        URL url;

        try {
            url = new URL(incomingUrl);
        } catch (MalformedURLException e) {
            System.out.println("Error while getting robots.txt for " + incomingUrl);
            return null;
        }

        String robotsTxtUrl = url.getProtocol() + "://" + url.getHost() + "/robots.txt";

        // get the robots.txt content

        URL robotsTxtUrlObject;
        BufferedReader in;

        try {
            robotsTxtUrlObject = new URL(robotsTxtUrl);

            in = new BufferedReader(new InputStreamReader(robotsTxtUrlObject.openStream()));

        } catch (IOException e) {
            return null;
        }

        return in;
    }

    public void initializeMap(HashMap<String, HashMap<RobotPermissionTypes, HashSet<String>>> map, String host) {
        map.put(host, new HashMap<>());
        map.get(host).put(RobotPermissionTypes.WILDCARDS, new HashSet<>());
        map.get(host).put(RobotPermissionTypes.STRAIGHT_MATCHES, new HashSet<>());
    }

    public static void addUrlToMap(HashMap<RobotPermissionTypes, HashSet<String>> map, String url, String absUrl) {


        if (url.contains("*") || url.contains("$")) {
            if (url.endsWith("*")) {
                if (!url.substring(0, url.length() - 1).contains("*") && !url.contains("$")){
                    map.get(RobotPermissionTypes.STRAIGHT_MATCHES).add(url.substring(0, url.length() - 1));
                } else {
                    // transform url to regex
                    String regex = absUrl.replace(".", "[.]").replace("*", ".+");
                    map.get(RobotPermissionTypes.WILDCARDS).add(regex);

                }
            }
        } else {
            map.get(RobotPermissionTypes.STRAIGHT_MATCHES).add(absUrl);
        }
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
                            String[] splitLine = line.split(":");
                            if (splitLine.length == 1) {
                                // means Disallow, equivalent to Allow: /
                                addUrlToMap(hostAllowedMap, "/", incomingUrl);
                            } else {
                                String disallowString = splitLine[1].trim();
                                addUrlToMap(hostDisallowedMap, disallowString, incomingUrl);

                            }

                       } else if (line.startsWith("Allow:")) {
                            String[] splitLine = line.split(":");
                            if (splitLine.length == 1) {
                                // means Allow: , equivalent to Disallow: /*
                                addUrlToMap(hostDisallowedMap, "/", incomingUrl);
                            } else {
                                String allowString = splitLine[1].trim();
                                addUrlToMap(hostDisallowedMap, allowString, incomingUrl);

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
        in.close();

        String robotsTxtUrl = incomingUrlObject.getProtocol() + "://" + incomingUrlObject.getHost() + "/robots.txt";
        // write to file
        FileWriter fw = new FileWriter(RobotsFilePath, true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(robotsTxtUrl + "\n");
        bw.close();
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
