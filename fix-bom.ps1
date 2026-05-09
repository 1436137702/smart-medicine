# Force remove all BOM from Java files and clean build
$javaFiles = Get-ChildItem -Path 'd:\smartMedicine\smart-medicine\src\main\java' -Filter '*.java' -Recurse
foreach ($file in $javaFiles) {
    $raw = [System.IO.File]::ReadAllBytes($file.FullName)
    if ($raw.Length -ge 3 -and $raw[0] -eq 0xEF -and $raw[1] -eq 0xBB -and $raw[2] -eq 0xBF) {
        $clean = $raw[3..($raw.Length-1)]
        [System.IO.File]::WriteAllBytes($file.FullName, $clean)
        Write-Host "Removed BOM from: $($file.Name)"
    }
}
Write-Host "All BOM characters removed. Ready to build."
