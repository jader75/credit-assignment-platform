param(
    [int]$TimeoutSeconds = 180,
    [int]$PollSeconds = 3
)

$ErrorActionPreference = 'Stop'

function Wait-UntilReady {
    param(
        [string]$Name,
        [scriptblock]$Check,
        [int]$TimeoutSeconds,
        [int]$PollSeconds
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            if (& $Check) {
                Write-Host "$Name pronto."
                return
            }
        } catch {
            Start-Sleep -Seconds $PollSeconds
            continue
        }

        Start-Sleep -Seconds $PollSeconds
    }

    throw "$Name nao ficou pronto em $TimeoutSeconds segundos."
}

function Start-HiddenProcess {
    param(
        [string]$FilePath,
        [string[]]$ArgumentList,
        [string]$WorkingDirectory,
        [string]$StdOutPath,
        [string]$StdErrPath,
        [switch]$PassThru
    )

    $startProcessArgs = @{
        FilePath         = $FilePath
        ArgumentList      = $ArgumentList
        WorkingDirectory  = $WorkingDirectory
        WindowStyle       = 'Hidden'
    }

    if ($StdOutPath) {
        $startProcessArgs.RedirectStandardOutput = $StdOutPath
    }

    if ($StdErrPath) {
        $startProcessArgs.RedirectStandardError = $StdErrPath
    }

    if ($PassThru) {
        $startProcessArgs.PassThru = $true
    }

    Start-Process @startProcessArgs
}

function Resolve-NodeExecutable {
    $candidates = @()

    $command = Get-Command node -ErrorAction SilentlyContinue
    if ($command) {
        $candidates += $command.Source
    }

    if ($env:ProgramFiles) {
        $candidates += (Join-Path $env:ProgramFiles 'nodejs\node.exe')
    }

    if ($env:LocalAppData) {
        $candidates += (Join-Path $env:LocalAppData 'Programs\nodejs\node.exe')
        $jetBrainsNodeRoots = Join-Path $env:LocalAppData 'JetBrains\*\acp-agents\.runtimes\node\*\bin\node.exe'
        $candidates += Get-ChildItem -Path $jetBrainsNodeRoots -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
    }

    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path $candidate)) {
            return (Resolve-Path $candidate).Path
        }
    }

    throw 'Nao foi possivel localizar o executavel do Node.js. Instale o Node.js ou ajuste o PATH antes de executar este script.'
}

function Resolve-Java21Home {
    $javaCommands = Get-Command java -All -ErrorAction SilentlyContinue

    foreach ($javaCommand in $javaCommands) {
        $javaExecutable = $javaCommand.Source
        if (-not (Test-Path $javaExecutable)) {
            continue
        }

        $processStartInfo = [System.Diagnostics.ProcessStartInfo]::new()
        $processStartInfo.FileName = $javaExecutable
        $processStartInfo.Arguments = '-version'
        $processStartInfo.RedirectStandardOutput = $true
        $processStartInfo.RedirectStandardError = $true
        $processStartInfo.UseShellExecute = $false
        $processStartInfo.CreateNoWindow = $true

        $process = [System.Diagnostics.Process]::Start($processStartInfo)
        $versionOutput = $process.StandardError.ReadToEnd() + $process.StandardOutput.ReadToEnd()
        $process.WaitForExit()

        if ($versionOutput -match 'version "21\.' -or $versionOutput -match 'openjdk version "21\.') {
            return (Split-Path -Parent (Split-Path -Parent $javaExecutable))
        }
    }

    throw 'Nao foi possivel localizar um Java 21 no PATH. Instale ou ajuste o JAVA_HOME antes de executar este script.'
}

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
Set-Location $repoRoot

$logRoot = Join-Path $PSScriptRoot 'logs'
New-Item -ItemType Directory -Path $logRoot -Force | Out-Null
$backendOutLog = Join-Path $logRoot 'backend.out.log'
$backendErrLog = Join-Path $logRoot 'backend.err.log'
$frontendOutLog = Join-Path $logRoot 'frontend.out.log'
$frontendErrLog = Join-Path $logRoot 'frontend.err.log'
$backendPidFile = Join-Path $logRoot 'backend.pid'
$frontendPidFile = Join-Path $logRoot 'frontend.pid'

function Write-PidFile {
    param(
        [string]$Path,
        [int]$ProcessId
    )

    Set-Content -Path $Path -Value $ProcessId -NoNewline
}

Write-Host 'Subindo PostgreSQL...'
docker compose up -d credit-postgres

Wait-UntilReady -Name 'PostgreSQL' -TimeoutSeconds $TimeoutSeconds -PollSeconds $PollSeconds -Check {
    (docker inspect -f '{{.State.Health.Status}}' credit-postgres-db 2>$null) -eq 'healthy'
}

try {
    Write-Host 'Subindo backend...'
    $javaHome = Resolve-Java21Home
    $backendCommand = @'
Set-Location "{0}"
$env:JAVA_HOME = "{1}"
$env:PATH = "{2};{3}"
& .\gradlew.bat bootRun > "{4}" 2> "{5}"
'@ -f $repoRoot, $javaHome, (Join-Path $javaHome 'bin'), $env:PATH, $backendOutLog, $backendErrLog

    $backendProcess = Start-HiddenProcess `
        -FilePath 'powershell.exe' `
        -ArgumentList @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-Command', $backendCommand) `
        -WorkingDirectory $repoRoot `
        -PassThru
    Write-PidFile -Path $backendPidFile -ProcessId $backendProcess.Id

    Wait-UntilReady -Name 'Backend' -TimeoutSeconds $TimeoutSeconds -PollSeconds $PollSeconds -Check {
        try {
            ((Invoke-RestMethod 'http://localhost:8080/actuator/health' -TimeoutSec 5).status) -eq 'UP'
        } catch {
            $false
        }
    }

    $frontendRoot = Join-Path $repoRoot 'frontend'
    if (-not (Test-Path (Join-Path $frontendRoot 'node_modules'))) {
        throw 'Execute `npm install` em `frontend` antes de iniciar o front.'
    }

    $nodeCommand = Resolve-NodeExecutable
    $angularCli = Join-Path $frontendRoot 'node_modules\@angular\cli\bin\ng.js'
    if (-not (Test-Path $angularCli)) {
        throw 'Nao foi possivel localizar `frontend\node_modules\@angular\cli\bin\ng.js`. Execute `npm install` em `frontend` novamente.'
    }

    Write-Host 'Subindo frontend...'
    $frontendProcess = Start-HiddenProcess `
        -FilePath $nodeCommand `
        -ArgumentList @($angularCli, 'serve', '--host', '0.0.0.0', '--port', '4200') `
        -WorkingDirectory $frontendRoot `
        -StdOutPath $frontendOutLog `
        -StdErrPath $frontendErrLog `
        -PassThru
    Write-PidFile -Path $frontendPidFile -ProcessId $frontendProcess.Id

    Wait-UntilReady -Name 'Frontend' -TimeoutSeconds $TimeoutSeconds -PollSeconds $PollSeconds -Check {
        try {
            (Invoke-WebRequest 'http://localhost:4200/' -UseBasicParsing -TimeoutSec 5).StatusCode -eq 200
        } catch {
            $false
        }
    }
} catch {
    & (Join-Path $PSScriptRoot 'stop-dev.ps1')
    throw
}

Write-Host 'Ambiente pronto.'
Write-Host 'Backend:  http://localhost:8080/actuator/health'
Write-Host 'Frontend: http://localhost:4200/'
Write-Host "Logs do backend:  $backendOutLog e $backendErrLog"
Write-Host "Logs do frontend: $frontendOutLog e $frontendErrLog"
