
$jars = Get-Content -Path .\download_metadata.json -Raw | ConvertFrom-Json 
foreach ($jar in $jars) {

    $dir = ".\pdb\" + $jar.reference_proteome
    
    # make directory based on reference_proteome
    mkdir -p $dir

    # copy tar files to directory
    Move-Item -Path $jar.archive_name -Destination $dir

    # move to directory
    Set-Location $dir 

    # extract tar file
    tar xvf $jar.archive_name

    # remove tar file
    Remove-Item *.tar
    
    # remove the cif files
    Remove-Item *.cif.gz

    # move back
    Set-Location $PSScriptRoot
}