package dev.playwright.context;

import dev.playwright.context.model.CanonEvent;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EventCandidateFinderTest {
    @Test void filtersUniverseAndMatchesAnyProvidedCategoryValueBeforeLimiting() {
        var mongo=mock(MongoTemplate.class);
        var finder=new EventCandidateFinder(mongo);
        finder.find("delta",List.of(
            new ContextRanker.DimensionValue("scene","valley-cas"),
            new ContextRanker.DimensionValue("character","silk")),50);
        var captured=ArgumentCaptor.forClass(Query.class);
        verify(mongo).find(captured.capture(),eq(CanonEvent.class));
        var query=captured.getValue();
        var filter=query.getQueryObject().toJson();
        assertThat(filter).contains("universeId","delta","$or","metadata.scene",
                                    "valley-cas","metadata.character","silk");
        assertThat(query.getLimit()).isEqualTo(50);
        assertThat(query.getSortObject().toJson()).contains("_id");
    }
    @Test void rejectsUnboundedOrEmptyCandidateQueries() {
        var finder=new EventCandidateFinder(mock(MongoTemplate.class));
        assertThatThrownBy(()->finder.find("delta",List.of(),50))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->finder.find("delta",
            List.of(new ContextRanker.DimensionValue("scene","x")),0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
