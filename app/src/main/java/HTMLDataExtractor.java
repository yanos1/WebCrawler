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
    public static final int ERROR_HTTP_RESPONSE_CODE = 400;

    /**
     *  downloads html content and extracts links from it.
     * @param url the url to extract its content
     * @param context the current crawl context
     * @return return the list of new urls fetched from the url
     * @throws IOException
     */
    public static List<String> downloadAndExtractLinks(String url, CrawlerContext context) throws IOException {
        List<String> extractedUrls = new ArrayList<>();

        var connection = openConnection(url);
        Document htmlDocument = connection.get();
        File file = prepareOutputFile(url, context);

        try {
            downloadHtmlToFile(htmlDocument, file , url);  // IMPORTANT NOTE: it is probably best to let a new
            extractUrls(htmlDocument, context, extractedUrls);
            // thread handle this task. for simplicity reasons, i chose not to.

        } catch (FileNotFoundException e) {
            System.err.println("Error 404: Page not found for URL: " + url);
        } catch (IOException e) {
            System.err.println("IOException while reading  from : " + url);
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
    private static Connection openConnection(String url){
        Connection connection = Jsoup.connect(url);
        // Sets a user-agent string to mimic a real browser (helps avoid being blocked)
        connection.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

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
        return new File(directory, refactorUrl(url) + ".html");
    }

    private static void extractUrls(Document document,
                                    CrawlerContext context, List<String> extractedUrls) throws IOException {
        // Select all <a> tags with href attribute
        Elements links = document.select("a[href]");

        int urlsFound = 0;
        for (Element link : links) {
            String foundUrl = link.attr("abs:href");

            if (!foundUrl.startsWith("http")) {
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
        return  isUrlGivingErrorCode(foundUrl) ||
                context.isUrlVisitedThisLevel(foundUrl) ||
                (context.isUniqueFlagActive() && context.isUrlVisitedCrossLevels(foundUrl));
    }

    /*
     * Checks if a URL returns an error HTTP response code
     */
    private static boolean isUrlGivingErrorCode(String stringUrl) throws IOException {
        Connection connection = Jsoup.connect(stringUrl).ignoreHttpErrors(true); // Ignore errors to retrieve status code
        int responseCode = connection.execute().statusCode();

        if (responseCode >= ERROR_HTTP_RESPONSE_CODE) {
            return true;
        }
        return false;
    }


    /*
     * Converts a URL to a sanitized string that can be safely used as a filename.
     * Removes protocol information and replaces non-alphanumeric characters with underscores.
     */
    private static String refactorUrl(String url) {
        return url.split("http[s]?://")[1].replaceAll("[^a-zA-Z0-9]", "_");
    }

}
