package dev.playwright.context;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Disabled unless PLAYWRIGHT_ADMIN_TOKEN is explicitly set.
 * Run behind loopback/TLS; header tokens must never be logged.
 */
@RestController
@RequestMapping("/api/v1/admin/relevance-config")
public class RelevanceAdminController {
    private final RelevanceSettingsService settings;
    private final RelevanceOverrideRepository repository;
    private final String token;
    public RelevanceAdminController(RelevanceSettingsService settings, RelevanceOverrideRepository repository,
            @org.springframework.beans.factory.annotation.Value("${PLAYWRIGHT_ADMIN_TOKEN:}") String token) {
        this.settings=settings; this.repository=repository; this.token=token;
    }
    private void authorize(String supplied) {
        if (token.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "Admin API disabled");
        var actual=(supplied==null?"":supplied).getBytes(StandardCharsets.UTF_8);
        var expected=token.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, actual))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid admin credential");
    }
    @GetMapping
    public RelevanceSettingsService.Snapshot get(@RequestHeader(value="X-Playwright-Admin-Token",required=false) String supplied) {
        authorize(supplied); return settings.current();
    }
    public record Update(Long expectedRevision,RelevanceOverride overrides) {}
    @PutMapping
    public RelevanceSettingsService.Snapshot update(
            @RequestHeader(value="X-Playwright-Admin-Token",required=false) String supplied,
            @RequestBody Update request) {
        authorize(supplied);
        if (request==null || request.overrides()==null || request.expectedRevision()==null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Expected revision and overrides required");
        var old=repository.findById("global");
        long actual=old.map(o->o.revision==null?0L:o.revision).orElse(0L);
        if (actual!=request.expectedRevision())
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Stale configuration revision");
        var next=request.overrides();
        next.id="global";
        next.revision=old.map(o->o.revision).orElse(null);
        try {
            RelevanceSettingsService.merge(settings.defaults(),next);
            repository.save(next);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid relevance configuration",ex);
        } catch (OptimisticLockingFailureException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Concurrent configuration update",ex);
        }
        return settings.current();
    }
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@RequestHeader(value="X-Playwright-Admin-Token",required=false) String supplied) {
        authorize(supplied);
        repository.deleteById("global");
    }
}
