package dev.playwright.canon;
import dev.playwright.canon.model.CanonChange;
import dev.playwright.canon.model.CanonEntity;
import dev.playwright.canon.model.Universe;
import java.time.Clock;
import java.util.*;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
@Service
public class CanonService {
    private final UniverseRepository universes;
    private final CanonEntityRepository entities;
    private final Clock clock;
    @Autowired
    public CanonService(UniverseRepository universes,CanonEntityRepository entities) {
        this(universes,entities,Clock.systemUTC());
    }
    CanonService(UniverseRepository universes,CanonEntityRepository entities,Clock clock) {
        this.universes=universes; this.entities=entities; this.clock=clock;
    }
    public Universe createUniverse(String name) {
        return universes.save(new Universe(UUID.randomUUID().toString(),name));
    }
    public CanonEntity createEntity(String universeId,String kind,String name,Map<String,String> facts) {
        return createEntity(universeId,kind,name,facts,Map.of());
    }
    public CanonEntity createEntity(String universeId,String kind,String name,Map<String,String> facts,
                                    Map<String,List<String>> metadata) {
        requireUniverse(universeId);
        return entities.save(new CanonEntity(UUID.randomUUID().toString(),universeId,kind,name,facts,metadata));
    }
    public CanonEntity get(String universeId,String entityId) {
        return entities.findByIdAndUniverseId(entityId,universeId)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Entity not found"));
    }
    public List<CanonEntity> scene(String universeId,List<String> entityIds) {
        requireUniverse(universeId);
        if(entityIds.size()>20) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Maximum 20 entities");
        return entityIds.stream().distinct().map(id->get(universeId,id)).toList();
    }
    public CanonEntity revise(String universeId,String entityId,long expectedRevision,
                              String source,String reason,Map<String,String> changes) {
        return revise(universeId,entityId,expectedRevision,source,reason,changes,Map.of());
    }
    public CanonEntity revise(String universeId,String entityId,long expectedRevision,
                              String source,String reason,Map<String,String> changes,
                              Map<String,List<String>> metadataChanges) {
        CanonEntity entity=get(universeId,entityId);
        if(!Objects.equals(entity.revision,expectedRevision))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Revision mismatch");
        var validatedMetadata=CanonEntity.copyMetadata(metadataChanges);
        var newFacts=new LinkedHashMap<>(entity.facts);
        newFacts.putAll(changes);
        var newMetadata=new TreeMap<>(entity.metadata==null?Map.<String,List<String>>of():entity.metadata);
        newMetadata.putAll(validatedMetadata);
        entity.facts=Map.copyOf(newFacts);
        entity.metadata=CanonEntity.copyMetadata(newMetadata);
        var history=new ArrayList<>(entity.history);
        history.add(new CanonChange(expectedRevision+1,clock.instant(),source,reason,
                                    Map.copyOf(changes),validatedMetadata));
        entity.history=history;
        try {return entities.save(entity);}
        catch(OptimisticLockingFailureException conflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Concurrent canon update",conflict);
        }
    }
    private void requireUniverse(String id) {
        if(!universes.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Universe not found");
    }
}
