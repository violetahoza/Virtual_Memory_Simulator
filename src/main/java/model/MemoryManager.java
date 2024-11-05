package model;

import utils.LogResults;

public class MemoryManager {
    private TLB tlb;
    private PageTable pageTable;
    private MainMemory mainMemory;
    private SecondaryStorage secondaryStorage;
    private ReplacementAlgorithm replacementAlgorithm;
    private int pageSize, virtualAddressWidth, virtualMemorySize;

    public MemoryManager(int virtualAddressWidth, int tlbSize, int pageTableSize, int nrFrames, int maxDiskPages, int pageSize, ReplacementAlgorithm replacementAlgorithm) {
        this.tlb = new TLB(tlbSize);
        this.pageTable = new PageTable(pageTableSize);
        this.mainMemory = new MainMemory(nrFrames, pageSize);
        this.secondaryStorage = new SecondaryStorage(maxDiskPages, pageSize);
        this.replacementAlgorithm = replacementAlgorithm;
        this.pageSize = pageSize;
        this.virtualAddressWidth = virtualAddressWidth;
        this.virtualMemorySize = (int) Math.pow(2, virtualAddressWidth);
    }

    public int getVirtualMemorySize() {
        return virtualMemorySize;
    }

    public void load(int virtualAddress) {
        int vpn = virtualAddress / pageSize;
        int offset = virtualAddress % pageSize;

        Address virtualAddr = new Address(vpn, offset);
        LogResults.log("Access request for virtual address: " + virtualAddr.printAddress("Virtual"));

        // TLB lookup
        int ppn = tlb.lookup(vpn);
        if (ppn != -1) {
            LogResults.log("TLB hit! Physical page number: " + ppn);
            Results.tlbHit++;
            loadFromMemory(new Address(ppn, offset));
        } else {
            LogResults.log("TLB miss for virtual page nr: " + vpn);
            Results.tlbMiss++;
            handlePageTableLookup(vpn, offset);
        }
    }
    private void handlePageTableLookup(int vpn, int offset) {
        PageTableEntry entry = pageTable.getEntry(vpn);
        if (entry != null) {
            LogResults.log("Page table hit! Physical page number: " + entry.getFrameNumber() + " for virtual page number: " + vpn);
            Results.pageTableHit++;
            tlb.addEntry(vpn, entry);
            loadFromMemory(new Address(entry.getFrameNumber(), offset));
        } else {
            LogResults.log("Page table miss for virtual page number: " + vpn);
            Results.pageTableMiss++;
            handlePageFault(vpn);
            loadFromMemory(new Address(pageTable.getPhysicalPageNumber(vpn), offset));
        }
    }

    public void store(int virtualAddress, int data) {
        int vpn = virtualAddress / pageSize;
        int offset = virtualAddress % pageSize;

        Address virtualAddr = new Address(vpn, offset);
        LogResults.log("Storing data to: " + virtualAddr.printAddress("Virtual") + " . Data stored: " + data);

        // TLB lookup
        int ppn = tlb.lookup(vpn);
        if (ppn != -1) {
            LogResults.log("TLB hit! Physical page number: " + ppn);
            Results.tlbHit++;
            storeToMemory(new Address(ppn, offset), data);
        } else {
            LogResults.log("TLB miss for virtual page number: " + vpn);
            Results.tlbMiss++;
            handlePageTableLookupForStore(vpn, offset, data);
        }
    }
    private void handlePageTableLookupForStore(int vpn, int offset, int data) {
        PageTableEntry entry = pageTable.getEntry(vpn);
        if (entry != null) {
            LogResults.log("Page table hit! Physical page number: " + entry.getFrameNumber()+ " for virtual page number: " + vpn);
            Results.pageTableHit++;
            tlb.addEntry(vpn, entry);
            storeToMemory(new Address(entry.getFrameNumber(), offset), data);
        } else {
            LogResults.log("Page table miss for virtual page number: " + vpn);
            Results.pageTableMiss++;
            handlePageFault(vpn);
            storeToMemory(new Address(pageTable.getPhysicalPageNumber(vpn), offset), data);
        }
    }

    private void handlePageFault(int vpn) {
        LogResults.log("Page fault for virtual page number: " + vpn + ". Loading page from disk.");

        // Step 1: Load the page from secondary storage
        Page page = secondaryStorage.load(vpn);

        // Step 2: Check if main memory is full and perform eviction if necessary
        if (mainMemory.isFull()) {
            int evictVpn = replacementAlgorithm.evictPage();  // Determine which page to evict
            PageTableEntry evictedEntry = pageTable.getEntry(evictVpn);

            if (evictedEntry != null) {
                LogResults.log("Evicting physical page number: " + evictedEntry.getFrameNumber() + " for virtual page number: " + evictVpn);

                // Step 3: If the evicted page is dirty, write it back to secondary storage
                if (evictedEntry.isDirty()) {
                    LogResults.log("Evicted page is dirty. Writing back to disk.");
                    secondaryStorage.store(evictVpn, mainMemory.getPage(evictedEntry.getFrameNumber()));
                    Results.diskWrite++;  // Increment disk write count
                }

                // Invalidate the evicted page in the page table
                pageTable.setValid(evictVpn, false);
                Results.pageEviction++;  // Increment page eviction count
            }
        }

        // Step 4: Load the new page into main memory
        int newFrame = mainMemory.loadPageIntoMemory(page);
        pageTable.addEntry(vpn, newFrame);  // Add page to page table as valid

        // Step 5: Add the new page to the replacement algorithm's tracking structure
        if (replacementAlgorithm instanceof FIFOReplacement) {
            ((FIFOReplacement) replacementAlgorithm).addPage(vpn); // Add the newly loaded page to FIFO queue
        }

        Results.diskRead++;  // Increment disk read count
        LogResults.log("Loaded virtual page number: " + vpn + " into physical frame number: " + newFrame);

        // Step 6: Add the new page table entry to the TLB
        PageTableEntry newEntry = new PageTableEntry(newFrame, true, false, true);
        tlb.addEntry(vpn, newEntry);  // Update TLB with the new entry
    }

    private void loadFromMemory(Address physicalAddress) {
        int data = mainMemory.load(physicalAddress);
        PageTableEntry entry = pageTable.getEntry(physicalAddress.getPageNumber());
        if (entry != null) {
            entry.setRefBit(true);  // Mark the referenced bit, since the page has been accessed
        }
        LogResults.log("Loaded data: " + data + " from: " + physicalAddress.printAddress("Physical"));
    }

    private void storeToMemory(Address physicalAddress, int data) {
        mainMemory.store(physicalAddress, data);
        PageTableEntry entry = pageTable.getEntry(physicalAddress.getPageNumber());
        if (entry != null) {
            entry.setDirtyBit(true); // Mark as dirty (modified)
            entry.setRefBit(true);  // Mark as referenced
        }
        LogResults.log("Stored data: " + data + " to: " + physicalAddress.printAddress("Physical"));
    }

    public void printMemoryContents() {
        mainMemory.printContents();
        secondaryStorage.printContents();
        tlb.printContents();
        pageTable.printContents();
    }
}
