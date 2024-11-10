package main.java;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * This class is implementing the Callable class, which is called everytime we want a new thread to
 * complete a crawling task.
 */
public class CrawlerTask implements Callable<List<String>> {
    private final String url;
    private final CrawlerContext context;


    public CrawlerTask(String url, CrawlerContext context) {
        this.url = url;
        this.context = context;
    }

    /**
     *  uses the HTMLDataExtractor Logic to complete the crawling task
     * @return a list of urls tha were found in the url
     * @throws IOException
     */
    @Override
    public List<String> call() throws IOException {

        String normalizedUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;

        return HTMLDataExtractor.downloadAndExtractLinks(normalizedUrl, context);
    }

}
