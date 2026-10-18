package dev.playwright.context;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface CanonEventRepository extends MongoRepository<CanonEvent,String> {
    List<CanonEvent> findByUniverseIdAndTagsIn(String universeId,List<String> tags,Pageable page);
}
