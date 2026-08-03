# ============================================================================
# Fellow Lodge - run the backend against Supabase PostgreSQL
# Reads credentials from the LOCAL, GITIGNORED file .env.supabase.local and
# starts the app with the "supabase" profile on port 8081.
#   Usage:  .\run-supabase.ps1
# Local dev (H2) is untouched - keep using `mvn spring-boot:run` for that.
# ============================================================================
$ErrorActionPreference = 'Stop'

$root = $PSScriptRoot
$envFile = Join-Path $root '.env.supabase.local'

if (-not (Test-Path -LiteralPath $envFile)) {
    Write-Host "[ERROR] Missing $envFile" -ForegroundColor Red
    Write-Host "        Copy .env.supabase.local.example to .env.supabase.local and fill in your credentials."
    exit 1
}

# Load KEY=VALUE lines into the process environment (does not touch the OS).
foreach ($line in Get-Content -LiteralPath $envFile) {
    $line = $line.Trim()
    if ($line -eq '' -or $line.StartsWith('#')) { continue }
    $kv = $line -split '=', 2
    if ($kv.Count -eq 2 -and -not [string]::IsNullOrWhiteSpace($kv[1])) {
        Set-Item -Path "Env:$($kv[0].Trim())" -Value $kv[1].Trim().Trim('"', "'")
    }
}

$required = @('SUPABASE_DB_HOST', 'SUPABASE_DB_PORT', 'SUPABASE_DB_NAME',
              'SUPABASE_DB_USERNAME', 'SUPABASE_DB_PASSWORD')
foreach ($k in $required) {
    $v = (Get-Item "Env:$k" -ErrorAction SilentlyContinue).Value
    if ([string]::IsNullOrWhiteSpace($v) -or $v -match '<' -or $v -eq 'your-database-password') {
        Write-Host "[ERROR] Required key $k is not set or still a placeholder in $envFile" -ForegroundColor Red
        exit 1
    }
}

if ($env:SUPABASE_DB_HOST -match '^\$|{{' ) {
    Write-Host "[ERROR] SUPABASE_DB_HOST still contains a template placeholder." -ForegroundColor Red
    exit 1
}

# If the H2/dev instance is still listening on 8081, offer to stop it first.
$inUse = Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue |
    Where-Object { $_.LocalPort -eq 8081 }
if ($inUse) {
    Write-Warning "Port 8081 is in use (PIDs: $($inUse.OwningProcess -join ', '))."
    $answer = Read-Host "Type STOP to kill them and continue, or press Enter to abort"
    if ($answer -ne 'STOP') { exit 1 }
    $inUse | Select-Object -ExpandProperty OwningProcess -Unique |
        ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }
    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "Starting backend  profile=$env:SPRING_PROFILES_ACTIVE  host=$env:SUPABASE_DB_HOST  port=8081" -ForegroundColor Cyan
Set-Location -LiteralPath $root
mvn spring-boot:run
