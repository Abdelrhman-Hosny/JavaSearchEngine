package Constants;

public final class Constants {
    private Constants() {
    }


    public static final String userAgent = "Crawler3505";
    public static final String SEED_PATH = "src/main/resources/InitialSeed.txt";

    public static final Integer MAX_LINKS_PER_DOC = 200, MAX_DOCS = 10;

    public static final String DOWNLOAD_PATH = "src/main/resources/CrawlerHTML/";

    public static final double spamPercentage = 0.4; // if certain word exceed that percentage report spam
    public static final int documentSizeThreshold = 100;
    public static final int numThreadsIndexer = 5;
}
