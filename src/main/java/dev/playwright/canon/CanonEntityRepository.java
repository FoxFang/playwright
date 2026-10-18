package dev.playwright.canon;
import dev.playwright.canon.model.CanonEntity;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface CanonEntityRepository extends MongoRepository<CanonEntity,String> {
    Optional<CanonEntity> findByIdAndUniverseId(String id, String universeId);
}
