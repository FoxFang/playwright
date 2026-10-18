package dev.playwright.context;
import java.time.Clock;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.PageRequest;
import dev.playwright.canon.UniverseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController
@RequestMapping("/api/v1/universes/{universeId}")
public class ContextController {
    private final CanonEventRepository events;
    private final UniverseRepository universes;
    private final RelevanceSettingsService settings;
    private final ContextRanker ranker;
    private final Clock clock=Clock.systemUTC();
    public ContextController(CanonEventRepository events,UniverseRepository universes,RelevanceSettingsService settings,ContextRanker ranker) {
        this.events=events; this.universes=universes; this.settings=settings; this.ranker=ranker;
    }
    public record CreateEvent(@NotBlank String title,@NotBlank String summary,
        @NotEmpty List<@NotBlank String> tags, List<String> participants,
        String worldTime,@NotBlank String source) {}
    public record ContextQuery(@NotBlank String primary,List<String> secondary) {}
    public record ContextEvent(String id,String title,String summary,String worldTime,
                               List<String> participants,String source) {}
    public record ContextPackage(String schemaVersion,String universeId,List<ContextEvent> events,
                                 List<String> unresolvedReferences) {}
    @PostMapping("/events") @ResponseStatus(HttpStatus.CREATED)
    public CanonEvent create(@PathVariable String universeId,@Valid @RequestBody CreateEvent body) {
        requireUniverse(universeId);
        return events.save(new CanonEvent(UUID.randomUUID().toString(),universeId,body.title(),
            body.summary(),List.copyOf(body.tags()),
            body.participants()==null?List.of():List.copyOf(body.participants()),
            clock.instant(),body.worldTime(),body.source()));
    }
    private void requireUniverse(String id) {
        if (!universes.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Universe not found");
    }
    @PostMapping("/context/resolve")
    public ContextPackage resolve(@PathVariable String universeId,@Valid @RequestBody ContextQuery query) {
        requireUniverse(universeId);
        var secondary=query.secondary()==null?List.<String>of():query.secondary();
        if(secondary.size()>3 || secondary.contains(query.primary()) ||
           new HashSet<>(secondary).size()!=secondary.size())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Up to three unique secondary tags");
        var config=settings.current().settings();
        var tags=new ArrayList<String>(); tags.add(query.primary()); tags.addAll(secondary);
        var candidates=events.findByUniverseIdAndTagsIn(universeId,tags,
            PageRequest.of(0,config.maxCandidates()));
        var selected=ranker.rank(candidates,query.primary(),secondary,config).stream()
            .limit(config.maxEvents()).map(r->new ContextEvent(r.event().id(),r.event().title(),
                r.event().summary(),r.event().worldTime(),r.event().participants(),
                r.event().source())).toList();
        return new ContextPackage("1.0",universeId,selected,List.of());
    }
}
