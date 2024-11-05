package model;

public interface ReplacementAlgorithm {
    int evictPage(); // this method will decide which page to evict
    void addPage(int vpn);
}
