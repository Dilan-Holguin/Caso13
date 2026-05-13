Feature: Authentication and Access Management

  Background:
    Given the database is empty

  Scenario: Registration succeeds with valid data
    When the client registers with name "Ana" email "ana@example.com" and password "Password123"
    Then the response status should be 201
    And the response should contain email "ana@example.com" and name "Ana"

  Scenario: Registration rejected with invalid email
    When the client registers with name "Ana" email "bad-email" and password "Password123"
    Then the response status should be 400
    And the error code should be "VALIDATION_ERROR"

  Scenario: Registration rejected with weak password
    When the client registers with name "Ana" email "ana@example.com" and password "short"
    Then the response status should be 400
    And the error code should be "VALIDATION_ERROR"

  Scenario: Registration rejected when email already exists
    Given a user exists with name "Ana" email "ana@example.com" and password "Password123"
    When the client registers with name "Ana" email "ana@example.com" and password "Password123"
    Then the response status should be 409
    And the error code should be "BUSINESS_ERROR"

  Scenario: Registration rejected when fields are empty
    When the client registers with name "" email "" and password ""
    Then the response status should be 400
    And the error code should be "VALIDATION_ERROR"

  Scenario: Login succeeds with valid credentials
    Given a user exists with name "Ana" email "ana@example.com" and password "Password123"
    When the client logs in with email "ana@example.com" and password "Password123"
    Then the response status should be 200
    And the response should contain email "ana@example.com" and name "Ana"

  Scenario: Login rejected with invalid credentials
    Given a user exists with name "Ana" email "ana@example.com" and password "Password123"
    When the client logs in with email "ana@example.com" and password "WrongPass"
    Then the response status should be 401
    And the error code should be "INVALID_CREDENTIALS"

  Scenario: Login rejected with empty fields
    When the client logs in with email "" and password ""
    Then the response status should be 400
    And the error code should be "VALIDATION_ERROR"

  Scenario: Logout succeeds
    When the client logs out
    Then the response status should be 200
    And the response message should contain "cerrada correctamente"

  Scenario: Password recovery succeeds for registered email
    Given a user exists with name "Ana" email "ana@example.com" and password "Password123"
    When the client requests password recovery for email "ana@example.com"
    Then the response status should be 200
    And a recovery token should be created

  Scenario: Password recovery returns success for non-registered email
    When the client requests password recovery for email "missing@example.com"
    Then the response status should be 200
    And no recovery token should exist

  Scenario: Password recovery rejected with invalid email
    When the client requests password recovery for email "bad-email"
    Then the response status should be 400
    And the error code should be "VALIDATION_ERROR"

  Scenario: Password recovery rejected with empty email
    When the client requests password recovery for email ""
    Then the response status should be 400
    And the error code should be "VALIDATION_ERROR"

  Scenario: Password reset succeeds with valid token
    Given a user exists with name "Ana" email "ana@example.com" and password "Password123"
    And a password reset token exists for email "ana@example.com" with token "token-123" and expires in minutes 30
    When the client resets password with token "token-123" and new password "NewPassword123"
    Then the response status should be 200
    And the user password for email "ana@example.com" should be updated to "NewPassword123"

  Scenario: Password reset rejected with invalid token
    When the client resets password with token "missing-token" and new password "NewPassword123"
    Then the response status should be 409
    And the error code should be "BUSINESS_ERROR"

  Scenario: Password reset rejected with expired token
    Given a user exists with name "Ana" email "ana@example.com" and password "Password123"
    And an expired password reset token exists for email "ana@example.com" with token "token-expired"
    When the client resets password with token "token-expired" and new password "NewPassword123"
    Then the response status should be 409
    And the error code should be "BUSINESS_ERROR"
