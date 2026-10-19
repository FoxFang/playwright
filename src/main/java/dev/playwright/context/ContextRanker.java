package dev.playwright.context;
import dev.playwright.context.model.CanonEvent;
import dev.playwright.context.model.RelevanceSettings;
import java.util.*;
import org.springframework.stereotype.Component;
@Component
public class ContextRanker {
    public record RankedEvent(CanonEvent event,double score,List<String> matchedTags) {}
    public record DimensionValue(String dimension,String value) {
        public static DimensionValue parse(String selector) {
            if(selector==null) throw new IllegalArgumentException("Selector required");
            int colon=selector.indexOf(':');
            if(colon<=0 || colon==selector.length()-1 || !selector.substring(0,colon).matches("[a-z][a-z0-9_-]*"))
                throw new IllegalArgumentException("Expected dimension:value");
            return new DimensionValue(selector.substring(0,colon),selector.substring(colon+1));
        }
        public String tag(){return dimension+":"+value;}
    }
    public List<RankedEvent> rank(List<CanonEvent> candidates,String primary,
                                    List<String> secondary,RelevanceSettings config) {
        DimensionValue first=DimensionValue.parse(primary);
        List<DimensionValue> other=secondary.stream().map(DimensionValue::parse).toList();
        List<RankedEvent> ranked=new ArrayList<>();
        for(CanonEvent event:candidates) {
            List<String> matched=new ArrayList<>();
            double score=0;
            // State attributes are searchable, but not relevance-weighted associations.
            if(matches(event,first)) {
                score+=config.primaryTagWeight();
                matched.add(first.tag());
            }
            for(DimensionValue selector:other) if(matches(event,selector) && !matched.contains(selector.tag())) {
                score+=config.secondaryTagWeight();
                matched.add(selector.tag());
            }
            long dimensions=matched.stream().map(DimensionValue::parse)
                    .map(DimensionValue::dimension).filter(d->!d.equals("status")).distinct().count();
            if(dimensions>1)score+=config.crossDimensionBonus()*(dimensions-1);
            if(!matched.isEmpty())ranked.add(new RankedEvent(event,score,List.copyOf(matched)));
        }
        ranked.sort(Comparator.comparingDouble(RankedEvent::score).reversed()
                .thenComparing(x->x.event().id()));
        return List.copyOf(ranked);
    }
    private boolean matches(CanonEvent event,DimensionValue selector) {
        return event.metadata().getOrDefault(selector.dimension(),List.of()).contains(selector.value());
    }
}
