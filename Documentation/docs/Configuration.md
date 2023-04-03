# Configuration for PowerDeComposer

## PowerDeComposerConfig
The PowerDeComposer configuration for composing and decomposing model files.

### Example
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<PowerDeComposerConfig>
	<!-- Specify a file removal strategy that is used to remove previously decomposed files. -->
	<!-- "includes" will traverse the includes tree of the decomposed folder and build the list based on them. This is also the default value that is used if not specified. -->
	<!-- "files" will traverse the decomposed directory and build the list based on the files that are present. -->
	<Decompose fileRemovalStrategy="...">

		<!-- Specify a XPath to execute on both sides (composed and decomposed) to decide whether the decompose. -->
		<!-- The value of the XPath must be found in the root XML document of the decomposed model, so it can be in a decompose part of the model. -->
		<!-- If the value doesn't exist on both sides we do nothing. -->
		<!-- If the value exists on one side but not on the other side, we decompose. -->
		<!-- If the value exists on both sides, we decompose if the value is different. -->
		<ChangeDetection xpath="..." />

		<!-- Configuration on how to replace identifiers. -->
		<IdentifierReplacements>
			<IdentifierReplacement identifierNodeXPath="..." replacementValueXPath="..." referencingNodeXPath="..." />
			<IdentifierReplacement identifierNodeXPath="..." replacementValueXPath="..." referencingNodeXPath="..." />
			...
		</IdentifierReplacements>

		<!-- Specify the nodes to remove before decomposing. -->
		<NodeRemovals>
			<NodeRemoval xpath="..." />
			<NodeRemoval xpath="..." />
			...
		</NodeRemovals>

		<!-- Specify which elements should be decomposed. -->
		<!-- The DecomposableElement is implicitly a AllConditions group. -->
		<DecomposableElement>
			<ElementCondition xpath="..." />
			<ElementCondition xpath="..." />
			...

			<OneOffConditions>
				...
			</OneOffConditions>

			<AllConditions>
				...
			</AllConditions>

            <!-- Here we specify the xpath to execute on a decomposable element to get the folder name to store the file in. -->
			<!-- The first TargetFolderName configuration which yields a value will be used. -->
			<TargetFolderNames>
				<TargetFolderName xpath="..." />
				<TargetFolderName xpath="..." />
				...
			</TargetFolderNames>

			<!-- Here we specify the xpath to execute on a decomposable element to get the file name (without .xml). -->
            <!-- Target target file names are considered in order of appearance in the config. Once a value has been found which hasen't been written before in the current run, the value is used. -->
            <!-- For example if the first TargetFileName would lead to the same file name (in the same target folder) for 2 decomposed elements,
                 the first one would get the name of the TargetFileName specified
                 and the second one will get the TargetFileName of the next configuration which leads to a value.
             -->
            <!-- If no value is found using any configuration an exception is thrown. -->
			<TargetFileNames>
				<TargetFileName xpath="..." />
				<TargetFileName xpath="..." />
				...
			</TargetFileNames>

			<!-- Here we specify which information to include as an attribute on the xi:include tag. -->
			<IncludeAttributes>
				<IncludeAttribute name="..." xpath="..." />
				<IncludeAttribute name="..." xpath="..." />
				...
			</IncludeAttributes>

		</DecomposableElement>

	</Decompose>

</PowerDeComposerConfig>
```

### Config elements
| Config element           | Description |
|:---                      |:---         |
| fileRemovalStrategy      | Specify a file removal strategy that is used to remove previously decomposed files. "includes" will traverse the includes tree of the decomposed files and build the list based on them. This is also the default value that is used if not specified. "files" will traverse the decomposed directory and build the list based on the files that are present. |
| ChangeDetection          | Node to evaluate on the composed and decomposed models. Of the value is empty on at least one side or the value differs the decompose is executed. |
| IdentifierReplacement    | Instruction to replace identifiers with alternative values. The identifierNodeXPath is the node which contains the identifier in the composed model. The replacementValueXPath must be executed to get the new identifier value. The referencingNodeXPath value is optional, it if specified the value in these nodes will also be replaced using the value of the old identifier and the new identifier. |
| NodeRemoval              | Instruction to remove nodes before decomposing. The XPath is evaluated to find the nodes to remove. An example for such an instruction is to remove the modification date on referenced objects. |
| ElementCondition         | A condition for an element which need to be extracted into seperate files (XPath expression per node). All conditions together inside the DecomposableElement element are evaluated against a model element to decide whether it needs to be decomposed. Element conditions can be grouped in OneOffConditions or AllConditions elements to specify whether one or all conditions should be met within the container.  |
| TargetFolderName         | The node contents to use as the folder name for the extracted elements. For PowerDesigner objects this can for example be ./Stereotype. The xpath can be a simple node selection like "./Stereotype", or it can also contain XPath 1.0 functions like "concat(./name(), '_', ./Stereotype)". |
| TargetFileName           | The node contents to use as the file name (without extension) for the extracted elements. For PowerDesigner objects this can for example be ./ObjectID, ./Code or ./Name. The xpath can be a simple node selection like "./ObjectID", or it can also contain XPath 1.0 functions like "concat(./ObjectID, '_', ./Code)". |
| IncludeAttribute         | Instruction to include a certain attribute in the xi:include tag. The name is the name for the xi:include attribute. The xpath expressions will be evaluated to get the value for the new attribute. The xpath can be a simple node selection like "./ObjectID", or it can also contain XPath 1.0 functions like "concat('ID=', ./ObjectID)". |

### Default config
When no configuration file is specified on the command, the following configuration will be used by default.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<PowerDeComposerConfig>
	<Decompose fileRemovalStrategy="includes">
	
		<!-- Specify a XPath to execute on both sides (composed and decomposed) to decide whether the decompose. -->
		<!-- The value of the XPath must be found in the root XML document of the decomposed model, so it can be in a decompose part of the model. -->
		<!-- If the value doesn't exist on both sides we do nothing. -->
		<!-- If the value exists on one side but not on the other side, we decompose. -->
		<!-- If the value exists on both sides, we decompose if the value is different. -->
		<ChangeDetection xpath="/processing-instruction('PowerDesigner')/@LastModificationDate" />
	
		<!-- Specify the nodes to remove before decomposing. -->
		<NodeRemovals>
			<!-- Remove the object count on the PowerDesigner processing instruction. -->
			<NodeRemoval xpath="/processing-instruction('PowerDesigner')/@Objects" />
			<!-- Remove the symbols count on the PowerDesigner processing instruction. -->
			<NodeRemoval xpath="/processing-instruction('PowerDesigner')/@Symbols" />
			<!--  Remove the last modification date on the PowerDesigner processing instruction. -->
			<NodeRemoval xpath="/processing-instruction('PowerDesigner')/@LastModificationDate" />
			
			<!-- Remove the CreationDate, Creator, ModificationDate & Modifier from all Shortcuts. -->
			<NodeRemoval xpath="//Shortcut/CreationDate" />
			<NodeRemoval xpath="//Shortcut/Creator" />
			<NodeRemoval xpath="//Shortcut/ModificationDate" />
			<NodeRemoval xpath="//Shortcut/Modifier" />
			
			<!-- Remove the TargetModelLastModificationDate from all TargetModels. -->
			<NodeRemoval xpath="//TargetModel/TargetModelLastModificationDate" />
		</NodeRemovals>

		<!-- Configuration on how to replace identifiers. -->
		<IdentifierReplacements>
			<IdentifierReplacement identifierNodeXPath="//*/@Id[../ObjectID]" replacementValueXPath="../ObjectID" referencingNodeXPath="//*/@Ref" />
			<IdentifierReplacement identifierNodeXPath="//Symbols/*/@Id[../Object/*/@Ref]" replacementValueXPath="concat('Symbol_', ../Object/*/@Ref)" />
		</IdentifierReplacements>
		
		<!-- Specify which elements should be decomposed. -->
		<!-- The DecomposableElement is implicitly a AllConditions group. -->
		<DecomposableElement>
			<!-- The element must have a ObjectID and a Code element as childs. -->
			<ElementCondition xpath="./ObjectID and ./Code" />
			<!-- Use the parent axis on the current node to set a predicate on the parent. -->
			<!-- Decompose all elements where the parent name starts with 'c:' (these are the collections in PowerDesigner). -->
			<ElementCondition xpath="parent::node()[starts-with(name(), 'c:')]" />
			<!-- Don't decompose the Children collection (this is the higher level collection of the model). -->
			<ElementCondition xpath="parent::node()[name() != 'c:Children']" />
			<!-- LDM collections not to decompose. -->
			<ElementCondition xpath="parent::node()[name() != 'c:Attributes']" />
			<ElementCondition xpath="parent::node()[name() != 'c:Identifiers']" />
			<!-- PDM collections not to decompose. -->
			<ElementCondition xpath="parent::node()[name() != 'c:Columns']" />
			<ElementCondition xpath="parent::node()[name() != 'c:Keys']" />
			<!-- Don't decompose sub shortcuts. -->
			<ElementCondition xpath="parent::node()[name() != 'c:SubShortcuts']" />
			
			<!-- Example for comparing on the element name. -->
			<!-- <ElementCondition xpath="name() = 'o:Entity'" /> -->
			<!-- Example for using starts-with with the name() function. -->
			<!-- <ElementCondition xpath="starts-with(name(), 'o:')" /> -->
			
			<!-- You can nest conditions groups (OneOfConditions and AllConditions). See the example below. -->
			<!--
			<OneOffConditions>
				<ElementCondition xpath="name() = 'SomeElementName'" />
				<ElementCondition xpath="name() = 'SomeOtherElementName'" />
				<AllConditions>
					<ElementCondition xpath="parent::node()[name() = 'c:Entities'" />
					<ElementCondition xpath="starts-with(./Code, 'ENT')" />
				</AllConditions>
			</OneOffConditions>
			 -->
			 
            <!-- Here we specify the xpath to execute on a decomposable element to get the folder name to store the file in. -->
            <TargetFolderNames>
            	<!-- For items with a Stereotype element, we use the Stereotype. -->
            	<TargetFolderName xpath="./Stereotype" condition="string-length(./Stereotype) > 0" />
            	<!-- For items with a TargetStereotype element, we use the TargetStereotype (the TargetStereotype is specified in case of shortcuts). -->
            	<TargetFolderName xpath="./TargetStereotype" condition="string-length(./TargetStereotype) > 0" />
            	<!-- For items which don't have a Stereotype or TargetStereotype element, we use the name of the element. -->
            	<TargetFolderName xpath="translate(name(), ':', '_')" />
            </TargetFolderNames>
			
			<!-- Here we specify the xpath to execute on a decomposable element to get the file name (without .xml). -->
            <!-- Target target file names are considered in order of appearance in the config. Once a value has been found which hasen't been written before in the current run, the value is used. -->
            <!-- For example if the first TargetFileName would lead to the same file name (in the same target folder) for 2 decomposed elements,
                 the first one would get the name of the TargetFileName specified
                 and the second one will get the TargetFileName of the next configuration which leads to a value.
             -->
            <!-- If no value is found using any configuration an exception is thrown. -->
            <TargetFileNames>
            	<!-- For items with a Code element and it is unique for the given TargetFolder, we use the Code. -->
            	<TargetFileName xpath="translate(normalize-space(./Code), ' ', '_')" condition="string-length(./Code) > 0" />
            	<!-- For items with a Code and ObjectID element and it is NOT unique for the given TargetFolder, we use the Code and ObjectID. -->
            	<TargetFileName xpath="concat(translate(normalize-space(./Code), ' ', '_'), '_', ./ObjectID)" condition="string-length(./Code) > 0 and string-length(./ObjectID) > 0" />
            	<!-- For items without a Code element, but with an ObjectID we use the ObjectID. -->
            	<TargetFileName xpath="./ObjectID" condition="string-length(./ObjectID) > 0" />
            	<!-- For items which don't have a Code and ObjectID element, we use the name of the element. -->
            	<TargetFileName xpath="translate(name(), ':', '_')" />
            </TargetFileNames>
            
			<!-- Here we specify which information to include as an attribute on the xi:include tag. -->
			<IncludeAttributes />
			<!--<IncludeAttributes>
				<IncludeAttribute name="ObjectID" xpath="./ObjectID" />
			</IncludeAttributes>-->

		</DecomposableElement>

	</Decompose>
</PowerDeComposerConfig>
```