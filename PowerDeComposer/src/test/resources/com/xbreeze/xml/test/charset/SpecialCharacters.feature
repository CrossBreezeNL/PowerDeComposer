@Unit

Feature: Special characters
  Here we test whether PowerDeComposer can handle special characters correctly.
  
  Scenario: Special charachter in file
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<Name>Willem Ötten</Name>
      </RootElement>
      """
    And the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<DecomposableElement />
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<Name>Willem Ötten</Name>
      </RootElement>
      """