package dev.playwright.canon.model;
import java.time.Instant;
import java.util.List;
import java.util.Map;
public record CanonChange(long revision,Instant timestamp,String source,String reason,
                          Map<String,String> facts,Map<String,List<String>> metadata) {}
