package dev.playwright.context;
import dev.playwright.context.model.CanonEvent;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface CanonEventRepository extends MongoRepository<CanonEvent,String> {
    // Broad universe-scoped pool. In-memory scoring remains transport-independent.
    List<CanonEvent> findByUniverseId(String universeId,Pageable page);
}
