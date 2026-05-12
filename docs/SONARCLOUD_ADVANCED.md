# Configuración Avanzada de Quality Gates en SonarCloud

## 📋 Plantillas de Quality Gates

### 🟢 Quality Gate BÁSICO (60% cobertura)
```json
{
  "name": "Domesticas - Basic",
  "conditions": [
    {
      "metric": "coverage",
      "op": "LT",
      "error": "60"
    },
    {
      "metric": "blocker_violations",
      "op": "GT",
      "error": "0"
    }
  ]
}
```

**Reglas:**
- ✗ Cobertura < 60%
- ✗ Violaciones bloqueantes > 0

---

### 🟡 Quality Gate INTERMEDIO (70% cobertura)
```json
{
  "name": "Domesticas - Intermediate",
  "conditions": [
    {
      "metric": "coverage",
      "op": "LT",
      "error": "70"
    },
    {
      "metric": "blocker_violations",
      "op": "GT",
      "error": "0"
    },
    {
      "metric": "critical_violations",
      "op": "GT",
      "error": "0"
    },
    {
      "metric": "duplicated_lines_density",
      "op": "GT",
      "error": "5"
    }
  ]
}
```

**Reglas:**
- ✗ Cobertura < 70%
- ✗ Violaciones bloqueantes > 0
- ✗ Violaciones críticas > 0
- ✗ Código duplicado > 5%

---

### 🔴 Quality Gate ESTRICTO (80% cobertura)
```json
{
  "name": "Domesticas - Strict",
  "conditions": [
    {
      "metric": "coverage",
      "op": "LT",
      "error": "80",
      "warning": "85"
    },
    {
      "metric": "blocker_violations",
      "op": "GT",
      "error": "0"
    },
    {
      "metric": "critical_violations",
      "op": "GT",
      "error": "0"
    },
    {
      "metric": "major_violations",
      "op": "GT",
      "error": "5"
    },
    {
      "metric": "duplicated_lines_density",
      "op": "GT",
      "error": "3"
    },
    {
      "metric": "security_rating",
      "op": "LT",
      "error": "A"
    },
    {
      "metric": "sqale_rating",
      "op": "LT",
      "error": "A"
    }
  ]
}
```

**Reglas:**
- ✗ Cobertura < 80% (⚠ < 85%)
- ✗ Violaciones bloqueantes > 0
- ✗ Violaciones críticas > 0
- ✗ Violaciones mayores > 5
- ✗ Código duplicado > 3%
- ✗ Seguridad < A
- ✗ Mantenibilidad < A

---

## 🔧 Configuración de Webhooks

### Habilitar PR Decoration en GitHub

**En SonarCloud:**
1. **Organization Settings** → **Developer Applications**
2. Busca "GitHub" App
3. **Configure** → **ALM Integration**
4. **Repository** → Tu repositorio
5. Habilitar: "Automatic PR Analysis"

**Resultado:** 
- SonarCloud comenta automáticamente en PRs
- Muestra problemas antes de mergear

---

## 📊 Configuración de Métricas Personalizadas

### Por Archivo
En `sonar-project.properties`, excluir según tipo:

```properties
# Excluir DTOs del cálculo de cobertura
sonar.coverage.exclusions=**/dto/**

# Excluir Configs del análisis
sonar.exclusions=**/config/**,**/Application.java

# Excluir Tests del cálculo de duplicidad
sonar.cpd.exclusions=**/*Test.java,**/test/**
```

---

## 🔐 Configuración de Seguridad

### Habilitar Análisis de Seguridad (SAST)
Por defecto, SonarCloud incluye:
- **OWASP Top 10**: Vulnerabilidades web
- **CWE**: Common Weakness Enumeration
- **CERT**: Estándares de seguridad

Para optimizar:
1. **Project Settings** → **Security**
2. Revisar "Security Hotspots"
3. Configurar exclusiones si es necesario

---

## 📈 Integración con Sistemas de Notificación

### Slack
1. **Organization Settings** → **Notifications**
2. URL webhook de Slack
3. Seleccionar eventos:
   - New issues
   - Quality gate failures
   - Security alerts

### Email
1. **Profile** → **Notifications** (usuario)
2. Opciones:
   - Weekly report
   - Daily digest
   - Real-time alerts

---

## 🚀 Optimización de Análisis

### Reducir tiempo de ejecución
```properties
# En sonar-project.properties
sonar.analysis.mode=preview  # Para PRs
sonar.skipPackageDesign=true
sonar.skipDesign=true
```

### Paralelizar análisis (Enterprise)
```xml
<!-- En pom.xml -->
<properties>
    <sonar.threads>4</sonar.threads>
</properties>
```

---

## 🎯 Reglas Personalizadas por Proyecto

### Desactivar reglas específicas
1. **Quality Profiles** → Tu perfil
2. Buscar la regla
3. **Deactivate** (si no aplica)

### Crear perfil personalizado
1. **Quality Profiles** → **Create**
2. Base: "Sonar way"
3. Agregar/Remover reglas según necesidad

---

## 📝 Ejemplos de Configuración en CI/CD

### GitHub Actions - Esperar Quality Gate

```yaml
- name: Esperar Quality Gate
  run: |
    mvn sonar:sonar \
      -Dsonar.token=${{ secrets.SONAR_TOKEN }} \
      -Dsonar.qualitygate.wait=true
```

### GitHub Actions - Fallar si no pasa QG

```yaml
- name: Verificar Quality Gate
  id: quality-gate
  run: |
    QG_STATUS=$(curl -s \
      -H "Authorization: Bearer ${{ secrets.SONAR_TOKEN }}" \
      "https://sonarcloud.io/api/qualitygates/project_status?projectKey=..." \
      | jq -r '.projectStatus.status')
    
    if [ "$QG_STATUS" != "OK" ]; then
      echo "❌ Quality Gate falló"
      exit 1
    fi
```

---

## 📚 Recursos Avanzados

| Tema | Enlace |
|------|--------|
| Quality Gates API | https://docs.sonarcloud.io/apidocs/quality-gates/ |
| Custom Rules | https://docs.sonarcloud.io/improving/quality-profiles/ |
| Webhooks | https://docs.sonarcloud.io/advanced-setup/webhooks/ |
| ALM Integration | https://docs.sonarcloud.io/advanced-setup/ci-cd-integration/github-actions/ |
| Security Reports | https://docs.sonarcloud.io/advanced-setup/security-reports/ |

