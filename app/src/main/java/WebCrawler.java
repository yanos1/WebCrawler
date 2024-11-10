package main.java;

import java.util.*;
import java.util.concurrent.*;

/**
 * WebCrawler class that crawls web pages up to a specified depth, collecting URLs using multithreading.
 */
public class WebCrawler {

    private final ExecutorService executor;
    private final CrawlerContext context;

    /**
     * Constructs a new WebCrawler with the specified parameters.
     *
     * @param maxUrlsPerPage        the maximum number of URLs to crawl in a page.
     * @param maxDepth       the maximum depth of the crawl
     * @param urlUniqueness  whether to enforce URL uniqueness across all levels of the crawl
     */
    public WebCrawler(int maxUrlsPerPage, int maxDepth, boolean urlUniqueness) {
        this.executor = Executors.newCachedThreadPool();
        this.context = new CrawlerContext(maxUrlsPerPage, maxDepth, urlUniqueness);
    }

    /**
     * Starts the web crawl from a specified starting URL.
     * This method initializes the queue, processes each level of URLs,
     * and increments the crawl depth until reaching the max depth or URL limit.
     *
     * @param startUrl the starting URL for the crawl
     */
    public void startCrawl(String startUrl) {
        Queue<String> queue = new LinkedList<>();
        queue.add(startUrl);

        while (!queue.isEmpty() && !context.reachedMaxDepth()) {
            int levelSize = queue.size();
            Queue<Future<List<String>>> futuresQueue = new LinkedBlockingQueue<>();

            for (int i = 0; i < levelSize; i++) {
                String url = queue.poll();
                futuresQueue.add(executor.submit(new CrawlerTask(url, context)));  // new thread created here
                context.addVisitedUrlCrossLevels(url);
            }

            int found = processFutures(futuresQueue, queue);

            System.out.println("--- found " + found + " urls in depth " + context.getCurrentDepth());

            context.clearUrlsVisitedThisLevel();
            context.incrementCurrentDepth();
        }

        executor.shutdown();
    }

    /**
     * Processes a queue of Futures, each representing a list of URLs found by crawling a single URL.
     * If a future is not yet complete, it re-queues it for later processing.
     *
     * @param futuresQueue the queue of Futures to process
     * @param queue        the main queue to add found URLs for further crawling
     * @return the number of URLs found during this level of the crawl
     */
    private static int processFutures(Queue<Future<List<String>>> futuresQueue, Queue<String> queue) {
        int found = 0;
        while (!futuresQueue.isEmpty()) {
            Future<List<String>> future = futuresQueue.poll();  // Get and remove the first future in the queue

            if (future.isDone()) {
                try {
                    List<String> foundUrls = future.get();
                    for (String newUrl : foundUrls) {
                        queue.add(newUrl);  // Add the URLs to the queue for the next depth level
                        found++;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                // Re-add unfinished futures to the end of the queue
                futuresQueue.add(future);
            }
        }
        return found;
    }
}


// questions -
//             if i found a link i found before, should i explore it?
//
//             social media links ok?

//              too many request on a server, should i care?

//


