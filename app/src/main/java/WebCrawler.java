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
     * @param context  the context object holding crawler state
     * @param executor the executor service for managing crawl tasks
     */
    public WebCrawler(CrawlerContext context, ExecutorService executor) {
        this.context = context;
        this.executor = executor;
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
            System.out.println("Searching depth " + context.getCurrentDepth() + "...");
            int levelSize = queue.size();
            Queue<Future<List<String>>> futuresQueue = new LinkedBlockingQueue<>();

            for (int i = 0; i < levelSize; i++) {
                String url = queue.poll();
                futuresQueue.add(submitCrawlTask(url));
                context.addVisitedUrlCrossLevels(url);
            }

            int found = processFutures(futuresQueue, queue);
            context.updateContext(found);
        }
        executor.shutdown();
        context.printMetadata();
    }

    /**
     * Submits a crawl task for a given URL to the executor service.
     *
     * @param url the URL to crawl
     * @return a Future representing the task result
     */
    private Future<List<String>> submitCrawlTask(String url) {
        return executor.submit(new CrawlerTask(url, context));  // new thread created here
    }

    /**
     * Processes a queue of Futures, each representing a list of URLs found by crawling a single URL.
     * If a future is not yet complete, it re-queues it for later processing.
     *
     * @param futuresQueue the queue of Futures to process
     * @param queue        the main queue to add found URLs for further crawling
     * @return the number of URLs found during this level of the crawl
     */
    private int processFutures(Queue<Future<List<String>>> futuresQueue, Queue<String> queue) {
        int found = 0;
        while (!futuresQueue.isEmpty()) {
            Future<List<String>> future = futuresQueue.poll();

            if (future.isDone()) {
                try {
                    List<String> foundUrls = future.get();
                    for (String newUrl : foundUrls) {
                        queue.add(newUrl);
                        found++;
                    }

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                futuresQueue.add(future);
            }
        }
        return found;
    }
}
