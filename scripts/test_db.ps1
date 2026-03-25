param(
    [string]$DbPath = (Join-Path $PSScriptRoot "..\medtrack_dev.db"),
    [string]$MigrationPath = (Join-Path $PSScriptRoot "..\db\migrations\001_init.sql"),
    [string]$SeedPath = (Join-Path $PSScriptRoot "..\db\seeds\001_seed_minimal.sql"),
    [string]$TestPath = (Join-Path $PSScriptRoot "..\db\tests\001_integrity_checks.sql"),
    [string]$SqlitePath = ""
)

$ErrorActionPreference = "Stop"

function Resolve-SqliteExecutable {
    param([string]$PreferredPath)

    if ($PreferredPath -and (Test-Path $PreferredPath)) {
        return (Resolve-Path $PreferredPath).Path
    }

    $cmd = Get-Command sqlite3 -ErrorAction SilentlyContinue
    if ($cmd) {
        return $cmd.Source
    }

    $wingetBase = Join-Path $env:LOCALAPPDATA "Microsoft\WinGet\Packages"
    if (Test-Path $wingetBase) {
        $pkgDir = Get-ChildItem $wingetBase -Directory -Filter "SQLite.SQLite*" |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1

        if ($pkgDir) {
            $exe = Join-Path $pkgDir.FullName "sqlite3.exe"
            if (Test-Path $exe) {
                return $exe
            }
        }
    }

    throw "sqlite3.exe not found. Install SQLite or pass -SqlitePath explicitly."
}

function To-SqliteReadPath {
    param([string]$Path)
    return ((Resolve-Path $Path).Path -replace '\\', '/')
}

$sqliteExe = Resolve-SqliteExecutable -PreferredPath $SqlitePath
$resolvedDbPath = (Resolve-Path (Split-Path -Parent $DbPath) -ErrorAction SilentlyContinue)
if (-not $resolvedDbPath) {
    $parentDir = Split-Path -Parent $DbPath
    New-Item -ItemType Directory -Path $parentDir -Force | Out-Null
}
$resolvedDbPath = (Join-Path (Split-Path -Parent $DbPath) (Split-Path -Leaf $DbPath))

$migrationReadPath = To-SqliteReadPath -Path $MigrationPath
$seedReadPath = To-SqliteReadPath -Path $SeedPath
$testReadPath = To-SqliteReadPath -Path $TestPath

if (Test-Path $resolvedDbPath) {
    Remove-Item $resolvedDbPath -Force
}

& $sqliteExe $resolvedDbPath ".read $migrationReadPath" | Out-Null
if ($LASTEXITCODE -ne 0) { throw "Failed to apply migration SQL." }

& $sqliteExe $resolvedDbPath ".read $seedReadPath" | Out-Null
if ($LASTEXITCODE -ne 0) { throw "Failed to apply seed SQL." }

$testOutput = & $sqliteExe $resolvedDbPath ".read $testReadPath"
if ($LASTEXITCODE -ne 0) { throw "Integrity test SQL execution failed." }

$lines = @($testOutput | ForEach-Object { $_.ToString().Trim() } | Where-Object { $_ })
$failures = @($lines | Where-Object { $_ -like "FAIL |*" })

if ($failures.Count -gt 0 -or -not ($lines -contains "ALL_TESTS_PASSED")) {
    Write-Host "Database integrity tests failed:" -ForegroundColor Red
    $failures | ForEach-Object { Write-Host "- $_" -ForegroundColor Red }
    if (-not ($lines -contains "ALL_TESTS_PASSED") -and $failures.Count -eq 0) {
        Write-Host "- FAIL | unknown | Missing ALL_TESTS_PASSED marker" -ForegroundColor Red
    }
    exit 1
}

Write-Host "All database integrity tests passed." -ForegroundColor Green
Write-Host "Database file: $resolvedDbPath"

