package dev.playwright.context;
import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document("canon_events")
public record CanonEvent(@Id String id, String universeId, String title,
    String summary, List<String> tags, List<String> participants,
    Instant recordedAt, String worldTime, String source) {}
