@Unit
Feature: Configure ChangeDetection
  Here we test the usage of the ChangeDetection configuration during decompose.

  Scenario Outline: ChangeDetection detects <Scenario>
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<Something changed="<InputChanged>" />
      	<Something else="B" />
      </RootElement>
      """
    Given the decomposed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<Something changed="<TargetChanged>" />
      	<Something else="A" />
      </RootElement>
      """
    And the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<ChangeDetection xpath="/RootElement/Something/@changed" />
      		<!-- The DecomposableElement element must be here, otherwise the decomposer won't write the composed file to the decomposed folder. -->
      		<DecomposableElement />
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<Something changed="<ResultChanged>" />
      	<Something else="<SomethingElse>" />
      </RootElement>
      """

    Examples: 
      | Scenario           | InputChanged | TargetChanged | ResultChanged | SomethingElse |
      | composed changed   | true         | false         | true          | B             |
      | both unchanged     | false        | false         | false         | A             |
      | decomposed changed | false        | true          | false         | B             |
      | both changed       | true         | true          | true          | A             |
      | composed empty     |              | false         |               | B             |
      | decomposed empty   | false        |               | false         | B             |
      | both empty         |              |               |               | A             |
