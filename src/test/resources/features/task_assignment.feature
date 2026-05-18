Feature: Asignación de tareas al hogar

  Background:
    Given la base de datos está vacía

  Scenario: Se asigna una tarea existente a un miembro válido del mismo hogar
    Given un hogar tiene un usuario editor "editor@example.com" y un usuario miembro "member@example.com" con una tarea existente
    When el editor asigna la tarea al miembro del hogar
    Then la asignación se completa correctamente
    And la respuesta muestra al miembro asignado con correo "member@example.com"
    And la tarea queda guardada asignada al miembro

  Scenario: Se crea una tarea sin responsable asignado
    Given un hogar tiene un usuario administrador "admin@example.com" sin tarea existente
    When el administrador crea una tarea sin indicar responsable
    Then la tarea se crea correctamente
    And la respuesta deja el responsable vacío
    And la tarea queda guardada sin responsable

  Scenario: Se reasigna una tarea a otro miembro del hogar
    Given un hogar tiene un usuario editor "editor@example.com" y un usuario miembro "member@example.com" con una tarea existente
    And la tarea está actualmente asignada al editor
    When el editor asigna la tarea al miembro del hogar
    Then la asignación se completa correctamente
    And la respuesta muestra al miembro asignado con correo "member@example.com"
    And la tarea queda guardada asignada al miembro

  Scenario: La tarea reasignada se conserva en la base de datos
    Given un hogar tiene un usuario editor "editor@example.com" y un usuario miembro "member@example.com" con una tarea existente
    And la tarea está actualmente asignada al editor
    When el editor asigna la tarea al miembro del hogar
    And el miembro consulta la tarea
    Then la consulta devuelve la tarea correctamente
    And la respuesta muestra al miembro asignado con correo "member@example.com"

  Scenario: Se rechaza la asignación cuando el destinatario no existe
    Given un hogar tiene un usuario editor "editor@example.com" con una tarea existente
    When el editor intenta asignar la tarea al usuario "ghost@example.com"
    Then la asignación falla porque el usuario no existe
    And el mensaje de respuesta contiene "Usuario no encontrado"
    And la tarea queda guardada sin cambios

  Scenario: Se rechaza la asignación cuando la persona no tiene permisos
    Given un hogar tiene un usuario observador "viewer@example.com" y un usuario miembro "member@example.com" con una tarea existente
    When el observador asigna la tarea al miembro del hogar
    Then la asignación falla porque no tiene permisos
    And el mensaje de respuesta contiene "No tiene permisos para asignar tareas"
    And la tarea queda guardada sin cambios

  Scenario: Se rechaza la asignación cuando el usuario es externo al hogar
    Given un hogar tiene un usuario editor "editor@example.com" y un usuario externo "external@example.com" con una tarea existente
    When el editor asigna la tarea al usuario externo "external@example.com"
    Then la asignación falla porque el usuario no pertenece al hogar
    And el mensaje de respuesta contiene "El usuario asignado no pertenece a este hogar"
    And la tarea queda guardada sin cambios

  Scenario: Se rechaza la asignación cuando la tarea no existe
    Given un hogar tiene un usuario editor "editor@example.com" y un usuario miembro "member@example.com"
    When el editor asigna una tarea inexistente al miembro del hogar
    Then la asignación falla porque la tarea no existe
    And el mensaje de respuesta contiene "Tarea no encontrada"

  Scenario: Se rechaza la asignación cuando la tarea ya está asignada al mismo usuario
    Given un hogar tiene un usuario editor "editor@example.com" con una tarea existente
    And la tarea ya está asignada al editor
    When el editor se asigna la tarea a sí mismo
    Then la asignación falla porque la tarea ya está asignada a ese usuario
    And el mensaje de respuesta contiene "La tarea ya está asignada a este usuario"

