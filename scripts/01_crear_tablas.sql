-- ===========================================================================
--  Microservicio foro-temas
--  Creacion de SUS tablas en el esquema FORO de Oracle Autonomous Database.
--
--  Ejecutar como script completo con el usuario FORO, despues de haber corrido
--  scripts/00_crear_esquema_foro.sql como ADMIN.
--
--  Este microservicio es dueno de tres tablas: CATEGORIA, TEMA y COMENTARIO.
--  Las tablas de cuentas (ROL, USUARIO, USUARIO_ROL) las crea el otro
--  microservicio, foro-usuarios, con su propio script. Los dos conviven en la
--  misma base sin claves foraneas entre ellos.
--
--  Las secuencias llevan INCREMENT BY 1 porque JPA las usa con
--  @SequenceGenerator(allocationSize = 1).
-- ===========================================================================


-- ---------------------------------------------------------------------------
-- 1. Tabla CATEGORIA
--    Las seis categorias de temas del foro de cubos y twisty puzzles.
-- ---------------------------------------------------------------------------
CREATE TABLE CATEGORIA (
    ID           NUMBER(19)      NOT NULL,
    NOMBRE       VARCHAR2(100)   NOT NULL,
    DESCRIPCION  VARCHAR2(400)   NOT NULL,
    SLUG         VARCHAR2(100)   NOT NULL,
    CONSTRAINT PK_CATEGORIA         PRIMARY KEY (ID),
    CONSTRAINT UK_CATEGORIA_NOMBRE  UNIQUE (NOMBRE),
    CONSTRAINT UK_CATEGORIA_SLUG    UNIQUE (SLUG)
);

COMMENT ON TABLE  CATEGORIA             IS 'Categorias de temas del foro';
COMMENT ON COLUMN CATEGORIA.ID          IS 'Identificador unico de la categoria';
COMMENT ON COLUMN CATEGORIA.NOMBRE      IS 'Nombre visible de la categoria';
COMMENT ON COLUMN CATEGORIA.DESCRIPCION IS 'Explicacion de que se discute en la categoria';
COMMENT ON COLUMN CATEGORIA.SLUG        IS 'Identificador legible para URL, sin espacios ni acentos';

CREATE SEQUENCE CATEGORIA_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;


-- ---------------------------------------------------------------------------
-- 2. Tabla TEMA
--    AUTOR_ID y AUTOR_NOMBRE se copian del JWT que emite foro-usuarios y NO
--    llevan clave foranea a USUARIO: cada microservicio mantiene su propio
--    modelo y no se acopla al del otro (ver docs/arquitectura.md).
-- ---------------------------------------------------------------------------
CREATE TABLE TEMA (
    ID              NUMBER(19)      NOT NULL,
    CATEGORIA_ID    NUMBER(19)      NOT NULL,
    AUTOR_ID        NUMBER(19)      NOT NULL,
    AUTOR_NOMBRE    VARCHAR2(50)    NOT NULL,
    TITULO          VARCHAR2(200)   NOT NULL,
    CONTENIDO       VARCHAR2(4000)  NOT NULL,
    FECHA_CREACION  TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    CERRADO         NUMBER(1)       DEFAULT 0 NOT NULL,
    CONSTRAINT PK_TEMA            PRIMARY KEY (ID),
    CONSTRAINT FK_TEMA_CATEGORIA  FOREIGN KEY (CATEGORIA_ID) REFERENCES CATEGORIA (ID),
    CONSTRAINT CK_TEMA_CERRADO    CHECK (CERRADO IN (0, 1))
);

COMMENT ON TABLE  TEMA                IS 'Temas publicados por los usuarios del foro';
COMMENT ON COLUMN TEMA.ID             IS 'Identificador unico del tema';
COMMENT ON COLUMN TEMA.CATEGORIA_ID   IS 'Categoria a la que pertenece el tema';
COMMENT ON COLUMN TEMA.AUTOR_ID       IS 'ID del usuario autor, tomado del JWT';
COMMENT ON COLUMN TEMA.AUTOR_NOMBRE   IS 'Nombre del autor, copiado del JWT para no depender del otro microservicio al listar';
COMMENT ON COLUMN TEMA.TITULO         IS 'Titulo del tema';
COMMENT ON COLUMN TEMA.CONTENIDO      IS 'Cuerpo del tema';
COMMENT ON COLUMN TEMA.FECHA_CREACION IS 'Momento de publicacion';
COMMENT ON COLUMN TEMA.CERRADO        IS '1 si el tema esta cerrado a nuevos comentarios, 0 si sigue abierto';

CREATE SEQUENCE TEMA_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;


-- ---------------------------------------------------------------------------
-- 3. Tabla COMENTARIO
--    Al borrar un tema se borran sus comentarios (ON DELETE CASCADE).
-- ---------------------------------------------------------------------------
CREATE TABLE COMENTARIO (
    ID              NUMBER(19)      NOT NULL,
    TEMA_ID         NUMBER(19)      NOT NULL,
    AUTOR_ID        NUMBER(19)      NOT NULL,
    AUTOR_NOMBRE    VARCHAR2(50)    NOT NULL,
    CONTENIDO       VARCHAR2(2000)  NOT NULL,
    FECHA_CREACION  TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_COMENTARIO        PRIMARY KEY (ID),
    CONSTRAINT FK_COMENTARIO_TEMA   FOREIGN KEY (TEMA_ID) REFERENCES TEMA (ID) ON DELETE CASCADE
);

COMMENT ON TABLE  COMENTARIO              IS 'Comentarios escritos por los usuarios dentro de un tema';
COMMENT ON COLUMN COMENTARIO.ID           IS 'Identificador unico del comentario';
COMMENT ON COLUMN COMENTARIO.TEMA_ID      IS 'Tema al que responde el comentario';
COMMENT ON COLUMN COMENTARIO.AUTOR_ID     IS 'ID del usuario autor, tomado del JWT';
COMMENT ON COLUMN COMENTARIO.AUTOR_NOMBRE IS 'Nombre del autor, copiado del JWT';
COMMENT ON COLUMN COMENTARIO.CONTENIDO    IS 'Texto del comentario';

CREATE SEQUENCE COMENTARIO_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;


-- ---------------------------------------------------------------------------
-- 4. Indices de apoyo a las consultas del microservicio
-- ---------------------------------------------------------------------------
-- Busqueda de temas por texto del titulo, sin distinguir mayusculas.
CREATE INDEX IX_TEMA_TITULO      ON TEMA (UPPER(TITULO));
-- Listado de temas por categoria.
CREATE INDEX IX_TEMA_CATEGORIA   ON TEMA (CATEGORIA_ID);
-- Busqueda de temas por autor.
CREATE INDEX IX_TEMA_AUTOR       ON TEMA (UPPER(AUTOR_NOMBRE));
-- Recuperacion de los comentarios de un tema.
CREATE INDEX IX_COMENTARIO_TEMA  ON COMENTARIO (TEMA_ID);
