package dev.playwright.context.model;
import java.time.Instant;
import java.util.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document("canon_events")
public record CanonEvent(@Id String id, String universeId, String title,
    String summary, Map<String,List<String>> metadata, List<String> participants,
    Instant recordedAt, String worldTime, String source) {
    public CanonEvent {
        if (metadata == null || metadata.isEmpty()) throw new IllegalArgumentException("Metadata required");
        Map<String,List<String>> normalized=new TreeMap<>();
        metadata.forEach((dimension,values)->{
            if(dimension==null || !dimension.matches("[a-z][a-z0-9_-]*") ||
                values==null || values.isEmpty() || values.stream().anyMatch(v->v==null || v.isBlank()))
                throw new IllegalArgumentException("Invalid metadata dimension or value");
            normalized.put(dimension,List.copyOf(new LinkedHashSet<>(values)));
        });
        metadata=Collections.unmodifiableMap(normalized);
        participants=participants==null?List.of():List.copyOf(participants);
    }
}
