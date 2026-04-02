package com.eap08.domesticas.security;

import com.eap08.domesticas.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// @RestControllerAdvice intercepta globalmente todas las excepciones
// que escapen de cualquier controlador en la aplicación
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Maneja los errores de validación — cuando @Valid falla en un DTO
    // Spring lanza MethodArgumentNotValidException con todos los campos inválidos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Extraemos todos los mensajes de error de cada campo que falló la validación
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message("Los datos enviados no son válidos")
                .details(details)
                .traceId(UUID.randomUUID().toString()) // ID único para rastrear en logs
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Maneja errores de lógica de negocio — como el correo duplicado
    // que lanzamos en AuthServiceImpl con throw new RuntimeException(...)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("BUSINESS_ERROR")
                .message(ex.getMessage())
                .details(List.of())
                .traceId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Maneja cualquier otra excepción inesperada — es el último recurso
    // para que nunca se le devuelva al cliente un error 500 sin estructura
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("INTERNAL_ERROR")
                .message("Ocurrió un error interno en el servidor")
                // Deliberadamente no exponemos el detalle técnico al cliente
                // pero el traceId permite buscarlo en los logs internamente
                .details(List.of())
                .traceId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}