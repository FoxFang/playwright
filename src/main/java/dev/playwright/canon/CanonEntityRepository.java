package dev.playwright.canon;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface CanonEntityRepository extends MongoRepository<CanonEntity,String> {
    Optional<CanonEntity> findByIdAndUniverseId(String id, String universeId);
}
