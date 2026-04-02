package com.eap08.domesticas.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitamos CSRF porque nuestra API usa JWT en lugar de sesiones.
            // CSRF protege contra ataques en aplicaciones con sesiones de servidor,
            // pero en una API stateless con tokens no aplica.
            .csrf(csrf -> csrf.disable())

            // Le decimos a Spring que no cree ni use sesiones HTTP.
            // Cada petición debe ser autónoma y autenticarse con su propio token JWT.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // El registro y login deben ser públicos — obviamente nadie
                // puede autenticarse si primero no puede crear su cuenta
                .requestMatchers("/api/auth/**").permitAll()

                // Swagger necesita acceso público para que el equipo pueda
                // consultar la documentación sin necesidad de estar autenticado.
                // /swagger-ui/** cubre la interfaz visual
                // /v3/api-docs/** cubre el JSON con la especificación OpenAPI
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // Cualquier otro endpoint que agreguemos en el futuro
                // va a requerir un JWT válido automáticamente
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt es el algoritmo estándar para hashear contraseñas.
        // A diferencia de un hash simple como MD5, BCrypt incluye un "salt"
        // aleatorio en cada hash, lo que significa que la misma contraseña
        // genera un hash diferente cada vez — esto protege contra ataques
        // de diccionario y rainbow tables.
        return new BCryptPasswordEncoder();
    }
}