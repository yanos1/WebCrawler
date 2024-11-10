package main.java;

import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class that provides static methods for HTML processing tasks.
 * It includes functionality for downloading a web page, extracting URLs, and saving the page content.
 */
public class HTMLDataExtractor {

    // Regular expression pattern to match URLs within href attributes in HTML content
    private static final Pattern URL_PATTERN = Pattern.compile(
            "href=[\"'](http[s]?://(?:www\\.)?[a-zA-Z0-9.-]+(?:\\.[a-zA-Z]{2,})(?:/[^\"'<>]*)?" +
                    "(?:\\?[^\s\"'<>]*)?)"  // need to remove kwrgs !
    );

    // Set of file extensions to exclude during URL extraction
    private static final Set<String> EXCLUDED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "bmp",
            "pdf", "css", "js", "zip", "rar", "exe", "svg", "ico", "onion");

    // Constant indicating the minimum response code that will be considered an error (4xx and 5xx)
    public static final char ERROR_HTTP_RESPONSE_CODE = '4';


    public static List<String> downloadAndExtractLinks(String url, CrawlerContext context) throws IOException {
        List<String> extractedUrls = new ArrayList<>();


        var connection = openConnection(url);

        File file = prepareOutputFile(url, context);

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            processHtmlContent(in, writer, context, extractedUrls);

        } catch (FileNotFoundException e) {
            System.err.println("Error 404: Page not found for URL: " + url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return extractedUrls;
    }

    /*
     * Opens an HTTP connection to the specified URL.
     */
    private static HttpURLConnection openConnection(String url) throws IOException {
        URL u = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) u.openConnection();
        connection.setRequestMethod("GET");
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

    /*
     * Processes the HTML content line by line, saving it to the output file and extracting valid URLs.
     */
    private static void processHtmlContent(BufferedReader reader, BufferedWriter writer,
                                           CrawlerContext context, List<String> extractedUrls) throws IOException {
        String inputLine;
        int urlsFound = 0;

        // Read HTML content line by line
        while ((inputLine = reader.readLine()) != null) {
            writer.write(inputLine);
            writer.newLine();

            // Only extract URLs if max depth and max URL count have not been reached
            if (!context.reachedMaxDepth() && urlsFound < context.getMaxUrlsPerLevel()) {
                // Add the count of newly extracted URLs
                urlsFound += extractUrls(inputLine, context, extractedUrls, context.getMaxUrlsPerLevel() - urlsFound);
            }
        }
    }

    /*
     * Extracts URLs from a line of HTML content, validating and adding them to the list if they meet criteria.
     */
    private static int extractUrls(String inputLine, CrawlerContext context,
                                   List<String> extractedUrls, int maxUrlsToExtract) throws IOException {
        Matcher matcher = URL_PATTERN.matcher(inputLine);
        int urlsFound = 0;

        // Find and validate URLs in the line up to the maxUrlsToExtract limit
        while (matcher.find() && urlsFound < maxUrlsToExtract) {
            String foundUrl = matcher.group(1);
            System.out.println(foundUrl);

            if (!isInvalidUrl(context, foundUrl)) {
                extractedUrls.add(foundUrl);
                context.addVisitedUrlThisLevel(foundUrl);
                urlsFound++;
                System.out.println("valid: " + foundUrl);
            }
        }
        return urlsFound;
    }
    /*
     * Determines if a URL is invalid based on several criteria such as file extensions,
     * HTTP response codes, and whether it has been visited before.
     */
    private static boolean isInvalidUrl(CrawlerContext context, String foundUrl) throws IOException {
        return hasExcludedExtension(foundUrl) ||
                isUrlGivingErrorCode(foundUrl) ||
                context.isUrlVisitedThisLevel(foundUrl) ||
                (context.isUniqueFlagActive() && context.isUrlVisitedCrossLevels(foundUrl));
    }

    /*
     * Checks if a URL returns an error HTTP response code
     */
    private static boolean isUrlGivingErrorCode(String stringUrl) throws IOException {
        URL url = new URL(stringUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String responseCode = String.valueOf(connection.getResponseCode());
        if (responseCode.charAt(0) >= ERROR_HTTP_RESPONSE_CODE) {
            System.err.println("bad response code : " + responseCode + " for " + stringUrl);
            return true;
        }
        return false;
    }

    /*
     * Checks if the URL has a file extension in the list of excluded extensions.
     */
    private static boolean hasExcludedExtension(String url) {
        int dotIndex = url.lastIndexOf('.');
        if (dotIndex == -1) {
            return false; // No extension
        }
        String extension = url.substring(dotIndex + 1).toLowerCase();
        return EXCLUDED_EXTENSIONS.contains(extension);
    }

    /*
     * Converts a URL to a sanitized string that can be safely used as a filename.
     * Removes protocol information and replaces non-alphanumeric characters with underscores.
     */
    private static String refactorUrl(String url) {
        return url.split("http[s]?://")[1].replaceAll("[^a-zA-Z0-9]", "_");
    }
}
