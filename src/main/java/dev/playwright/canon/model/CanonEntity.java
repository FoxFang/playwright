package dev.playwright.canon.model;
import java.util.*;
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
    public Map<String,List<String>> metadata;
    public List<CanonChange> history;
    @Version public Long revision;
    protected CanonEntity() {}
    public CanonEntity(String id,String universeId,String kind,String name,Map<String,String> facts) {
        this(id,universeId,kind,name,facts,Map.of());
    }
    public CanonEntity(String id,String universeId,String kind,String name,
                       Map<String,String> facts,Map<String,List<String>> metadata) {
        this.id=id; this.universeId=universeId; this.kind=kind; this.name=name;
        this.facts=Map.copyOf(facts);
        this.metadata=copyMetadata(metadata);
        this.history=new ArrayList<>();
    }
    public static Map<String,List<String>> copyMetadata(Map<String,List<String>> metadata) {
        if(metadata==null) return Map.of();
        Map<String,List<String>> copy=new TreeMap<>();
        metadata.forEach((dimension,values)->{
            if(dimension==null || !dimension.matches("[a-z][a-z0-9_-]*") ||
               values==null || values.isEmpty() || values.stream().anyMatch(v->v==null || v.isBlank()))
                throw new IllegalArgumentException("Invalid metadata dimension or value");
            copy.put(dimension,List.copyOf(new LinkedHashSet<>(values)));
        });
        return Collections.unmodifiableMap(copy);
    }
}
