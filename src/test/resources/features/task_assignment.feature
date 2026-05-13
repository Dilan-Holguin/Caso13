Feature: Task assignment to household member

  Background:
    Given the database is empty

  Scenario: Assign an existing task to a valid member of the same household
    Given a household has an editor user "editor@example.com" and member user "member@example.com" with an existing task
    When the editor assigns the task to the household member
    Then the assignment response status should be 200
    And the response should include assigned info for member email "member@example.com"
    And the task should be persisted assigned to the member

  Scenario: Create a task without an assigned responsible member
    Given a household has an admin user "admin@example.com" with no existing task
    When the admin creates a task without specifying an assignee
    Then the task creation response status should be 201
    And the task creation response should include null assignee info
    And the task should be persisted without an assignee in the database
