Feature: Task deadline assignment

    Background:
        Given the database is empty

    Scenario: Task creation succeeds with valid future due date
        Given a registered user "ana@example.com" with password "Password123"
        And the user is a member of household "Hogar Test"
        When the client creates a task with title "Pagar servicios" category "Otro" description "" and fechaLimite "2026-12-31T23:59:00"
        Then the response status should be 201
        And the response should contain title "Pagar servicios" and category "Otro"
        And the response should contain fechaLimite "2026-12-31T23:59:00"
        And the task should be persisted with fechaLimite "2026-12-31T23:59:00"

    Scenario: Task creation rejected with past due date
        Given a registered user "ana@example.com" with password "Password123"
        And the user is a member of household "Hogar Test"
        When the client creates a task with title "Tarea retroactiva" category "Otro" description "" and fechaLimite "2020-01-01T00:00:00"
        Then the response status should be 400
        And the error details should contain "La fecha limite debe ser futura"
        And no task should be created