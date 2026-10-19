package dev.playwright.canon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CanonApiIntegrationTest {
    @Container static MongoDBContainer mongo =
        new MongoDBContainer(DockerImageName.parse("mongo:7.0"));
    @DynamicPropertySource
    static void mongodb(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }
    @Autowired TestRestTemplate http;
    @Autowired ObjectMapper mapper;
    @LocalServerPort int port;

    JsonNode post(String path, Object request, HttpStatus expected) throws Exception {
        ResponseEntity<String> response = http.postForEntity(path, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(expected);
        return mapper.readTree(response.getBody());
    }
    String universe() throws Exception {
        return post("/api/v1/universes",Map.of("name","Delta"),HttpStatus.CREATED).get("id").asText();
    }
    String entity(String universeId) throws Exception {
        return post("/api/v1/universes/"+universeId+"/entities",
            Map.of("kind","character","name","Scott Winstead",
                   "facts",Map.of("callsign","Fang")),HttpStatus.CREATED).get("id").asText();
    }
    @Test void recordsRevisionsAndPreservesPreviousProvenance() throws Exception {
        String u=universe(), e=entity(u);
        String path="/api/v1/universes/"+u+"/entities/"+e;
        JsonNode revised=post(path+"/revisions",
            Map.of("expectedRevision",0,"source","player-confirmed","reason","canonical update",
                   "facts",Map.of("squadron","Fox")),HttpStatus.OK);
        assertThat(revised.get("revision").asLong()).isEqualTo(1);
        assertThat(revised.get("facts").get("callsign").asText()).isEqualTo("Fang");
        assertThat(revised.get("facts").get("squadron").asText()).isEqualTo("Fox");
        assertThat(revised.get("history").size()).isEqualTo(1);
        assertThat(revised.get("history").get(0).get("source").asText()).isEqualTo("player-confirmed");

        ResponseEntity<String> stale=http.postForEntity(path+"/revisions",
            Map.of("expectedRevision",0,"source","narrator","reason","stale",
                   "facts",Map.of("squadron","Other")),String.class);
        assertThat(stale.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        JsonNode saved=mapper.readTree(http.getForObject(path,String.class));
        assertThat(saved.get("facts").get("squadron").asText()).isEqualTo("Fox");
        assertThat(saved.get("history").size()).isEqualTo(1);
    }
    @Test void isolatesWorldsAndReturnsOnlyRequestedSceneCast() throws Exception {
        String first=universe(), second=universe();
        String e=entity(first);
        ResponseEntity<String> forbiddenRead=http.getForEntity(
            "/api/v1/universes/"+second+"/entities/"+e,String.class);
        assertThat(forbiddenRead.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JsonNode selected=post("/api/v1/universes/"+first+"/scene-context",
            Map.of("entityIds",new String[]{e,e}),HttpStatus.OK);
        assertThat(selected.size()).isEqualTo(1);
        assertThat(selected.get(0).get("id").asText()).isEqualTo(e);
        ResponseEntity<String> crossWorld=http.postForEntity(
            "/api/v1/universes/"+second+"/scene-context",Map.of("entityIds",new String[]{e}),String.class);
        assertThat(crossWorld.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    @Test void validatesRequiredProvenance() throws Exception {
        String u=universe(), e=entity(u);
        ResponseEntity<String> response=http.postForEntity(
            "/api/v1/universes/"+u+"/entities/"+e+"/revisions",
            Map.of("expectedRevision",0,"source","","reason","update",
                   "facts",Map.of("rank","Captain")),String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test void persistsDimensionedMetadataAndRevisionHistory() throws Exception {
        String u=universe();
        JsonNode created=post("/api/v1/universes/"+u+"/entities",
            Map.of("kind","character","name","Natalie Zorra",
                "facts",Map.of("callsign","Silk"),
                "metadata",Map.of("scene",new String[]{"valley-cas"},
                                  "unit",new String[]{"fox-squadron"})),HttpStatus.CREATED);
        String e=created.get("id").asText();
        assertThat(created.path("metadata").path("scene").get(0).asText()).isEqualTo("valley-cas");
        JsonNode revised=post("/api/v1/universes/"+u+"/entities/"+e+"/revisions",
            Map.of("expectedRevision",0,"source","player-confirmed",
                   "reason","new assignment",
                   "metadata",Map.of("scene",new String[]{"valley-cas","base-operations"})),
            HttpStatus.OK);
        assertThat(revised.path("revision").asLong()).isEqualTo(1);
        assertThat(revised.path("metadata").path("unit").get(0).asText()).isEqualTo("fox-squadron");
        assertThat(revised.path("metadata").path("scene").size()).isEqualTo(2);
        assertThat(revised.path("history").get(0).path("metadata").path("scene").size()).isEqualTo(2);
        ResponseEntity<String> bad=http.postForEntity(
            "/api/v1/universes/"+u+"/entities",
            Map.of("kind","character","name","Bad","facts",Map.of(),
                   "metadata",Map.of("Not Valid",new String[]{"x"})),String.class);
        assertThat(bad.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    @Test void eventPostAcceptsMultidimensionalMetadata() throws Exception {
        String u=universe();
        JsonNode event=post("/api/v1/universes/"+u+"/events",
            Map.of("title","Northern Shilka ambush",
                   "summary","A concealed SPAAG awaits Southern CAS.",
                   "metadata",Map.of("scene",new String[]{"valley-cas"},
                                     "status",new String[]{"unresolved","concealed"}),
                   "source","player-confirmed"),HttpStatus.CREATED);
        assertThat(event.path("metadata").path("scene").get(0).asText()).isEqualTo("valley-cas");
        JsonNode context=post("/api/v1/universes/"+u+"/context/resolve",
            Map.of("primary","scene:valley-cas","secondary",new String[]{}),HttpStatus.OK);
        assertThat(context.path("events").size()).isEqualTo(1);
        assertThat(context.path("events").get(0).path("title").asText()).isEqualTo("Northern Shilka ambush");
    }
}
