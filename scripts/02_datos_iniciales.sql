-- ===========================================================================
--  Microservicio foro-temas
--  Datos iniciales de SUS tablas.
--
--  Ejecutar como script completo con el usuario FORO, despues de
--  scripts/01_crear_tablas.sql.
--
--  Las tres tablas quedan con mas de tres registros, como exige la actividad:
--    CATEGORIA 6 | TEMA 9 | COMENTARIO 13
--
--  AUTOR_ID va con valores literales y no con una subconsulta sobre USUARIO:
--  esa tabla pertenece al otro microservicio y este script tiene que poder
--  ejecutarse por si solo. Los valores 1 a 5 corresponden a las cuentas que
--  siembra foro-usuarios en su scripts/02_datos_iniciales.sql, en este orden:
--    1 admin | 2 moderadora | 3 feliks | 4 yiheng | 5 tymon
--  En operacion normal estos dos campos no se escriben a mano: los copia el
--  microservicio desde el JWT de quien publica.
-- ===========================================================================


-- ---------------------------------------------------------------------------
-- 1. Las seis categorias del foro
-- ---------------------------------------------------------------------------
INSERT INTO CATEGORIA (ID, NOMBRE, DESCRIPCION, SLUG) VALUES (CATEGORIA_SEQ.NEXTVAL,
 'Cubo 3x3', 'El clásico de 3x3x3: métodos, tiempos y primeros pasos.', 'cubo-3x3');

INSERT INTO CATEGORIA (ID, NOMBRE, DESCRIPCION, SLUG) VALUES (CATEGORIA_SEQ.NEXTVAL,
 'Big cubes', '4x4, 5x5 y superiores: reducción, paridades y hardware.', 'big-cubes');

INSERT INTO CATEGORIA (ID, NOMBRE, DESCRIPCION, SLUG) VALUES (CATEGORIA_SEQ.NEXTVAL,
 'Métodos y algoritmos', 'CFOP, Roux, ZZ, Petrus y la discusión eterna sobre cuál es mejor.', 'metodos-y-algoritmos');

INSERT INTO CATEGORIA (ID, NOMBRE, DESCRIPCION, SLUG) VALUES (CATEGORIA_SEQ.NEXTVAL,
 'Puzzles no cúbicos', 'Megaminx, Pyraminx, Skewb, Square-1 y todo lo que no es un cubo.', 'puzzles-no-cubicos');

INSERT INTO CATEGORIA (ID, NOMBRE, DESCRIPCION, SLUG) VALUES (CATEGORIA_SEQ.NEXTVAL,
 'Competencias WCA', 'Torneos oficiales, reglamento, récords y experiencias compitiendo.', 'competencias-wca');

INSERT INTO CATEGORIA (ID, NOMBRE, DESCRIPCION, SLUG) VALUES (CATEGORIA_SEQ.NEXTVAL,
 'Hardware y mods', 'Cubos magnéticos, lubricantes, tensiones y modificaciones.', 'hardware-y-mods');


-- ---------------------------------------------------------------------------
-- 2. Temas
-- ---------------------------------------------------------------------------
INSERT INTO TEMA (ID, CATEGORIA_ID, AUTOR_ID, AUTOR_NOMBRE, TITULO, CONTENIDO) VALUES (
 TEMA_SEQ.NEXTVAL, (SELECT ID FROM CATEGORIA WHERE SLUG = 'cubo-3x3'), 3, 'feliks',
 'Por fin bajé de 20 segundos',
 'Llevaba tres meses estancado en 24 segundos y esta semana promedié 19.8. Lo que me desbloqueó fue dejar de mirar el cubo entre pasos.');

INSERT INTO TEMA (ID, CATEGORIA_ID, AUTOR_ID, AUTOR_NOMBRE, TITULO, CONTENIDO) VALUES (
 TEMA_SEQ.NEXTVAL, (SELECT ID FROM CATEGORIA WHERE SLUG = 'metodos-y-algoritmos'), 4, 'yiheng',
 '¿CFOP o Roux para bajar de 15?',
 'Promedio 17 segundos con CFOP y siento que el F2L me limita. ¿Vale la pena aprender Roux desde cero a esta altura?');

INSERT INTO TEMA (ID, CATEGORIA_ID, AUTOR_ID, AUTOR_NOMBRE, TITULO, CONTENIDO) VALUES (
 TEMA_SEQ.NEXTVAL, (SELECT ID FROM CATEGORIA WHERE SLUG = 'metodos-y-algoritmos'), 3, 'feliks',
 'Memorizar las 57 OLL en un mes',
 'Comparto el plan que usé: seis algoritmos nuevos por día, repaso de los anteriores y nada de aprender dos simétricos el mismo día.');

INSERT INTO TEMA (ID, CATEGORIA_ID, AUTOR_ID, AUTOR_NOMBRE, TITULO, CONTENIDO) VALUES (
 TEMA_SEQ.NEXTVAL, (SELECT ID FROM CATEGORIA WHERE SLUG = 'big-cubes'), 4, 'yiheng',
 'La paridad del 4x4 me sigue costando',
 'Entiendo por qué ocurre pero sigo ejecutando el algoritmo de OLL parity muy lento. ¿Alguna variante más corta que recomienden?');

INSERT INTO TEMA (ID, CATEGORIA_ID, AUTOR_ID, AUTOR_NOMBRE, TITULO, CONTENIDO) VALUES (
 TEMA_SEQ.NEXTVAL, (SELECT ID FROM CATEGORIA WHERE SLUG = 'puzzles-no-cubicos'), 2, 'moderadora',
 'Megaminx: cómo memorizar la última capa',
 'La última capa del Megaminx asusta por la cantidad de casos, pero con los algoritmos de 2-look se resuelve con muy poca memorización.');

INSERT INTO TEMA (ID, CATEGORIA_ID, AUTOR_ID, AUTOR_NOMBRE, TITULO, CONTENIDO) VALUES (
 TEMA_SEQ.NEXTVAL, (SELECT ID FROM CATEGORIA WHERE SLUG = 'competencias-wca'), 1, 'admin',
 'Nacional WCA Chile 2026: organización',
 'Abrimos la convocatoria de jueces y runners para el nacional. No hace falta experiencia previa, se capacita en el lugar.');

INSERT INTO TEMA (ID, CATEGORIA_ID, AUTOR_ID, AUTOR_NOMBRE, TITULO, CONTENIDO) VALUES (
 TEMA_SEQ.NEXTVAL, (SELECT ID FROM CATEGORIA WHERE SLUG = 'competencias-wca'), 2, 'moderadora',
 'Las tres reglas que más descalifican',
 'Repaso rápido del reglamento: inspección de 15 segundos, detener el timer con las manos planas y no soltar el cubo antes de parar.');

INSERT INTO TEMA (ID, CATEGORIA_ID, AUTOR_ID, AUTOR_NOMBRE, TITULO, CONTENIDO) VALUES (
 TEMA_SEQ.NEXTVAL, (SELECT ID FROM CATEGORIA WHERE SLUG = 'hardware-y-mods'), 3, 'feliks',
 'Reseña del GAN 14 con MagLev',
 'Tres semanas con el GAN 14. El MagLev se siente raro los primeros días y después no quieres volver a los resortes tradicionales.');

INSERT INTO TEMA (ID, CATEGORIA_ID, AUTOR_ID, AUTOR_NOMBRE, TITULO, CONTENIDO) VALUES (
 TEMA_SEQ.NEXTVAL, (SELECT ID FROM CATEGORIA WHERE SLUG = 'hardware-y-mods'), 4, 'yiheng',
 'Lubricantes: Angstrom contra DNM-37',
 'Probé los dos en el mismo cubo durante dos semanas cada uno. Mi conclusión es que la diferencia se nota más en el core que en las piezas.');


-- ---------------------------------------------------------------------------
-- 3. Comentarios
-- ---------------------------------------------------------------------------
INSERT INTO COMENTARIO (ID, TEMA_ID, AUTOR_ID, AUTOR_NOMBRE, CONTENIDO) VALUES (COMENTARIO_SEQ.NEXTVAL,
 (SELECT ID FROM TEMA WHERE TITULO = 'Por fin bajé de 20 segundos'), 4, 'yiheng',
 'Felicitaciones. El look-ahead es justo donde se gana el tiempo.');

INSERT INTO COMENTARIO (ID, TEMA_ID, AUTOR_ID, AUTOR_NOMBRE, CONTENIDO) VALUES (COMENTARIO_SEQ.NEXTVAL,
 (SELECT ID FROM TEMA WHERE TITULO = 'Por fin bajé de 20 segundos'), 2, 'moderadora',
 'Buen hito. ¿Con qué método estás armando la cruz?');

INSERT INTO COMENTARIO (ID, TEMA_ID, AUTOR_ID, AUTOR_NOMBRE, CONTENIDO) VALUES (COMENTARIO_SEQ.NEXTVAL,
 (SELECT ID FROM TEMA WHERE TITULO = '¿CFOP o Roux para bajar de 15?'), 3, 'feliks',
 'Cambiar de método cuesta unos dos meses de retroceso. Yo apuraría el F2L primero.');

INSERT INTO COMENTARIO (ID, TEMA_ID, AUTOR_ID, AUTOR_NOMBRE, CONTENIDO) VALUES (COMENTARIO_SEQ.NEXTVAL,
 (SELECT ID FROM TEMA WHERE TITULO = '¿CFOP o Roux para bajar de 15?'), 1, 'admin',
 'Hay sub-10 con los dos métodos. El método no es el cuello de botella a los 17 segundos.');

INSERT INTO COMENTARIO (ID, TEMA_ID, AUTOR_ID, AUTOR_NOMBRE, CONTENIDO) VALUES (COMENTARIO_SEQ.NEXTVAL,
 (SELECT ID FROM TEMA WHERE TITULO = '¿CFOP o Roux para bajar de 15?'), 2, 'moderadora',
 'Dejo fijado el tema: la pregunta se repite cada semana.');

INSERT INTO COMENTARIO (ID, TEMA_ID, AUTOR_ID, AUTOR_NOMBRE, CONTENIDO) VALUES (COMENTARIO_SEQ.NEXTVAL,
 (SELECT ID FROM TEMA WHERE TITULO = 'Memorizar las 57 OLL en un mes'), 4, 'yiheng',
 'Lo de no mezclar simétricos el mismo día es el mejor consejo del post.');

INSERT INTO COMENTARIO (ID, TEMA_ID, AUTOR_ID, AUTOR_NOMBRE, CONTENIDO) VALUES (COMENTARIO_SEQ.NEXTVAL,
 (SELECT ID FROM TEMA WHERE TITULO = 'La paridad del 4x4 me sigue costando'), 1, 'admin',
 'Practica la versión de Lucas Parity. Es más larga de leer pero más cómoda de ejecutar.');

INSERT INTO COMENTARIO (ID, TEMA_ID, AUTOR_ID, AUTOR_NOMBRE, CONTENIDO) VALUES (COMENTARIO_SEQ.NEXTVAL,
 (SELECT ID FROM TEMA WHERE TITULO = 'Megaminx: cómo memorizar la última capa'), 3, 'feliks',
 'El 2-look es lo correcto para empezar. Nadie aprende el full de entrada.');

INSERT INTO COMENTARIO (ID, TEMA_ID, AUTOR_ID, AUTOR_NOMBRE, CONTENIDO) VALUES (COMENTARIO_SEQ.NEXTVAL,
 (SELECT ID FROM TEMA WHERE TITULO = 'Nacional WCA Chile 2026: organización'), 4, 'yiheng',
 'Me anoto como juez. ¿Hay que llevar algo?');

INSERT INTO COMENTARIO (ID, TEMA_ID, AUTOR_ID, AUTOR_NOMBRE, CONTENIDO) VALUES (COMENTARIO_SEQ.NEXTVAL,
 (SELECT ID FROM TEMA WHERE TITULO = 'Nacional WCA Chile 2026: organización'), 3, 'feliks',
 'Primera vez que compito. ¿Se puede usar cubo propio?');

INSERT INTO COMENTARIO (ID, TEMA_ID, AUTOR_ID, AUTOR_NOMBRE, CONTENIDO) VALUES (COMENTARIO_SEQ.NEXTVAL,
 (SELECT ID FROM TEMA WHERE TITULO = 'Reseña del GAN 14 con MagLev'), 4, 'yiheng',
 '¿Cómo se comporta en giros rápidos? Mi miedo es que se pase de vuelta.');

INSERT INTO COMENTARIO (ID, TEMA_ID, AUTOR_ID, AUTOR_NOMBRE, CONTENIDO) VALUES (COMENTARIO_SEQ.NEXTVAL,
 (SELECT ID FROM TEMA WHERE TITULO = 'Reseña del GAN 14 con MagLev'), 1, 'admin',
 'Buena reseña. Agrego que el precio sigue siendo difícil de justificar.');

INSERT INTO COMENTARIO (ID, TEMA_ID, AUTOR_ID, AUTOR_NOMBRE, CONTENIDO) VALUES (COMENTARIO_SEQ.NEXTVAL,
 (SELECT ID FROM TEMA WHERE TITULO = 'Lubricantes: Angstrom contra DNM-37'), 3, 'feliks',
 'Coincido. En las piezas casi no se nota después del primer día.');

COMMIT;


-- ---------------------------------------------------------------------------
-- 4. Verificacion
-- ---------------------------------------------------------------------------
SELECT 'CATEGORIA' AS TABLA, COUNT(*) AS REGISTROS FROM CATEGORIA
UNION ALL SELECT 'TEMA', COUNT(*) FROM TEMA
UNION ALL SELECT 'COMENTARIO', COUNT(*) FROM COMENTARIO;
