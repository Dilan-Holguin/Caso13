#!/usr/bin/env bash
# ============================================================
# generate-api-docs.sh — Genera documentación de APIs
# Uso: ./scripts/generate-api-docs.sh
#
# Requiere:
#   1. La aplicación corriendo en localhost:8080
#   2. curl instalado
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
DOCS_DIR="${PROJECT_DIR}/docs"
BASE_URL="${1:-http://localhost:8080}"

echo "============================================="
echo " Generación de documentación de API"
echo " Base URL: ${BASE_URL}"
echo "============================================="

mkdir -p "${DOCS_DIR}"

# --- OpenAPI 3.0 Spec ---
echo "[INFO] Extrayendo OpenAPI spec desde ${BASE_URL}/v3/api-docs..."
if curl -s -o "${DOCS_DIR}/openapi.json" "${BASE_URL}/v3/api-docs"; then
    echo "[OK] openapi.json generado (${DOCS_DIR}/openapi.json)"
else
    echo "[ERROR] No se pudo conectar a la aplicación. ¿Está corriendo?"
    echo "  Ejecuta primero: ./mvnw spring-boot:run"
    exit 1
fi

# --- OpenAPI YAML (requiere python3 + PyYAML) ---
if command -v python3 &> /dev/null; then
    python3 -c "
import json, yaml, sys
try:
    with open('${DOCS_DIR}/openapi.json') as f:
        spec = json.load(f)
    with open('${DOCS_DIR}/openapi.yaml', 'w') as f:
        yaml.dump(spec, f, allow_unicode=True, sort_keys=False)
    print('[OK] openapi.yaml generado')
except ImportError:
    print('[WARN] PyYAML no instalado — salteando YAML. pip install pyyaml')
except Exception as e:
    print(f'[WARN] Error generando YAML: {e}')
" 2>/dev/null || echo "[WARN] No se pudo generar YAML"
fi

# --- Postman Collection ---
echo "[INFO] Generando colección Postman..."

python3 -c "
import json, sys

with open('${DOCS_DIR}/openapi.json') as f:
    spec = json.load(f)

collection = {
    'info': {
        'name': 'Domésticas API',
        'description': spec.get('info', {}).get('description', 'Colección autogenerada'),
        'schema': 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json'
    },
    'item': []
}

paths = spec.get('paths', {})
for path, methods in paths.items():
    for method, details in methods.items():
        if method.upper() not in ('GET', 'POST', 'PUT', 'PATCH', 'DELETE'):
            continue
        item = {
            'name': details.get('summary', path),
            'request': {
                'method': method.upper(),
                'header': [{'key': 'Content-Type', 'value': 'application/json'}],
                'url': {
                    'raw': '{{baseUrl}}' + path,
                    'host': ['{{baseUrl}}'],
                    'path': path.strip('/').split('/')
                }
            }
        }
        if 'requestBody' in details:
            example = details['requestBody'].get('content', {}).get('application/json', {}).get('example', '{}')
            item['request']['body'] = {
                'mode': 'raw',
                'raw': json.dumps(example, indent=2, ensure_ascii=False)
            }
        collection['item'].append(item)

with open('${DOCS_DIR}/domesticas.postman_collection.json', 'w') as f:
    json.dump(collection, f, indent=2, ensure_ascii=False)

print('[OK] domesticas.postman_collection.json generado')
" 2>/dev/null || echo "[WARN] No se pudo generar colección Postman (requiere app corriendo)"

echo ""
echo "============================================="
echo " Documentación generada en: ${DOCS_DIR}/"
echo "  - openapi.json          (OpenAPI 3.0 spec)"
echo "  - openapi.yaml          (OpenAPI 3.0 spec)"
echo "  - domesticas.postman_collection.json"
echo "  - vulnerability-report.md"
echo ""
echo " Swagger UI disponible en: ${BASE_URL}/swagger-ui/index.html"
echo "============================================="
