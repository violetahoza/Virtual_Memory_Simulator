package model;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUReplacement implements ReplacementAlgorithm {
    private final Map<Integer, Integer> pageMap; // stores VPN with access-order

    public LRUReplacement(int capacity) {
        this.pageMap = new LinkedHashMap<>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                return size() > capacity; // remove oldest entry if over capacity
            }
        };
    }

    @Override
    public int evictPage() {
        int oldestVpn = pageMap.keySet().iterator().next(); // get the least recently used VPN
        pageMap.remove(oldestVpn);
        return oldestVpn;
    }

    public void addPage(int vpn) {
        pageMap.put(vpn, 1);
    }
}
