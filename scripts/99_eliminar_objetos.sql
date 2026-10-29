-- ===========================================================================
--  Microservicio foro-temas
--  Elimina SUS objetos del esquema FORO para poder recrearlos desde cero.
--
--  Ejecutar con el usuario FORO. El orden importa por las claves foraneas.
--  No toca las tablas del otro microservicio (ROL, USUARIO, USUARIO_ROL).
-- ===========================================================================

DROP TABLE COMENTARIO  CASCADE CONSTRAINTS;
DROP TABLE TEMA        CASCADE CONSTRAINTS;
DROP TABLE CATEGORIA   CASCADE CONSTRAINTS;

DROP SEQUENCE COMENTARIO_SEQ;
DROP SEQUENCE TEMA_SEQ;
DROP SEQUENCE CATEGORIA_SEQ;
