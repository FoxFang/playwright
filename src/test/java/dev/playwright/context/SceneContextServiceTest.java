package dev.playwright.context;
import dev.playwright.canon.UniverseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SceneContextServiceTest {
    UniverseRepository universes=mock(UniverseRepository.class);
    CanonEventRepository events=mock(CanonEventRepository.class);
    RelevanceSettingsService settings=mock(RelevanceSettingsService.class);
    ContextRanker ranker=new ContextRanker();
    SceneContextService service=new SceneContextService(universes,events,settings,ranker);
    @Test void ranksAcrossDimensionsWithTransportNeutralContract() {
        when(universes.existsById("delta")).thenReturn(true);
        var config=new RelevanceSettings(10,4,3,3,1,2,200,12,3000);
        when(settings.current()).thenReturn(new RelevanceSettingsService.Snapshot(config,"defaults",0));
        when(events.findByUniverseIdAndTagsIn(eq("delta"),anyList(),any(Pageable.class)))
           .thenReturn(List.of(
             new CanonEvent("single","delta","Other","Less relevant",List.of("event:sortie"),
                List.of("scott"),Instant.EPOCH,"day-1","scene-1"),
             new CanonEvent("best","delta","First sortie","Highly relevant",
                List.of("event:sortie","character:scott","theme:trust"),
                List.of("scott","winter"),Instant.EPOCH,"day-1","scene-1")
           ));
        var result=service.resolve("delta","event:sortie",List.of("character:scott","theme:trust"));
        assertThat(result.schemaVersion()).isEqualTo("1.0");
        assertThat(result.events()).extracting(SceneContextService.ContextEvent::id)
            .containsExactly("best","single");
        assertThat(result.events().get(0).summary()).isEqualTo("Highly relevant");
    }
    @Test void rejectsCrossUniverseAndExcessSecondaryTags() {
        assertThatThrownBy(()->service.resolve("missing","event:x",List.of()))
            .isInstanceOf(ResponseStatusException.class);
        when(universes.existsById("delta")).thenReturn(true);
        assertThatThrownBy(()->service.resolve("delta","event:x",List.of("a","b","c","d")))
            .isInstanceOf(ResponseStatusException.class);
    }
}
