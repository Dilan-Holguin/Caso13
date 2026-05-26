-- ============================================================
-- Flyway V3: Agregar prioridad y completada_at a la tabla tarea
-- ============================================================

ALTER TABLE tarea
    ADD COLUMN IF NOT EXISTS prioridad VARCHAR(10),
    ADD COLUMN IF NOT EXISTS completada_at TIMESTAMP;

ALTER TABLE tarea
    ADD CONSTRAINT chk_prioridad CHECK (prioridad IN ('Alta', 'Media', 'Baja'));
