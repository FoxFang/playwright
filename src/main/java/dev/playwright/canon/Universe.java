package dev.playwright.canon;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document("universes")
public record Universe(@Id String id, String name) {}
