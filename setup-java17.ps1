# Script to help configure Java 17 for building

Write-Host "Searching for Java 17 installation..." -ForegroundColor Yellow

# Common Java installation paths
$javaPaths = @(
    "C:\Program Files\Java",
    "C:\Program Files (x86)\Java",
    "$env:ProgramFiles\Eclipse Adoptium",
    "$env:ProgramFiles\Microsoft",
    "$env:LOCALAPPDATA\Programs\Eclipse Adoptium"
)

$java17 = $null

foreach ($path in $javaPaths) {
    if (Test-Path $path) {
        $found = Get-ChildItem $path -ErrorAction SilentlyContinue | 
                 Where-Object { $_.Name -match "jdk-17|jre-17|java-17|17\." } | 
                 Select-Object -First 1
        
        if ($found) {
            $java17 = $found.FullName
            Write-Host "Found Java 17 at: $java17" -ForegroundColor Green
            break
        }
    }
}

if (-not $java17) {
    Write-Host "Java 17 not found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please install Java 17:" -ForegroundColor Yellow
    Write-Host "1. Download from: https://adoptium.net/temurin/releases/?version=17" -ForegroundColor Cyan
    Write-Host "2. Or use winget: winget install EclipseAdoptium.Temurin.17.JDK" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "After installation, run this script again." -ForegroundColor Yellow
    exit 1
}

# Find the actual Java executable
$javaExe = Join-Path $java17 "bin\java.exe"
if (-not (Test-Path $javaExe)) {
    $javaExe = Join-Path $java17 "bin\java.exe"
    if (-not (Test-Path $javaExe)) {
        Write-Host "Could not find java.exe in $java17" -ForegroundColor Red
        exit 1
    }
}

# Verify it's Java 17
$version = & $javaExe -version 2>&1 | Select-String "version"
if ($version -match "17") {
    Write-Host "Verified: $version" -ForegroundColor Green
} else {
    Write-Host "Warning: This doesn't appear to be Java 17: $version" -ForegroundColor Yellow
}

# Update gradle.properties
$gradleProps = "gradle.properties"
if (Test-Path $gradleProps) {
    $content = Get-Content $gradleProps -Raw
    if ($content -match "org\.gradle\.java\.home=") {
        $content = $content -replace "org\.gradle\.java\.home=.*", "org.gradle.java.home=$java17"
    } else {
        $content += "`norg.gradle.java.home=$java17"
    }
    Set-Content $gradleProps $content
    Write-Host "Updated gradle.properties with Java 17 path" -ForegroundColor Green
} else {
    Write-Host "gradle.properties not found!" -ForegroundColor Red
}

Write-Host ""
Write-Host "Setup complete! You can now run: .\gradlew.bat build" -ForegroundColor Green


