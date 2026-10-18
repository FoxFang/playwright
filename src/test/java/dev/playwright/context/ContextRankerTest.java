package dev.playwright.context;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class ContextRankerTest {
    final ContextRanker ranker=new ContextRanker();
    final RelevanceSettings settings=new RelevanceSettings(10,4,3,3,1,2,200,12,3000);
    CanonEvent event(String id,String... tags) {
        return new CanonEvent(id,"delta",id,"summary",List.of(tags),List.of(),
            Instant.EPOCH,"day-1","scene-1");
    }
    @Test void independentTagDimensionsPromoteConvergentEvents() {
        var results=ranker.rank(List.of(
            event("single","event:sortie"),
            event("convergent","event:sortie","character:winter","theme:trust"),
            event("secondary","character:winter")), "event:sortie",
            List.of("character:winter","theme:trust"),settings);
        assertThat(results).extracting(r->r.event().id())
            .containsExactly("convergent","single","secondary");
        assertThat(results.get(0).score()).isEqualTo(24);
    }
    @Test void repeatedTagsDoNotCreateMultipleBonuses() {
        var results=ranker.rank(List.of(event("a","character:winter","character:scott")),
            "event:sortie",List.of("character:winter","character:scott"),settings);
        assertThat(results.get(0).score()).isEqualTo(8);
    }
}
