@Unit
Feature: Compose
  Here we test the compose.

  Scenario Outline: Compose <Scenario>
    Given the decomposed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement xmlns:xi="http://www.w3.org/2001/XInclude">
        <ChildElements>
          <xi:include href="<SubFolder>FirstFileName.xml" />
          <xi:include href="<SubFolder>SecondFileName.xml" />
        </ChildElements>
      </RootElement>
      """
    And the decomposed file '<SubFolder>FirstFileName.xml':
      """
      <ChildElement id="FirstId">
            <Name>FirstName</Name>
          </ChildElement>
      """
    And the decomposed file '<SubFolder>SecondFileName.xml':
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

    Examples: 
      | Scenario                   | SubFolder   |
      | in one folder              |             |
      | in Unix style subfolder    | SubFolder/  |
      | in Windows style subfolder | SubFolder\\ |

  Scenario: Compose recursive
    Given the decomposed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement xmlns:xi="http://www.w3.org/2001/XInclude">
        <ChildElements>
          <xi:include href="ChildElements/FirstFileName.xml" />
        </ChildElements>
      </RootElement>
      """
    And the decomposed file 'ChildElements/FirstFileName.xml':
      """
      <ChildElement id="FirstId" xmlns:xi="http://www.w3.org/2001/XInclude">
        <Name>FirstName</Name>
        <ChildElements>
          <xi:include href="FirstFileName/ChildElements/SecondFileName.xml" />
        </ChildElements>
      </ChildElement>
      """
    And the decomposed file 'ChildElements/FirstFileName/ChildElements/SecondFileName.xml':
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
            <ChildElements>
              <ChildElement id="SecondId">
                <Name>SecondName</Name>
              </ChildElement>
            </ChildElements>
          </ChildElement>
        </ChildElements>
      </RootElement>
      """

  Scenario: Compose include with own XML declaration
    Given the decomposed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement xmlns:xi="http://www.w3.org/2001/XInclude">
        <ChildElements>
          <xi:include href="FirstFileName.xml" />
        </ChildElements>
      </RootElement>
      """
    And the decomposed file 'FirstFileName.xml':
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <ChildElement id="FirstId">
        <Name>FirstName</Name>
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
        </ChildElements>
      </RootElement>
      """

  # Issue: Newline after processing instructions is ignored.
  # This issue is solved when using pretty-print option.
  Scenario: Compose with processing instruction
    Given the decomposed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <?CustomProcessingInstruction CustomPIAttribute="SomeThing" version="0.0.1"?>
      <RootElement>
        <ChildElement/>
      </RootElement>
      """
    When I perform a compose
    Then I expect a composed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <?CustomProcessingInstruction CustomPIAttribute="SomeThing" version="0.0.1"?>
      <RootElement>
        <ChildElement/>
      </RootElement>
      """

  # Isse: Newlines after processing instructions are ignored.
  # This issue is solved when using pretty-print option.
  Scenario: Compose with two processing instructions
    Given the decomposed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <?CustomProcessingInstruction CustomPIAttribute="SomeThing" version="0.0.1"?>
      <?SecondCustomProcessingInstruction SecondCustomPIAttribute="SomeThing" version="0.0.2"?>
      <RootElement>
        <ChildElement/>
      </RootElement>
      """
    When I perform a compose
    Then I expect a composed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <?CustomProcessingInstruction CustomPIAttribute="SomeThing" version="0.0.1"?>
      <?SecondCustomProcessingInstruction SecondCustomPIAttribute="SomeThing" version="0.0.2"?>
      <RootElement>
        <ChildElement/>
      </RootElement>
      """
  
  # Isse: All comments before the root element is not included in the output.
  # This is solved by setting http://xml.org/sax/properties/lexical-handler.
  Scenario: Compose with comment before root node
    Given the decomposed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <!--Comment before root node.-->
      <RootElement/>
      """
    When I perform a compose
    Then I expect a composed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <!--Comment before root node.-->
      <RootElement/>
      """

  # Issue: Comment inside the document is also ignored.
  # This is solved by setting http://xml.org/sax/properties/lexical-handler.
  Scenario: Compose with comment inside root node
    Given the decomposed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
        <!--Some comment-->
      </RootElement>
      """
    When I perform a compose
    Then I expect a composed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
        <!--Some comment-->
      </RootElement>
      """

  Scenario: Compose with non-existing include
    Given the decomposed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement xmlns:xi="http://www.w3.org/2001/XInclude">
        <ChildElements>
          <xi:include href="ChildElements/FirstFileName.xml" />
        </ChildElements>
      </RootElement>
      """
    And the decomposed file 'ChildElements/FirstFileName.xml':
      """
      <ChildElement id="FirstId" xmlns:xi="http://www.w3.org/2001/XInclude">
        <Name>FirstName</Name>
        <ChildElements>
          <xi:include href="FirstFileName/ChildElements/SecondFileName.xml" />
        </ChildElements>
      </ChildElement>
      """
    When I perform a compose then I expect the following exception:
      """
      Error while reading InlineFile.xml: Error attempting to parse XML file (href='ChildElements/FirstFileName.xml'). Reason: An include with href 'FirstFileName/ChildElements/SecondFileName.xml'failed, and no fallback element was found.
      """