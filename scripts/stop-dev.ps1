param(
    [int[]]$Ports = @(8080, 4200)
)

$ErrorActionPreference = 'Stop'

function Stop-ProcessById {
    param(
        [int]$ProcessId
    )

    try {
        Stop-Process -Id $ProcessId -Force -ErrorAction Stop
        Write-Host "Processo $ProcessId parado."
    } catch {
        Write-Host "Nao foi possivel parar o processo $ProcessId."
    }
}

function Stop-ProcessFromPidFile {
    param(
        [string]$PidFile
    )

    if (-not (Test-Path $PidFile)) {
        return
    }

    $rawPid = (Get-Content $PidFile -Raw).Trim()
    $parsedPid = 0
    if ([int]::TryParse($rawPid, [ref]$parsedPid)) {
        Stop-ProcessById -ProcessId $parsedPid
    }

    Remove-Item $PidFile -Force -ErrorAction SilentlyContinue
}

function Stop-ListeningPort {
    param(
        [int]$Port
    )

    $connections = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    foreach ($connection in $connections) {
        if ($connection.OwningProcess) {
            try {
                Stop-Process -Id $connection.OwningProcess -Force -ErrorAction Stop
                Write-Host "Processo na porta $Port parado (PID $($connection.OwningProcess))."
            } catch {
                Write-Host "Nao foi possivel parar o processo na porta $Port (PID $($connection.OwningProcess))."
            }
        }
    }
}

function Wait-UntilStopped {
    param(
        [string]$Description,
        [scriptblock]$Check,
        [int]$TimeoutSeconds = 30
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (& $Check) {
            Start-Sleep -Seconds 1
            continue
        }

        return
    }

    throw "$Description nao encerrou dentro do prazo."
}

Write-Host 'Encerrando processos registrados...'
$logRoot = Join-Path $PSScriptRoot 'logs'
Stop-ProcessFromPidFile -PidFile (Join-Path $logRoot 'frontend.pid')
Stop-ProcessFromPidFile -PidFile (Join-Path $logRoot 'backend.pid')

Write-Host 'Parando frontend/backend...'
foreach ($port in $Ports) {
    Stop-ListeningPort -Port $port
}

Write-Host 'Parando PostgreSQL...'
docker compose stop credit-postgres
docker compose stop credit-redis

Wait-UntilStopped -Description 'PostgreSQL' -Check {
    try {
        (docker inspect -f '{{.State.Running}}' credit-postgres-db 2>$null) -eq 'true'
    } catch {
        $false
    }
}

Wait-UntilStopped -Description 'Backend/frontend' -Check {
    $listeners = Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue | Where-Object { $_.LocalPort -in $Ports }
    $listeners.Count -gt 0
}

Write-Host 'Ambiente parado.'
