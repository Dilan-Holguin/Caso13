# Configuración de SonarCloud y JaCoCo

## Requisitos Previos
- Cuenta GitHub conectada a SonarCloud
- Token de acceso personal en GitHub (PAT)
- Repositorio en GitHub

---

## 📋 Paso 1: Crear Organización y Proyecto en SonarCloud

### 1.1 Acceder a SonarCloud
1. Ve a [https://sonarcloud.io](https://sonarcloud.io)
2. Click en **"Log in"** y selecciona **GitHub**
3. Autoriza la aplicación de SonarCloud

### 1.2 Crear una Organización (si es la primera vez)
1. En el dashboard, click en **"+"** → **"Create new organization"**
2. Selecciona **"Analyze new project"**
3. Autoriza el acceso a tu repositorio

### 1.3 Crear un nuevo proyecto
1. Click en **"Analyze new project"**
2. Selecciona tu repositorio (domesticas)
3. Elige **"Maven"** como tipo de proyecto
4. Copia tu `ORGANIZATION_KEY` (ej: `tu-organizacion`)

---

## 🔐 Paso 2: Generar y Configurar Tokens

### 2.1 Generar SONAR_TOKEN
1. En SonarCloud, ve a **Account** → **Security**
2. Click en **"Generate Tokens"**
3. Nombre: `GitHub Actions`
4. Copia el token generado

### 2.2 Generar GITHUB_TOKEN (Personal Access Token)
1. Ve a GitHub → **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)**
2. Click en **"Generate new token (classic)"**
3. Dale los permisos:
   - `repo` (full control)
   - `workflow`
   - `read:org`
4. Copia el token

---

## 🔧 Paso 3: Configurar Secretos en GitHub

### 3.1 Agregar secretos al repositorio
1. Ve a tu repositorio en GitHub
2. **Settings** → **Secrets and variables** → **Actions**
3. Click en **"New repository secret"** y agrega:

| Nombre | Valor |
|--------|-------|
| `SONAR_TOKEN` | Token generado en SonarCloud |
| `GITHUB_TOKEN` | Token personal de GitHub (opcional, se crea automáticamente) |

---

## ⚙️ Paso 4: Actualizar Configuración Local

### 4.1 Actualizar sonar-project.properties
```properties
# Reemplaza con tus valores
sonar.projectKey=tu-usuario_domesticas
sonar.organization=tu-organizacion
```

### 4.2 Actualizar pom.xml
```xml
<properties>
    <sonar.projectKey>tu-usuario_domesticas</sonar.projectKey>
    <sonar.organization>tu-organizacion</sonar.organization>
</properties>
```

**Dónde encontrar estos valores:**
- `projectKey`: SonarCloud → Tu proyecto → **Project Settings** → **Project Key**
- `organization`: SonarCloud → **Organization Settings**

---

## 🚀 Paso 5: Ejecutar Análisis Localmente (Opcional)

```bash
# Instalar Maven (si no está instalado)
mvn --version

# Ejecutar build con cobertura
mvn clean verify

# Ejecutar análisis SonarCloud
mvn sonar:sonar \
  -Dsonar.token=tu-sonar-token \
  -Dsonar.host.url=https://sonarcloud.io

# Para Windows (PowerShell):
mvn sonar:sonar `
  -Dsonar.token=tu-sonar-token `
  -Dsonar.host.url=https://sonarcloud.io
```

---

## 🔄 Paso 6: Configurar Quality Gates

### 6.1 Ver Quality Gates predeterminados
1. En SonarCloud, ve a **Quality Gates**
2. Selecciona **"Sonar way"** (default)

### 6.2 Configurar Quality Gate personalizado
1. Click en **"Create"**
2. Nombre: `Domesticas Quality Gate`
3. Agrega condiciones (`+Add condition`):

| Métrica | Operador | Valor |
|---------|----------|-------|
| Coverage | is less than | 60% |
| Bugs | is greater than | 0 |
| Code Smells | is greater than | 0 |
| Security Hotspots Reviewed | is less than | 100% |
| Duplicated Lines (%) | is greater than | 3% |

### 6.3 Asignar Quality Gate al proyecto
1. Ve a tu proyecto
2. **Project Settings** → **Quality Gates**
3. Selecciona tu Quality Gate personalizado

---

## 📊 Paso 7: Configurar Reglas y Perfiles

### 7.1 Crear perfil de calidad personalizado
1. Ve a **Quality Profiles**
2. Click en **Java** profile
3. Click en **"Create"**
4. Nombre: `Domesticas Rules`
5. Copia desde: `Sonar way`
6. Descativa reglas que no apliquen a tu proyecto

### 7.2 Asociar el perfil al proyecto
1. Ve a tu proyecto
2. **Project Settings** → **Quality Profiles**
3. Selecciona `Domesticas Rules`

---

## 🔔 Paso 8: Configurar Notificaciones y Webhooks

### 8.1 Configurar notificaciones de Quality Gate
1. En tu proyecto, **Project Settings** → **Notifications**
2. Habilita notificaciones por email/Slack

### 8.2 Configurar webhook en GitHub (opcional)
SonarCloud automáticamente crea un webhook. Para verificar:
1. GitHub → **Settings** → **Webhooks**
2. Deberías ver una entrada de SonarCloud

---

## 📈 Paso 9: Agregar Verificación de Pull Requests

### 9.1 Habilitar análisis en PRs
1. En SonarCloud, ve a tu proyecto
2. **Project Settings** → **Analysis Method**
3. Asegúrate que **"SonarCloud Automatic Analysis"** está habilitado

### 9.2 Configurar restricción en GitHub (Ruleset)
1. GitHub → **Settings** → **Rules**
2. Click **"New ruleset"**
3. Nombre: `Code Quality`
4. Targets: `Pull requests`
5. Agrega regla: **Require status checks to pass**
   - Busca y selecciona `sonarcloud/sonarcloud-github-pr-decoration`

---

## 🧪 Paso 10: Validar la Configuración

### 10.1 Ejecutar un test completo
```bash
# Hacer un commit y push
git add .
git commit -m "chore: configure SonarCloud and JaCoCo"
git push origin main

# GitHub Actions deberían ejecutar automáticamente
# Ve a: GitHub → Actions para ver el progreso
```

### 10.2 Verificar resultados
1. Ve a **Actions** en GitHub
2. Click en el workflow más reciente
3. Verifica que ambos workflows pasaron:
   - ✅ Build e Tests
   - ✅ SonarCloud Analysis

### 10.3 Ver análisis en SonarCloud
1. Ve a [sonarcloud.io](https://sonarcloud.io)
2. Selecciona tu proyecto
3. Verifica:
   - 📊 Métricas generales
   - 🐛 Bugs encontrados
   - 💬 Code Smells
   - 📈 Cobertura de código (% de cobertura)

---

## 🔍 Paso 11: Interpretar Resultados

### 11.1 Dashboard de SonarCloud
- **Coverage**: Porcentaje de código cubierto por tests
- **Bugs**: Errores potenciales encontrados
- **Code Smells**: Problemas de calidad/mantenimiento
- **Security Hotspots**: Posibles vulnerabilidades
- **Duplicated Code**: Código duplicado

### 11.2 Reportes de JaCoCo
- Ubicación: `target/site/jacoco/index.html`
- Muestra cobertura línea por línea
- Identifica qué código NO está cubierto

---

## 📝 Paso 12: Configurar Exclusiones (Opcional)

### 12.1 Excluir archivos del análisis
En `sonar-project.properties`:
```properties
# Excluir Tests y Configs
sonar.exclusions=**/*Test*.java,**/config/**,**/dto/**

# Excluir del cálculo de duplicidad
sonar.cpd.exclusions=**/*Test*.java
```

### 12.2 Excluir packages en SonarCloud UI
1. Project Settings → **Analysis Scope** → **Source File Exclusions**
2. Agrega patterns a excluir

---

## 🚨 Troubleshooting

| Problema | Solución |
|----------|----------|
| Quality Gate fallando sin razón | Verifica que el `projectKey` esté correcto en SonarCloud |
| No se ve cobertura (0%) | Asegúrate que JaCoCo genera `target/site/jacoco/jacoco.xml` |
| Workflow falla por SONAR_TOKEN | Verifica que el token no haya expirado en SonarCloud |
| PR decorate no funciona | Habilita "Analyze PR" en Project Settings |
| Análisis muy lento | Excluye directorios innecesarios en `sonar.exclusions` |

---

## 🎯 Próximos Pasos

1. **Crear tests adicionales** para alcanzar mínimo 60% de cobertura
2. **Revisar y arreglar bugs** encontrados por SonarCloud
3. **Implementar Code Reviews** basados en reportes
4. **Configurar Slack** para notificaciones en tiempo real
5. **Analizar tendencias** de calidad en el tiempo

---

## 📚 Recursos

- [SonarCloud Documentation](https://docs.sonarcloud.io/)
- [SonarCloud Java Guide](https://docs.sonarcloud.io/getting-started/github/)
- [JaCoCo Maven Plugin](https://www.jacococoverage.org/jacoco/trunk/doc/maven.html)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Quality Gates en SonarCloud](https://docs.sonarcloud.io/improving/quality-gates/)

