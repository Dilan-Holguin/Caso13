Feature: Task assignment to household member

  Background:
    Given the database is empty

  Scenario: Assign an existing task to a valid member of the same household
    Given a household has an editor user "editor@example.com" and member user "member@example.com" with an existing task
    When the editor assigns the task to the household member
    Then the assignment response status should be 200
    And the response should include assigned info for member email "member@example.com"
    And the task should be persisted assigned to the member