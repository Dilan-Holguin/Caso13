#!/usr/bin/env bash
# ============================================================
# test-api.sh — Prueba automatizada de todos los endpoints
# Uso: ./scripts/test-api.sh [http://localhost:8080]
# ============================================================
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
PASS=0
FAIL=0
TOKEN_ANA=""
TOKEN_PEDRO=""
HOGAR_ID=""
INVITE_TOKEN=""
TAREA_ID=""

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
GREY='\033[0;90m'
BOLD='\033[1m'
NC='\033[0m'

# ─── Utilidades ───────────────────────────────────────────
step()   { echo ""; echo -e "${BOLD}${CYAN}[TEST]${NC} ${BOLD}$*${NC}"; }
ok()     { PASS=$((PASS+1)); echo -e "  ${GREEN}✓${NC} $1"; }
fail()   { FAIL=$((FAIL+1)); echo -e "  ${RED}✗${NC} $1 — $2"; }
info()   { echo -e "  ${YELLOW}ℹ${NC}  $1"; }

# Muestra el comando curl exacto para copiar/pegar
show() {
    echo -e "  ${GREY}┌─ curl ──────────────────────────────${NC}"
    echo -e "  ${GREY}│${NC} $1"
    shift
    while [ $# -gt 0 ]; do
        echo -e "  ${GREY}│${NC}   $1"
        shift
    done
    echo -e "  ${GREY}└──────────────────────────────────────${NC}"
}

result() {
    echo ""
    echo "============================================="
    echo -e " Resultados: ${GREEN}${PASS} pasaron${NC}, ${RED}${FAIL} fallaron${NC}"
    echo "============================================="
    if [ "$FAIL" -gt 0 ]; then exit 1; fi
}

# ─── Verificar que el servidor está corriendo ─────────────
echo ""
echo "============================================="
echo "  Domésticas API — Pruebas de endpoints"
echo "  ${BASE_URL}"
echo "============================================="

if ! curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/swagger-ui/index.html" | grep -q "200\|301\|302"; then
    echo -e "${RED}[ERROR]${NC} No se pudo conectar a ${BASE_URL}"
    echo "  ¿Arrancaste la app? ./mvnw spring-boot:run"
    exit 1
fi
info "Servidor respondiendo en ${BASE_URL}"

# ═══════════════════════════════════════════════════════════
step "1. Registrar Ana"
show "curl -X POST ${BASE_URL}/api/auth/register" \
     "-H \"Content-Type: application/json\"" \
     "-d '{\"nombre\":\"Ana\",\"email\":\"ana@test.com\",\"password\":\"Test1234\"}'"

RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/register" \
    -H "Content-Type: application/json" \
    -d '{"nombre":"Ana","email":"ana@test.com","password":"Test1234"}')
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

if [[ "$HTTP" =~ ^2 ]]; then
    TOKEN_ANA=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('token',''))" 2>/dev/null || echo "")
    ok "Registro Ana   → $HTTP (token=${TOKEN_ANA})"
elif [[ "$HTTP" == "409" ]]; then
    ok "Registro Ana   → $HTTP (usuario ya existe, se usara login)"
else
    fail "Registro Ana" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "2. Login Ana"
show "curl -X POST ${BASE_URL}/api/auth/login" \
     "-H \"Content-Type: application/json\"" \
     "-d '{\"email\":\"ana@test.com\",\"password\":\"Test1234\"}'"

RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"ana@test.com","password":"Test1234"}')
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

if [[ "$HTTP" =~ ^2 ]]; then
    TOKEN_ANA=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['token'])" 2>/dev/null || echo "")
    ok "Login Ana      → $HTTP (token=${TOKEN_ANA})"
else
    fail "Login Ana" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "3. Crear hogar (Ana admin)"
show "curl -X POST ${BASE_URL}/api/households" \
     "-H \"Content-Type: application/json\"" \
     "-H \"Authorization: Bearer ${TOKEN_ANA:0:30}...\"" \
     "-d '{\"nombre\":\"Casa Test\",\"descripcion\":\"Hogar de pruebas automatizadas\"}'"

RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/households" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN_ANA}" \
    -d '{"nombre":"Casa Test","descripcion":"Hogar de pruebas automatizadas"}')
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

if [[ "$HTTP" =~ ^2 ]]; then
    HOGAR_ID=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['hogarId'])" 2>/dev/null || echo "")
    ok "Crear hogar    → $HTTP (hogarId=$HOGAR_ID)"
else
    fail "Crear hogar" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "4. Crear tarea en el hogar"
show "curl -X POST ${BASE_URL}/api/households/${HOGAR_ID}/tasks" \
     "-H \"Content-Type: application/json\"" \
     "-H \"Authorization: Bearer <token_ana>\"" \
     "-d '{\"titulo\":\"Lavar platos\",\"descripcion\":\"Usar jabon biodegradable\",\"categoria\":\"Cocina\"}'"

RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/households/${HOGAR_ID}/tasks" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN_ANA}" \
    -d '{"titulo":"Lavar platos","descripcion":"Usar jabon biodegradable","categoria":"Cocina"}')
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

if [[ "$HTTP" =~ ^2 ]]; then
    TAREA_ID=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['tareaId'])" 2>/dev/null || echo "")
    ok "Crear tarea    → $HTTP (tareaId=$TAREA_ID, estado:Pendiente)"
else
    fail "Crear tarea" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "5. Crear segunda tarea"
show "curl -X POST ${BASE_URL}/api/households/${HOGAR_ID}/tasks" \
     "-H \"Content-Type: application/json\"" \
     "-H \"Authorization: Bearer <token_ana>\"" \
     "-d '{\"titulo\":\"Barrer el piso\",\"categoria\":\"Limpieza\",\"fechaLimite\":\"2026-12-31T23:59:00\"}'"

RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/households/${HOGAR_ID}/tasks" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN_ANA}" \
    -d '{"titulo":"Barrer el piso","categoria":"Limpieza","fechaLimite":"2026-12-31T23:59:00"}')
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

if [[ "$HTTP" =~ ^2 ]]; then
    T2_ID=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['tareaId'])" 2>/dev/null || echo "")
    ok "Crear tarea 2  → $HTTP (tareaId=$T2_ID)"
else
    fail "Crear tarea 2" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "6. Listar tareas del hogar"
show "curl -X GET \"${BASE_URL}/api/households/${HOGAR_ID}/tasks\"" \
     "-H \"Authorization: Bearer <token_ana>\""

RESP=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/api/households/${HOGAR_ID}/tasks" \
    -H "Authorization: Bearer ${TOKEN_ANA}")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

COUNT=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d))" 2>/dev/null || echo "0")
if [[ "$HTTP" =~ ^2 ]]; then
    ok "Listar tareas  → $HTTP ($COUNT tareas encontradas)"
else
    fail "Listar tareas" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "7. Listar tareas con filtro por categoria"
show "curl -X GET \"${BASE_URL}/api/households/${HOGAR_ID}/tasks?categoria=Cocina\"" \
     "-H \"Authorization: Bearer <token_ana>\""

RESP=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/api/households/${HOGAR_ID}/tasks?categoria=Cocina" \
    -H "Authorization: Bearer ${TOKEN_ANA}")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

COUNT=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d))" 2>/dev/null || echo "0")
if [[ "$HTTP" =~ ^2 && "$COUNT" -ge 1 ]]; then
    ok "Filtro cocina  → $HTTP ($COUNT tarea(s) Cocina)"
else
    fail "Filtro cocina" "HTTP $HTTP → $COUNT tareas → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "8. Obtener detalle de tarea"
show "curl -X GET ${BASE_URL}/api/tasks/${TAREA_ID}" \
     "-H \"Authorization: Bearer <token_ana>\""

RESP=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/api/tasks/${TAREA_ID}" \
    -H "Authorization: Bearer ${TOKEN_ANA}")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

TITULO=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('titulo',''))" 2>/dev/null || echo "")
if [[ "$HTTP" =~ ^2 && "$TITULO" = "Lavar platos" ]]; then
    ok "Obtener tarea  → $HTTP (titulo: '$TITULO')"
else
    fail "Obtener tarea" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "9. Cambiar estado de tarea (Pendiente → En_progreso)"
show "curl -X PATCH ${BASE_URL}/api/tasks/${TAREA_ID}/status" \
     "-H \"Content-Type: application/json\"" \
     "-H \"Authorization: Bearer <token_ana>\"" \
     "-d '{\"estado\":\"En_progreso\"}'"

RESP=$(curl -s -w "\n%{http_code}" -X PATCH "${BASE_URL}/api/tasks/${TAREA_ID}/status" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN_ANA}" \
    -d '{"estado":"En_progreso"}')
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

ESTADO=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('estado',''))" 2>/dev/null || echo "")
if [[ "$HTTP" =~ ^2 && "$ESTADO" = "En_progreso" ]]; then
    ok "Cambiar estado → $HTTP (Pendiente → En_progreso)"
else
    fail "Cambiar estado" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "10. Completar tarea"
show "curl -X PATCH ${BASE_URL}/api/tasks/${TAREA_ID}/status" \
     "-H \"Content-Type: application/json\"" \
     "-H \"Authorization: Bearer <token_ana>\"" \
     "-d '{\"estado\":\"Completada\"}'"

RESP=$(curl -s -w "\n%{http_code}" -X PATCH "${BASE_URL}/api/tasks/${TAREA_ID}/status" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN_ANA}" \
    -d '{"estado":"Completada"}')
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

ESTADO=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('estado',''))" 2>/dev/null || echo "")
if [[ "$HTTP" =~ ^2 && "$ESTADO" = "Completada" ]]; then
    ok "Completar      → $HTTP (En_progreso → Completada)"
else
    fail "Completar" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "11. Actualizar tarea (cambiar titulo)"
show "curl -X PUT ${BASE_URL}/api/tasks/${TAREA_ID}" \
     "-H \"Content-Type: application/json\"" \
     "-H \"Authorization: Bearer <token_ana>\"" \
     "-d '{\"titulo\":\"Lavar platos y secar\",\"descripcion\":\"Actualizado: usar jabon liquido\"}'"

RESP=$(curl -s -w "\n%{http_code}" -X PUT "${BASE_URL}/api/tasks/${TAREA_ID}" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN_ANA}" \
    -d '{"titulo":"Lavar platos y secar","descripcion":"Actualizado: usar jabon liquido"}')
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

TITULO=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('titulo',''))" 2>/dev/null || echo "")
if [[ "$HTTP" =~ ^2 && "$TITULO" = "Lavar platos y secar" ]]; then
    ok "Actualizar     → $HTTP (nuevo titulo: '$TITULO')"
else
    fail "Actualizar" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "12. Registrar Pedro"
show "curl -X POST ${BASE_URL}/api/auth/register" \
     "-H \"Content-Type: application/json\"" \
     "-d '{\"nombre\":\"Pedro\",\"email\":\"pedro@test.com\",\"password\":\"Test5678\"}'"

RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/register" \
    -H "Content-Type: application/json" \
    -d '{"nombre":"Pedro","email":"pedro@test.com","password":"Test5678"}')
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

if [[ "$HTTP" =~ ^2 ]]; then
    TOKEN_PEDRO=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('token',''))" 2>/dev/null || echo "")
    ok "Registro Pedro → $HTTP (creado)"
elif [[ "$HTTP" == "409" ]]; then
    ok "Registro Pedro → $HTTP (usuario ya existe, se usara login)"
else
    fail "Registro Pedro" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "13. Login Pedro"
show "curl -X POST ${BASE_URL}/api/auth/login" \
     "-H \"Content-Type: application/json\"" \
     "-d '{\"email\":\"pedro@test.com\",\"password\":\"Test5678\"}'"

RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"pedro@test.com","password":"Test5678"}')
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

if [[ "$HTTP" =~ ^2 ]]; then
    TOKEN_PEDRO=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['token'])" 2>/dev/null || echo "")
    ok "Login Pedro    → $HTTP (token=${TOKEN_PEDRO:0:20}...)"
else
    fail "Login Pedro" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "14. Invitar a Pedro al hogar (Ana como admin)"
show "curl -X POST ${BASE_URL}/api/households/${HOGAR_ID}/invite" \
     "-H \"Content-Type: application/json\"" \
     "-H \"Authorization: Bearer <token_ana>\"" \
     "-d '{\"emailInvitado\":\"pedro@test.com\"}'"

RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/households/${HOGAR_ID}/invite" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN_ANA}" \
    -d '{"emailInvitado":"pedro@test.com"}')
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

if [[ "$HTTP" =~ ^2 ]]; then
    INVITE_TOKEN=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['token'])" 2>/dev/null || echo "")
    ok "Invitar Pedro  → $HTTP (inviteToken=${INVITE_TOKEN:0:8}...)"
else
    fail "Invitar Pedro" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "15. Pedro acepta la invitacion"
show "curl -X POST ${BASE_URL}/api/households/invitations/${INVITE_TOKEN}/respond" \
     "-H \"Content-Type: application/json\"" \
     "-H \"Authorization: Bearer <token_pedro>\"" \
     "-d '{\"accion\":\"ACEPTAR\"}'"

RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/households/invitations/${INVITE_TOKEN}/respond" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN_PEDRO}" \
    -d '{"accion":"ACEPTAR"}')
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

ESTADO=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('estado',''))" 2>/dev/null || echo "")
if [[ "$HTTP" =~ ^2 && "$ESTADO" = "Aceptada" ]]; then
    ok "Aceptar invit  → $HTTP (estado: Aceptada)"
else
    fail "Aceptar invit" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "16. Listar miembros del hogar"
show "curl -X GET ${BASE_URL}/api/households/${HOGAR_ID}/members" \
     "-H \"Authorization: Bearer <token_ana>\""

RESP=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/api/households/${HOGAR_ID}/members" \
    -H "Authorization: Bearer ${TOKEN_ANA}")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

COUNT=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d))" 2>/dev/null || echo "0")
if [[ "$HTTP" =~ ^2 && "$COUNT" -ge 2 ]]; then
    ok "Listar miembros → $HTTP ($COUNT miembros: Ana+Pedro)"
else
    fail "Listar miembros" "HTTP $HTTP → $COUNT miembros → $BODY"
fi

# Extraer el usuarioId de Ana para usarlo en asignaciones
ANA_ID=$(echo "$BODY" | python3 -c "import sys,json; miembros=json.load(sys.stdin); [print(m['usuarioId']) for m in miembros if m.get('nombre')=='Ana']" 2>/dev/null || echo "")
if [[ -n "$ANA_ID" ]]; then
    info "Ana tiene usuarioId=$ANA_ID"
fi

# ═══════════════════════════════════════════════════════════
step "17. Pedro crea tarea en el hogar asignada a Ana"
show "curl -X POST ${BASE_URL}/api/households/${HOGAR_ID}/tasks" \
     "-H \"Content-Type: application/json\"" \
     "-H \"Authorization: Bearer <token_pedro>\"" \
     "-d '{\"titulo\":\"Sacar la basura\",\"categoria\":\"Mantenimiento\",\"asignadoAId\":${ANA_ID}}'"

RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/households/${HOGAR_ID}/tasks" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN_PEDRO}" \
    -d '{"titulo":"Sacar la basura","categoria":"Mantenimiento","asignadoAId":'"$ANA_ID"'}')
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

if [[ "$HTTP" =~ ^2 ]]; then
    T3_ID=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['tareaId'])" 2>/dev/null || echo "")
    ok "Pedro crea tarea → $HTTP (tareaId=$T3_ID)"
else
    fail "Pedro crea tarea" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "18. Pedro intenta eliminar tarea de Ana (debe fallar: no es admin)"
show "curl -X DELETE ${BASE_URL}/api/tasks/${TAREA_ID}" \
     "-H \"Authorization: Bearer <token_pedro>\""

RESP=$(curl -s -w "\n%{http_code}" -X DELETE "${BASE_URL}/api/tasks/${TAREA_ID}" \
    -H "Authorization: Bearer ${TOKEN_PEDRO}")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

if [[ "$HTTP" =~ ^(403|409|500) ]]; then
    ok "Pedro elimina (bloqueado) → $HTTP (no-admin no puede eliminar)"
else
    fail "Pedro elimina" "Esperaba 403/409, recibio $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "19. Ana elimina su tarea (admin)"
show "curl -X DELETE ${BASE_URL}/api/tasks/${TAREA_ID}" \
     "-H \"Authorization: Bearer <token_ana>\""

RESP=$(curl -s -w "\n%{http_code}" -X DELETE "${BASE_URL}/api/tasks/${TAREA_ID}" \
    -H "Authorization: Bearer ${TOKEN_ANA}")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

if [[ "$HTTP" =~ ^(204|200) ]]; then
    ok "Ana elimina tarea → $HTTP (admin puede eliminar)"
else
    fail "Ana elimina tarea" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "20. Verificar tarea eliminada (debe dar error)"
show "curl -X GET ${BASE_URL}/api/tasks/${TAREA_ID}" \
     "-H \"Authorization: Bearer <token_ana>\""

RESP=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/api/tasks/${TAREA_ID}" \
    -H "Authorization: Bearer ${TOKEN_ANA}")
HTTP=$(echo "$RESP" | tail -1)

if [[ ! "$HTTP" =~ ^2 ]]; then
    ok "Tarea eliminada → $HTTP (ya no existe, correcto)"
else
    fail "Tarea eliminada" "Esperaba error, recibio $HTTP"
fi

# ═══════════════════════════════════════════════════════════
step "21. Asignar prioridad a tarea"
show "curl -X PUT ${BASE_URL}/api/tasks/${T2_ID}" \
     "-H \"Content-Type: application/json\"" \
     "-H \"Authorization: Bearer <token_ana>\"" \
     "-d '{\"prioridad\":\"Alta\"}'"

RESP=$(curl -s -w "\n%{http_code}" -X PUT "${BASE_URL}/api/tasks/${T2_ID}" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN_ANA}" \
    -d '{"prioridad":"Alta"}')
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

PRIO=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('prioridad',''))" 2>/dev/null || echo "")
if [[ "$HTTP" =~ ^2 && "$PRIO" = "Alta" ]]; then
    ok "Asignar prioridad → $HTTP (prioridad: Alta)"
else
    fail "Asignar prioridad" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "22. Verificar prioridad en detalle"
show "curl -X GET ${BASE_URL}/api/tasks/${T2_ID}" \
     "-H \"Authorization: Bearer <token_ana>\""

RESP=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/api/tasks/${T2_ID}" \
    -H "Authorization: Bearer ${TOKEN_ANA}")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

PRIO=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('prioridad',''))" 2>/dev/null || echo "")
if [[ "$HTTP" =~ ^2 && "$PRIO" = "Alta" ]]; then
    ok "Verificar prioridad → $HTTP (prioridad: $PRIO)"
else
    fail "Verificar prioridad" "HTTP $HTTP → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "23. Completar tarea y verificar completadaAt"
show "curl -X PATCH ${BASE_URL}/api/tasks/${T2_ID}/status" \
     "-H \"Content-Type: application/json\"" \
     "-H \"Authorization: Bearer <token_ana>\"" \
     "-d '{\"estado\":\"Completada\"}'"

RESP=$(curl -s -w "\n%{http_code}" -X PATCH "${BASE_URL}/api/tasks/${T2_ID}/status" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN_ANA}" \
    -d '{"estado":"Completada"}')
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

ESTADO=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('estado',''))" 2>/dev/null || echo "")
COMPLETADA=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('completadaAt','N/A'))" 2>/dev/null || echo "")
if [[ "$HTTP" =~ ^2 && "$ESTADO" = "Completada" && "$COMPLETADA" != "null" && "$COMPLETADA" != "" ]]; then
    ok "Completar tarea → $HTTP (completadaAt: $COMPLETADA)"
else
    fail "Completar tarea" "HTTP $HTTP → estado=$ESTADO completadaAt=$COMPLETADA"
fi

# ═══════════════════════════════════════════════════════════
step "24. Reporte de distribucion"
show "curl -X GET \"${BASE_URL}/api/households/${HOGAR_ID}/reports/distribution\"" \
     "-H \"Authorization: Bearer <token_ana>\""

RESP=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/api/households/${HOGAR_ID}/reports/distribution" \
    -H "Authorization: Bearer ${TOKEN_ANA}")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

COUNT=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); miembros=d.get('miembros',[]); print(len(miembros))" 2>/dev/null || echo "0")
if [[ "$HTTP" =~ ^2 && "$COUNT" -ge 1 ]]; then
    ok "Reporte distribucion → $HTTP ($COUNT miembros)"
else
    fail "Reporte distribucion" "HTTP $HTTP → $COUNT miembros → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "25. Reporte de cumplimiento"
show "curl -X GET \"${BASE_URL}/api/households/${HOGAR_ID}/reports/cumplimiento\"" \
     "-H \"Authorization: Bearer <token_ana>\""

RESP=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/api/households/${HOGAR_ID}/reports/cumplimiento" \
    -H "Authorization: Bearer ${TOKEN_ANA}")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | sed '$d')

COUNT=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); usuarios=d.get('usuarios',[]); print(len(usuarios))" 2>/dev/null || echo "0")
if [[ "$HTTP" =~ ^2 && "$COUNT" -ge 1 ]]; then
    ok "Reporte cumplimiento → $HTTP ($COUNT usuarios)"
else
    fail "Reporte cumplimiento" "HTTP $HTTP → $COUNT usuarios → $BODY"
fi

# ═══════════════════════════════════════════════════════════
step "26. Endpoint publico sin token (debe funcionar)"
show "curl -X POST ${BASE_URL}/api/auth/forgot-password" \
     "-H \"Content-Type: application/json\"" \
     "-d '{\"email\":\"inexistente@test.com\"}'"

RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/auth/forgot-password" \
    -H "Content-Type: application/json" \
    -d '{"email":"inexistente@test.com"}')
HTTP=$(echo "$RESP" | tail -1)

if [[ "$HTTP" =~ ^2 ]]; then
    ok "Forgot password (publico) → $HTTP"
else
    fail "Forgot password" "HTTP $HTTP"
fi

# ═══════════════════════════════════════════════════════════
step "27. Endpoint protegido sin token (debe fallar)"
show "curl -X GET ${BASE_URL}/api/households/${HOGAR_ID}/tasks"

RESP=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/api/households/${HOGAR_ID}/tasks" \
    -H "Content-Type: application/json")
HTTP=$(echo "$RESP" | tail -1)

if [[ "$HTTP" =~ ^(401|403) ]]; then
    ok "Sin token → $HTTP (bloqueado, correcto)"
else
    fail "Sin token" "Esperaba 401/403, recibio $HTTP"
fi

result
