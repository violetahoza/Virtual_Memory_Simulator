package model;

import utils.LogResults;

import java.util.HashMap;
import java.util.Map;

public class MainMemory {

    private Map<Integer, Page> memory; // Stores pages mapped to frame numbers
    private int nrFrames;              // Total number of frames in memory
    private int lastFrameNr;           // Tracks the next available frame
    private int pageSize;              // Size of each page

    public MainMemory(int nrFrames, int pageSize) {
        this.nrFrames = nrFrames;
        this.pageSize = pageSize;
        this.memory = new HashMap<>(); // Start with an empty memory (no pages loaded)
        this.lastFrameNr = 0;          // Initially, no frames are occupied
    }

    // Loads data from memory for a specific address
    public int load(Address address) {
        Page page = memory.get(address.getPageNumber());
        if (page != null) {
            return page.load(address.getOffset());
        }
        throw new IllegalArgumentException("Invalid physical address: page not found.");
    }

    // Stores data in memory for a specific address
    public void store(Address address, int data) {
        if (address.getOffset() >= pageSize) {
            throw new IllegalArgumentException("Offset exceeds page size.");
        }
        Page page = memory.get(address.getPageNumber());
        if (page != null) {
            page.store(address.getOffset(), data);
            LogResults.log("Stored data at: " + address);
        } else {
            throw new IllegalArgumentException("Invalid physical address: page not found.");
        }
    }

    // Loads a page into memory, assigning it a frame.
    public int loadPageIntoMemory(Page page) {
        if (isFull()) {
            throw new IllegalStateException("Memory is full, cannot load more pages.");
        }
        memory.put(lastFrameNr, page.getCopy());  // Deep copy the page and store it in memory
        return lastFrameNr++;                     // Return frame number and increment lastFrameNr
    }

    // Checks if memory is full
    public boolean isFull() {
        return lastFrameNr >= nrFrames;
    }

    // Retrieve a page at the specified frame number
    public Page getPage(int frameNr) {
        return memory.get(frameNr);
    }

    // Retrieve the full memory contents as a map (frame -> {address -> data})
    public Map<Integer, Map<Integer, Integer>> getMemoryContents() {
        Map<Integer, Map<Integer, Integer>> memoryCopy = new HashMap<>();
        for (Map.Entry<Integer, Page> entry : memory.entrySet()) {
            int frameNumber = entry.getKey();
            Page page = entry.getValue();
            Map<Integer, Integer> pageContents = page.getPageContents();
            Map<Integer, Integer> addressContents = new HashMap<>();

            // Convert page contents to physical addresses
            for (Map.Entry<Integer, Integer> pageEntry : pageContents.entrySet()) {
                int offset = pageEntry.getKey();
                int address = frameNumber * pageSize + offset;
                addressContents.put(address, pageEntry.getValue());
            }
            memoryCopy.put(frameNumber, addressContents);
        }
        return memoryCopy;
    }

    public void printContents() {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Main memory contents (size: ").append(memory.size()).append("):\n");

        for (Map.Entry<Integer, Page> entry : memory.entrySet()) {
            logBuilder.append("Frame ").append(entry.getKey()).append(":\n");
            logBuilder.append(entry.getValue().printContents());
        }
        logBuilder.append("----------------------");
        LogResults.log(logBuilder.toString());
    }
}
