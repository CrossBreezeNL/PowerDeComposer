# Configuration for PowerDeComposer

## PowerDeComposerConfig
The PowerDeComposer configuration for composing and decomposing model files.

### Example
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<PowerDeComposerConfig>
	<ElementsToExtract>
		<Element>...</Element>
		<Element>...</Element>
		...
	</ElementsToExtract>
	<TargetFileNameSubElement>...</TargetFileNameSubElement>
	<IncludeSubElements>
		<SubElement>...</SubElement>
		<SubElement>...</SubElement>
		...
	</IncludeSubElements>
</PowerDeComposerConfig>
```

### Config elements
| Config element           | Description |
|:---                      |:---         |
| ElementsToExtract        | List of document nodes (Element) for which the child elements need to be extracted into seperate files (XPath expression per node). |
| TargetFileNameSubElement | The sub element contents to use as the filename (without extension) for the extracted elements. For PowerDesigner objects this can for examle be ObjectID, Code or Name. |
| IncludeSubElements       | List of sub elements (SubElement) to include as an attribute on the include instruction. If for example ObjectID is used for the TargetFileNameSubElement you might want to include the Code or Name in the include instruction for the file to know what object it is about. |

### Default config
When not configuration file is specified on the command, the following configuration will be used by default.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<PowerDeComposerConfig>
	<ElementsToExtract>
		<Element>c:DBMS</Element>
		<Element>c:ExtendedModelDefinitions</Element>
		<Element>c:PhysicalDiagrams</Element>
		<Element>c:DataSources</Element>
		<Element>c:Packages</Element>
		<Element>c:Mappings</Element>
		<Element>c:Tables</Element>
		<Element>c:References</Element>
		<Element>c:Reports</Element>
		<Element>c:SourceModels</Element>
		<Element>c:Users</Element>
		<Element>c:TargetModels</Element>
		<Element>c:Entities</Element>
		<Element>c:Relationships</Element>
		<Element>c:LogicalDiagrams</Element>
	</ElementsToExtract>	
	<IncludeSubElements>
		<SubElement>Name</SubElement>
	</IncludeSubElements>
	<TargetFileNameSubElement>ObjectID</TargetFileNameSubElement>
</PowerDeComposerConfig>
```