Feature: Create task feature

  Background:
    When I swipe right
    And I touch plus button
    And I wait to see "Начать новую задачу"

  Scenario: Create task without tags
    When I create task with name "Задаченция"
    And I touch save button
    Then I wait to see "Задаченция"

  Scenario: Create task with empty name
    When I touch save button
    Then I wait to see "Пожалуйста, введите описание задачи"

  Scenario: Create task with tag
    When I create task with name "Задаченция c тегом"
    And I tap task tag field
    And I touch text "Test tag |0|"
    And I go back
    And I go back
    And I touch save button
    And I wait to see "TimeActivity"
    Then I wait to see "Задаченция c тегом"
    And I wait to see "Test tag |0|"