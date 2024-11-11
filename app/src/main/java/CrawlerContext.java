package main.java;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class holds the crawler state and needed information for the crawl task.
 */   // NOTE: I could have turned this into  a singleton. this has pros and cons. main pro: no need to
// inject this everywhere. main con: only 1 web crawler can exist for a program, which limits the
// scalability greatly. I chose to leave it as is.
public class CrawlerContext {
    public final Object lock = new Object();   // will be used for synchronization

    private final Set<String> visitedUrlsCrossLevel;  // Shared set of visited URLs
    private final Set<String> visitedUrlsCurrentLevel;  // Shared set of visited URLs
    private final int maxUrlsPerPage;
    private final int maxDepth;
    private int currentDepth;
    private final boolean urlUniqueness;
    private final int maximumLinksToFind;
    private int linksFound;


    public CrawlerContext(int maxUrlsPerLevel, int maxDepth, boolean urlUniqueness) {
        // thread safe version of set
        this.visitedUrlsCrossLevel = Collections.synchronizedSet(new HashSet<>());
        this.visitedUrlsCurrentLevel = Collections.synchronizedSet(new HashSet<>());

        this.maxUrlsPerPage = maxUrlsPerLevel;
        this.maxDepth = maxDepth;
        this.currentDepth = 0;
        this.urlUniqueness = urlUniqueness;
        this.maximumLinksToFind = getMaximumLinksToFind();
        this.linksFound = 1;

    }

    public boolean isUniqueFlagActive() {
        return this.urlUniqueness;
    }

    public int getMaxUrlsPerPage() {
        return this.maxUrlsPerPage;
    }

    public boolean isUrlVisitedCrossLevels(String url) {
        return this.visitedUrlsCrossLevel.contains(url);
    }

    public void addVisitedUrlCrossLevels(String url) {
        this.visitedUrlsCrossLevel.add(url);
    }

    public boolean isUrlVisitedCurrentLevel(String url) {
        return this.visitedUrlsCurrentLevel.contains(url);
    }

    public void addVisitedUrlCurrentLevel(String url) {
        this.visitedUrlsCurrentLevel.add(url);  // Thread-safe addition to visited URLs
    }

    public int getCurrentDepth() {
        return this.currentDepth;
    }



    public boolean reachedMaxDepth() {
        return this.currentDepth > this.maxDepth;
    }

    public boolean reachedLastDepth() {
        return this.currentDepth == this.maxDepth;
    }

    /**
     * At the end of execution this will print some useful data about the run.
     */
    public void printMetadata() {
        double percentageLinksFound = ((double) this.linksFound / this.maximumLinksToFind) * 100;

        System.out.println("=== Crawler Metadata ===");
        System.out.println("Total links found: " + this.linksFound + " out of " + this.maximumLinksToFind);
        System.out.printf("Percentage of maximum links found: %.2f%%%n", percentageLinksFound);
        System.out.println("URL uniqueness enforced: " + this.urlUniqueness);
        System.out.println("Current depth reached: " + --this.currentDepth + " out of maximum depth " + this.maxDepth);
        System.out.println("Total unique URLs visited (cross levels): " + this.visitedUrlsCrossLevel.size());
        System.out.println("=========================");
    }

    /**
     * After finishing exploring a single depth of the crawl, we update the context for the next depth;
     * @param found
     */
    public void updateContext(int found) {
        linksFound+= found;
        this.visitedUrlsCurrentLevel.clear();
        this.currentDepth++;

    }

    private int getMaximumLinksToFind() {
        if (this.maxUrlsPerPage == 1) {   // to avoid zero division.
            return this.maxDepth + 1;
        }
        return (int) ((Math.pow(this.maxUrlsPerPage, this.maxDepth + 1) - 1) / (this.maxUrlsPerPage - 1));
    }



}
