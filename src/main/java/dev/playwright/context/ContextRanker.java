package dev.playwright.context;
import java.util.*;
import org.springframework.stereotype.Component;
@Component
public class ContextRanker {
    public record RankedEvent(CanonEvent event,double score,List<String> matchedTags) {}
    public List<RankedEvent> rank(List<CanonEvent> candidates,String primary,
                                    List<String> secondary,RelevanceSettings config) {
        List<RankedEvent> ranked=new ArrayList<>();
        for (CanonEvent event:candidates) {
            Set<String> eventTags=new HashSet<>(event.tags());
            List<String> matched=new ArrayList<>();
            double score=0;
            if(eventTags.contains(primary)) {
                score+=config.primaryTagWeight();
                matched.add(primary);
            }
            for(String tag:secondary) if(eventTags.contains(tag) && !matched.contains(tag)) {
                score+=config.secondaryTagWeight();
                matched.add(tag);
            }
            long dimensions=matched.stream().map(t->t.contains(":")?t.substring(0,t.indexOf(':')):"other")
                    .distinct().count();
            if(dimensions>1)score+=config.crossDimensionBonus()*(dimensions-1);
            if (!matched.isEmpty())ranked.add(new RankedEvent(event,score,List.copyOf(matched)));
        }
        ranked.sort(Comparator.comparingDouble(RankedEvent::score).reversed()
                .thenComparing(x->x.event().id()));
        return List.copyOf(ranked);
    }
}
