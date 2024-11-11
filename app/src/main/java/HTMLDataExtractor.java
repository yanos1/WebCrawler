package main.java;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * A utility class that provides static methods for HTML processing tasks.
 * It includes functionality for downloading a web page, extracting URLs, and saving the page content.
 */
public class HTMLDataExtractor {
    private static final int ERROR_HTTP_RESPONSE_CODE = 400;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private static final String HTTP_PREFIX = "http";
    private static final String FILE_EXTENSION = ".html";
    private static final String URL_REGEX = "http[s]?://";
    private static final String FILENAME_REPLACEMENT = "_";


    /**
     * Downloads HTML content and extracts links from it.
     *
     * @param url     the URL to extract its content
     * @param context the current crawl context
     * @return the list of new URLs fetched from the URL
     * @throws IOException
     */
    public static List<String> downloadAndExtractLinks(String url, CrawlerContext context) throws IOException {
        List<String> extractedUrls = new ArrayList<>();

        var connection = openConnection(url);
        Document htmlDocument = connection.get();
        File file = prepareOutputFile(url, context);

        try {
            downloadHtmlToFile(htmlDocument, file, url);

            extractUrls(htmlDocument, context, extractedUrls);

        } catch (FileNotFoundException e) {
            System.err.println("Error 404: Page not found for URL: " + url);
        } catch (IOException e) {
            System.err.println("IOException while reading from: " + url);
        }

        return extractedUrls;
    }

    private static void downloadHtmlToFile(Document htmlDocument, File file, String url) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(htmlDocument.html());
    }

    /*
     * Opens an HTTP connection to the specified URL.
     */
    private static Connection openConnection(String url) {
        Connection connection = Jsoup.connect(url);
        connection.userAgent(USER_AGENT);
        return connection;
    }

    /*
     * Prepares a file to store the downloaded HTML content, creating a directory for the current depth level if necessary.
     */
    private static File prepareOutputFile(String url, CrawlerContext context) {
        File directory = new File(String.valueOf(context.getCurrentDepth()));
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return new File(directory, refactorUrl(url) + FILE_EXTENSION);
    }

    private static void extractUrls(Document document,
                                    CrawlerContext context, List<String> extractedUrls) throws IOException {
        Elements links = document.select("a[href]");
        int urlsFound = 0;

        for (Element link : links) {
            String foundUrl = link.attr("abs:href");

            if (!foundUrl.startsWith(HTTP_PREFIX)) {
                continue;
            }
            if (!context.reachedLastDepth() && urlsFound < context.getMaxUrlsPerLevel()) {
                if (!isInvalidUrl(context, foundUrl)) {
                    extractedUrls.add(foundUrl);
                    context.addVisitedUrlThisLevel(foundUrl);
                    urlsFound++;
                }
            } else {
                break;
            }
        }
    }

    /*
     * Determines if a URL is invalid based on several criteria such as file extensions,
     * HTTP response codes, and whether it has been visited before.
     */
    private static boolean isInvalidUrl(CrawlerContext context, String foundUrl) throws IOException {
        return isUrlGivingErrorCode(foundUrl) ||
                context.isUrlVisitedThisLevel(foundUrl) ||
                (context.isUniqueFlagActive() && context.isUrlVisitedCrossLevels(foundUrl));
    }

    /*
     * Checks if a URL returns an error HTTP response code.
     */
    private static boolean isUrlGivingErrorCode(String stringUrl) throws IOException {
        Connection connection = Jsoup.connect(stringUrl).ignoreHttpErrors(true);
        int responseCode = connection.execute().statusCode();

        return responseCode >= ERROR_HTTP_RESPONSE_CODE;
    }

    /*
     * Converts a URL to a sanitized string that can be safely used as a filename.
     * Removes protocol information and replaces non-alphanumeric characters with underscores.
     */
    private static String refactorUrl(String url) {
        return url.split(URL_REGEX)[1].replaceAll("[^a-zA-Z0-9]", FILENAME_REPLACEMENT);
    }
}
