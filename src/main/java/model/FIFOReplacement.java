package model;

import java.util.LinkedList;
import java.util.Queue;

public class FIFOReplacement implements ReplacementAlgorithm {
    private Queue<Integer> pageQueue; // stores VPNs in the order they were added

    public FIFOReplacement() {
        this.pageQueue = new LinkedList<>();
    }

    @Override
    public int evictPage() {
        Integer evictedPage = pageQueue.poll(); // remove and return the first inserted page
        if (evictedPage == null) {
            throw new IllegalStateException("No pages to evict, the queue is empty.");
        }
        return evictedPage; // Ensure to return the evicted page
    }

    public void addPage(int vpn) {
        pageQueue.add(vpn); // add new page to the end of the queue
    }
}
