package com.eap08.domesticas.acceptance.steps;

import com.eap08.domesticas.acceptance.ScenarioContext;
import com.eap08.domesticas.model.PasswordResetToken;
import com.eap08.domesticas.model.Usuario;
import com.eap08.domesticas.repository.HogarRepository;
import com.eap08.domesticas.repository.PasswordResetTokenRepository;
import com.eap08.domesticas.repository.TareaRepository;
import com.eap08.domesticas.repository.UsuarioHogarRepository;
import com.eap08.domesticas.repository.UsuarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private UsuarioHogarRepository usuarioHogarRepository;

    @Autowired
    private HogarRepository hogarRepository;

    @Autowired
    private ScenarioContext context;

    @Before
    public void clearDatabase() {
        tokenRepository.deleteAll();
        tareaRepository.deleteAll();
        usuarioHogarRepository.deleteAll();
        hogarRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Given("la base de datos está vacía")
    public void theDatabaseIsEmpty() {
        tokenRepository.deleteAll();
        tareaRepository.deleteAll();
        usuarioHogarRepository.deleteAll();
        hogarRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Given("existe un usuario con nombre {string} correo {string} y contraseña {string}")
    public void aUserExists(String name, String email, String password) {
        Usuario usuario = new Usuario();
        usuario.setNombre(name);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuarioRepository.saveAndFlush(usuario);
    }

    @Given("existe un token de recuperación para el correo {string} con el token {string} y vence en {int} minutos")
    public void aPasswordResetTokenExists(String email, String token, int minutes) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setNombre("User");
                    nuevo.setEmail(email);
                    nuevo.setPasswordHash(passwordEncoder.encode("Password123"));
                    return usuarioRepository.saveAndFlush(nuevo);
                });

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUsuario(usuario);
        resetToken.setToken(token);
        resetToken.setExpiracion(LocalDateTime.now().plusMinutes(minutes));
        tokenRepository.saveAndFlush(resetToken);
    }

    @Given("existe un token de recuperación vencido para el correo {string} con el token {string}")
    public void anExpiredPasswordResetTokenExists(String email, String token) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setNombre("User");
                    nuevo.setEmail(email);
                    nuevo.setPasswordHash(passwordEncoder.encode("Password123"));
                    return usuarioRepository.saveAndFlush(nuevo);
                });

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUsuario(usuario);
        resetToken.setToken(token);
        resetToken.setExpiracion(LocalDateTime.now().minusMinutes(1));
        tokenRepository.saveAndFlush(resetToken);
    }

    @When("the client registers with name {string} email {string} and password {string}")
    @When("la persona se registra con nombre {string} correo {string} y contraseña {string}")
    public void theClientRegisters(String name, String email, String password) throws Exception {
        Map<String, Object> body = Map.of(
                "nombre", name,
                "email", email,
                "password", password);
        post("/api/auth/register", body);
    }

    @When("the client logs in with email {string} and password {string}")
    @When("la persona inicia sesión con correo {string} y contraseña {string}")
    public void theClientLogsIn(String email, String password) throws Exception {
        Map<String, Object> body = Map.of(
                "email", email,
                "password", password);
        post("/api/auth/login", body);
    }

    @When("the client logs out")
    @When("la persona cierra sesión")
    public void theClientLogsOut() throws Exception {
        post("/api/auth/logout", Map.of());
    }

    @When("the client requests password recovery for email {string}")
    @When("la persona solicita recuperación de contraseña para el correo {string}")
    public void theClientRequestsPasswordRecovery(String email) throws Exception {
        Map<String, Object> body = Map.of("email", email);
        post("/api/auth/forgot-password", body);
    }

    @When("the client resets password with token {string} and new password {string}")
    @When("la persona restablece la contraseña con el token {string} y la nueva contraseña {string}")
    public void theClientResetsPassword(String token, String newPassword) throws Exception {
        Map<String, Object> body = Map.of(
                "token", token,
                "nuevaPassword", newPassword);
        post("/api/auth/reset-password", body);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(context.getLastResponse().getStatusCode().value()).isEqualTo(expectedStatus);
    }

    @Then("el registro se completa correctamente")
    public void elRegistroSeCompletaCorrectamente() {
        theResponseStatusShouldBe(201);
    }

    @Then("el inicio de sesión se completa correctamente")
    public void elInicioDeSesionSeCompletaCorrectamente() {
        theResponseStatusShouldBe(200);
    }

    @Then("el cierre de sesión se completa correctamente")
    public void elCierreDeSesionSeCompletaCorrectamente() {
        theResponseStatusShouldBe(200);
    }

    @Then("la recuperación de contraseña se solicita correctamente")
    public void laRecuperacionDeContrasenaSeSolicitaCorrectamente() {
        theResponseStatusShouldBe(200);
    }

    @Then("el restablecimiento de contraseña se completa correctamente")
    public void elRestablecimientoDeContrasenaSeCompletaCorrectamente() {
        theResponseStatusShouldBe(200);
    }

    @Then("el registro se rechaza por validación")
    public void elRegistroSeRechazaPorValidacion() {
        theResponseStatusShouldBe(400);
    }

    @Then("el inicio de sesión se rechaza por validación")
    public void elInicioDeSesionSeRechazaPorValidacion() {
        theResponseStatusShouldBe(400);
    }

    @Then("la recuperación de contraseña se rechaza por validación")
    public void laRecuperacionDeContrasenaSeRechazaPorValidacion() {
        theResponseStatusShouldBe(400);
    }

    @Then("el acceso se rechaza por credenciales inválidas")
    public void elAccesoSeRechazaPorCredencialesInvalidas() {
        theResponseStatusShouldBe(401);
    }

    @Then("el restablecimiento de contraseña se rechaza por una regla de negocio")
    public void elRestablecimientoDeContrasenaSeRechazaPorUnaReglaDeNegocio() {
        theResponseStatusShouldBe(409);
    }

    @Then("el registro se rechaza por una regla de negocio")
    public void elRegistroSeRechazaPorUnaReglaDeNegocio() {
        theResponseStatusShouldBe(409);
    }

    @Then("el inicio de sesión se rechaza por credenciales inválidas")
    public void elInicioDeSesionSeRechazaPorCredencialesInvalidas() {
        theResponseStatusShouldBe(401);
    }

    @Then("the response should contain email {string} and name {string}")
    public void theResponseShouldContainEmailAndName(String email, String name) throws Exception {
        Map<String, Object> body = responseAsMap();
        assertThat(body.get("email")).isEqualTo(email);
        assertThat(body.get("nombre")).isEqualTo(name);
    }

    @Then("la respuesta devuelve el correo {string} y el nombre {string}")
    public void laRespuestaDevuelveElCorreoYYElNombre(String email, String name) throws Exception {
        theResponseShouldContainEmailAndName(email, name);
    }

    @Then("the response message should contain {string}")
    public void theResponseMessageShouldContain(String expected) throws Exception {
        Map<String, Object> body = responseAsMap();
        assertThat(body.get("message").toString()).contains(expected);
    }

    @Then("se muestra que el mensaje contiene {string}")
    public void elMensajeDeRespuestaContiene(String expected) throws Exception {
        theResponseMessageShouldContain(expected);
    }

    @Then("the error code should be {string}")
    public void theErrorCodeShouldBe(String expectedCode) throws Exception {
        Map<String, Object> body = responseAsMap();
        assertThat(body.get("errorCode")).isEqualTo(expectedCode);
    }

    @Then("se muestra que hay un error de validación")
    public void elSistemaMuestraUnErrorDeValidacion() throws Exception {
        theErrorCodeShouldBe("VALIDATION_ERROR");
    }

    @Then("se muestra que hay una regla de negocio")
    public void elSistemaMuestraUnaReglaDeNegocio() throws Exception {
        theErrorCodeShouldBe("BUSINESS_ERROR");
    }

    @Then("se muestra que las credenciales son inválidas")
    public void elSistemaMuestraCredencialesInvalidas() throws Exception {
        theErrorCodeShouldBe("INVALID_CREDENTIALS");
    }

    @Then("a recovery token should be created")
    public void aRecoveryTokenShouldBeCreated() {
        assertThat(tokenRepository.count()).isGreaterThan(0);
    }

    @Then("se crea un token de recuperación")
    public void seCreaUnTokenDeRecuperacion() {
        aRecoveryTokenShouldBeCreated();
    }

    @Then("no recovery token should exist")
    public void noRecoveryTokenShouldExist() {
        assertThat(tokenRepository.count()).isEqualTo(0);
    }

    @Then("no se crea ningún token de recuperación")
    public void noSeCreaNingunTokenDeRecuperacion() {
        noRecoveryTokenShouldExist();
    }

    @Then("the user password for email {string} should be updated to {string}")
    public void theUserPasswordShouldBeUpdated(String email, String newPassword) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        assertThat(passwordEncoder.matches(newPassword, usuario.getPasswordHash())).isTrue();
    }

    @Then("la contraseña del usuario para el correo {string} queda actualizada a {string}")
    public void laContrasenaDelUsuarioParaElCorreoQuedaActualizadaA(String email, String newPassword) {
        theUserPasswordShouldBeUpdated(email, newPassword);
    }

    private void post(String path, Object body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String json = objectMapper.writeValueAsString(body);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        context.setLastResponse(restTemplate.postForEntity(url(path), entity, String.class));
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
