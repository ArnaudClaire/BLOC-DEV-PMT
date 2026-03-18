$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$envFile = Join-Path $root ".env"
$minimumNodeVersion = [Version]"20.19.0"

function Import-DotEnv {
    param([string]$Path)

    if (-not (Test-Path $Path)) {
        return
    }

    Get-Content $Path | ForEach-Object {
        $line = $_.Trim()

        if (-not $line -or $line.StartsWith("#")) {
            return
        }

        $parts = $line -split "=", 2
        if ($parts.Length -eq 2) {
            [System.Environment]::SetEnvironmentVariable($parts[0], $parts[1])
        }
    }
}

Import-DotEnv -Path $envFile

$frontendDir = Join-Path $root "frontend"
$backendDir = Join-Path $root "backend"
$nodeVersionOutput = & node -v 2>$null

if (-not $nodeVersionOutput) {
    throw "Node.js est introuvable. Installe Node 20.19.0 LTS puis relance le script."
}

$currentNodeVersion = [Version]($nodeVersionOutput.TrimStart("v"))
if ($currentNodeVersion -lt $minimumNodeVersion) {
    throw "Node.js $currentNodeVersion detecte. Le frontend requiert Node 20.19.0 LTS minimum. Lance 'nvm use 20.19.0' puis relance ce script."
}

Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "Set-Location '$frontendDir'; npm start"
)

Set-Location $backendDir
& ".\mvnw.cmd" "spring-boot:run"
