package com.eap08.domesticas.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder // Lombok genera un patrón Builder — muy útil para construir objetos con muchos campos
public class ErrorResponse {

    // Código semántico del error — le dice al cliente QUÉ tipo de error ocurrió
    private String errorCode;

    // Mensaje legible para humanos — puede mostrarse directamente al usuario
    private String message;

    // Lista de detalles específicos — útil cuando hay múltiples errores de validación
    private List<String> details;

    // Identificador único de la petición — facilita rastrear el error en los logs
    private String traceId;

    // Cuándo ocurrió el error
    private LocalDateTime timestamp;
}