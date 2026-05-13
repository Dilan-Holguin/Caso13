# 📊 Proyecto Domesticas - Configuración de Calidad de Código

## 🎯 Resumen Ejecutivo

Este proyecto está configurado con:
- ✅ **JaCoCo** - Medición de cobertura de código
- ✅ **SonarCloud** - Análisis estático de código
- ✅ **GitHub Actions** - Automatización de CI/CD
- ✅ **Quality Gates** - Validación de calidad

---

## 📁 Archivos Configurados

### En `pom.xml`
```xml
<!-- JaCoCo Plugin v0.8.11 -->
<!-- SonarCloud Maven Plugin v4.0.0.4121 -->
<!-- Propiedades de SonarCloud -->
```

### En raíz del proyecto
```
sonar-project.properties     # Configuración de SonarCloud
```

### En `.github/workflows/`
```
sonarcloud-analysis.yml      # Workflow principal de análisis
build-test.yml               # Workflow de build y tests
```

### En `scripts/`
```
quality-analysis.sh          # Script para Linux/Mac
quality-analysis.ps1         # Script para Windows
```

### En `docs/`
```
SONARCLOUD_JACOCO_SETUP.md   # Guía completa paso a paso
SONARCLOUD_QUICK_START.md    # Guía rápida de 5 minutos
SONARCLOUD_ADVANCED.md       # Configuración avanzada
```

---

## 🚀 Pasos Iniciales

### 1. Crear Cuenta en SonarCloud
```bash
# Ir a https://sonarcloud.io
# Log in con GitHub
# Autorizar la aplicación
```

### 2. Crear Organización y Proyecto
```bash
# En SonarCloud
# Crear organización
# Crear proyecto "domesticas"
# Copiar: ORGANIZATION_KEY y PROJECT_KEY
```

### 3. Generar Tokens
```bash
# En SonarCloud: Account → Security → Generate Tokens
# Copiar: SONAR_TOKEN

# En GitHub: Settings → Developer settings → Personal access tokens
# Copiar: GITHUB_TOKEN (con permisos repo + workflow)
```

### 4. Guardar Secretos en GitHub
```bash
# GitHub → Repositorio → Settings → Secrets → Actions
# Crear:
# - SONAR_TOKEN = (token de SonarCloud)
# - GITHUB_TOKEN = (token personal de GitHub)
```

### 5. Actualizar Configuración Local
```bash
# El workflow de SonarCloud fija organization y projectKey al ejecutar el análisis.
# El análisis corre en GitHub Actions para la organización:
# fabrica-2026-1-calidad
```

### 6. Commit y Push
```bash
git add .
git commit -m "chore: configure SonarCloud and JaCoCo"
git push origin main
```

### 7. Verificar
```bash
# GitHub → Actions
# Ambos workflows deben pasar ✅

# SonarCloud
# Ver análisis en tu proyecto
```

---

## 🛠️ Comandos Útiles

### Build y Tests
```bash
mvn clean verify -DskipITs=true
```

### Generar Reporte de Cobertura
```bash
mvn jacoco:report
# Resultado: target/site/jacoco/index.html
```

### Análisis SonarCloud Local
```bash
mvn sonar:sonar \
  -Dsonar.token=TU_SONAR_TOKEN \
  -Dsonar.host.url=https://sonarcloud.io
```

### Análisis Completo (Local)
```bash
# Opción 1: Windows (PowerShell)
.\scripts\quality-analysis.ps1 5

# Opción 2: Linux/Mac (Bash)
bash scripts/quality-analysis.sh 5
```

---

## 📊 Métricas Monitoreadas

| Métrica | Mínimo | Tipo |
|---------|--------|------|
| **Cobertura** | 60% | 📈 Más es mejor |
| **Bugs** | 0 | 🐛 Menos es mejor |
| **Code Smells** | - | 💬 Revisar regularmente |
| **Vulnerabilidades** | 0 | 🔒 Crítico |
| **Duplicidad** | < 3% | 🔄 Menos es mejor |
| **Deuda técnica** | - | 📋 Monitorear |

---

## 🔄 Workflows Automáticos

### `build-test.yml` (Cada Push/PR)
```
✓ Build y Compile
✓ Test Unitarios
✓ Generar Cobertura (JaCoCo)
✓ Comentar en PR
```

### `sonarcloud-analysis.yml` (Cada Push/PR)
```
✓ Build con BD (PostgreSQL)
✓ Tests
✓ Cobertura JaCoCo
✓ Análisis SonarCloud
✓ Verificar Quality Gate
✓ Comentar en PR
```

---

## 📈 Mejorar Cobertura

### Pasos para aumentar cobertura
1. Ejecutar análisis local: `./scripts/quality-analysis.ps1 4`
2. Abrir: `target/site/jacoco/index.html`
3. Identificar código sin cobertura (rojo)
4. Crear tests unitarios para ese código
5. Ejecutar: `mvn test`
6. Repetir hasta alcanzar 60%+

### Clases críticas a testear
```
controllers/    - Endpoints HTTP
services/       - Lógica de negocio
repositories/   - Acceso a datos
security/       - Autenticación
```

---

## 🐛 Troubleshooting

| Problema | Causa | Solución |
|----------|-------|----------|
| Error: "projectKey not found" | Clave incorrecta | Verificar en SonarCloud → Settings |
| QG falla sin errores claros | Métricas desconocidas | Revisar sonar-project.properties |
| 0% cobertura | JaCoCo no genera XML | Ejecutar `mvn clean verify` |
| Workflow timeout | Análisis muy lento | Aumentar timeout o excluir directorios |
| Sin comentarios en PR | Webhook no activado | Habilitar en Project Settings |

---

## 📚 Documentación Completa

- [🔧 Guía de Instalación Completa](./SONARCLOUD_JACOCO_SETUP.md)
- [⚡ Guía Rápida (5 min)](./SONARCLOUD_QUICK_START.md)
- [🚀 Configuración Avanzada](./SONARCLOUD_ADVANCED.md)

---

## 🎯 Próximos Pasos

### Fase 1: Setup (Completo ✅)
- [x] Instalar JaCoCo
- [x] Instalar SonarCloud
- [x] Crear workflows
- [x] Documentar

### Fase 2: Integración (Próximo)
- [ ] Crear cuenta en SonarCloud
- [ ] Crear proyecto
- [ ] Guardar secrets en GitHub
- [ ] Ejecutar primer análisis

### Fase 3: Mejora Continua
- [ ] Alcanzar 60%+ cobertura
- [ ] Revisar y arreglar bugs
- [ ] Implementar code reviews
- [ ] Monitorear tendencias

---

## 🔗 Enlaces Importantes

| Recurso | URL |
|---------|-----|
| **SonarCloud** | https://sonarcloud.io |
| **GitHub Actions** | https://github.com/TU_USUARIO/domesticas/actions |
| **JaCoCo Local** | file://target/site/jacoco/index.html |
| **Maven Goal** | `mvn sonar:sonar` |

---

## 💡 Consejos

✨ **Mejor práctica:** Ejecutar `./scripts/quality-analysis.ps1` antes de mergear a main

✨ **Automatismo:** El workflow se ejecuta automáticamente en cada push

✨ **Feedback:** Los PRs incluyen comentarios de SonarCloud

✨ **Tendencias:** SonarCloud almacena histórico de métricas

---

## 📞 Soporte

Para más información:
- [SonarCloud Docs](https://docs.sonarcloud.io/)
- [JaCoCo Guide](https://www.jacococoverage.org/)
- [GitHub Actions](https://docs.github.com/en/actions)

---

**Última actualización:** Viernes, 4 de mayo de 2026

