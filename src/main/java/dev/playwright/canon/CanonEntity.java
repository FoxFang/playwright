package dev.playwright.canon;
import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
@Document("canon_entities")
public class CanonEntity {
    @Id public String id;
    public String universeId;
    public String kind;
    public String name;
    public Map<String,String> facts;
    public List<CanonChange> history;
    @Version public Long revision;
    protected CanonEntity() {}
    public CanonEntity(String id, String universeId, String kind, String name, Map<String,String> facts) {
        this.id=id; this.universeId=universeId; this.kind=kind; this.name=name;
        this.facts=Map.copyOf(facts); this.history=new java.util.ArrayList<>();
    }
}
