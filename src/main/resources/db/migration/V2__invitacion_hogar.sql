-- ============================================================
-- Flyway V2: Tabla de invitaciones al hogar
-- ============================================================

CREATE TABLE IF NOT EXISTS invitacion_hogar (
    invitacion_id   BIGSERIAL PRIMARY KEY,
    token           VARCHAR(100)  NOT NULL UNIQUE,
    email_invitado  VARCHAR(150)  NOT NULL,
    hogar_id        BIGINT        NOT NULL,
    invitado_por    BIGINT        NOT NULL,
    estado          VARCHAR(20)   NOT NULL DEFAULT 'Pendiente',
    fecha_expiracion TIMESTAMP    NOT NULL,
    created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_inv_hogar    FOREIGN KEY (hogar_id)     REFERENCES hogar(hogar_id),
    CONSTRAINT fk_inv_usuario  FOREIGN KEY (invitado_por) REFERENCES usuario(usuario_id),
    CONSTRAINT chk_estado_inv  CHECK (estado IN ('Pendiente', 'Aceptada', 'Rechazada', 'Expirada'))
);

CREATE INDEX IF NOT EXISTS idx_inv_token ON invitacion_hogar(token);
CREATE INDEX IF NOT EXISTS idx_inv_email_hogar ON invitacion_hogar(email_invitado, hogar_id);
