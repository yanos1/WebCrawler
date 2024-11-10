package main.java;
import main.java.WebCrawler;

public class Main {


    public static final int VALID_NUM_OF_ARGUMENTS = 4;

    public static void validateArgs(String[] args) throws IllegalArgumentException {
        if (args.length != VALID_NUM_OF_ARGUMENTS) {
            throw new IllegalArgumentException("Incorrect number of arguments. Expected 4, got " + args.length);
        }

        // Validate start URL
        if (args[0] == null || args[0].isEmpty()) {
            throw new IllegalArgumentException("Start URL cannot be null or empty.");
        }

        // Validate maxUrlsPerPage
        try {
            int maxUrlsPerPage = Integer.parseInt(args[1]);
            if (maxUrlsPerPage <= 0) {
                throw new IllegalArgumentException("maxUrlsPerPage must be a positive integer.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("maxUrlsPerPage must be an integer.");
        }

        // Validate maxDepth
        try {
            int maxDepth = Integer.parseInt(args[2]);
            if (maxDepth < 0) {
                throw new IllegalArgumentException("maxDepth must be a non-negative integer.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("maxDepth must be an integer.");
        }

        // Validate urlUniqueness
        if (!args[3].equalsIgnoreCase("true") && !args[3].equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("urlUniqueness must be 'true' or 'false'.");
        }
    }

    public static void main(String[] args) {
        validateArgs(args);

        String startUrl = args[0];
        int maxUrlsPerPage = Integer.parseInt(args[1]);
        int maxDepth = Integer.parseInt(args[2]);
        boolean urlUniqueness = Boolean.parseBoolean(args[3]);

        WebCrawler webCrawler = new WebCrawler(maxUrlsPerPage,maxDepth,urlUniqueness);
        webCrawler.startCrawl(startUrl);
    }
}
