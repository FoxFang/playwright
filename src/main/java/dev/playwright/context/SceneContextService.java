package dev.playwright.context;
import java.util.*;
import dev.playwright.canon.UniverseRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
/** Transport-neutral context assembly entrypoint for REST and future MCP adapters. */
@Service
public class SceneContextService {
    private final UniverseRepository universes;
    private final CanonEventRepository events;
    private final RelevanceSettingsService settings;
    private final ContextRanker ranker;
    public SceneContextService(UniverseRepository universes,CanonEventRepository events,
                               RelevanceSettingsService settings,ContextRanker ranker) {
        this.universes=universes; this.events=events; this.settings=settings; this.ranker=ranker;
    }
    public record ContextEvent(String id,String title,String summary,String worldTime,
                               List<String> participants,String source) {}
    public record ContextPackage(String schemaVersion,String universeId,List<ContextEvent> events,
                                 List<String> unresolvedReferences) {}
    public ContextPackage resolve(String universeId,String primary,List<String> secondary) {
        if(!universes.existsById(universeId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Universe not found");
        var others=secondary==null?List.<String>of():secondary;
        if(others.size()>3 || others.contains(primary) || others.stream().anyMatch(Objects::isNull) ||
                new HashSet<>(others).size()!=others.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Up to three unique secondary tags");
        try {
            ContextRanker.DimensionValue.parse(primary);
            others.forEach(ContextRanker.DimensionValue::parse);
        } catch(IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,e.getMessage(),e);
        }
        var config=settings.current().settings();
        // Deterministic broad retrieval before in-memory dimensional ranking.
        var candidates=events.findByUniverseId(universeId,
            PageRequest.of(0,config.maxCandidates(),Sort.by("id").ascending()));
        var selected=ranker.rank(candidates,primary,others,config).stream()
            .limit(config.maxEvents())
            .map(r->new ContextEvent(r.event().id(),r.event().title(),
                r.event().summary(),r.event().worldTime(),r.event().participants(),r.event().source()))
            .toList();
        return new ContextPackage("1.0",universeId,selected,List.of());
    }
}
