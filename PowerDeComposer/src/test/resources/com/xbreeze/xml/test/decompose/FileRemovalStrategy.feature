@Unit
Feature: Configure FileRemovalStrategy
  Here we test the working of the fileRemovalStrategy attribute.

  Background: 
    Given the decomposed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ChildElements>
      		<xi:include href="SubFolder/IncludedFileName.xml" />
      	</ChildElements>
      </RootElement>
      """
    And the decomposed file 'SubFolder/IncludedFileName.xml':
      """
      <ChildElement id="FirstId">
      			<Name>FirstName</Name>
      		</ChildElement>
      """
    And the decomposed file 'SubFolder/ExtraFileName.xml':
      """
      <ChildElement id="SecondId">
      			<Name>ExtraFile</Name>
      		</ChildElement>
      """
    And the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ChildElements>
      		<ChildElement id="SecondId">
      			<Name>FirstName</Name>
      		</ChildElement>
      	</ChildElements>
      </RootElement>
      """

  Scenario Outline: fileRemovalStrategy <Scenario>
    Given the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose <FileRemovalStrategy> >
      		<!-- The DecomposableElement element must be here, otherwise the decomposer won't write the composed file to the decomposed folder. -->
      		<DecomposableElement />
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I <DoOrDont> expect the file 'SubFolder/ExtraFileName.xml'

    Examples: 
      | Scenario     | FileRemovalStrategy            | DoOrDont |
      | includes     | fileRemovalStrategy="includes" | do       |
      | files        | fileRemovalStrategy="files"    | do not   |
      | no-specified |                                | do       |
