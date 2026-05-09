# Remove BOM and run Spring Boot
$javaFiles = Get-ChildItem -Path 'd:\smartMedicine\smart-medicine\src\main\java' -Filter '*.java' -Recurse
foreach ($file in $javaFiles) {
    $raw = [System.IO.File]::ReadAllBytes($file.FullName)
    if ($raw.Length -ge 3 -and $raw[0] -eq 0xEF -and $raw[1] -eq 0xBB -and $raw[2] -eq 0xBF) {
        $clean = $raw[3..($raw.Length-1)]
        [System.IO.File]::WriteAllBytes($file.FullName, $clean)
    }
}
cd d:\smartMedicine\smart-medicine
mvn spring-boot:run
