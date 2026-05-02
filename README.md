# Domésticas API

Backend REST para la gestión de tareas del hogar. Construido con Spring Boot 3.5, PostgreSQL, Flyway y Docker.

---

## Tecnologías

- **Java 21** + **Spring Boot 3.5.13**
- **Spring Security** + **JWT** (filtro de autenticación implementado)
- **Spring Data JPA** + **Hibernate** para persistencia
- **Flyway** para migraciones versionadas de base de datos
- **PostgreSQL 15** como base de datos
- **Docker Desktop** para correr la base de datos localmente
- **Mailtrap** para pruebas de envío de correo
- **Springdoc OpenAPI** (Swagger UI) para documentación interactiva de endpoints

---

## Requisitos previos

- [Java 21](https://adoptium.net/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- Cuenta gratuita en [Mailtrap](https://mailtrap.io) (para probar recuperación de contraseña)

---

## Configuración paso a paso

### 1. Clonar y configurar variables de entorno

```bash
git clone https://github.com/tu-usuario/domesticas.git
cd domesticas
cp .env.example .env
# Edita .env con tus credenciales de Mailtrap y ajusta el JWT_SECRET
```

### 2. Levantar la base de datos

```bash
docker compose up -d
```

### 3. Ejecutar migraciones (Flyway)

```bash
./scripts/deploy-db.sh local
```

### 4. Configurar Mailtrap

Edita `.env` con tus credenciales de Mailtrap (Email Testing → My Sandbox → Integration).

### 5. Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`.

---

## Documentación interactiva (Swagger UI)

```
http://localhost:8080/swagger-ui/index.html
```

Para generar documentación estática:

```bash
./scripts/generate-api-docs.sh
# Archivos generados en docs/: openapi.json, openapi.yaml, postman_collection.json
```

---

## Endpoints disponibles

Todos los prefijos bajo `/api`.

### Autenticación — `/api/auth`

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/auth/register` | Registrar nuevo usuario | No |
| `POST` | `/api/auth/login` | Iniciar sesión, obtener JWT | No |
| `POST` | `/api/auth/logout` | Cerrar sesión (cliente descarta token) | No |
| `POST` | `/api/auth/forgot-password` | Solicitar recuperación de contraseña | No |
| `POST` | `/api/auth/reset-password` | Establecer nueva contraseña con token | No |

### Hogares — `/api/households`

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/households` | Crear un nuevo hogar | JWT |
| `POST` | `/api/households/{id}/invite` | Invitar miembro al hogar | JWT (Admin) |
| `POST` | `/api/households/invitations/{token}/respond` | Aceptar/rechazar invitación | JWT |
| `GET` | `/api/households/{id}/members` | Listar miembros del hogar | JWT (Miembro) |

### Tareas — `/api/households/{id}/tasks` y `/api/tasks`

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/households/{hogarId}/tasks` | Crear tarea en un hogar | JWT (Miembro) |
| `GET` | `/api/households/{hogarId}/tasks?estado=&categoria=&asignadoA=` | Listar tareas con filtros | JWT (Miembro) |
| `GET` | `/api/tasks/{tareaId}` | Obtener detalle de tarea | JWT (Miembro) |
| `PUT` | `/api/tasks/{tareaId}` | Actualizar tarea | JWT (Miembro) |
| `PATCH` | `/api/tasks/{tareaId}/status` | Cambiar estado de tarea | JWT (Miembro) |
| `DELETE` | `/api/tasks/{tareaId}` | Eliminar tarea | JWT (Admin) |

### Páginas HTML — `/join` y `/reset-password`

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `GET` | `/join?token=` | Página para aceptar o rechazar invitación a un hogar | No |
| `GET` | `/reset-password?token=` | Página para establecer nueva contraseña | No |

### Flujo completo de uso

1. **Registro** → `POST /api/auth/register`
2. **Login** → `POST /api/auth/login` → obtienes JWT
3. **Crear hogar** → `POST /api/households` (Header: `Authorization: Bearer <token>`)
4. **Invitar miembros** → `POST /api/households/{id}/invite`
5. **Crear tareas** → `POST /api/households/{id}/tasks`
6. **Gestionar tareas** → `GET`, `PUT`, `PATCH` sobre `/api/tasks/{id}`

### Estados de tarea

`Pendiente` → `En_progreso` → `Completada`

### Categorías de tarea

`Limpieza`, `Cocina`, `Compras`, `Mantenimiento`, `Otro`

---

## Estructura del proyecto

```
src/main/java/com/eap08/domesticas/
├── DomesticasApplication.java
├── controller/
│   ├── AuthController.java        ← autenticación
│   ├── HogarController.java       ← gestión de hogares
│   ├── PageController.java        ← páginas HTML (/join, /reset-password)
│   └── TareaController.java       ← gestión de tareas
├── dto/
│   ├── AuthResponse.java
│   ├── ErrorResponse.java
│   ├── HogarRequest.java          ← records: CreateHogar, InvitarMiembro, ResponderInvitacion
│   ├── HogarResponse.java         ← records: HogarData, InvitacionResponse, MiembroResponse
│   ├── TareaRequest.java          ← records: CreateTarea, UpdateTarea, UpdateStatus
│   ├── TareaResponse.java         ← records: TareaData, TareaListData, AsignadoInfo
│   └── ...
├── model/
│   ├── Hogar.java
│   ├── InvitacionHogar.java
│   ├── PasswordResetToken.java
│   ├── Tarea.java
│   ├── Usuario.java
│   ├── UsuarioHogar.java
│   └── UsuarioHogarId.java        ← clave compuesta embeddable
├── repository/
│   ├── HogarRepository.java
│   ├── InvitacionHogarRepository.java
│   ├── PasswordResetTokenRepository.java
│   ├── TareaRepository.java
│   ├── UsuarioHogarRepository.java
│   └── UsuarioRepository.java
├── security/
│   ├── GlobalExceptionHandler.java
│   ├── JwtAuthFilter.java         ← filtro JWT (nuevo)
│   ├── JwtUtil.java
│   └── SecurityConfig.java        ← config con JWT filter
└── service/
    ├── AuthService.java
    ├── HogarService.java
    ├── TareaService.java
    └── impl/
        ├── AuthServiceImpl.java
        ├── EmailService.java
        └── UserDetailsServiceImpl.java

src/main/resources/
├── META-INF/
│   └── additional-spring-configuration-metadata.json  ← autocompletado IDE
├── application.properties         ← variables de entorno con defaults
└── db/migration/
    ├── V1__init_schema.sql        ← esquema base
    └── V2__invitacion_hogar.sql   ← tabla de invitaciones

scripts/
├── deploy-db.sh                   ← despliegue de migraciones
├── generate-api-docs.sh           ← generación de OpenAPI + Postman
└── test-api.sh                    ← 22 pruebas automatizadas de endpoints

docs/
├── openapi.json                   ← especificación OpenAPI 3.0
├── domesticas.postman_collection.json
└── vulnerability-report.md        ← informe de vulnerabilidades
```

---

## Manejo de errores

Todos los errores siguen una estructura uniforme:

```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Los datos enviados no son válidos",
  "details": ["El formato del correo no es válido"],
  "traceId": "a1b2c3d4-...",
  "timestamp": "2026-04-03T10:00:00"
}
```

Códigos: `VALIDATION_ERROR` (400), `INVALID_CREDENTIALS` (401), `BUSINESS_ERROR` (409), `INTERNAL_ERROR` (500).

---

## Autenticación

Todas las peticiones a endpoints protegidos requieren el header:

```
Authorization: Bearer <jwt_token>
```

El token se obtiene mediante `POST /api/auth/login`. El filtro `JwtAuthFilter` valida automáticamente el token en cada petición. Endpoints públicos (`/api/auth/**`, Swagger) no requieren token.

---

## Variables de entorno

| Variable | Descripción | Default (dev) |
|----------|-------------|---------------|
| `SPRING_DATASOURCE_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/domesticas_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuario BD | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña BD | `postgres` |
| `APP_JWT_SECRET` | Secreto para firmar JWT | *(cambiar en producción)* |
| `APP_JWT_EXPIRATION` | Expiración del token (ms) | `86400000` (24h) |
| `SPRING_MAIL_HOST` | Servidor SMTP | `sandbox.smtp.mailtrap.io` |
| `SPRING_MAIL_PORT` | Puerto SMTP | `2525` |
| `SPRING_MAIL_USERNAME` | Usuario SMTP | *(configurar)* |
| `SPRING_MAIL_PASSWORD` | Contraseña SMTP | *(configurar)* |
| `APP_FRONTEND_URL` | URL del frontend | `http://localhost:3000` |
| `APP_CORS_ORIGINS` | Orígenes CORS (coma) | `https://project-tdwx8.vercel.app,http://localhost:3000` |

---

## Seguridad — Informe de vulnerabilidades

Ver `docs/vulnerability-report.md` para el informe completo con 8 hallazgos documentados (corregidos y recomendaciones pendientes).

---

## Flujo de trabajo con Git

El equipo trabaja con **feature branches**. No hacer push directo a `main` — requiere Pull Request y revisión.
