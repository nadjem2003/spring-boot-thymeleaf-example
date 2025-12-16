# PowerShell script to check OWASP dependency-check reports
Write-Host "=== OWASP Dependency-Check Report Checker ===" -ForegroundColor Green

Write-Host "`nChecking target directory structure..." -ForegroundColor Yellow
Get-ChildItem target -Directory | Select-Object Name

Write-Host "`nLooking for security reports..." -ForegroundColor Yellow
$reports = Get-ChildItem target -Recurse -Include "*.html", "*.xml", "*.json" -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -like "*dependency*" -or $_.FullName -like "*security*" -or $_.FullName -like "*report*" }

if ($reports) {
    Write-Host "Found reports:" -ForegroundColor Green
    $reports | Select-Object FullName
} else {
    Write-Host "No reports found!" -ForegroundColor Red
}

Write-Host "`nChecking if dependency-check directory exists..." -ForegroundColor Yellow
if (Test-Path "target/dependency-check") {
    Write-Host "target/dependency-check exists!" -ForegroundColor Green
    Get-ChildItem "target/dependency-check" | Select-Object Name
} else {
    Write-Host "target/dependency-check does not exist!" -ForegroundColor Red
}

if (Test-Path "target/security-reports") {
    Write-Host "target/security-reports exists!" -ForegroundColor Green
    Get-ChildItem "target/security-reports" | Select-Object Name
} else {
    Write-Host "target/security-reports does not exist!" -ForegroundColor Red
}
