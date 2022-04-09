# Define arguments to be populated by the method in Powerdesigner
$Extension=$args[0] # For Example "LDM"
$ModelSubFolder=$args[1] # For Example "SourceSystems"
$ModelName=$args[2] # For Example "ExampleSource"

# Set the debug preference to Continue so the debug output is printed. Default is SilentlyContinue.
$DebugPreference = "SilentlyContinue"
#$DebugPreference = "Continue"

# Include PowerDeComposer PowerShell Functions.
Import-Module  $PSScriptRoot\PowerDeComposer.psm1 -Force

# Extension argument will be used as the actual extension of the file, so make it lowercase.
$Extension = $Extension.ToLower()

# Variables used for PowerDeComposer.
$ComposedFolder = [System.IO.DirectoryInfo]"$PSScriptRoot\..\composed"
$DecomposedFolder = [System.IO.DirectoryInfo]"$PSScriptRoot\..\decomposed"

# Given the base ComposedFolder, DecomposedFolder and the arguments
# Construct the ModelFileLocation and TargetFolderLocation.
$ModelFileLocation = Join-Path -Path $ComposedFolder -childPath "$ModelSubFolder\$ModelName.$Extension"
$TargetFolderLocation = Join-Path -Path $DecomposedFolder -childPath "$ModelSubFolder\$ModelName"

switch ($Extension) {
    "xem" {  
        # Invoke the Decompose on the XEM.
        Invoke-DecomposeXEM -ModelFileLocation $ModelFileLocation -TargetFolderLocation $TargetFolderLocation
    }
    "ldm" { 
        # Invoke the Decompose on the LDM.
        Invoke-DecomposeLDM -ModelFileLocation $ModelFileLocation -TargetFolderLocation $TargetFolderLocation
     }
    "pdm" {
        # Invoke the Decompose on the PDM using the LDM config.
        # TODO: Change this to call a DecomposePDM function and have a specific configuration for this.
        Invoke-DecomposeLDM -ModelFileLocation $ModelFileLocation -TargetFolderLocation $TargetFolderLocation
    }
    "sws" {  
        # Invoke the Decompose on the SWS.
        Invoke-DecomposeSWS -ModelFileLocation $ModelFileLocation -TargetFolderLocation $TargetFolderLocation
    }    
    Default {}
}