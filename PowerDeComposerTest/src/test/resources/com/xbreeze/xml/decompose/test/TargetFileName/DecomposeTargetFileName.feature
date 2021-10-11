Feature: PowerDeComposer TargetFileNames
	Here we test the usage of the TargetFileNames configuration during decompose.

	Scenario: One leven decompose
		Given the input file 'Input.xml':
		"""
		<?xml version="1.0" encoding="UTF-8" ?>
		<RootElement>
			<ChildElements>
				<ChildElement>
					<Name>FirstName</Name>
				</ChildElement>
				<ChildElement>
					<Name>SecondName</Name>
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
					
					<TargetFolderNames />
					<TargetFileNames>
						<TargetFileName xpath="./Name" condition="string-length(./Name) > 0" />
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
				<xi:include href="ChildElements\FirstName.xml" />
				<xi:include href="ChildElements\SecondName.xml" />
			</ChildElements>
		</RootElement>
		"""
		And I expect a target file 'ChildElements/FirstName.xml' with the following content:
		"""
		<ChildElement>
					<Name>FirstName</Name>
				</ChildElement>
		"""
		And I expect a target file 'ChildElements/SecondName.xml' with the following content:
		"""
		<ChildElement>
					<Name>SecondName</Name>
				</ChildElement>
		"""