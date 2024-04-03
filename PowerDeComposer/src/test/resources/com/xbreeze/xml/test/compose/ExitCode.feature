@Unit
Feature: Compose Exit Code
  Here we test the exit code while composing

  Scenario: Succesfull exit code
    Given the decomposed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement/>
      """
    When I perform a compose in separate process
    Then I expect exit code 0

  Scenario: Unsuccesfull exit code
    Given the decomposed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      """
    When I perform a compose in separate process
    Then I expect exit code 1