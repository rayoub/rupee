
$jars = Get-Content -Path .\download_metadata.json -Raw | ConvertFrom-Json 
foreach ($jar in $jars) {

    $dir = ".\pdb\" + $jar.reference_proteome
    
    $proteins = Get-ChildItem -Path $dir | Select-Object -ExpandProperty BaseName | ForEach-Object { $_.Substring(0, $_.Length - 4) }
    foreach($protein in $proteins) {

        # output a line
        $line = @(
            $jar.reference_proteome,
            $protein
        ) -join ','
        $line | Out-File -Append .\protein.txt
    }
}