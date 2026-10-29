-- ===========================================================================
--  Microservicio foro-temas
--  Consultas para demostrar en vivo que los datos estan en Oracle.
--
--  Ejecutar con el usuario FORO, desde Database Actions o con:
--      ./scripts/sql.sh foro scripts/consultas_demo.sql
--
--  Pensadas para el video: se corren despues de publicar un tema y comentarlo
--  por Postman, para evidenciar que lo que entro por la API quedo en la base.
-- ===========================================================================


-- ---------------------------------------------------------------------------
-- 1. Cuantos registros hay en cada tabla de este microservicio
-- ---------------------------------------------------------------------------
SELECT 'CATEGORIA' AS TABLA, COUNT(*) AS REGISTROS FROM CATEGORIA
UNION ALL SELECT 'TEMA', COUNT(*) FROM TEMA
UNION ALL SELECT 'COMENTARIO', COUNT(*) FROM COMENTARIO;


-- ---------------------------------------------------------------------------
-- 2. Las seis categorias con su cantidad de temas y de comentarios
--    Es el mismo conteo que devuelve GET /api/categorias.
-- ---------------------------------------------------------------------------
SELECT c.NOMBRE AS CATEGORIA,
       COUNT(DISTINCT t.ID)  AS TEMAS,
       COUNT(cm.ID)          AS COMENTARIOS
  FROM CATEGORIA c
  LEFT JOIN TEMA t        ON t.CATEGORIA_ID = c.ID
  LEFT JOIN COMENTARIO cm ON cm.TEMA_ID = t.ID
 GROUP BY c.NOMBRE
 ORDER BY TEMAS DESC, c.NOMBRE;


-- ---------------------------------------------------------------------------
-- 3. Los temas mas recientes con su autor y su categoria
--    Es el mismo orden que devuelve GET /api/temas.
-- ---------------------------------------------------------------------------
SELECT t.ID,
       c.NOMBRE AS CATEGORIA,
       t.AUTOR_NOMBRE,
       t.TITULO,
       TO_CHAR(t.FECHA_CREACION, 'DD-MM-YYYY HH24:MI:SS') AS PUBLICADO
  FROM TEMA t
  JOIN CATEGORIA c ON c.ID = t.CATEGORIA_ID
 ORDER BY t.FECHA_CREACION DESC
 FETCH FIRST 10 ROWS ONLY;


-- ---------------------------------------------------------------------------
-- 4. El ultimo tema publicado con todos sus comentarios
--    Es la consulta que conviene correr justo despues de publicar y comentar
--    por Postman: muestra el dato recien creado dentro de Oracle.
-- ---------------------------------------------------------------------------
SELECT t.TITULO,
       cm.AUTOR_NOMBRE AS COMENTA,
       cm.CONTENIDO,
       TO_CHAR(cm.FECHA_CREACION, 'HH24:MI:SS') AS HORA
  FROM TEMA t
  LEFT JOIN COMENTARIO cm ON cm.TEMA_ID = t.ID
 WHERE t.ID = (SELECT MAX(ID) FROM TEMA)
 ORDER BY cm.FECHA_CREACION;


-- ---------------------------------------------------------------------------
-- 5. Busqueda por texto, la misma que hace el microservicio
--    El ESCAPE va declarado a proposito: sin el, Hibernate genera escape ''
--    y en Oracle la cadena vacia es NULL, con lo que el LIKE no devuelve nada.
-- ---------------------------------------------------------------------------
SELECT t.ID, t.AUTOR_NOMBRE, t.TITULO
  FROM TEMA t
 WHERE UPPER(t.TITULO) LIKE UPPER('%cfop%') ESCAPE '\'
 ORDER BY t.ID;


-- ---------------------------------------------------------------------------
-- 6. Quien ha publicado mas temas
--    Agrupa por el autor que este microservicio guarda denormalizado, sin
--    necesidad de consultar la tabla USUARIO del otro microservicio.
-- ---------------------------------------------------------------------------
SELECT AUTOR_NOMBRE, COUNT(*) AS TEMAS
  FROM TEMA
 GROUP BY AUTOR_NOMBRE
 ORDER BY TEMAS DESC, AUTOR_NOMBRE;


-- ---------------------------------------------------------------------------
-- 7. Comprobacion de que los acentos y la enie viajan intactos
--    Si la codificacion estuviera mal, estas filas saldrian con caracteres
--    rotos en lugar de tildes.
-- ---------------------------------------------------------------------------
SELECT TITULO
  FROM TEMA
 WHERE TITULO LIKE '%é%' OR TITULO LIKE '%ó%' OR TITULO LIKE '%ñ%' OR TITULO LIKE '%ú%'
 ORDER BY ID;
