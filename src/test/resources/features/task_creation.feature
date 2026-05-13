Feature: Task creation

  Background:
    Given the database is empty

  Scenario: Task creation succeeds with valid data
    Given a registered user "ana@example.com" with password "Password123"
    And the user is a member of household "Hogar Test"
    When the client creates a task with title "Lavar la loza" category "Cocina" and description "Después del almuerzo"
    Then the response status should be 201
    And the response should contain title "Lavar la loza" and category "Cocina"
    And the task estado should be "Pendiente"
