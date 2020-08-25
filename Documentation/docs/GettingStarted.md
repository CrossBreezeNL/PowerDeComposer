# Getting started
On this wiki you will find all needed documentation to get started with PowerDeComposer.

[//]: #(test met comment)

## How to get PowerDeComposer
PowerDeComposer is available as a [standalone download](https://dev.azure.com/x-breeze/PowerDeComposer_Public/_git/PowerDeComposer_Public?path=%2FRun) or via [Maven](https://repo1.maven.org/maven2/com/x-breeze/powerdecomposer/PowerDeComposer/). You can either download the latest standalone jar or create a Maven pom.xml file to let the Maven utility download and execute the latest jar.

## How to use PowerDeComposer standalone
In order to use PowerDeComposer you need to call the utility using the command line.

### Decompose a model
The decompose command consist of the following parts:

``` xml
decompose <ModelFileLocation> <TargetFolderLocation> [<ConfigFileLocation>]
```

#### Parameters
| Config element           | Required | Description |
|:---                      |:---      |:---         |
| ModelFileLocation        | Yes      | The file location of the model to decompose. |
| TargetFolderLocation     | Yes      | The folder location to write the decomposed model to. |
| ConfigFileLocation       | No       | The file location of the configuration file. See [Configuration](Configuration.md). |

#### Example

Example command line call to decompose a PowerDesigner model file into a decomposed model:
``` xml
java -jar XmlDeComposer.jar decompose "C:\Path\To\ExamplePowerDesignerModelFile.ldm" "C:\Path\To\Decomposed\Output"
```

### Compose a model
The compose command consist of the following parts:

``` xml
compose <ModelFileLocation> <TargetFileLocation> [<ConfigFileLocation>]
```

#### Parameters
| Config element           | Required | Description |
|:---                      |:---      |:---         |
| ModelFileLocation        | Yes      | The file location of the model to compose. |
| TargetFileLocation       | Yes      | The file location to write the composed model to. |
| ConfigFileLocation       | No       | The file location of the configuration file. See [Configuration](Configuration.md). |

#### Example
Example command line call to compose a PowerDesigner model file based on the decomposed model:

``` xml
java -jar XmlDeComposer.jar compose "C:\Path\To\Decomposed\Output\DecomposedModelFile.ldm" "C:\Path\To\ExamplePowerDesignerModelFile.ldm"
```

## How to use PowerDeComposer with Maven
Maven is a build tool that can be used for automating different tasks in a build process. Maven can also be used to obtain and run a specific version of PowerDeComposer. For this you need to have [Maven installed](http://maven.apache.org/download.cgi).

For running PowerDeComposer with maven see the example pom.xml and command files [here](https://dev.azure.com/x-breeze/PowerDeComposer_Public/_git/PowerDeComposer_Public?path=%2FRunWithMaven)

Please note that you can only run PowerDeComposer versions from 1.1.0 and up via this route.

## Bugs & issues
When you encounter an issue while using PowerDeComposer please report it by sending an e-mail to [info@x-breeze.com](mailto:info@x-breeze.com?SUBJECT=PowerDeComposer%20-%20Bug%20report) with the subject 'PowerDeComposer - Bug report'.

Please provide the following information:

- Steps to reproduce
- Expected behaviour
- PowerDeComposer version used
