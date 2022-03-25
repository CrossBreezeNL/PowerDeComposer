Feature: Character set detection
  Here we test whether PowerDeComposer can handle the characters sets needed.

  # KnownIssue: Currently with VTD-Nav we can't handle certain special characters.
  @KnownIssue
  Scenario: UTF-16 character set
  	# In Eclipse you don't see any special characters here, but the Name contains a special character. You can see this in VS Code.
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
				<a:ObjectID>C2B4D530-BB77-4024-BF09-68D766A416FD</a:ObjectID>
				<a:Name>left_CONCERN_CUSTOMER_RISK_HISTOR_INCEPTION</a:Name>
				<a:Code>left_CONCERN_CUSTOMER_RISK_HISTOR_INCEPTION</a:Code>
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
				<a:ObjectID>C2B4D530-BB77-4024-BF09-68D766A416FD</a:ObjectID>
				<a:Name>left_CONCERN_CUSTOMER_RISK_HISTOR_INCEPTION</a:Name>
				<a:Code>left_CONCERN_CUSTOMER_RISK_HISTOR_INCEPTION</a:Code>
      </RootElement>
      """