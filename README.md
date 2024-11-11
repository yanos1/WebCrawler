# WebCrawler

## Overview

The **WebCrawler** is a multithreaded web crawler that performs a search for URLs and downloads their HTML contents. The program uses **Breadth-First Search (BFS)** to explore the web and supports **multithreading** for efficient crawling.

## Usage

To run the WebCrawler, you need to provide four arguments:

1. **Start URL**: The URL to start crawling from.
2. **Max URLs per page**: The maximum number of URLs to crawl on each page. (integer > 0)
3. **Max Depth**: The maximum depth of crawling (how many levels deep from the start URL). (integer >= 0)
4. **Cross-level URL Uniqueness**: A boolean value (`true` or `false`) to specify if URLs across levels should be considered unique.


