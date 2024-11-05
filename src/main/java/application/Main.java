package application;

import model.FIFOReplacement;
import model.MemoryManager;
import model.ReplacementAlgorithm;
import model.Results;

public class Main {
    public static void main(String[] args) {
        int tlbSize = 2;
        int pageTableSize = 4; // Allows tracking of 4 virtual pages
        int nrFrames = 2; // Only 2 physical frames, forcing eviction
        int maxDiskPages = 5; // Secondary storage can hold 5 pages
        int pageSize = 64; // Size of each page
        int virtualAddressWidth = 10; // 10-bit virtual address space

        ReplacementAlgorithm replacementAlgorithm = new FIFOReplacement();

        // Initialize Memory Manager with FIFO Replacement
        MemoryManager memoryManager = new MemoryManager(virtualAddressWidth, tlbSize, pageTableSize, nrFrames, maxDiskPages, pageSize, null);

        System.out.println("Virtual Memory Size: " + memoryManager.getVirtualMemorySize() + " bytes");

        // Access a sequence of virtual addresses to demonstrate eviction
        memoryManager.load(128);   // Load virtual address 128 (page fault)
        memoryManager.store(128, 42); // Store value 42 at virtual address 128 (dirty)

        memoryManager.load(192); // Load virtual address 192 (page fault, since it's not in memory)
        memoryManager.store(192, 12); // Store value 12 at virtual address 192 (dirty)

        // At this point, the memory should be full (with pages 128 and 192 loaded).
        // The next load should cause eviction.
        memoryManager.load(256);   // Load virtual address 256 (page fault, triggers eviction)
        memoryManager.store(256, 84); // Store value 84 at virtual address 256 (dirty)

        // Accessing the previously stored addresses again to see TLB behavior and possibly evictions
        memoryManager.load(128);    // Load virtual address 128 again (TLB hit)
        memoryManager.load(192);    // Load virtual address 192 again (should now trigger eviction since 256 is dirty)

        // Finally load address 256 to check if it was evicted properly
        memoryManager.load(256);    // Check if page 256 is present and can be loaded (should also trigger a TLB hit if correctly managed)

        // Print contents of memory, TLB, page table, and secondary storage
        memoryManager.printMemoryContents();

        // Display simulation results
        System.out.println("Simulation Results:");
        System.out.println("TLB hits: " + Results.tlbHit);
        System.out.println("TLB misses: " + Results.tlbMiss);
        System.out.println("Page table hits: " + Results.pageTableHit);
        System.out.println("Page table misses: " + Results.pageTableMiss);
        System.out.println("Disk reads: " + Results.diskRead);
        System.out.println("Disk writes: " + Results.diskWrite);
        System.out.println("Pages evicted: " + Results.pageEviction);
    }
}
