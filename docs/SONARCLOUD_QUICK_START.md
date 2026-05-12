# 🎯 Guía Rápida: SonarCloud + JaCoCo + GitHub Actions

## ⚡ Configuración en 5 Minutos

### 1️⃣ En SonarCloud (sonarcloud.io)
```
1. Log in con GitHub
2. Authorize SonarCloud
3. Crear organización (si es la primera vez)
4. Seleccionar repositorio: domesticas
5. Copiar ORGANIZATION_KEY
6. Account → Security → Generate Token
7. Copiar SONAR_TOKEN
```

### 2️⃣ En GitHub (tu repositorio)
```
1. Settings → Secrets and variables → Actions
2. Crear secret: SONAR_TOKEN (valor del paso anterior)
3. Crear secret: GITHUB_TOKEN (PAT con repo + workflow permisos)
```

### 3️⃣ Aquí (en tu máquina local)
```bash
# Actualizar sonar-project.properties
sonar.projectKey=TU_USUARIO_domesticas
sonar.organization=TU_ORGANIZACION

# Actualizar pom.xml
<sonar.organization>TU_ORGANIZACION</sonar.organization>
<sonar.projectKey>TU_USUARIO_domesticas</sonar.projectKey>

# Commit y Push
git add .
git commit -m "chore: configure SonarCloud"
git push origin main
```

### 4️⃣ Verificar
- GitHub → **Actions** → Ver que workflows pasen ✅
- SonarCloud → Tu proyecto → Ver análisis y cobertura 📊

---

## 📊 Command Locals (Desarrollo)

```bash
# Build + Tests + Coverage
mvn clean verify

# Análisis con SonarCloud
mvn sonar:sonar \
  -Dsonar.token=TU_SONAR_TOKEN \
  -Dsonar.host.url=https://sonarcloud.io

# Ver reporte de cobertura localmente
mvn jacoco:report
# Abre: target/site/jacoco/index.html
```

---

## ✅ Checklist de Configuración

- [ ] Cuenta SonarCloud creada
- [ ] Organización en SonarCloud creada
- [ ] Proyecto "domesticas" añadido en SonarCloud
- [ ] SONAR_TOKEN generado y guardado en GitHub Secrets
- [ ] GITHUB_TOKEN generado y guardado en GitHub Secrets
- [ ] pom.xml actualizado con organization + projectKey
- [ ] sonar-project.properties actualizado
- [ ] Workflow `.github/workflows/sonarcloud-analysis.yml` creado
- [ ] Primer push ejecutado
- [ ] Workflows completados exitosamente en GitHub Actions
- [ ] Resultados visibles en SonarCloud
- [ ] Quality Gate configurado

---

## 🔗 Enlaces Importantes

| Recurso | URL |
|---------|-----|
| SonarCloud | https://sonarcloud.io |
| Mi Proyecto | https://sonarcloud.io/organizations/TU_ORGANIZACION/projects |
| GitHub Actions | https://github.com/TU_USUARIO/domesticas/actions |
| Reportes Locales | file://target/site/jacoco/index.html |

---

## 🐛 Problemas Comunes

**P: El workflow falla con "SONAR_TOKEN not found"**
- R: Verifica que el secret esté en **repo → Settings → Secrets → Actions**

**P: Quality Gate falla sin motivo**
- R: El projectKey debe coincidir exactamente en SonarCloud + pom.xml + sonar-project.properties

**P: No se ve cobertura (0%)**
- R: Ejecuta `mvn clean verify` localmente primero. El `jacoco.xml` debe existir en `target/site/jacoco/`

**P: Análisis muy lento**
- R: Aumenta timeout en `.github/workflows/sonarcloud-analysis.yml` si es necesario

---

## 📚 Ver También

- [Documentación Completa](./SONARCLOUD_JACOCO_SETUP.md)
- [Workflow Build](../.github/workflows/build-test.yml)
- [Workflow SonarCloud](../.github/workflows/sonarcloud-analysis.yml)

