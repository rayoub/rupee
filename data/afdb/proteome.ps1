
$jars = Get-Content -Path .\download_metadata.json -Raw | ConvertFrom-Json 
foreach ($jar in $jars) {

    # output a line
    $line = @(
        $jar.reference_proteome,
        $jar.species,
        $jar.common_name
    ) -join ','
    $line | Out-File -Append .\proteome.txt
}