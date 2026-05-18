package com.eap08.domesticas.acceptance.steps;

import com.eap08.domesticas.model.Hogar;
import com.eap08.domesticas.model.Tarea;
import com.eap08.domesticas.model.Usuario;
import com.eap08.domesticas.model.UsuarioHogar;
import com.eap08.domesticas.model.UsuarioHogarId;
import com.eap08.domesticas.acceptance.ScenarioContext;
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
    @Autowired
    private ScenarioContext context;

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

    @Given("un hogar tiene un usuario editor {string} y un usuario miembro {string} con una tarea existente")
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

    @Given("un hogar tiene un usuario editor {string} con una tarea existente")
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

    @Given("un hogar tiene un usuario editor {string} y un usuario miembro {string}")
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

    @Given("un hogar tiene un usuario observador {string} y un usuario miembro {string} con una tarea existente")
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

    @When("el editor asigna la tarea al usuario {string}")
    @When("el editor intenta asignar la tarea al usuario {string}")
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

        storeLastResponse(restTemplate.exchange(
                url("/api/tasks/" + tareaId),
                HttpMethod.PUT,
                request,
                String.class));
    }

    @When("el editor asigna una tarea inexistente al miembro del hogar")
    public void assignNonExistingTaskToMember() throws Exception {
        String token = loginAndGetToken(editorEmail, editorPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        String json = objectMapper.writeValueAsString(Map.of("asignadoAId", memberId));
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        storeLastResponse(restTemplate.exchange(
                url("/api/tasks/" + Long.MAX_VALUE),
                HttpMethod.PUT,
                request,
                String.class));
    }

    @When("el observador asigna la tarea al miembro del hogar")
    public void viewerAssignsTaskToMember() throws Exception {
        String token = loginAndGetToken(editorEmail, editorPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        String json = objectMapper.writeValueAsString(Map.of("asignadoAId", memberId));
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        storeLastResponse(restTemplate.exchange(
                url("/api/tasks/" + tareaId),
                HttpMethod.PUT,
                request,
                String.class));
    }

    @When("el editor se asigna la tarea a sí mismo")
    public void editorAssignsTaskToHimself() throws Exception {
        String token = loginAndGetToken(editorEmail, editorPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        String json = objectMapper.writeValueAsString(Map.of("asignadoAId", editorId));
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        storeLastResponse(restTemplate.exchange(
                url("/api/tasks/" + tareaId),
                HttpMethod.PUT,
                request,
                String.class));
    }

    @Given("un hogar tiene un usuario editor {string} y un usuario externo {string} con una tarea existente")
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

    @When("el editor asigna la tarea al miembro del hogar")
    public void assignTaskToMember() throws Exception {
        String token = loginAndGetToken(editorEmail, editorPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        String json = objectMapper.writeValueAsString(Map.of("asignadoAId", memberId));
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        storeLastResponse(restTemplate.exchange(
                url("/api/tasks/" + tareaId),
                HttpMethod.PUT,
                request,
                String.class));
    }

    @When("el editor asigna la tarea al usuario externo {string}")
    public void assignTaskToExternalUser(String externalEmail) throws Exception {
        String token = loginAndGetToken(editorEmail, editorPassword);

        Usuario external = usuarioRepository.findByEmail(externalEmail).orElseThrow();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        String json = objectMapper.writeValueAsString(Map.of("asignadoAId", external.getUsuarioId()));
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        storeLastResponse(restTemplate.exchange(
                url("/api/tasks/" + tareaId),
                HttpMethod.PUT,
                request,
                String.class));
    }

    public void assignmentResponseStatusShouldBe(int expectedStatus) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(expectedStatus);
    }

    @Then("la asignación se completa correctamente")
    public void laAsignacionSeCompletaCorrectamente() {
        assignmentResponseStatusShouldBe(200);
    }

    @Then("la asignación falla porque el usuario no existe")
    public void laAsignacionFallaPorqueElUsuarioNoExiste() {
        assignmentResponseStatusShouldBe(404);
    }

    @Then("la asignación falla porque no tiene permisos")
    public void laAsignacionFallaPorqueNoTienePermisos() {
        assignmentResponseStatusShouldBe(403);
    }

    @Then("la asignación falla porque el usuario no pertenece al hogar")
    public void laAsignacionFallaPorqueElUsuarioNoPerteneceAlHogar() {
        assignmentResponseStatusShouldBe(409);
    }

    @Then("la asignación falla porque la tarea no existe")
    public void laAsignacionFallaPorqueLaTareaNoExiste() {
        assignmentResponseStatusShouldBe(404);
    }

    @Then("la asignación falla porque la tarea ya está asignada a ese usuario")
    public void laAsignacionFallaPorqueLaTareaYaEstaAsignadaAEseUsuario() {
        assignmentResponseStatusShouldBe(409);
    }

    @Then("la respuesta muestra al miembro asignado con correo {string}")
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
    @Then("el mensaje de respuesta contiene {string}")
    public void responseShouldContainErrorMessage(String expectedMessage) throws Exception {
        ResponseEntity<String> response = this.lastResponse != null ? this.lastResponse : context.getLastResponse();
        assertThat(response).isNotNull();
        Map<String, Object> body = objectMapper.readValue(response.getBody(),
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                });
        Object message = body.get("message");
        assertThat(message).isInstanceOf(String.class);
        assertThat(message.toString()).contains(expectedMessage);
    }

    @Then("la tarea queda guardada sin cambios")
    public void taskShouldBePersistedWithoutChanges() {
        Tarea persisted = tareaRepository.findById(tareaId).orElseThrow();
        assertThat(persisted.getAsignadoA()).isNull();
    }

    @Then("la tarea queda guardada asignada al miembro")
    public void taskShouldBePersistedAssignedToMember() {
        Tarea persisted = tareaRepository.findById(tareaId).orElseThrow();
        assertThat(persisted.getAsignadoA()).isNotNull();
        assertThat(persisted.getAsignadoA().getUsuarioId()).isEqualTo(memberId);
    }

    @Given("la tarea está actualmente asignada al editor")
    @Given("la tarea ya está asignada al editor")
    public void taskIsCurrentlyAssignedToEditor() {
        Tarea tarea = tareaRepository.findById(tareaId).orElseThrow();
        Usuario editor = usuarioRepository.findByEmail(editorEmail).orElseThrow();
        tarea.setAsignadoA(editor);
        tareaRepository.saveAndFlush(tarea);
    }

    @When("el miembro consulta la tarea")
    public void memberRetrievesTask() throws Exception {
        String token = loginAndGetToken(memberEmail, memberPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        storeLastResponse(restTemplate.exchange(
                url("/api/tasks/" + tareaId),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class));
    }

    @Then("the task retrieval response status should be {int}")
    public void taskRetrievalResponseStatusShouldBe(int expectedStatus) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(expectedStatus);
    }

    @Then("la consulta devuelve la tarea correctamente")
    public void laConsultaDevuelveLaTareaCorrectamente() {
        taskRetrievalResponseStatusShouldBe(200);
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

    private void storeLastResponse(ResponseEntity<String> response) {
        this.lastResponse = response;
        context.setLastResponse(response);
    }
}