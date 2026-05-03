# Volumen Estimado de Datos y Roles de BD — Sprint 2
**Sistema:** Organización de Tareas Domésticas | **Motor:** PostgreSQL 15

---

## Criterios de estimación

El sistema está orientado a una comunidad universitaria o grupos familiares pequeños.
Las estimaciones se basan en los siguientes supuestos de negocio:

- **Contexto:** plataforma adoptada inicialmente por estudiantes de una facultad.
- **Adopción:** crecimiento orgánico de ~50 usuarios/mes en el primer año.
- **Comportamiento:** se estima que 1 de cada 2-3 usuarios registrados crea o pertenece a un hogar activo.
- **Actividad de tareas:** un hogar activo con 3 miembros genera en promedio 6-8 tareas por semana, lo que equivale a ~25-30 tareas/mes por hogar.
- **Tokens y invitaciones:** son registros de vida corta (expiran en 24-48h), por lo que su volumen acumulado es bajo incluso con alta actividad.

---

## Volumen estimado por tabla

| Tabla | Registros iniciales | Crecimiento mensual | Registros año 1 | Justificación |
|-------|-------------------|--------------------|-----------------|-|
| `usuario`                | 50 | ~50 | ~650 | Adopción progresiva en comunidad universitaria |
| `hogar`                  | 20 | ~20 | ~260 | 1 hogar por cada 2.5 usuarios registrados |
| `usuario_hogar`          | 60 | ~60 | ~780 | Promedio 3 miembros por hogar |
| `tarea`                  | 200| ~500| ~6.200| 25 tareas/mes × 200 hogares activos al final del año |
| `password_reset_token`   | 10 | ~20 | ~250 | ~10% de usuarios solicita reset por mes; registros se marcan como usados |
| `invitacion_hogar`       | 30 | ~40 | ~510 | ~2 invitaciones por hogar nuevo + invitaciones recurrentes |

> **Nota:** `tarea` es la tabla de mayor crecimiento por ser el núcleo funcional del sistema.
> Se recomienda evaluar particionamiento por `hogar_id` o archivado de tareas completadas
> a partir del año 2 si el volumen supera los 50.000 registros.

---

## Índices definidos y justificación

| Índice | Tabla | Columnas | Justificación |
|--------|-------|----------|---------------|
| `idx_tarea_hogar_estado` | `tarea` | `(hogar_id, estado)` | Consulta más frecuente: tareas pendientes de un hogar |
| `idx_tarea_fecha_limite` | `tarea` | `(fecha_limite)` WHERE estado <> 'Completada' | Detección de tareas vencidas sin escanear completadas |
| `idx_tarea_asignado` | `tarea` | `(asignado_a)` | Filtrar tareas por miembro asignado |
| `idx_usuario_email` | `usuario` | `(email)` | Login y búsqueda por email en cada autenticación |
| `idx_inv_token` | `invitacion_hogar` | `(token)` | Validación de token en cada aceptación de invitación |
| `idx_inv_email_hogar` | `invitacion_hogar` | `(email_invitado, hogar_id)` | Verificar invitaciones duplicadas |

---

## Roles de base de datos

| Rol | Tipo | Permisos | Usado por |
|-----|------|----------|-----------|
| `postgres` | Superusuario | Todos — creación de BD, roles, extensiones | Solo administración y despliegue inicial |
| `app_user` | Usuario de aplicación | SELECT, INSERT, UPDATE, DELETE en todas las tablas | Spring Boot en tiempo de ejecución |

### Justificación del esquema de roles

Se aplica el **principio de mínimo privilegio**: la aplicación Spring Boot opera con `app_user`,
que no tiene permisos para crear o eliminar tablas, ni acceso a otras bases de datos del servidor.
Esto limita el impacto en caso de una vulnerabilidad de inyección SQL o credenciales comprometidas.

El usuario `postgres` solo se usa durante el despliegue inicial (ejecución de migraciones Flyway)
y nunca se expone en las variables de entorno de la aplicación en producción.

### Script de creación del rol (incluido en V3)

```sql
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'app_user') THEN
    CREATE ROLE app_user WITH LOGIN PASSWORD 'app_secret_2024';
  END IF;
END $$;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_user;
```