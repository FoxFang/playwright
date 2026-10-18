package dev.playwright.context;
public record RelevanceSettings(
    double primaryTagWeight, double secondaryTagWeight, double crossDimensionBonus,
    double directConnectionBonus, double secondHopBonus,
    int maxDepth, int maxCandidates, int maxEvents, int defaultTokenBudget) {
    public RelevanceSettings {
        for (double value : new double[] {primaryTagWeight, secondaryTagWeight,
                crossDimensionBonus, directConnectionBonus, secondHopBonus}) {
            if (!Double.isFinite(value) || value < 0 || value > 1000)
                throw new IllegalArgumentException("Invalid weight");
        }
        if (maxDepth < 0 || maxDepth > 2 || maxCandidates < 1 || maxCandidates > 10000 ||
            maxEvents < 0 || maxEvents > 1000 || defaultTokenBudget < 256 ||
            defaultTokenBudget > 100000)
            throw new IllegalArgumentException("Invalid retrieval limit");
    }
}
