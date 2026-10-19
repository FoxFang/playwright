package dev.playwright.context;

import dev.playwright.context.model.CanonEvent;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/** Restrict candidates in MongoDB before computing scene-specific scores. */
@Component
public class EventCandidateFinder {
    private final MongoTemplate mongo;
    public EventCandidateFinder(MongoTemplate mongo) { this.mongo=mongo; }

    public List<CanonEvent> find(String universeId,
                                List<ContextRanker.DimensionValue> selectors, int limit) {
        if (selectors.isEmpty() || limit < 1)
            throw new IllegalArgumentException("Selectors and positive limit required");
        // OR across selectors: a missing secondary dimension must not exclude a useful event.
        var alternatives=selectors.stream()
            .map(s -> Criteria.where("metadata."+s.dimension()).is(s.value()))
            .toArray(Criteria[]::new);
        Query query=new Query(new Criteria().andOperator(
            Criteria.where("universeId").is(universeId),
            new Criteria().orOperator(alternatives)));
        query.with(Sort.by(Sort.Direction.ASC,"_id")).limit(limit);
        return mongo.find(query,CanonEvent.class);
    }
}
