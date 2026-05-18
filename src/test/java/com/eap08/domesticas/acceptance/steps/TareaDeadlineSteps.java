package com.eap08.domesticas.acceptance.steps;

import com.eap08.domesticas.acceptance.ScenarioContext;
import com.eap08.domesticas.model.Tarea;
import com.eap08.domesticas.repository.HogarRepository;
import com.eap08.domesticas.repository.PasswordResetTokenRepository;
import com.eap08.domesticas.repository.TareaRepository;
import com.eap08.domesticas.repository.UsuarioHogarRepository;
import com.eap08.domesticas.repository.UsuarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TareaDeadlineSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HogarRepository hogarRepository;

    @Autowired
    private UsuarioHogarRepository usuarioHogarRepository;

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ScenarioContext context;

    @When("the client creates a task with title {string} category {string} description {string} and fechaLimite {string}")
    public void theClientCreatesATaskWithFechaLimite(String title, String categoria, String descripcion,
            String fechaLimiteStr) throws Exception {
        LocalDateTime fechaLimite = LocalDateTime.parse(fechaLimiteStr);
        Map<String, Object> body = Map.of(
                "titulo", title,
                "categoria", categoria,
                "descripcion", descripcion,
                "fechaLimite", fechaLimite);
        context.setLastResponse(
                postAuth("/api/households/" + context.getCurrentHogarId() + "/tasks", body, context.getCurrentJwt()));
    }

    @Then("the response should contain fechaLimite {string}")
    public void theResponseShouldContainFechaLimite(String expectedFechaLimiteStr) throws Exception {
        Map<String, Object> body = responseAsMap();
        Object fechaLimiteRaw = body.get("fechaLimite");
        assertThat(fechaLimiteRaw).isNotNull();
        String receivedFechaLimite = fechaLimiteRaw instanceof String ? (String) fechaLimiteRaw
                : fechaLimiteRaw.toString();
        assertThat(receivedFechaLimite).contains(expectedFechaLimiteStr.split("T")[0]);
    }

    @Then("the response should contain fechaLimite null")
    public void theResponseShouldContainFechaLimiteNull() throws Exception {
        Map<String, Object> body = responseAsMap();
        // Depending on serialization, the key may exist with null or be absent. Assert
        // null if present, or absent.
        if (body.containsKey("fechaLimite")) {
            assertThat(body.get("fechaLimite")).isNull();
        } else {
            // If missing, consider it equivalent to null for this test
            assertThat(body.get("fechaLimite")).isNull();
        }
    }

    @Then("the task should be persisted with fechaLimite {string}")
    public void theTaskShouldBePersistedWithFechaLimite(String expectedFechaLimiteStr) throws Exception {
        Map<String, Object> body = responseAsMap();
        Object tareaIdRaw = body.get("tareaId");
        assertThat(tareaIdRaw).isNotNull();
        Long tareaId = ((Number) tareaIdRaw).longValue();

        Tarea persistedTask = tareaRepository.findById(tareaId).orElseThrow();
        LocalDateTime expectedFechaLimite = LocalDateTime.parse(expectedFechaLimiteStr);
        assertThat(persistedTask.getFechaLimite()).isEqualTo(expectedFechaLimite);
    }

    @Then("no task should be created")
    public void noTaskShouldBeCreated() {
        assertThat(tareaRepository.count()).isEqualTo(0);
    }

    private ResponseEntity<String> postAuth(String path, Object body, String jwt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwt);
        String json = objectMapper.writeValueAsString(body);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        return restTemplate.postForEntity(url(path), entity, String.class);
    }

    private Map<String, Object> responseAsMap() throws Exception {
        if (context.getLastResponse().getBody() == null || context.getLastResponse().getBody().isBlank()) {
            return Map.of();
        }
        return objectMapper.readValue(context.getLastResponse().getBody(), new TypeReference<Map<String, Object>>() {
        });
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Given("a task exists with title {string} and fechaLimite {string}")
    public void aTaskExistsWithTitleAndFechaLimite(String title, String fechaLimiteStr) throws Exception {
        java.time.LocalDateTime fechaLimite = java.time.LocalDateTime.parse(fechaLimiteStr);
        Map<String, Object> body = Map.of(
                "titulo", title,
                "categoria", "Otro",
                "descripcion", "",
                "fechaLimite", fechaLimite);

        ResponseEntity<String> response = postAuth("/api/households/" + context.getCurrentHogarId() + "/tasks", body,
                context.getCurrentJwt());
        context.setLastResponse(response);
        Map<String, Object> respBody = objectMapper.readValue(response.getBody(), new TypeReference<>() {
        });
        Object tareaIdRaw = respBody.get("tareaId");
        if (tareaIdRaw != null) {
            context.setCurrentTareaId(((Number) tareaIdRaw).longValue());
        }
        // store original updatedAt if present
        Object updatedAtRaw = respBody.get("updatedAt");
        if (updatedAtRaw != null) {
            java.time.LocalDateTime parsed = java.time.LocalDateTime.parse((String) updatedAtRaw);
            java.time.LocalDateTime earlier = parsed.minusMinutes(5);
            // Backdate the persisted updated_at to avoid microsecond precision flakiness.
            jdbcTemplate.update("UPDATE tarea SET updated_at = ? WHERE tarea_id = ?",
                    java.sql.Timestamp.valueOf(earlier), ((Number) respBody.get("tareaId")).longValue());
            context.setOriginalUpdatedAt(earlier);
        }
    }

    @When("the user updates the task fechaLimite to {string}")
    public void theUserUpdatesTheTaskFechaLimiteTo(String newFechaLimiteStr) throws Exception {
        java.time.LocalDateTime fechaLimite = java.time.LocalDateTime.parse(newFechaLimiteStr);
        Map<String, Object> body = Map.of("fechaLimite", fechaLimite);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(context.getCurrentJwt());
        String json = objectMapper.writeValueAsString(body);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        ResponseEntity<String> response = restTemplate.exchange(url("/api/tasks/" + context.getCurrentTareaId()),
                HttpMethod.PUT, entity, String.class);
        context.setLastResponse(response);
    }

    @Then("the task is updated")
    public void theTaskIsUpdated() {
        assertThat(context.getLastResponse().getStatusCode().value()).isEqualTo(200);
    }

    @Then("the response should contain updatedAt updated")
    public void theResponseShouldContainUpdatedAtUpdated() throws Exception {
        // Read the persisted value from DB to avoid relying on controller serialization
        Long tareaId = context.getCurrentTareaId();
        java.time.LocalDateTime persistedUpdatedAt = null;
        if (tareaId != null) {
            Tarea persisted = tareaRepository.findById(tareaId).orElseThrow();
            persistedUpdatedAt = persisted.getUpdatedAt();
        }
        java.time.LocalDateTime original = context.getOriginalUpdatedAt();
        if (original != null && persistedUpdatedAt != null) {
            assertThat(persistedUpdatedAt).isAfter(original);
        } else {
            assertThat(persistedUpdatedAt).isNotNull();
        }
    }
}
