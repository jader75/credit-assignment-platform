Write-Host ""
Write-Host "Running quality gate..."

./gradlew.bat clean check

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "Push blocked."
    exit 1
}

Write-Host ""
Write-Host "Quality gate OK."
exit 0