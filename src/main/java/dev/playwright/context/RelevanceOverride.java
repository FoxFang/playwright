package dev.playwright.context;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
@Document("relevance_overrides")
public class RelevanceOverride {
    @Id public String id;
    @Version public Long revision;
    public Double primaryTagWeight, secondaryTagWeight, crossDimensionBonus;
    public Double directConnectionBonus, secondHopBonus;
    public Integer maxDepth, maxCandidates, maxEvents, defaultTokenBudget;
    public RelevanceOverride() {}
}
