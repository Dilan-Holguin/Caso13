#!/usr/bin/env bash
# ============================================================
# deploy-db.sh — Despliegue de estructura BD para Domésticas
# Uso:   ./scripts/deploy-db.sh [local|render]
# ============================================================
set -euo pipefail

ENV="${1:-local}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "============================================="
echo " Domésticas — Despliegue de Base de Datos"
echo " Entorno: ${ENV}"
echo "============================================="

# --- Carga de variables de entorno ---
if [ -f "${PROJECT_DIR}/.env" ]; then
    echo "[INFO] Cargando variables desde .env..."
    set -a
    source "${PROJECT_DIR}/.env"
    set +a
else
    echo "[WARN] No se encontró .env — usa defaults de application.properties"
fi

# --- Funciones de utilidad ---
check_connection() {
    local url="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/domesticas_db}"
    local user="${SPRING_DATASOURCE_USERNAME:-postgres}"
    local pass="${SPRING_DATASOURCE_PASSWORD:-postgres}"

    # Extraer host, puerto y db del JDBC URL
    local host=$(echo "$url" | sed -n 's|jdbc:postgresql://\([^:/]*\).*|\1|p')
    local port=$(echo "$url" | sed -n 's|.*:\([0-9]\+\)/.*|\1|p')
    local db=$(echo "$url" | sed -n 's|.*/\([^?]*\).*|\1|p')

    echo "[INFO] Verificando conexión a PostgreSQL en ${host}:${port}/${db}..."

    PGPASSWORD="${pass}" psql -h "${host}" -p "${port:-5432}" \
        -U "${user}" -d "${db}" -c "SELECT 1 AS connection_test;" > /dev/null 2>&1

    if [ $? -eq 0 ]; then
        echo "[OK] Conexión exitosa"
    else
        echo "[ERROR] No se pudo conectar a PostgreSQL"
        echo "  Verifica que el contenedor esté corriendo: docker ps | grep postgres"
        exit 1
    fi
}

flyway_migrate() {
    echo "[INFO] Ejecutando migraciones Flyway..."

    cd "${PROJECT_DIR}"
    ./mvnw flyway:migrate -q -Dflyway.url="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/domesticas_db}" \
                           -Dflyway.user="${SPRING_DATASOURCE_USERNAME:-postgres}" \
                           -Dflyway.password="${SPRING_DATASOURCE_PASSWORD:-postgres}"

    if [ $? -eq 0 ]; then
        echo "[OK] Migraciones aplicadas correctamente"
    else
        echo "[ERROR] Falló la ejecución de migraciones"
        exit 1
    fi
}

flyway_info() {
    echo "[INFO] Estado de migraciones:"

    cd "${PROJECT_DIR}"
    ./mvnw flyway:info -q -Dflyway.url="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/domesticas_db}" \
                        -Dflyway.user="${SPRING_DATASOURCE_USERNAME:-postgres}" \
                        -Dflyway.password="${SPRING_DATASOURCE_PASSWORD:-postgres}"
}

flyway_repair() {
    echo "[INFO] Reparando historial de Flyway..."
    cd "${PROJECT_DIR}"
    ./mvnw flyway:repair -q -Dflyway.url="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/domesticas_db}" \
                          -Dflyway.user="${SPRING_DATASOURCE_USERNAME:-postgres}" \
                          -Dflyway.password="${SPRING_DATASOURCE_PASSWORD:-postgres}"
}

# --- Inicio del proceso ---
case "${ENV}" in
    local)
        echo "[INFO] Modo local: se asume PostgreSQL corriendo en Docker"
        check_connection
        flyway_migrate
        flyway_info
        ;;
    render)
        echo "[INFO] Modo Render: usando variables de entorno del servidor"
        check_connection
        flyway_migrate
        flyway_info
        ;;
    info)
        check_connection
        flyway_info
        ;;
    repair)
        check_connection
        flyway_repair
        flyway_migrate
        flyway_info
        ;;
    *)
        echo "[ERROR] Entorno no reconocido: ${ENV}"
        echo "  Uso: $0 [local|render|info|repair]"
        exit 1
        ;;
esac

echo ""
echo "============================================="
echo " Despliegue completado"
echo "============================================="
