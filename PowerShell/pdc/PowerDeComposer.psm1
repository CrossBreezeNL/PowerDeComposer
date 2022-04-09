###
# PowerDeComposer PowerShell module
###

# The location of the PowerDeComposer jar file.
$PDCJarLocation = "$PSScriptRoot\PowerDeComposer.jar"
# Set minimum memory to 64MB and maximum to 2GB.
$JavaArguments = "-Xms128M -Xmx2G"
# The location of the PowerDeComposer LDM config file.
$LdmConfigFileLocation = "$PSScriptRoot\pdc_config_ldm.xml"
# The location of the PowerDeComposer XEM config file.
$XemConfigFileLocation = "$PSScriptRoot\pdc_config_xem.xml"
# The location of the PowerDeComposer SWS config file.
$SwsConfigFileLocation = "$PSScriptRoot\pdc_config_sws.xml"

# Function to invoke PowerDeComposer as a java call.
Function Invoke-PowerDeComposer {
    param (
        $Mode,
        $InputLocation,
        $OutputLocation,
        $ConfigFileLocation
    )

    # Create the Java call arguments list.
    $JavaCall = "$JavaArguments -jar -Dfile.encoding=UTF-8 $PDCJarLocation $Mode `"$InputLocation`" `"$OutputLocation`""
    # If the config file locatin is set, add it to the java call.
    if ($ConfigFileLocation.Length -gt 0) {
        $JavaCall += " `"$ConfigFileLocation`""
    }

    try
    {
        $pinfo = New-Object System.Diagnostics.ProcessStartInfo
        $pinfo.FileName = "java"
        $pinfo.RedirectStandardError = $true
        $pinfo.RedirectStandardOutput = $true
        $pinfo.UseShellExecute = $false
        $pinfo.Arguments = $JavaCall
        $pInfo.CreateNoWindow = $true
        $pdcProcess = New-Object System.Diagnostics.Process
        $pdcProcess.StartInfo = $pinfo
        $pdcProcess.Start() # | Out-Null
        #$pdcProcess.WaitForExit()

        # Write the process standard out for debug purposes.
        Write-Debug "Process standard out:"
        Write-Debug "--------------------------------------------------"
        Write-Debug $pdcProcess.StandardOutput.ReadToEnd()
        Write-Debug "--------------------------------------------------"

        [pscustomobject]@{
            #StandardOut = $pdcProcess.StandardOutput.ReadToEnd()
            StandardError = $pdcProcess.StandardError.ReadToEnd()
            ExitCode = $pdcProcess.ExitCode
        }
    }
    catch [Exception]
    {
        #Write-Host $_
        [pscustomobject]@{
            #StandardOut = $null
            StandardError = $_
            ExitCode = 1
        }
    }
}

# Function to invoke a decompose.
function Invoke-DecomposeModel {
    param (
        $ModelFileLocation,
        $TargetFolderLocation,
        $ConfigFileLocation
    )

    # Use the Invoke-PowerDeComposer function to perform the decompose.
    $ProcessResults = Invoke-PowerDeComposer -Mode "decompose" -InputLocation $ModelFileLocation -OutputLocation $TargetFolderLocation -ConfigFileLocation $ConfigFileLocation
    # If the process failed, throw the error.
    if (!$ProcessResults.ExitCode.Equals(0)) {
        throw "Decomposing '$ModelFileLocation' failed: $($ProcessResults.StandardError)"
    }
}

# Function to invoke a model compose.
function Invoke-ComposeModel {
    param (
        $ModelFileLocation,
        $TargetFileLocation
    )

    $TargetFolderLocation = Split-Path -Path $TargetFileLocation -Parent
    $TargetFolderLocation_Exists = Test-Path $TargetFolderLocation
    # If the target folder doesn't exist, create it.
    If (!$TargetFolderLocation_Exists) {
        Write-Host "The target folder location ($TargetFolderLocation) doesn't exist, so creating it."
        New-Item -Path $TargetFolderLocation -ItemType Directory
    }

    # Use the Invoke-PowerDeComposer function to perform the decompose.
    $ProcessResults = Invoke-PowerDeComposer -Mode "compose" -InputLocation $ModelFileLocation -OutputLocation $TargetFileLocation
    # If the process failed, throw the error.
    if (!$ProcessResults.ExitCode.Equals(0)) {
        throw "Composing '$ModelFileLocation' failed: $($ProcessResults.StandardError)"
    }
}
Export-ModuleMember -Function Invoke-ComposeModel

# Function to invoke a LDM model decompose.
function Invoke-DecomposeLDM {
    param (
        $ModelFileLocation,
        $TargetFolderLocation
    )

    Invoke-DecomposeModel -ModelFileLocation $ModelFileLocation -TargetFolderLocation $TargetFolderLocation -ConfigFileLocation $LdmConfigFileLocation
}
Export-ModuleMember -Function Invoke-DecomposeLDM

# Function to invoke a XEM decompose.
function Invoke-DecomposeXEM {
    param (
        $ModelFileLocation,
        $TargetFolderLocation
    )

    Invoke-DecomposeModel -ModelFileLocation $ModelFileLocation -TargetFolderLocation $TargetFolderLocation -ConfigFileLocation $XemConfigFileLocation
}
Export-ModuleMember -Function Invoke-DecomposeXEM

# Function to invoke a XEM decompose.
function Invoke-DecomposeSWS {
    param (
        $ModelFileLocation,
        $TargetFolderLocation
    )

    Invoke-DecomposeModel -ModelFileLocation $ModelFileLocation -TargetFolderLocation $TargetFolderLocation -ConfigFileLocation $SwsConfigFileLocation
}
Export-ModuleMember -Function Invoke-DecomposeSWS