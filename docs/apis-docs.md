# Documentación de APIs — Sistema de Tareas Domésticas
**Versión:** 1.0 | **Sprint:** 2 | **Base URL:** `https://caso13.onrender.com`

---

## Autenticación

Todos los endpoints (excepto `/api/auth/*`) requieren un token JWT en el header:
```
Authorization: Bearer <token>
```

---

## Módulo Auth

### POST /api/auth/register
Registra un nuevo usuario en el sistema.

**Body:**
```json
{
  "nombre": "María López",
  "email": "maria@test.com",
  "password": "Admin123!"
}
```
**Respuestas:**
| Código | Descripción |
|--------|-------------|
| 201 | Usuario registrado exitosamente |
| 400 | Datos inválidos o email ya registrado |

---

### POST /api/auth/login
Autentica un usuario y retorna el token JWT.

**Body:**
```json
{
  "email": "maria@test.com",
  "password": "Admin123!"
}
```
**Respuesta exitosa (200):**
```json
{
  "token": "eyJhbGci...",
  "tipo": "Bearer",
  "email": "maria@test.com"
}
```

---

### POST /api/auth/forgot-password
Envía un email con el link de recuperación de contraseña.

**Body:**
```json
{ "email": "maria@test.com" }
```
**Respuestas:**
| Código | Descripción |
|--------|-------------|
| 200 | Email enviado si el usuario existe |
| 400 | Email inválido |

---

### POST /api/auth/reset-password
Cambia la contraseña usando el token recibido por email.

**Body:**
```json
{
  "token": "uuid-del-email",
  "nuevaPassword": "NuevoPass123!"
}
```
**Respuestas:**
| Código | Descripción |
|--------|-------------|
| 200 | Contraseña actualizada |
| 400 | Token inválido o expirado |

---

## Módulo Hogares

### POST /api/households
Crea un nuevo hogar. El usuario autenticado queda como Administrador.

**Body:**
```json
{
  "nombre": "Casa Medellín",
  "descripcion": "Hogar principal"
}
```
**Respuesta exitosa (201):**
```json
{
  "hogarId": 1,
  "nombre": "Casa Medellín",
  "descripcion": "Hogar principal",
  "creadoEn": "2026-05-01T10:00:00"
}
```

---

### POST /api/households/{hogarId}/invite
Invita a un usuario al hogar por email. Solo el Administrador puede invitar.

**Body:**
```json
{ "emailInvitado": "carlos@test.com" }
```
**Respuesta exitosa (201):**
```json
{
  "invitacionId": 1,
  "emailInvitado": "carlos@test.com",
  "nombreHogar": "Casa Medellín",
  "token": "uuid-token",
  "fechaExpiracion": "2026-05-03T10:00:00",
  "estado": "Pendiente"
}
```
**Respuestas:**
| Código | Descripción |
|--------|-------------|
| 201 | Invitación generada |
| 403 | No eres Administrador |
| 400 | Usuario ya es miembro o invitación duplicada |

---

### POST /api/households/invitations/{token}/respond
Acepta o rechaza una invitación. Solo el email destinatario puede responder.

**Body:**
```json
{ "accion": "ACEPTAR" }
```
> Valores válidos: `ACEPTAR` o `RECHAZAR`

**Respuestas:**
| Código | Descripción |
|--------|-------------|
| 200 | Invitación procesada |
| 400 | Invitación ya procesada o expirada |
| 403 | El email no corresponde a esta invitación |
| 404 | Token no válido |

---

### GET /api/households/{hogarId}/members
Lista todos los miembros activos del hogar.

**Respuesta exitosa (200):**
```json
[
  {
    "usuarioId": 1,
    "nombre": "María López",
    "email": "maria@test.com",
    "rol": "Administrador",
    "fechaUnion": "2026-05-01T10:00:00"
  }
]
```

---

## Módulo Tareas

### POST /api/households/{hogarId}/tasks
Crea una nueva tarea en el hogar.

**Body:**
```json
{
  "titulo": "Limpiar cocina",
  "descripcion": "Limpiar estufa y nevera",
  "categoria": "Limpieza",
  "fechaLimite": "2026-05-10T18:00:00",
  "asignadoA": 2
}
```
> Categorías válidas: `Limpieza`, `Cocina`, `Compras`, `Mantenimiento`, `Otro`

**Respuestas:**
| Código | Descripción |
|--------|-------------|
| 201 | Tarea creada |
| 400 | Datos inválidos |
| 403 | No perteneces al hogar |

---

### GET /api/households/{hogarId}/tasks
Lista las tareas del hogar con filtros opcionales.

**Query params:**
| Parámetro | Tipo | Valores válidos |
|-----------|------|-----------------|
| estado | String | `Pendiente`, `En_progreso`, `Completada` |
| categoria | String | `Limpieza`, `Cocina`, `Compras`, `Mantenimiento`, `Otro` |
| asignadoA | Long | ID del usuario |

**Ejemplo:** `GET /api/households/1/tasks?estado=Pendiente&categoria=Limpieza`

---

### GET /api/tasks/{tareaId}
Retorna el detalle de una tarea específica.

**Respuestas:**
| Código | Descripción |
|--------|-------------|
| 200 | Detalle de la tarea |
| 403 | No tienes acceso |
| 404 | Tarea no encontrada |

---

### PUT /api/tasks/{tareaId}
Actualiza los datos de una tarea.

**Body:**
```json
{
  "titulo": "Limpiar cocina y baño",
  "descripcion": "Incluir el baño principal",
  "categoria": "Limpieza",
  "fechaLimite": "2026-05-11T18:00:00",
  "asignadoA": 3
}
```

---

### PATCH /api/tasks/{tareaId}/status
Cambia únicamente el estado de una tarea.

**Body:**
```json
{ "estado": "En_progreso" }
```
> Estados válidos: `Pendiente`, `En_progreso`, `Completada`

---

### DELETE /api/tasks/{tareaId}
Elimina una tarea. Solo el Administrador del hogar puede eliminar.

**Respuestas:**
| Código | Descripción |
|--------|-------------|
| 204 | Tarea eliminada |
| 403 | Solo el Administrador puede eliminar |
| 404 | Tarea no encontrada |

---

## Páginas HTML

### GET /reset-password?token={token}
Renderiza el formulario de cambio de contraseña.

### GET /join?token={token}
Renderiza la página para aceptar o rechazar una invitación al hogar.

---

## Códigos de error estándar

Todos los errores retornan la misma estructura:
```json
{
  "status": 403,
  "message": "No tienes permiso para realizar esta acción",
  "timestamp": "2026-05-01T10:00:00"
}
```