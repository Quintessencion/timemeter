Feature: Delete task feature

  Background:
    When I swipe right
    And I start edit task with name "Пробная задача |0|"
    And I wait to see "Редактировать задачу"

  Scenario: Delete task canceled
    When I touch delete button
    Then I wait to see "Удалить задачу?"
    And I touch text "Отмена"
    Then I wait to see "Пробная задача |0|"

  Scenario: Delete task
    When I touch delete button
    Then I wait to see "Удалить задачу?"
    And I touch text "Удалить"
    And I wait to see "TimeActivity"
    Then I wait to not see text "Пробная задача |0|"