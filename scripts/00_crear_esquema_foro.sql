-- ===========================================================================
--  Foro en Linea - Cubos Rubik y Twisty Puzzles
--  Creacion del esquema propio FORO en Oracle Autonomous Database.
--
--  IMPORTANTE: este script se ejecuta UNA SOLA VEZ y con el usuario ADMIN,
--  no con FORO (que todavia no existe).
--
--  Antes de ejecutarlo, reemplaza __CLAVE_FORO__ por la contrasena que quieras
--  asignar al esquema. Oracle Autonomous Database exige al menos 12 caracteres,
--  con mayusculas, minusculas y numeros.
--
--  La contrasena NO se guarda en este archivo ni en el repositorio: se almacena
--  en el Llavero de macOS con el nombre "foro-online-db" (ver scripts/run-todo.sh).
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- 1. Usuario / esquema propietario de las tablas del foro
--    Se crea un esquema aparte en vez de usar ADMIN para no mezclar las tablas
--    de distintos proyectos bajo el mismo superusuario.
-- ---------------------------------------------------------------------------
CREATE USER FORO IDENTIFIED BY "__CLAVE_FORO__";

-- ---------------------------------------------------------------------------
-- 2. Privilegios minimos necesarios
--    CONNECT permite abrir sesion; RESOURCE permite crear tablas, secuencias
--    e indices dentro de su propio esquema. No se conceden privilegios de
--    administracion: el microservicio solo necesita trabajar con sus tablas.
-- ---------------------------------------------------------------------------
GRANT CONNECT, RESOURCE TO FORO;

-- Sin cuota el usuario no puede escribir datos aunque pueda crear las tablas.
ALTER USER FORO QUOTA UNLIMITED ON DATA;

-- ---------------------------------------------------------------------------
-- 3. Verificacion
-- ---------------------------------------------------------------------------
SELECT USERNAME, ACCOUNT_STATUS, DEFAULT_TABLESPACE
  FROM DBA_USERS
 WHERE USERNAME = 'FORO';
