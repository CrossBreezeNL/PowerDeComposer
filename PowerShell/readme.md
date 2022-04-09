# PowerDeComposer PowerShell module

To simplify running PowerDeComposer a PowerShell module, scripts and sample configurations were developed. With this set you can get a kick-start into using PowerDeComposer, simply follow the steps in the 'Quick start' section and you're good to go.

## Quick start

When you want to simply use all the scripts here, follow these steps:

> Make sure you have PowerShell 7 or higher installed.
> Download it [here](https://docs.microsoft.com/en-us/powershell/scripting/install/installing-powershell-on-windows#msi)

1. Download the 'PowerShell' folder (click on the dots in the right top corner, next to the '+ New' button, and choose 'Download as Zip').
1. Copy the whole contents of the 'PowerShell' folder into the location where you want to manage your PowerDesigner models (a git managed folder).
1. Create a folder 'composed' (next to the 'pdc' folder).
1. Create/move all your PowerDesigner models into the composed folder.
1. Copy the latest PowerDeComposer jar file into the 'pdc' folder (download it [here](https://dev.azure.com/x-breeze/PowerDeComposer_Public/_git/PowerDeComposer_Public?path=%2FRun)).
1. Execute the 'decompose_all.ps1' PowerShell script to decompose all models in the composed folder.
1. Add the 'composed' folder to gitignore (only the decomposed models need to be checked in).

If for example the folder where you want to store you models is called 'models' and we have a ExampleSource.ldm model in a SourceSystems folder, the folder structure will be as follows:

- models
   - composed
      - SourceSystems
         - ExampleSource.ldm
   - decomposed
      - SourceSystems
         - ExampleSource
            - Entities
               - ...
            - ...
            - ExampleSource.ldm
   - pdc
      - decompose.ps1
      - pdc_config_ldm.xml
      - pdc_config_sws.xml
      - pdc_config_xem.xml
      - PowerDeComposer.jar
      - PowerDeComposer.psm1
   - compose_all.ps1
   - decompose_all.ps1


## PowerShell module

The module provides simple cmdlets to invoke a decompose or compose. The PowerShell module itself can be found [here](./pdc/PowerDeComposer.psm1).

To use the module it needs to be imported using the Import-Module cmdlet. For example:

``` powershell
Import-Module -Name "$PSScriptRoot\PowerDeComposer.psm1" -Force
```

## PowerShell scripts

Some PowerShell scripts to decompose and compose files are also available which use the PowerShell module. The scripts are described below.

### compose_all

In order to compose all models, [this](./compose_all.ps1) PowerShell script can be executed. It will compose all decomposed LDM, PDM, XEM and SWS models which reside in the 'decomposed' folder (relative to the script file).

### decompose_all

In order to decompose all models, [this](./decompose_all.ps1) PowerShell script can be executed. It will decompose all LDM, PDM, XEM and SWS files which reside in the 'composed' folder (relative to the script file). Files in subfolders will also be decomposed.

### decompose

In order to decompose one model, [this](./pdc/decompose.ps1) PowerShell script can be executed. Since it is ment only to decompose 1 model, it requires you to pass in the needed arguments to find the file to decompose and the folder to write to. The arguments are:

| Position | Description                          | Example       |
|----------|--------------------------------------|---------------|
| 1        | Extension                            | LDM           |
| 2        | ModelSubFolder                       | SourceSystems |
| 3        | ModelName (without file extensions!) | ExampleSource |


## PowerDeComposer configurations

As part of this PowerShell module example configurations are provided for PowerDesigner LDM, XEM and SWS files.

- [pdc/pdc_config_ldm.xml](./pdc/pdc_config_ldm.xml) - LDM (logical data model) decompose configuration
- [pdc/pdc_config_sws.xml](./pdc/pdc_config_sws.xml) - SWS (workspace) decompose configuration
- [pdc/pdc_config_xem.xml](./pdc/pdc_config_xem.xml) - XEM (extension) decompose configuration
