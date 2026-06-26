Write-Host ""
Write-Host "Running pre-commit quality gate..."

./gradlew.bat spotlessApply
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "Spotless apply failed."
    exit 1
}

Write-Host ""
Write-Host "Pre-commit formatting OK."
exit 0
