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

    public CrawlerContext(int maxUrlsPerLevel, int maxDepth, boolean urlUniqueness) {
        this.visitedUrlsCrossLevel = Collections.synchronizedSet(new HashSet<>());
        this.visitedUrlsThisLevel = Collections.synchronizedSet(new HashSet<>());
        this.maxUrlsPerLevel = maxUrlsPerLevel;
        this.maxDepth = maxDepth;
        this.currentDepth = 0;
        this.urlUniqueness = urlUniqueness;
    }

    public boolean isUniqueFlagActive() {
        return this.urlUniqueness;
    }

    public Set<String> getVisitedUrlsCrossLevel() {
        return visitedUrlsCrossLevel;
    }

    public int getMaxUrlsPerLevel() {
        return maxUrlsPerLevel;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public boolean isUrlVisitedCrossLevels(String url) {
        return visitedUrlsCrossLevel.contains(url);
    }
    public boolean addVisitedUrlCrossLevels(String url) {
        return visitedUrlsCrossLevel.add(url);  // Thread-safe addition to visited URLs
    }
    public boolean isUrlVisitedThisLevel(String url) {
        return visitedUrlsThisLevel.contains(url);
    }
    public boolean addVisitedUrlThisLevel(String url) {
        return visitedUrlsThisLevel.add(url);  // Thread-safe addition to visited URLs
    }

    public int getCurrentDepth() {
        return this.currentDepth;
    }

    public void incrementCurrentDepth() {
        this.currentDepth++;
    }
    public void clearUrlsVisitedThisLevel() {
        visitedUrlsThisLevel.clear();
    }

    public boolean reachedMaxDepth() {
        return this.currentDepth > this.maxDepth;
    }
}
