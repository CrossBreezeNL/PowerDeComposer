@Unit @Debug

Feature: Compose New Line Handling
  Here we test whether PowerDeComposer Compose can handle different newline characters correctly.
  
  Scenario: CRLF handling
    Given the config file 'Config/Config.xml'
    And the decomposed file 'Decomposed/RootFile.xml'
    When I perform a compose
    Then I expect a composed file with the content equal to 'Composed/ExpectedComposedFile.xml'

  Scenario: LF handling
    Given the config file 'Config/Config.xml'
    And the decomposed file 'Decomposed/RootFile.xml'
    When I perform a compose
    Then I expect a composed file with the content equal to 'Composed/ExpectedComposedFile.xml'