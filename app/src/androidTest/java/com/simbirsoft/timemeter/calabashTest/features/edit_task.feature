Feature: Edit task feature

  Background:
    When I swipe right
    And I start edit task with name "Пробная задача |0|"
    And I wait to see "Редактировать задачу"

  Scenario: Edit task empty name
    When I create task with name ""
    And I touch save button
    Then I wait to see "Пожалуйста, введите описание задачи"

  Scenario: Edit task success
    When I create task with name "Задача!!!"
    And I touch save button
    Then I wait to see "Задача!!!"
    And I wait to not see text "Пробная задача |0|"