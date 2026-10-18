package dev.playwright.context;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
@Service
public class RelevanceSettingsService {
    private final RelevanceSettings defaults;
    private final RelevanceOverrideRepository repository;
    public RelevanceSettingsService(RelevanceOverrideRepository repository) throws IOException {
        this.repository = repository;
        try (var in = new ClassPathResource("relevance-defaults.yml").getInputStream()) {
            defaults = new ObjectMapper(new YAMLFactory()).readValue(in, RelevanceSettings.class);
        }
    }
    public Snapshot current() {
        var override = repository.findById("global");
        if (override.isEmpty()) return new Snapshot(defaults,"defaults",0);
        var o=override.get();
        return new Snapshot(merge(defaults,o),"mongo-override",o.revision==null?0:o.revision);
    }
    public RelevanceSettings defaults() { return defaults; }
    public record Snapshot(RelevanceSettings settings,String source,long revision) {}
    public static RelevanceSettings merge(RelevanceSettings d, RelevanceOverride o) {
        return new RelevanceSettings(
            o.primaryTagWeight==null?d.primaryTagWeight():o.primaryTagWeight,
            o.secondaryTagWeight==null?d.secondaryTagWeight():o.secondaryTagWeight,
            o.crossDimensionBonus==null?d.crossDimensionBonus():o.crossDimensionBonus,
            o.directConnectionBonus==null?d.directConnectionBonus():o.directConnectionBonus,
            o.secondHopBonus==null?d.secondHopBonus():o.secondHopBonus,
            o.maxDepth==null?d.maxDepth():o.maxDepth,
            o.maxCandidates==null?d.maxCandidates():o.maxCandidates,
            o.maxEvents==null?d.maxEvents():o.maxEvents,
            o.defaultTokenBudget==null?d.defaultTokenBudget():o.defaultTokenBudget);
    }
}
