param(
    [int]$TimeoutSeconds = 180,
    [int]$PollSeconds = 3
)

$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

& (Join-Path $scriptDir 'stop-dev.ps1')
& (Join-Path $scriptDir 'start-dev.ps1') -TimeoutSeconds $TimeoutSeconds -PollSeconds $PollSeconds
