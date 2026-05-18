Feature: Gestión de fechas límite de tareas

    Background:
        Given la base de datos está vacía

    Scenario: La tarea se crea correctamente con fecha límite futura
        Given existe un usuario registrado "ana@example.com" con contraseña "Password123"
        And el usuario pertenece al hogar "Hogar Test"
        When la persona crea una tarea con título "Pagar servicios" categoría "Otro" descripción "" y fecha límite "2026-12-31T23:59:00"
        Then la tarea se crea correctamente
        And la respuesta devuelve el título "Pagar servicios" y la categoría "Otro"
        And la respuesta muestra la fecha límite "2026-12-31T23:59:00"
        And la tarea queda guardada con la fecha límite "2026-12-31T23:59:00"

    Scenario: La tarea se rechaza cuando la fecha límite ya pasó
        Given existe un usuario registrado "ana@example.com" con contraseña "Password123"
        And el usuario pertenece al hogar "Hogar Test"
        When la persona crea una tarea con título "Tarea retroactiva" categoría "Otro" descripción "" y fecha límite "2020-01-01T00:00:00"
        Then la tarea se rechaza porque la fecha límite debe ser futura
        And el detalle del error contiene "La fecha limite debe ser futura"
        And no se crea ninguna tarea

    Scenario: Crear tarea sin fecha límite
        Given existe un usuario registrado "ana@example.com" con contraseña "Password123"
        And el usuario pertenece al hogar "Hogar Test"
        When la persona crea una tarea con título "Recordar comprar leche" categoría "Compras" descripción ""
        Then la tarea se crea correctamente
        And la respuesta devuelve el título "Recordar comprar leche" y la categoría "Compras"
        And la respuesta muestra la fecha límite vacía

    Scenario: Actualizar la fecha límite de una tarea existente
        Given existe un usuario registrado "ana@example.com" con contraseña "Password123"
        And el usuario pertenece al hogar "Hogar Test"
        And existe una tarea con título "Tarea actualizable" y fecha límite "2027-01-01T12:00:00"
        When la persona actualiza la fecha límite de la tarea a "2027-01-15T12:00:00"
        Then la tarea se actualiza correctamente
        And la respuesta muestra la fecha límite "2027-01-15T12:00:00"
        And la modificación queda registrada