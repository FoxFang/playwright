package dev.playwright.canon;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/universes")
public class CanonController {
    private final CanonService service;
    public CanonController(CanonService service) { this.service=service; }
    public record CreateUniverse(@NotBlank String name) {}
    public record CreateEntity(@NotBlank String kind, @NotBlank String name, @NotNull Map<String,String> facts) {}
    public record ReviseEntity(@Min(0) long expectedRevision, @NotBlank String source,
                               @NotBlank String reason, @NotEmpty Map<String,String> facts) {}
    public record SceneRequest(@NotNull List<@NotBlank String> entityIds) {}
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Universe createUniverse(@Valid @RequestBody CreateUniverse request) {
        return service.createUniverse(request.name());
    }
    @PostMapping("/{universeId}/entities") @ResponseStatus(HttpStatus.CREATED)
    public CanonEntity createEntity(@PathVariable String universeId,@Valid @RequestBody CreateEntity request) {
        return service.createEntity(universeId,request.kind(),request.name(),request.facts());
    }
    @GetMapping("/{universeId}/entities/{entityId}")
    public CanonEntity get(@PathVariable String universeId,@PathVariable String entityId) {
        return service.get(universeId,entityId);
    }
    @PostMapping("/{universeId}/entities/{entityId}/revisions")
    public CanonEntity revise(@PathVariable String universeId,@PathVariable String entityId,
                              @Valid @RequestBody ReviseEntity request) {
        return service.revise(universeId,entityId,request.expectedRevision(),
                 request.source(),request.reason(),request.facts());
    }
    @PostMapping("/{universeId}/scene-context")
    public List<CanonEntity> context(@PathVariable String universeId,@Valid @RequestBody SceneRequest request) {
        return service.scene(universeId,request.entityIds());
    }
}
