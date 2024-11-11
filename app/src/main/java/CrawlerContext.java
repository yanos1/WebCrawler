package main.java;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class holds the information needed for the crawl task.
 */
public class CrawlerContext {

    private final Set<String> visitedUrlsCrossLevel;  // Shared set of visited URLs
    private final Set<String> visitedUrlsThisLevel;  // Shared set of visited URLs
    private final int maxUrlsPerLevel;
    private final int maxDepth;
    private int currentDepth;
    private final boolean urlUniqueness;
    private final int maximumLinksToFind;
    private int linksFound;


    public CrawlerContext(int maxUrlsPerLevel, int maxDepth, boolean urlUniqueness) {
        // thread safe version of set
        this.visitedUrlsCrossLevel = Collections.synchronizedSet(new HashSet<>());
        this.visitedUrlsThisLevel = Collections.synchronizedSet(new HashSet<>());

        this.maxUrlsPerLevel = maxUrlsPerLevel;
        this.maxDepth = maxDepth;
        this.currentDepth = 0;
        this.urlUniqueness = urlUniqueness;
        this.maximumLinksToFind = getMaximumLinksToFind();
        this.linksFound = 1;

    }

    public boolean isUniqueFlagActive() {
        return this.urlUniqueness;
    }

    public int getMaxUrlsPerLevel() {
        return maxUrlsPerLevel;
    }

    public boolean isUrlVisitedCrossLevels(String url) {
        return visitedUrlsCrossLevel.contains(url);
    }

    public void addVisitedUrlCrossLevels(String url) {
        visitedUrlsCrossLevel.add(url);
    }

    public boolean isUrlVisitedThisLevel(String url) {
        return visitedUrlsThisLevel.contains(url);
    }

    public void addVisitedUrlThisLevel(String url) {
        visitedUrlsThisLevel.add(url);  // Thread-safe addition to visited URLs
    }

    public int getCurrentDepth() {
        return this.currentDepth;
    }


    private void clearUrlsVisitedThisLevel() {
        visitedUrlsThisLevel.clear();
    }

    public boolean reachedMaxDepth() {
        return this.currentDepth > this.maxDepth;
    }

    public boolean reachedLastDepth() {
        return this.currentDepth == this.maxDepth;
    }


    public void printMetadata() {
        double percentageLinksFound = ((double) linksFound / maximumLinksToFind) * 100;

        System.out.println("=== Crawler Metadata ===");
        System.out.println("Total links found: " + linksFound + " out of " + maximumLinksToFind);
        System.out.printf("Percentage of maximum links found: %.2f%%%n", percentageLinksFound);
        System.out.println("URL uniqueness enforced: " + urlUniqueness);
        System.out.println("Current depth reached: " + --currentDepth + " out of maximum depth " + maxDepth);
        System.out.println("Total unique URLs visited (cross levels): " + visitedUrlsCrossLevel.size());
        System.out.println("=========================");
    }

    public void updateContext(int found) {
        incrementLinksFound(found);
        clearUrlsVisitedThisLevel();
        incrementCurrentDepth();
    }


    private int getMaximumLinksToFind() {
        if (this.maxUrlsPerLevel == 1) {   // to avoid zero division.
            return this.maxDepth + 1;
        }
        return (int) ((Math.pow(this.maxUrlsPerLevel, this.maxDepth + 1) - 1) / (this.maxUrlsPerLevel - 1));
    }

    private void incrementLinksFound(int amount) {
        linksFound += amount;
    }

    private void incrementCurrentDepth() {
        this.currentDepth++;
    }

}
