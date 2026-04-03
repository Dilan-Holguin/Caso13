# Domésticas API

Backend REST para la gestión de tareas del hogar. Construido con Spring Boot 3.5, PostgreSQL y Docker.

---

## Tecnologías

- **Java 21** + **Spring Boot 3.5.13**
- **Spring Security** + **JWT** para autenticación
- **Spring Data JPA** + **Hibernate** para persistencia
- **PostgreSQL 15** como base de datos
- **Docker Desktop** para correr la base de datos localmente
- **Mailtrap** para pruebas de envío de correo
- **Springdoc OpenAPI** (Swagger UI) para documentación de endpoints

---

## Requisitos previos

Antes de clonar y correr el proyecto, asegúrate de tener instalado lo siguiente en tu máquina.

- [Java 21](https://adoptium.net/) — puedes verificar con `java -version`
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) — para levantar la base de datos
- Una cuenta gratuita en [Mailtrap](https://mailtrap.io) — para probar el envío de correos de recuperación de contraseña

---

## Configuración paso a paso

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/domesticas.git
cd domesticas
```

### 2. Levantar la base de datos con Docker

El proyecto incluye un `docker-compose.yml` en la raíz con toda la configuración necesaria. El primer `docker compose up` ejecuta automáticamente el script `init-db/01_schema.sql` que crea todas las tablas del esquema.

```bash
docker compose up -d
```

Puedes verificar que las tablas se crearon correctamente con este comando:

```bash
docker exec -it postgres_domesticas psql -U postgres -d domesticas_db -c "\dt"
```

Deberías ver las tablas `hogar`, `usuario`, `usuario_hogar`, `tarea` y `password_reset_token`.

> **Importante:** si ya tenías una versión anterior del volumen de Docker, elimínalo antes de levantar el contenedor para que el script de inicialización se ejecute desde cero. Puedes hacerlo desde la sección **Volumes** de Docker Desktop.

### 3. Configurar Mailtrap

El proyecto usa Mailtrap como servidor SMTP falso para capturar los correos de recuperación de contraseña durante el desarrollo, de forma que nadie recibe correos reales mientras se prueba.

1. Inicia sesión en [mailtrap.io](https://mailtrap.io) y ve a **Email Testing → Inboxes → My Sandbox → Integration**.
2. Selecciona el formato **Other: Java, Scala - Play-Mailer** para ver las credenciales.
3. Copia el `user` y el `password` que aparecen ahí.

### 4. Configurar el archivo application.properties

Abre el archivo `src/main/resources/application.properties` y reemplaza los valores de Mailtrap con los de tu propia cuenta:

```properties
spring.mail.username=TU_USERNAME_DE_MAILTRAP
spring.mail.password=TU_PASSWORD_DE_MAILTRAP
```

El resto de la configuración (base de datos, JWT, etc.) ya está lista para desarrollo local y no necesita cambios.

> **Nota de seguridad:** nunca subas credenciales reales de producción (Gmail, SendGrid, etc.) al repositorio. El `application.properties` actual solo contiene credenciales de Mailtrap, que son seguras de compartir en el contexto de desarrollo de equipo.

### 5. Ejecutar la aplicación

Desde la raíz del proyecto ejecuta:

```bash
.\mvnw spring-boot:run        # Windows (PowerShell)
./mvnw spring-boot:run        # Mac / Linux
```

No necesitas tener Maven instalado globalmente — el `mvnw` es un wrapper que descarga automáticamente la versión correcta de Maven para este proyecto.

Cuando veas este mensaje en la consola, la aplicación está lista:

```
Started DomesticasApplication in X seconds
```

---

## Documentación interactiva (Swagger UI)

Una vez que la aplicación esté corriendo, abre el navegador y ve a:

```
http://localhost:8080/swagger-ui/index.html
```

Ahí encontrarás todos los endpoints documentados y podrás probarlos directamente desde el navegador sin necesidad de instalar ninguna herramienta adicional.

---

## Endpoints disponibles

Todos los endpoints se encuentran bajo el prefijo `/api`.

### Autenticación — `/api/auth`

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Registrar nuevo usuario | No requerida |
| POST | `/api/auth/login` | Iniciar sesión y obtener JWT | No requerida |
| POST | `/api/auth/logout` | Cerrar sesión | No requerida |
| POST | `/api/auth/forgot-password` | Solicitar recuperación de contraseña | No requerida |
| POST | `/api/auth/reset-password` | Establecer nueva contraseña con token | No requerida |

### Flujo de recuperación de contraseña

1. Llama a `POST /api/auth/forgot-password` con `{ "email": "tu@correo.com" }`.
2. Revisa la bandeja de entrada de Mailtrap — encontrarás un enlace con el token.
3. Copia el token de la URL del enlace (el parámetro `?token=...`).
4. Llama a `POST /api/auth/reset-password` con `{ "token": "...", "nuevaPassword": "..." }`.

---

## Estructura del proyecto

```
src/main/java/com/eap08/domesticas/
├── DomesticasApplication.java    ← punto de entrada de Spring Boot
├── controller/                   ← recibe peticiones HTTP y delega al servicio
├── dto/                          ← objetos de transferencia de datos (entrada/salida de la API)
├── model/                        ← entidades JPA que mapean las tablas de la BD
├── repository/                   ← interfaces de acceso a datos (Spring Data JPA)
├── security/                     ← configuración de Spring Security, JWT y manejo de errores
└── service/
    ├── (interfaces)              ← contratos de la lógica de negocio
    └── impl/                     ← implementaciones concretas de los servicios
```

---

## Manejo de errores

Todos los errores de la API siguen una estructura uniforme:

```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Los datos enviados no son válidos",
  "details": ["El formato del correo no es válido"],
  "traceId": "a1b2c3d4-...",
  "timestamp": "2026-04-03T10:00:00"
}
```

Los códigos de error posibles son `VALIDATION_ERROR` (400), `INVALID_CREDENTIALS` (401), `BUSINESS_ERROR` (409) e `INTERNAL_ERROR` (500).

---

## Variables de entorno para producción

Cuando se despliegue en producción (Render u otro proveedor), las siguientes variables deben configurarse como variables de entorno del servidor — nunca deben estar hardcodeadas en el código:

| Variable | Descripción |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | URL JDBC de la base de datos en producción |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos |
| `APP_JWT_SECRET` | Clave secreta para firmar los tokens JWT |
| `SPRING_MAIL_USERNAME` | Usuario SMTP del servicio de correo en producción |
| `SPRING_MAIL_PASSWORD` | Contraseña SMTP del servicio de correo en producción |
| `APP_FRONTEND_URL` | URL del frontend en producción |

---

## Flujo de trabajo con Git

El equipo trabaja con **feature branches**. Cada Historia de Usuario tiene su propia rama con el prefijo `feature/`. Cuando el trabajo esté listo, se abre un Pull Request hacia `main` para revisión antes de hacer merge.

```bash
# Crear una rama para una HU nueva
git checkout -b feature/nombre-de-la-hu

# Subir cambios durante el desarrollo
git add .
git commit -m "feat: descripción breve del cambio"
git push origin feature/nombre-de-la-hu
```
