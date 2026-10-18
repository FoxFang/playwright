package dev.playwright.context;
import dev.playwright.context.model.RelevanceOverride;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface RelevanceOverrideRepository extends MongoRepository<RelevanceOverride,String> {}
