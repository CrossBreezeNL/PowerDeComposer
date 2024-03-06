@Unit @Debug

Feature: Decompose New Line Handling
  Here we test whether PowerDeComposer Decompose can handle different newline characters correctly.
  
  Scenario: CRLF handling
    Given the config file 'Config/Config.xml'
    And the composed file 'Composed/ComposedFile.xml'
    When I perform a decompose
    Then I expect a decomposed file 'ComposedFile.xml' with content equal to 'Decomposed/ExpectedDecomposedFile.xml'
    And I expect a decomposed file 'ChildElements/FirstId.xml' with content equal to 'Decomposed/ChildElements/FirstId.xml'

  Scenario: LF handling
    Given the config file 'Config/Config.xml'
    And the composed file 'Composed/ComposedFile.xml'
    When I perform a decompose
    Then I expect a decomposed file 'ComposedFile.xml' with content equal to 'Decomposed/ExpectedDecomposedFile.xml'
    And I expect a decomposed file 'ChildElements/FirstId.xml' with content equal to 'Decomposed/ChildElements/FirstId.xml'