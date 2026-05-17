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
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
}
