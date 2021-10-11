Feature: Configure TargetFolderName
  Here we test the usage of the TargetFolderNames configuration during decompose.

  Scenario Outline: TargetFolderName is <Scenario>
    Given the input file 'Input.xml':
      """
      <?xml version="1.0" encoding="UTF-8" ?>
      <RootElement>
      	<ChildElements>
      		<ChildElement type="<FirstType>" id="1">
      			<Name><FirstName></Name>
      		</ChildElement>
      		<ChildElement type="<SecondType>" id="2">
      			<Name><SecondName></Name>
      		</ChildElement>
      	</ChildElements>
      </RootElement>
      """
    And the config file:
      """
      <?xml version="1.0" encoding="UTF-8" ?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<DecomposableElement>
      			<!-- Decompose elements with a Name child-element. -->
      			<ElementCondition xpath="./Name" />
      			<!-- The target file name should be the value of the Name element. -->
      			<TargetFolderNames>
      				<TargetFolderName xpath="./@type" />
      			</TargetFolderNames>
      			<!-- The target file name should be the value of the Name element. -->
      			<TargetFileNames>
      				<TargetFileName xpath="./Name" condition="string-length(./Name) > 0" />
      				<TargetFileName xpath="./@id" />
      			</TargetFileNames>
      		</DecomposableElement>
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I execute PowerDeComposer
    Then I expect a target file 'Input.xml' with the following content:
      """
      <?xml version="1.0" encoding="UTF-8" ?>
      <RootElement>
      	<ChildElements>
      		<xi:include href="<FirstFileName>.xml" />
      		<xi:include href="<SecondFileName>.xml" />
      	</ChildElements>
      </RootElement>
      """
    And I expect a target file '<FirstFileName>.xml' with the following content:
      """
      <ChildElement type="<FirstType>" id="1">
      			<Name><FirstName></Name>
      		</ChildElement>
      """
    And I expect a target file '<SecondFileName>.xml' with the following content:
      """
      <ChildElement type="<SecondType>" id="2">
      			<Name><SecondName></Name>
      		</ChildElement>
      """

    Examples: 
      | Scenario               | FirstType | FirstName | SecondType | SecondName | FirstFileName                       | SecondFileName                        |
      | the same               | SameType  | FirstName | SameType   | SecondName | ChildElements\\SameType\\FirstName  | ChildElements\\SameType\\SecondName   |
      | different              | FirstType | FirstName | SecondType | SecondName | ChildElements\\FirstType\\FirstName | ChildElements\\SecondType\\SecondName |
      | the same and same name | SameType  | SameName  | SameType   | SameName   | ChildElements\\SameType\\SameName   | ChildElements\\SameType\\2            |
