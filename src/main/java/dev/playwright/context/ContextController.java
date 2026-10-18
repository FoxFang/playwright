package dev.playwright.context;
import dev.playwright.context.model.CanonEvent;
import java.time.Clock;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import dev.playwright.canon.UniverseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/universes/{universeId}")
public class ContextController {
    private final CanonEventRepository events;
    private final UniverseRepository universes;
    private final SceneContextService sceneContext;
    private final Clock clock=Clock.systemUTC();
    public ContextController(CanonEventRepository events, UniverseRepository universes,
                             SceneContextService sceneContext) {
        this.events=events; this.universes=universes; this.sceneContext=sceneContext;
    }
    public record CreateEvent(@NotBlank String title,@NotBlank String summary,
        @NotEmpty Map<String,List<String>> metadata, List<String> participants,
        String worldTime,@NotBlank String source) {}
    public record ContextQuery(@NotBlank String primary,List<String> secondary) {}
    @PostMapping("/events") @ResponseStatus(HttpStatus.CREATED)
    public CanonEvent create(@PathVariable String universeId,@Valid @RequestBody CreateEvent body) {
        if(!universes.existsById(universeId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Universe not found");
        try {
            return events.save(new CanonEvent(UUID.randomUUID().toString(),universeId,body.title(),
                body.summary(),body.metadata(),
                body.participants(),clock.instant(),body.worldTime(),body.source()));
        } catch(IllegalArgumentException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,error.getMessage(),error);
        }
    }
    @PostMapping("/context/resolve")
    public SceneContextService.ContextPackage resolve(
            @PathVariable String universeId,@Valid @RequestBody ContextQuery query) {
        return sceneContext.resolve(universeId,query.primary(),query.secondary());
    }
}
