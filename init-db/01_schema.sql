-- Tabla Hogar
CREATE TABLE IF NOT EXISTS hogar (
    hogar_id     BIGSERIAL PRIMARY KEY,
    nombre       VARCHAR(150) NOT NULL,
    descripcion  TEXT,
    h_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla Usuario
CREATE TABLE IF NOT EXISTS usuario (
    usuario_id   BIGSERIAL PRIMARY KEY,
    email        VARCHAR(150) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    nombre       VARCHAR(70),
    u_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    u_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla intermedia Usuario_Hogar
CREATE TABLE IF NOT EXISTS usuario_hogar (
    usuario_id   BIGINT NOT NULL,
    hogar_id     BIGINT NOT NULL,
    rol          VARCHAR(50) NOT NULL,
    fecha_union  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, hogar_id),
    CONSTRAINT fk_uh_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id),
    CONSTRAINT fk_uh_hogar   FOREIGN KEY (hogar_id)   REFERENCES hogar(hogar_id),
    CONSTRAINT chk_rol CHECK (rol IN ('Administrador', 'Miembro'))
);

-- Tabla Tarea
CREATE TABLE IF NOT EXISTS tarea (
    tarea_id     BIGSERIAL PRIMARY KEY,
    hogar_id     BIGINT NOT NULL,
    asignado_a   BIGINT,  -- nullable: una tarea puede existir sin responsable aún
    titulo       VARCHAR(150) NOT NULL,
    descripcion  TEXT,
    categoria    VARCHAR(50) NOT NULL,
    estado       VARCHAR(20) NOT NULL DEFAULT 'Pendiente',
    fecha_limite TIMESTAMP,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tarea_hogar    FOREIGN KEY (hogar_id)   REFERENCES hogar(hogar_id),
    CONSTRAINT fk_tarea_usuario  FOREIGN KEY (asignado_a) REFERENCES usuario(usuario_id),
    CONSTRAINT chk_categoria CHECK (categoria IN ('Limpieza', 'Cocina', 'Compras', 'Mantenimiento', 'Otro')),
    CONSTRAINT chk_estado    CHECK (estado    IN ('Pendiente', 'En_progreso', 'Completada'))
);

CREATE TABLE IF NOT EXISTS password_reset_token (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT NOT NULL,
    token           VARCHAR(255) NOT NULL UNIQUE,
    expiracion      TIMESTAMP NOT NULL,
    usado           BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_prt_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(usuario_id)
);