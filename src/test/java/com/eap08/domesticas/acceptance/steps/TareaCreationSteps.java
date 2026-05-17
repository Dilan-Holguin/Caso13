package com.eap08.domesticas.acceptance.steps;

import com.eap08.domesticas.model.Hogar;
import com.eap08.domesticas.model.Tarea;
import com.eap08.domesticas.model.Usuario;
import com.eap08.domesticas.model.UsuarioHogar;
import com.eap08.domesticas.model.UsuarioHogarId;
import com.eap08.domesticas.repository.HogarRepository;
import com.eap08.domesticas.repository.TareaRepository;
import com.eap08.domesticas.repository.UsuarioHogarRepository;
import com.eap08.domesticas.repository.UsuarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TareaCreationSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HogarRepository hogarRepository;

    @Autowired
    private UsuarioHogarRepository usuarioHogarRepository;

    @Autowired
    private TareaRepository tareaRepository;

    private ResponseEntity<String> lastResponse;
    private String adminEmail;
    private String adminPassword;
    private Long adminId;
    private Long hogarId;
    private Long tareaId;

    @Given("a household has an admin user {string} with no existing task")
    public void setupHouseholdWithoutAssignee(String adminEmail) {
        this.adminEmail = adminEmail;
        this.adminPassword = "Password123";

        Usuario admin = new Usuario();
        admin.setNombre("Admin QA");
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin = usuarioRepository.saveAndFlush(admin);

        this.adminId = admin.getUsuarioId();

        Hogar hogar = Hogar.builder()
                .nombre("Hogar QA")
                .descripcion("Hogar para prueba de creacion sin asignado")
                .build();
        hogar = hogarRepository.saveAndFlush(hogar);

        this.hogarId = hogar.getHogarId();

        usuarioHogarRepository.saveAndFlush(UsuarioHogar.builder()
                .id(new UsuarioHogarId(admin.getUsuarioId(), hogar.getHogarId()))
                .usuario(admin)
                .hogar(hogar)
                .rol(UsuarioHogar.ROL_ADMINISTRADOR)
                .build());
    }

    @When("the admin creates a task without specifying an assignee")
    public void createTaskWithoutAssignee() throws Exception {
        String token = loginAndGetToken(adminEmail, adminPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> requestBody = Map.of(
                "titulo", "Barrer sala",
                "descripcion", "Tarea sin responsable",
                "categoria", "Limpieza"
        );
        String json = objectMapper.writeValueAsString(requestBody);
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        lastResponse = restTemplate.postForEntity(
                url("/api/households/" + hogarId + "/tasks"),
                request,
                String.class);

        // Log error response for debugging
        if (lastResponse.getStatusCode().value() != 201) {
            System.err.println("Task creation failed with status: " + lastResponse.getStatusCode().value());
            System.err.println("Response body: " + lastResponse.getBody());
        } else {
            // Extract task ID from response for later verification
            Map<String, Object> body = responseAsMap();
            Object tareaIdRaw = body.get("tareaId");
            if (tareaIdRaw != null) {
                this.tareaId = ((Number) tareaIdRaw).longValue();
            }
        }
    }

        @Then("the task creation response status should be {int}")
        public void taskCreationResponseStatusShouldBe(int expectedStatus) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(expectedStatus);
    }

        @Then("the task creation response should include null assignee info")
        public void taskCreationResponseShouldIncludeNullAssigneeInfo() throws Exception {
        Map<String, Object> body = responseAsMap();
        assertThat(body).containsEntry("asignadoA", null);
    }

        @Then("the task should be persisted without an assignee in the database")
        public void taskShouldBePersistedWithoutAssigneeInDatabase() {
        assertThat(tareaId).isNotNull();
        Tarea persisted = tareaRepository.findById(tareaId).orElseThrow();
        assertThat(persisted.getAsignadoA()).isNull();
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String json = objectMapper.writeValueAsString(Map.of("email", email, "password", password));
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(url("/api/auth/login"), entity, String.class);
        assertThat(loginResponse.getStatusCode().value()).isEqualTo(200);

        Map<String, Object> body = objectMapper.readValue(loginResponse.getBody(),
                new TypeReference<Map<String, Object>>() {
                });
        Object token = body.get("token");
        assertThat(token).isInstanceOf(String.class);
        return token.toString();
    }

    private Map<String, Object> responseAsMap() throws Exception {
        assertThat(lastResponse.getBody()).isNotBlank();
        return objectMapper.readValue(lastResponse.getBody(), new TypeReference<Map<String, Object>>() {
        });
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
