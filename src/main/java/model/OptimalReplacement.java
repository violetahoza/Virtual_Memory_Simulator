package model;

import java.util.List;
import java.util.Map;

public class OptimalReplacement implements ReplacementAlgorithm {
    private final Map<Integer, List<Integer>> futureReferences; // map VPN to future access points
    private int currentStep = 0;

    public OptimalReplacement(Map<Integer, List<Integer>> futureReferences) {
        this.futureReferences = futureReferences;
    }

    @Override
    public int evictPage() {
        int vpnToEvict = -1;
        int furthestUse = -1;

        for (Map.Entry<Integer, List<Integer>> entry : futureReferences.entrySet()) {
            List<Integer> accessTimes = entry.getValue();
            int nextUse = accessTimes.stream().filter(time -> time > currentStep).findFirst().orElse(Integer.MAX_VALUE);

            if (nextUse > furthestUse) {
                furthestUse = nextUse;
                vpnToEvict = entry.getKey();
            }
        }
        currentStep++;
        return vpnToEvict;
    }

    public void updateStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public void addPage(int vpn) {
    }
}
