$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$envFile = Join-Path $root ".env"
$minimumNodeVersion = [Version]"20.19.0"

function Get-DotEnvValues {
    param([string]$Path)

    $values = @{}

    if (-not (Test-Path $Path)) {
        return $values
    }

    Get-Content $Path | ForEach-Object {
        $line = $_.Trim()

        if (-not $line -or $line.StartsWith("#")) {
            return
        }

        $parts = $line -split "=", 2
        if ($parts.Length -eq 2) {
            $values[$parts[0]] = $parts[1]
        }
    }

    return $values
}

function Set-ProcessEnvironmentVariables {
    param([hashtable]$Variables)

    $previousValues = @{}

    foreach ($name in $Variables.Keys) {
        $previousValues[$name] = [System.Environment]::GetEnvironmentVariable($name, "Process")
        [System.Environment]::SetEnvironmentVariable($name, $Variables[$name], "Process")
    }

    return $previousValues
}

function Restore-ProcessEnvironmentVariables {
    param([hashtable]$Variables)

    foreach ($name in $Variables.Keys) {
        [System.Environment]::SetEnvironmentVariable($name, $Variables[$name], "Process")
    }
}

function Get-BackendEnvironmentVariables {
    param([hashtable]$DotEnvValues)

    $backendEnvironment = @{}

    foreach ($name in $DotEnvValues.Keys) {
        $backendEnvironment[$name] = $DotEnvValues[$name]
    }

    $datasourceUrl = $backendEnvironment["SPRING_DATASOURCE_URL"]
    if ($datasourceUrl -and $datasourceUrl -match "^jdbc:postgresql://postgres:5432/") {
        $backendEnvironment["SPRING_DATASOURCE_URL"] = $datasourceUrl -replace "^jdbc:postgresql://postgres:5432/", "jdbc:postgresql://localhost:5433/"
    }

    return $backendEnvironment
}

function Start-PostgresContainerIfNeeded {
    param([hashtable]$BackendEnvironment)

    $datasourceUrl = $BackendEnvironment["SPRING_DATASOURCE_URL"]
    if (-not $datasourceUrl -or $datasourceUrl -notmatch "^jdbc:postgresql://localhost:5433/") {
        return
    }

    $dockerCommand = Get-Command docker -ErrorAction SilentlyContinue
    if (-not $dockerCommand) {
        throw "Docker est requis pour demarrer PostgreSQL sur localhost:5433. Installe ou demarre Docker Desktop, ou adapte SPRING_DATASOURCE_URL pour viser une base locale existante."
    }

    Write-Host "Demarrage du conteneur PostgreSQL de dev..."
    & $dockerCommand.Source compose up -d postgres

    if ($LASTEXITCODE -ne 0) {
        throw "Impossible de demarrer le service PostgreSQL via Docker Compose. Verifie que Docker Desktop tourne puis relance le script."
    }
}

$dotEnvValues = Get-DotEnvValues -Path $envFile
$backendEnvironment = Get-BackendEnvironmentVariables -DotEnvValues $dotEnvValues

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

$previousEnvironment = Set-ProcessEnvironmentVariables -Variables $backendEnvironment

try {
    Start-PostgresContainerIfNeeded -BackendEnvironment $backendEnvironment

    Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-Command",
        "Set-Location '$frontendDir'; npm start"
    )

    Set-Location $backendDir
    & ".\mvnw.cmd" "spring-boot:run"
}
finally {
    Restore-ProcessEnvironmentVariables -Variables $previousEnvironment
}
