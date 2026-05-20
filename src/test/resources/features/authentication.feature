Feature: Autenticación y control de acceso

  Background:
    Given la base de datos está vacía

  Scenario: El registro se completa con datos válidos
    When la persona se registra con nombre "Ana" correo "ana@example.com" y contraseña "Password123"
    Then el registro se completa correctamente
    And la respuesta devuelve el correo "ana@example.com" y el nombre "Ana"

  Scenario: El registro se rechaza por correo inválido
    When la persona se registra con nombre "Ana" correo "bad-email" y contraseña "Password123"
    Then el registro se rechaza por validación
    And se muestra que hay un error de validación

  Scenario: El registro se rechaza por contraseña débil
    When la persona se registra con nombre "Ana" correo "ana@example.com" y contraseña "short"
    Then el registro se rechaza por validación
    And se muestra que hay un error de validación

  Scenario: El registro se rechaza cuando el correo ya existe
    Given existe un usuario con nombre "Ana" correo "ana@example.com" y contraseña "Password123"
    When la persona se registra con nombre "Ana" correo "ana@example.com" y contraseña "Password123"
    Then el registro se rechaza por una regla de negocio
    And se muestra que hay una regla de negocio

  Scenario: El registro se rechaza cuando faltan campos obligatorios
    When la persona se registra con nombre "Ana" correo "" y contraseña ""
    Then el registro se rechaza por validación
    And se muestra que hay un error de validación

  Scenario: El inicio de sesión se completa con credenciales válidas
    Given existe un usuario con nombre "Ana" correo "ana@example.com" y contraseña "Password123"
    When la persona inicia sesión con correo "ana@example.com" y contraseña "Password123"
    Then el inicio de sesión se completa correctamente
    And la respuesta devuelve el correo "ana@example.com" y el nombre "Ana"

  Scenario: El inicio de sesión se rechaza por credenciales inválidas
    Given existe un usuario con nombre "Ana" correo "ana@example.com" y contraseña "Password123"
    When la persona inicia sesión con correo "ana@example.com" y contraseña "WrongPass"
    Then el acceso se rechaza por credenciales inválidas
    And se muestra que las credenciales son inválidas

  Scenario: El inicio de sesión se rechaza cuando faltan datos
    When la persona inicia sesión con correo "" y contraseña ""
    Then el inicio de sesión se rechaza por validación
    And se muestra que hay un error de validación

  Scenario: El cierre de sesión se completa correctamente
    When la persona cierra sesión
    Then el cierre de sesión se completa correctamente
    And el mensaje de respuesta contiene "cerrada correctamente"

  Scenario: La recuperación de contraseña se solicita para un correo registrado
    Given existe un usuario con nombre "Ana" correo "ana@example.com" y contraseña "Password123"
    When la persona solicita recuperación de contraseña para el correo "ana@example.com"
    Then la recuperación de contraseña se solicita correctamente
    And se crea un token de recuperación

  Scenario: La recuperación de contraseña no revela si el correo no existe
    When la persona solicita recuperación de contraseña para el correo "missing@example.com"
    Then la recuperación de contraseña se solicita correctamente
    And no se crea ningún token de recuperación

  Scenario: La recuperación de contraseña se rechaza por correo inválido
    When la persona solicita recuperación de contraseña para el correo "bad-email"
    Then la recuperación de contraseña se rechaza por validación
    And se muestra que hay un error de validación

  Scenario: La recuperación de contraseña se rechaza cuando el correo está vacío
    When la persona solicita recuperación de contraseña para el correo ""
    Then la recuperación de contraseña se rechaza por validación
    And se muestra que hay un error de validación

  Scenario: El restablecimiento de contraseña se completa con un token válido
    Given existe un usuario con nombre "Ana" correo "ana@example.com" y contraseña "Password123"
    And existe un token de recuperación para el correo "ana@example.com" con el token "token-123" y vence en 30 minutos
    When la persona restablece la contraseña con el token "token-123" y la nueva contraseña "NewPassword123"
    Then el restablecimiento de contraseña se completa correctamente
    And la contraseña del usuario para el correo "ana@example.com" queda actualizada a "NewPassword123"

  Scenario: El restablecimiento de contraseña se rechaza por token inválido
    When la persona restablece la contraseña con el token "missing-token" y la nueva contraseña "NewPassword123"
    Then el restablecimiento de contraseña se rechaza por una regla de negocio
    And se muestra que hay una regla de negocio

  Scenario: El restablecimiento de contraseña se rechaza por token vencido
    Given existe un usuario con nombre "Ana" correo "ana@example.com" y contraseña "Password123"
    And existe un token de recuperación vencido para el correo "ana@example.com" con el token "token-expired"
    When la persona restablece la contraseña con el token "token-expired" y la nueva contraseña "NewPassword123"
    Then el restablecimiento de contraseña se rechaza por una regla de negocio
    And se muestra que hay una regla de negocio
