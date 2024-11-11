package main.java;

import main.java.WebCrawler;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final int VALID_NUM_OF_ARGUMENTS = 4;
    private static final int VALID_STATUS_CODE = 200;
    private static final int MIN_DEPTH = 0;
    private static final int MIN_URLS_PER_PAGE = 1;

    // Error messages
    private static final String ERROR_ARGUMENTS = "Incorrect number of arguments. Expected 4, got ";
    private static final String ERROR_URL_EMPTY = "Start URL cannot be null or empty.";
    private static final String ERROR_URL_CONNECTION = "Failed to connect to the given URL: ";
    private static final String ERROR_MAX_URLS = "maxUrlsPerPage must be a positive integer.";
    private static final String ERROR_MAX_DEPTH = "maxDepth must be a non-negative integer.";
    private static final String ERROR_URL_UNIQUENESS = "urlUniqueness must be 'true' or 'false'.";

    public static void validateArgs(String[] args) throws IllegalArgumentException, IOException {
        if (args.length != VALID_NUM_OF_ARGUMENTS) {
            throw new IllegalArgumentException(ERROR_ARGUMENTS + args.length);
        }

        // Validate start URL
        if (args[0] == null || args[0].isEmpty()) {
            throw new IllegalArgumentException(ERROR_URL_EMPTY);
        } else {
            int statusCode = Jsoup.connect(args[0]).execute().statusCode();
            if (statusCode != VALID_STATUS_CODE) {
                throw new IOException(ERROR_URL_CONNECTION + args[0]);
            }
        }

        // Validate maxUrlsPerPage
        try {
            int maxUrlsPerPage = Integer.parseInt(args[1]);
            if (maxUrlsPerPage < MIN_URLS_PER_PAGE) {
                throw new IllegalArgumentException(ERROR_MAX_URLS);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ERROR_MAX_URLS);
        }

        // Validate maxDepth
        try {
            int maxDepth = Integer.parseInt(args[2]);
            if (maxDepth < MIN_DEPTH) {
                throw new IllegalArgumentException(ERROR_MAX_DEPTH);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(ERROR_MAX_DEPTH);
        }

        // Validate urlUniqueness
        if (!args[3].equalsIgnoreCase("true") && !args[3].equalsIgnoreCase("false")) {
            throw new IllegalArgumentException(ERROR_URL_UNIQUENESS);
        }
    }

    public static void main(String[] args) {
        try {
            validateArgs(args);

            String startUrl = args[0];
            int maxUrlsPerPage = Integer.parseInt(args[1]);
            int maxDepth = Integer.parseInt(args[2]);
            boolean urlUniqueness = Boolean.parseBoolean(args[3]);

            CrawlerContext context = new CrawlerContext(maxUrlsPerPage, maxDepth, urlUniqueness);
            ExecutorService executor = Executors.newCachedThreadPool();

            WebCrawler webCrawler = new WebCrawler(context, executor);
            webCrawler.startCrawl(startUrl);

        } catch (IllegalArgumentException e) {
            System.err.println("Argument error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }

}
