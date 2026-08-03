$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$envFile = Join-Path $root '.env.supabase.local'
$logFile = $env:SUPABASE_RUN_LOG
if (-not $logFile) { $logFile = Join-Path $env:TEMP 'opencode\supabase-backend.log' }

if (-not (Test-Path -LiteralPath $envFile)) { throw "Missing $envFile" }

foreach ($line in Get-Content -LiteralPath $envFile) {
    $line = $line.Trim()
    if ($line -eq '' -or $line.StartsWith('#')) { continue }
    $kv = $line -split '=', 2
    if ($kv.Count -eq 2 -and -not [string]::IsNullOrWhiteSpace($kv[1])) {
        Set-Item -Path "Env:$($kv[0].Trim())" -Value $kv[1].Trim().Trim('"', "'")
    }
}

$logDir = Split-Path -Parent $logFile
if (-not (Test-Path -LiteralPath $logDir)) { New-Item -ItemType Directory -Path $logDir -Force | Out-Null }

Write-Host "Starting backend  profile=$env:SPRING_PROFILES_ACTIVE  host=$env:SUPABASE_DB_HOST  port=8081"
Write-Host "Log: $logFile"

Set-Location -LiteralPath $root
mvn spring-boot:run 2>&1 | Tee-Object -FilePath $logFile
