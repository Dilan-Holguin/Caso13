package com.eap08.domesticas.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TareaRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldRejectPastFechaLimite() {
        TareaRequest.CreateTareaRequest request = new TareaRequest.CreateTareaRequest(
                "Tarea retroactiva",
                "Descripcion",
                "Otro",
                LocalDateTime.parse("2020-01-01T00:00:00"),
                null);

        Set<ConstraintViolation<TareaRequest.CreateTareaRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(ConstraintViolation::getMessage))
                .contains("La fecha limite debe ser futura");
    }
}
