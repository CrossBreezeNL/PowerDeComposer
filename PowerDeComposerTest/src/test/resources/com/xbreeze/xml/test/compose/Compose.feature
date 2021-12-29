@Unit
Feature: Compose
  Here we test the compose.

  Scenario: Compose
    Given the decomposed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ChildElements>
      		<xi:include href="FirstFileName.xml" />
      		<xi:include href="SecondFileName.xml" />
      	</ChildElements>
      </RootElement>
      """
    And the decomposed file 'FirstFileName.xml':
      """
      <ChildElement id="FirstId">
      			<Name>FirstName</Name>
      		</ChildElement>
      """
    And the decomposed file 'SecondFileName.xml':
      """
      <ChildElement id="SecondId">
      			<Name>SecondName</Name>
      		</ChildElement>
      """
    When I perform a compose
    Then I expect a composed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ChildElements>
      		<ChildElement id="FirstId">
      			<Name>FirstName</Name>
      		</ChildElement>
      		<ChildElement id="SecondId">
      			<Name>SecondName</Name>
      		</ChildElement>
      	</ChildElements>
      </RootElement>
      """
