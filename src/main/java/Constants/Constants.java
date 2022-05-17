package Constants;

public final class Constants {
    private Constants() {
    }


    public static final String userAgent = "Crawler3505";
    public static final String SEED_PATH = "src/main/resources/InitialSeed.txt";

    public static final Integer MAX_LINKS_PER_DOC = 200, MAX_DOCS = 500;

    public static final String DOWNLOAD_PATH = "src/main/resources/CrawlerHTML/";

    public static final String CRAWLER_PROGRESS_PATH = "src/main/resources/CrawlingProgress/";
    public static final String ROBOTS_SAVE_FILE = "robotsURLs.txt";

    public static final String VISITED_SAVE_FILE = "visitedURLs.txt";
    public static final String TO_VISIT_SAVE_FILE = "toVisitURLs.txt";
}
