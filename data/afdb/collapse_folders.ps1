
$jars = Get-Content -Path .\download_metadata.json -Raw | ConvertFrom-Json 
foreach ($jar in $jars) {

    $pdbDir = ".\pdb"
    $proDir = $pdbDir + "\" + $jar.reference_proteome
    $proFiles = $proDir + "\*.gz"
    
    # move pro files up a directory
    Move-Item -Path $proFiles -Destination $pdbDir

    # remove pro directory
    Remove-Item -Path $proDir
}