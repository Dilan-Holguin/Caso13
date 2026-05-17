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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TareaAssignmentSteps {

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
    private String editorEmail;
    private String editorPassword;
    private Long memberId;
    private String memberName;
    private String memberEmail;
    private String memberPassword;
    private Long editorId;
    private Long tareaId;
    private Long externalId;
    private String externalEmail;

    @Given("a household has an editor user {string} and member user {string} with an existing task")
    public void setupHouseholdWithTask(String editorEmail, String memberEmail) {
        this.editorEmail = editorEmail;
        this.memberEmail = memberEmail;
        this.editorPassword = "Password123";
        this.memberPassword = "Password123";

        Usuario editor = new Usuario();
        editor.setNombre("Editor QA");
        editor.setEmail(editorEmail);
        editor.setPasswordHash(passwordEncoder.encode(editorPassword));
        editor = usuarioRepository.saveAndFlush(editor);
        this.editorId = editor.getUsuarioId();

        Usuario member = new Usuario();
        member.setNombre("Miembro QA");
        member.setEmail(memberEmail);
        member.setPasswordHash(passwordEncoder.encode("Password123"));
        member = usuarioRepository.saveAndFlush(member);

        this.memberId = member.getUsuarioId();
        this.memberName = member.getNombre();

        Hogar hogar = Hogar.builder()
                .nombre("Hogar QA")
                .descripcion("Hogar para prueba de asignacion")
                .build();
        hogar = hogarRepository.saveAndFlush(hogar);

        usuarioHogarRepository.saveAndFlush(UsuarioHogar.builder()
                .id(new UsuarioHogarId(editor.getUsuarioId(), hogar.getHogarId()))
                .usuario(editor)
                .hogar(hogar)
                .rol(UsuarioHogar.ROL_ADMINISTRADOR)
                .build());

        usuarioHogarRepository.saveAndFlush(UsuarioHogar.builder()
                .id(new UsuarioHogarId(member.getUsuarioId(), hogar.getHogarId()))
                .usuario(member)
                .hogar(hogar)
                .rol(UsuarioHogar.ROL_MIEMBRO)
                .build());

        Tarea tarea = Tarea.builder()
                .hogar(hogar)
                .titulo("Barrer sala")
                .descripcion("Caso positivo de asignacion")
                .categoria(Tarea.CAT_LIMPIEZA)
                .estado(Tarea.ESTADO_PENDIENTE)
                .build();
        tarea = tareaRepository.saveAndFlush(tarea);
        this.tareaId = tarea.getTareaId();
    }

    @Given("a household has an editor user {string} with an existing task")
    public void setupHouseholdWithEditorAndTask(String editorEmail) {
        this.editorEmail = editorEmail;
        this.editorPassword = "Password123";

        Usuario editor = new Usuario();
        editor.setNombre("Editor QA");
        editor.setEmail(editorEmail);
        editor.setPasswordHash(passwordEncoder.encode(editorPassword));
        editor = usuarioRepository.saveAndFlush(editor);
        this.editorId = editor.getUsuarioId();

        Hogar hogar = Hogar.builder()
                .nombre("Hogar QA")
                .descripcion("Hogar para prueba de asignacion")
                .build();
        hogar = hogarRepository.saveAndFlush(hogar);

        usuarioHogarRepository.saveAndFlush(UsuarioHogar.builder()
                .id(new UsuarioHogarId(editor.getUsuarioId(), hogar.getHogarId()))
                .usuario(editor)
                .hogar(hogar)
                .rol(UsuarioHogar.ROL_ADMINISTRADOR)
                .build());

        Tarea tarea = Tarea.builder()
                .hogar(hogar)
                .titulo("Barrer sala")
                .descripcion("Caso positivo de asignacion")
                .categoria(Tarea.CAT_LIMPIEZA)
                .estado(Tarea.ESTADO_PENDIENTE)
                .build();
        tarea = tareaRepository.saveAndFlush(tarea);
        this.tareaId = tarea.getTareaId();
    }

    @Given("a household has an editor user {string} and member user {string}")
    public void setupHouseholdWithEditorAndMember(String editorEmail, String memberEmail) {
        this.editorEmail = editorEmail;
        this.memberEmail = memberEmail;
        this.editorPassword = "Password123";
        this.memberPassword = "Password123";

        Usuario editor = new Usuario();
        editor.setNombre("Editor QA");
        editor.setEmail(editorEmail);
        editor.setPasswordHash(passwordEncoder.encode(editorPassword));
        editor = usuarioRepository.saveAndFlush(editor);
        this.editorId = editor.getUsuarioId();

        Usuario member = new Usuario();
        member.setNombre("Miembro QA");
        member.setEmail(memberEmail);
        member.setPasswordHash(passwordEncoder.encode(memberPassword));
        member = usuarioRepository.saveAndFlush(member);

        this.memberId = member.getUsuarioId();
        this.memberName = member.getNombre();

        Hogar hogar = Hogar.builder()
                .nombre("Hogar QA")
                .descripcion("Hogar para prueba de asignacion")
                .build();
        hogar = hogarRepository.saveAndFlush(hogar);

        usuarioHogarRepository.saveAndFlush(UsuarioHogar.builder()
                .id(new UsuarioHogarId(editor.getUsuarioId(), hogar.getHogarId()))
                .usuario(editor)
                .hogar(hogar)
                .rol(UsuarioHogar.ROL_ADMINISTRADOR)
                .build());

        usuarioHogarRepository.saveAndFlush(UsuarioHogar.builder()
                .id(new UsuarioHogarId(member.getUsuarioId(), hogar.getHogarId()))
                .usuario(member)
                .hogar(hogar)
                .rol(UsuarioHogar.ROL_MIEMBRO)
                .build());
    }

    @Given("a household has a viewer user {string} and member user {string} with an existing task")
    public void setupHouseholdWithViewerAndMember(String viewerEmail, String memberEmail) {
        this.editorEmail = viewerEmail;
        this.editorPassword = "Password123";
        this.memberEmail = memberEmail;
        this.memberPassword = "Password123";

        Usuario viewer = new Usuario();
        viewer.setNombre("Viewer QA");
        viewer.setEmail(viewerEmail);
        viewer.setPasswordHash(passwordEncoder.encode(editorPassword));
        viewer = usuarioRepository.saveAndFlush(viewer);
        this.editorId = viewer.getUsuarioId();

        Usuario member = new Usuario();
        member.setNombre("Miembro QA");
        member.setEmail(memberEmail);
        member.setPasswordHash(passwordEncoder.encode(memberPassword));
        member = usuarioRepository.saveAndFlush(member);

        this.memberId = member.getUsuarioId();
        this.memberName = member.getNombre();

        Hogar hogar = Hogar.builder()
                .nombre("Hogar QA")
                .descripcion("Hogar para prueba de asignacion")
                .build();
        hogar = hogarRepository.saveAndFlush(hogar);

        usuarioHogarRepository.saveAndFlush(UsuarioHogar.builder()
                .id(new UsuarioHogarId(viewer.getUsuarioId(), hogar.getHogarId()))
                .usuario(viewer)
                .hogar(hogar)
                .rol(UsuarioHogar.ROL_MIEMBRO)
                .build());

        usuarioHogarRepository.saveAndFlush(UsuarioHogar.builder()
                .id(new UsuarioHogarId(member.getUsuarioId(), hogar.getHogarId()))
                .usuario(member)
                .hogar(hogar)
                .rol(UsuarioHogar.ROL_MIEMBRO)
                .build());

        Tarea tarea = Tarea.builder()
                .hogar(hogar)
                .titulo("Barrer sala")
                .descripcion("Caso negativo - viewer sin permisos")
                .categoria(Tarea.CAT_LIMPIEZA)
                .estado(Tarea.ESTADO_PENDIENTE)
                .build();
        tarea = tareaRepository.saveAndFlush(tarea);
        this.tareaId = tarea.getTareaId();
    }

    @When("the editor assigns the task to user {string}")
    public void assignTaskToUser(String targetEmail) throws Exception {
        String token = loginAndGetToken(editorEmail, editorPassword);
        Long targetId = usuarioRepository.findByEmail(targetEmail)
                .map(Usuario::getUsuarioId)
                .orElse(Long.MAX_VALUE);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        String json = objectMapper.writeValueAsString(Map.of("asignadoAId", targetId));
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        lastResponse = restTemplate.exchange(
                url("/api/tasks/" + tareaId),
                HttpMethod.PUT,
                request,
                String.class);
    }

    @When("the editor assigns a non existing task to the household member")
    public void assignNonExistingTaskToMember() throws Exception {
        String token = loginAndGetToken(editorEmail, editorPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        String json = objectMapper.writeValueAsString(Map.of("asignadoAId", memberId));
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        lastResponse = restTemplate.exchange(
                url("/api/tasks/" + Long.MAX_VALUE),
                HttpMethod.PUT,
                request,
                String.class);
    }

    @When("the viewer assigns the task to the household member")
    public void viewerAssignsTaskToMember() throws Exception {
        String token = loginAndGetToken(editorEmail, editorPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        String json = objectMapper.writeValueAsString(Map.of("asignadoAId", memberId));
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        lastResponse = restTemplate.exchange(
                url("/api/tasks/" + tareaId),
                HttpMethod.PUT,
                request,
                String.class);
    }

    @When("the editor assigns the task to himself")
    public void editorAssignsTaskToHimself() throws Exception {
        String token = loginAndGetToken(editorEmail, editorPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        String json = objectMapper.writeValueAsString(Map.of("asignadoAId", editorId));
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        lastResponse = restTemplate.exchange(
                url("/api/tasks/" + tareaId),
                HttpMethod.PUT,
                request,
                String.class);
    }

    @Given("a household has an editor user {string} and external user {string} with an existing task")
    public void setupHouseholdWithEditorAndExternalUser(String editorEmail, String externalEmail) {
        this.editorEmail = editorEmail;
        this.externalEmail = externalEmail;
        this.editorPassword = "Password123";

        Usuario editor = new Usuario();
        editor.setNombre("Editor QA");
        editor.setEmail(editorEmail);
        editor.setPasswordHash(passwordEncoder.encode(editorPassword));
        editor = usuarioRepository.saveAndFlush(editor);

        Usuario external = new Usuario();
        external.setNombre("Usuario Externo");
        external.setEmail(externalEmail);
        external.setPasswordHash(passwordEncoder.encode("Password123"));
        external = usuarioRepository.saveAndFlush(external);

        this.externalId = external.getUsuarioId();

        Hogar hogar = Hogar.builder()
                .nombre("Hogar QA")
                .descripcion("Hogar para prueba de asignacion")
                .build();
        hogar = hogarRepository.saveAndFlush(hogar);

        usuarioHogarRepository.saveAndFlush(UsuarioHogar.builder()
                .id(new UsuarioHogarId(editor.getUsuarioId(), hogar.getHogarId()))
                .usuario(editor)
                .hogar(hogar)
                .rol(UsuarioHogar.ROL_ADMINISTRADOR)
                .build());

        Tarea tarea = Tarea.builder()
                .hogar(hogar)
                .titulo("Barrer sala")
                .descripcion("Caso negativo - asignar a externo")
                .categoria(Tarea.CAT_LIMPIEZA)
                .estado(Tarea.ESTADO_PENDIENTE)
                .build();
        tarea = tareaRepository.saveAndFlush(tarea);
        this.tareaId = tarea.getTareaId();
    }

    @When("the editor assigns the task to the household member")
    public void assignTaskToMember() throws Exception {
        String token = loginAndGetToken(editorEmail, editorPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        String json = objectMapper.writeValueAsString(Map.of("asignadoAId", memberId));
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        lastResponse = restTemplate.exchange(
                url("/api/tasks/" + tareaId),
                HttpMethod.PUT,
                request,
                String.class);
    }

    @When("the editor assigns the task to external user {string}")
    public void assignTaskToExternalUser(String externalEmail) throws Exception {
        String token = loginAndGetToken(editorEmail, editorPassword);

        Usuario external = usuarioRepository.findByEmail(externalEmail).orElseThrow();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        String json = objectMapper.writeValueAsString(Map.of("asignadoAId", external.getUsuarioId()));
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        lastResponse = restTemplate.exchange(
                url("/api/tasks/" + tareaId),
                HttpMethod.PUT,
                request,
                String.class);
    }

    @Then("the assignment response status should be {int}")
    public void assignmentResponseStatusShouldBe(int expectedStatus) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(expectedStatus);
    }

    @Then("the response should include assigned info for member email {string}")
    public void responseShouldIncludeAssignedInfo(String expectedEmail) throws Exception {
        Map<String, Object> body = responseAsMap();
        Object asignadoRaw = body.get("asignadoA");
        assertThat(asignadoRaw).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> asignado = (Map<String, Object>) asignadoRaw;
        assertThat(((Number) asignado.get("usuarioId")).longValue()).isEqualTo(memberId);
        assertThat(asignado.get("nombre")).isEqualTo(memberName);
        assertThat(asignado.get("email")).isEqualTo(expectedEmail);
    }

    @Then("the assignment response body should contain error message {string}")
    @Then("the response body should contain error message {string}")
    public void responseShouldContainErrorMessage(String expectedMessage) throws Exception {
        Map<String, Object> body = responseAsMap();
        Object message = body.get("message");
        assertThat(message).isInstanceOf(String.class);
        assertThat(message).isEqualTo(expectedMessage);
    }

    @Then("the task should be persisted without changes")
    public void taskShouldBePersistedWithoutChanges() {
        Tarea persisted = tareaRepository.findById(tareaId).orElseThrow();
        assertThat(persisted.getAsignadoA()).isNull();
    }

    @Then("the task should be persisted assigned to the member")
    public void taskShouldBePersistedAssignedToMember() {
        Tarea persisted = tareaRepository.findById(tareaId).orElseThrow();
        assertThat(persisted.getAsignadoA()).isNotNull();
        assertThat(persisted.getAsignadoA().getUsuarioId()).isEqualTo(memberId);
    }

    @Given("the task is currently assigned to the editor")
    @Given("the task is already assigned to the editor")
    public void taskIsCurrentlyAssignedToEditor() {
        Tarea tarea = tareaRepository.findById(tareaId).orElseThrow();
        Usuario editor = usuarioRepository.findByEmail(editorEmail).orElseThrow();
        tarea.setAsignadoA(editor);
        tareaRepository.saveAndFlush(tarea);
    }

    @When("the member retrieves the task")
    public void memberRetrievesTask() throws Exception {
        String token = loginAndGetToken(memberEmail, memberPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        lastResponse = restTemplate.exchange(
                url("/api/tasks/" + tareaId),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
    }

    @Then("the task retrieval response status should be {int}")
    public void taskRetrievalResponseStatusShouldBe(int expectedStatus) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(expectedStatus);
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