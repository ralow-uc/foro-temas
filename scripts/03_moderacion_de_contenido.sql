-- ===========================================================================
--  Microservicio foro-temas  ·  v2.0.0 (Experiencia 2, Semana 5)
--  Agrega a TEMA y COMENTARIO las columnas que usa la moderacion.
--
--  Ejecutar con el usuario FORO, despues de 01 y 02:
--    ./scripts/sql.sh foro scripts/03_moderacion_de_contenido.sql
--
--  Es una migracion: no borra datos. Los registros existentes quedan con
--  BANEADO = 0 (visibles). El microservicio arranca con ddl-auto=validate, asi
--  que esta migracion tiene que correr ANTES de desplegar foro-temas v2.0.0.
--
--  El contenido baneado no se elimina: deja de mostrarse en el foro y se puede
--  restituir si la sancion se levanta en foro-moderacion.
-- ===========================================================================

ALTER TABLE TEMA ADD (
    BANEADO       NUMBER(1)      DEFAULT 0 NOT NULL,
    MOTIVO_BANEO  VARCHAR2(500)
);
ALTER TABLE TEMA ADD CONSTRAINT CK_TEMA_BANEADO CHECK (BANEADO IN (0, 1));

COMMENT ON COLUMN TEMA.BANEADO      IS '1 si un moderador retiro el tema por incumplir las normas';
COMMENT ON COLUMN TEMA.MOTIVO_BANEO IS 'Motivo del baneo, nulo si el tema esta visible';

ALTER TABLE COMENTARIO ADD (
    BANEADO       NUMBER(1)      DEFAULT 0 NOT NULL,
    MOTIVO_BANEO  VARCHAR2(500)
);
ALTER TABLE COMENTARIO ADD CONSTRAINT CK_COMENTARIO_BANEADO CHECK (BANEADO IN (0, 1));

COMMENT ON COLUMN COMENTARIO.BANEADO      IS '1 si un moderador retiro el comentario por incumplir las normas';
COMMENT ON COLUMN COMENTARIO.MOTIVO_BANEO IS 'Motivo del baneo, nulo si el comentario esta visible';

-- Los listados publicos filtran siempre por BANEADO = 0.
CREATE INDEX IX_TEMA_BANEADO       ON TEMA (BANEADO);
CREATE INDEX IX_COMENTARIO_BANEADO ON COMENTARIO (BANEADO);

-- Verificacion
SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, NULLABLE, DATA_DEFAULT
FROM USER_TAB_COLUMNS
WHERE TABLE_NAME IN ('TEMA', 'COMENTARIO') AND COLUMN_NAME IN ('BANEADO', 'MOTIVO_BANEO')
ORDER BY TABLE_NAME, COLUMN_NAME;
