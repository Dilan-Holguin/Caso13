# Script de análisis de calidad para Windows
# Uso: .\scripts\quality-analysis.ps1

param(
    [string]$option = ""
)

# Colores
function Write-Header { param([string]$text); Write-Host "`n▶ $text" -ForegroundColor Cyan }
function Write-Success { param([string]$text); Write-Host "✓ $text" -ForegroundColor Green }
function Write-Error-Custom { param([string]$text); Write-Host "✗ $text" -ForegroundColor Red }

# Validar Maven instalado
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Error-Custom "Maven no está instalado. Por favor instálalo primero."
    exit 1
}

# Si no hay opción, mostrar menú
if ([string]::IsNullOrEmpty($option)) {
    Write-Host "🔍 Herramientas de Análisis de Calidad" -ForegroundColor Cyan
    Write-Host "=====================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Opciones disponibles:"
    Write-Host "  1. Ejecutar Build + Tests"
    Write-Host "  2. Generar reporte de cobertura (JaCoCo)"
    Write-Host "  3. Ejecutar análisis SonarCloud"
    Write-Host "  4. Build + Tests + JaCoCo"
    Write-Host "  5. Build + Tests + JaCoCo + SonarCloud (completo)"
    Write-Host ""
    $option = Read-Host "Selecciona una opción (1-5)"
}

switch ($option) {
    "1" {
        Write-Header "Ejecutando Build y Tests..."
        mvn clean verify -DskipITs=true
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Build y Tests completados"
        }
    }
    "2" {
        Write-Header "Generando reporte de cobertura JaCoCo..."
        mvn jacoco:report
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Reporte de cobertura generado"
            Write-Host "📊 Abre: target\site\jacoco\index.html" -ForegroundColor Yellow
        }
    }
    "3" {
        Write-Header "Ejecutando análisis SonarCloud..."
        
        $sonarToken = $env:SONAR_TOKEN
        if ([string]::IsNullOrEmpty($sonarToken)) {
            $sonarToken = Read-Host "Por favor ingresa tu SONAR_TOKEN"
        }
        
        mvn sonar:sonar `
            -Dsonar.token=$sonarToken `
            -Dsonar.host.url=https://sonarcloud.io
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Análisis SonarCloud completado"
            Write-Host "📊 Ve a: https://sonarcloud.io" -ForegroundColor Yellow
        }
    }
    "4" {
        Write-Header "Ejecutando Build + Tests + Cobertura..."
        
        Write-Header "Paso 1: Build y Tests"
        mvn clean verify -DskipITs=true
        if ($LASTEXITCODE -ne 0) { exit 1 }
        Write-Success "Build y Tests completados"
        
        Write-Header "Paso 2: Generando reporte de cobertura"
        mvn jacoco:report
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Análisis local completado"
            Write-Host "📊 Reporte: target\site\jacoco\index.html" -ForegroundColor Yellow
        }
    }
    "5" {
        Write-Header "Ejecutando análisis completo..."
        
        # Build + Tests
        Write-Header "Paso 1: Build y Tests"
        mvn clean verify -DskipITs=true
        if ($LASTEXITCODE -ne 0) { exit 1 }
        Write-Success "Build y Tests completados"
        
        # JaCoCo
        Write-Header "Paso 2: Generando reporte de cobertura"
        mvn jacoco:report
        if ($LASTEXITCODE -ne 0) { exit 1 }
        Write-Success "Reporte JaCoCo generado"
        
        # SonarCloud
        Write-Header "Paso 3: Análisis SonarCloud"
        
        $sonarToken = $env:SONAR_TOKEN
        if ([string]::IsNullOrEmpty($sonarToken)) {
            $sonarToken = Read-Host "Por favor ingresa tu SONAR_TOKEN"
        }
        
        mvn sonar:sonar `
            -Dsonar.token=$sonarToken `
            -Dsonar.host.url=https://sonarcloud.io
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Análisis completo finalizado"
            Write-Host ""
            Write-Host "📊 Reportes disponibles:" -ForegroundColor Green
            Write-Host "   - Local (JaCoCo): target\site\jacoco\index.html"
            Write-Host "   - SonarCloud: https://sonarcloud.io" -ForegroundColor Yellow
        }
    }
    default {
        Write-Error-Custom "Opción no válida"
        exit 1
    }
}

Write-Host "`n✓ Proceso completado`n" -ForegroundColor Green
