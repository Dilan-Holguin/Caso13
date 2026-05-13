#!/bin/bash

# Script de utilidad para ejecutar análisis de calidad localmente
# Uso: ./scripts/quality-analysis.sh

set -e  # Exit on error

echo "🔍 Herramientas de Análisis de Calidad"
echo "====================================="
echo ""

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para imprimir encabezados
print_header() {
    echo -e "\n${BLUE}▶ $1${NC}"
}

# Función para imprimir éxito
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Validar que Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo -e "${YELLOW}⚠ Maven no está instalado. Instálalo primero.${NC}"
    exit 1
fi

# Menú principal
if [ "$#" -eq 0 ]; then
    echo "Opciones disponibles:"
    echo "  1. Ejecutar Build + Tests"
    echo "  2. Generar reporte de cobertura (JaCoCo)"
    echo "  3. Ejecutar análisis SonarCloud"
    echo "  4. Build + Tests + JaCoCo"
    echo "  5. Build + Tests + JaCoCo + SonarCloud (completo)"
    echo ""
    read -p "Selecciona una opción (1-5): " option
else
    option=$1
fi

case $option in
    1)
        print_header "Ejecutando Build y Tests..."
        mvn clean verify -DskipITs=true
        print_success "Build y Tests completados"
        ;;
    2)
        print_header "Generando reporte de cobertura JaCoCo..."
        mvn jacoco:report
        print_success "Reporte de cobertura generado"
        echo "📊 Abre: target/site/jacoco/index.html"
        ;;
    3)
        print_header "Ejecutando análisis SonarCloud..."
        
        # Solicitar SONAR_TOKEN si no está definido
        if [ -z "$SONAR_TOKEN" ]; then
            read -p "Por favor ingresa tu SONAR_TOKEN: " SONAR_TOKEN
        fi
        
        mvn sonar:sonar \
            -Dsonar.token=$SONAR_TOKEN \
            -Dsonar.host.url=https://sonarcloud.io
        
        print_success "Análisis SonarCloud completado"
        echo "📊 Ve a: https://sonarcloud.io"
        ;;
    4)
        print_header "Ejecutando Build + Tests + Cobertura..."
        mvn clean verify -DskipITs=true
        mvn jacoco:report
        print_success "Análisis local completado"
        echo "📊 Reporte: target/site/jacoco/index.html"
        ;;
    5)
        print_header "Ejecutando análisis completo..."
        
        # Build + Tests
        print_header "Step 1: Build y Tests"
        mvn clean verify -DskipITs=true
        print_success "Build y Tests completados"
        
        # JaCoCo
        print_header "Step 2: Generando reporte de cobertura"
        mvn jacoco:report
        print_success "Reporte JaCoCo generado"
        
        # SonarCloud
        print_header "Step 3: Análisis SonarCloud"
        
        if [ -z "$SONAR_TOKEN" ]; then
            read -p "Por favor ingresa tu SONAR_TOKEN: " SONAR_TOKEN
        fi
        
        mvn sonar:sonar \
            -Dsonar.token=$SONAR_TOKEN \
            -Dsonar.host.url=https://sonarcloud.io
        
        print_success "Análisis completo finalizado"
        echo ""
        echo "📊 Reportes disponibles:"
        echo "   - Local (JaCoCo): target/site/jacoco/index.html"
        echo "   - SonarCloud: https://sonarcloud.io"
        ;;
    *)
        echo "❌ Opción no válida"
        exit 1
        ;;
esac

echo -e "\n${GREEN}✓ Proceso completado${NC}\n"
