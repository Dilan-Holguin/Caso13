Feature: Creación de tareas

  Background:
    Given la base de datos está vacía

  Scenario: La creación de una tarea se completa con datos válidos
    Given existe un usuario registrado "ana@example.com" con contraseña "Password123"
    And el usuario pertenece al hogar "Hogar Test"
    When la persona crea una tarea con título "Lavar la loza" categoría "Cocina" y descripción "Después del almuerzo"
    Then la tarea se crea correctamente
    And la respuesta devuelve el título "Lavar la loza" y la categoría "Cocina"
    And la tarea queda en estado "Pendiente"

  Scenario: La creación de una tarea se rechaza cuando el título está vacío
    Given existe un usuario registrado "ana@example.com" con contraseña "Password123"
    And el usuario pertenece al hogar "Hogar Test"
    When la persona crea una tarea con título "" categoría "Limpieza" y descripción ""
    Then la creación se rechaza por validación
    And se muestra que hay un error de validación
    And el detalle del error contiene "El titulo es obligatorio"

  Scenario: La creación de una tarea se rechaza por categoría no válida
    Given existe un usuario registrado "ana@example.com" con contraseña "Password123"
    And el usuario pertenece al hogar "Hogar Test"
    When la persona crea una tarea con título "Hacer ejercicio" categoría "Deportes" y descripción ""
    Then la creación se rechaza por una regla de negocio
    And se muestra que hay una regla de negocio
    And el mensaje de respuesta contiene "Categoria no valida"

  Scenario: La creación de una tarea se rechaza cuando la persona no pertenece al hogar
    Given existe un usuario registrado "ana@example.com" con contraseña "Password123"
    And el usuario pertenece al hogar "Hogar Test"
    And existe una persona externa "externo@example.com" con contraseña "Password123" que no pertenece al hogar
    When la persona externa intenta crear una tarea con título "Lavar la loza" categoría "Cocina" y descripción ""
    Then la creación se rechaza por una regla de negocio
    And se muestra que hay una regla de negocio
    And el mensaje de respuesta contiene "No perteneces a este hogar"
