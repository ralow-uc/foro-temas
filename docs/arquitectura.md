# Arquitectura

## Visión general

Dos aplicaciones Spring Boot **independientes**, **cada una en su propio
repositorio Git**, con su propio `pom.xml`, su propio Maven Wrapper y su propio
puerto. Comparten la base de datos Oracle y un secreto de firma, nada más.

| Repositorio | Puerto | Responsabilidad |
|---|---|---|
| `foro-usuarios` | 8081 | Cuentas, roles e inicio de sesión. Emite el JWT. |
| `foro-temas` | 8082 | Categorías, temas y comentarios. Valida el JWT. |

Cada repositorio trae **sus propios scripts SQL**: crea y puebla solo las tres
tablas de las que es dueño, sin tocar las del otro. También trae su copia de la
infraestructura común (`00_crear_esquema_foro.sql`, `sql.sh`, `EjecutarSql.java`
y `entorno-oracle.sh`), de modo que se puede clonar y poner a andar por sí solo.

La colección de Postman es la misma en los dos repositorios porque la
demostración recorre los dos servicios: el token se obtiene en uno y se usa en
el otro.

```
            ┌────────────────────────┐        ┌────────────────────────┐
            │     foro-usuarios      │        │       foro-temas       │
            │        :8081           │        │         :8082          │
            ├────────────────────────┤        ├────────────────────────┤
  POST      │ AutenticacionController│        │ CategoriaController    │
  /login ──▶│ UsuarioController      │        │ TemaController         │◀── GET
            │ EstadoController       │        │ ComentarioController   │    /temas
            ├────────────────────────┤        │ EstadoController       │
            │ UsuarioService         │        ├────────────────────────┤
            │  + reglas de negocio   │        │ CategoriaService       │
            ├────────────────────────┤        │ TemaService            │
            │ UsuarioRepository      │        │  + reglas autor/ADMIN  │
            │ RolRepository          │        ├────────────────────────┤
            └───────────┬────────────┘        │ CategoriaRepository    │
                        │                     │ TemaRepository         │
                        │   JWT firmado       │ ComentarioRepository   │
                        │   (HS256, secreto   └───────────┬────────────┘
                        │    compartido)                  │
                        └────────────┐       ┌────────────┘
                                     ▼       ▼
                        ┌────────────────────────────────┐
                        │  Oracle Autonomous Database    │
                        │  FULLSTACK-3 · esquema FORO    │
                        │                                │
                        │  ROL  USUARIO  USUARIO_ROL     │
                        │  CATEGORIA  TEMA  COMENTARIO   │
                        └────────────────────────────────┘
```

### Cómo se comunican los dos servicios

`foro-usuarios` valida las credenciales y emite un **JWT firmado con HS256**. El
token lleva el nombre del usuario, su ID y sus roles.

`foro-temas` **no llama a `foro-usuarios` en ninguna petición**: comparte el secreto
de firma y con eso comprueba por sí mismo que el token es auténtico y está
vigente. Las consecuencias:

- No hay una llamada de red por petición, ni latencia añadida.
- `foro-temas` sigue funcionando aunque `foro-usuarios` esté caído; lo único que no
  se podrá hacer mientras tanto es iniciar sesión.
- A cambio, revocar un token antes de que expire no es inmediato. Por eso la
  vigencia es corta (120 minutos, configurable).

El secreto viaja en la variable de entorno `FORO_JWT_SECRETO`, que
`scripts/entorno-oracle.sh` toma del Llavero de macOS. Nunca está en el código.

---

## Modelo de datos

```
   foro-usuarios                            foro-temas
   ─────────────                            ──────────

   ┌──────────┐                        ┌──────────────┐
   │   ROL    │                        │  CATEGORIA   │
   │──────────│                        │──────────────│
   │ ID       │                        │ ID           │
   │ NOMBRE   │                        │ NOMBRE    UK │
   └────┬─────┘                        │ DESCRIPCION  │
        │ N                            │ SLUG      UK │
        │                              └──────┬───────┘
   ┌────┴────────┐                            │ 1
   │ USUARIO_ROL │                            │
   │─────────────│                            │ N
   │ USUARIO_ID  │                     ┌──────┴───────┐
   │ ROL_ID      │                     │     TEMA     │
   └────┬────────┘                     │──────────────│
        │ N                            │ ID           │
        │                              │ CATEGORIA_ID │
        │ 1                            │ AUTOR_ID     │····┐
   ┌────┴────────────┐                 │ AUTOR_NOMBRE │    │ sin
   │     USUARIO     │                 │ TITULO       │    │ clave
   │─────────────────│                 │ CONTENIDO    │    │ foránea
   │ ID              │◀················│ FECHA_CREAC. │    │
   │ NOMBRE_USUARIO  │                 │ CERRADO      │    │
   │ CORREO       UK │                 └──────┬───────┘    │
   │ CONTRASENA_HASH │                        │ 1          │
   │ ACTIVO          │                        │            │
   │ FECHA_REGISTRO  │                        │ N          │
   └─────────────────┘                 ┌──────┴───────┐    │
                                       │  COMENTARIO  │    │
                                       │──────────────│    │
                                       │ ID           │    │
                                       │ TEMA_ID      │    │
                                       │ AUTOR_ID     │····┘
                                       │ AUTOR_NOMBRE │
                                       │ CONTENIDO    │
                                       │ FECHA_CREAC. │
                                       └──────────────┘
```

### Por qué `AUTOR_ID` no tiene clave foránea

Es la decisión de diseño que más conviene explicar, porque a primera vista
parece un olvido.

`TEMA` y `COMENTARIO` pertenecen a `foro-temas`; `USUARIO` pertenece a
`foro-usuarios`. Si pusiéramos una clave foránea y una relación JPA entre ellas,
los dos servicios quedarían atados: `foro-temas` necesitaría conocer la entidad
`Usuario`, y un cambio en el modelo de usuarios rompería el foro.

En su lugar, `foro-temas` guarda **el ID y el nombre del autor que vienen firmados
en el JWT**. Es el patrón habitual entre microservicios: cada servicio es dueño
de sus tablas y replica el mínimo dato ajeno que necesita.

El nombre se guarda además de el ID precisamente para no tener que preguntarle a
`foro-usuarios` cada vez que se lista un tema.

Dentro de cada servicio sí hay claves foráneas: `TEMA → CATEGORIA` y
`COMENTARIO → TEMA`, esta última con `ON DELETE CASCADE`.

---

## Roles y privilegios

Hay **tres roles**. El enunciado exige al menos dos, pero también pide al menos
tres registros en cada tabla, y con solo dos roles la tabla `ROL` no cumpliría.

| Operación | Anónimo | `USUARIO` | `MODERADOR` | `ADMIN` |
|---|---|---|---|---|
| Leer categorías, temas y comentarios | sí | sí | sí | sí |
| Registrarse | sí | — | — | — |
| Publicar un tema y comentar | no | sí | sí | sí |
| Editar o borrar **lo propio** | no | sí | sí | sí |
| Editar o borrar **contenido ajeno** | no | **no** | no | **sí** |
| Ver su propia cuenta | no | sí | sí | sí |
| Ver o editar **cuentas ajenas** | no | **no** | no | **sí** |
| Listar todas las cuentas | no | **no** | no | **sí** |
| Crear categorías | no | **no** | no | **sí** |
| Asignar roles | no | no | no | **sí** |

`MODERADOR` existe en el modelo y se puede asignar, pero sus privilegios
específicos de moderación corresponden al tercer microservicio, que el enunciado
permite no construir.

### Dónde vive cada regla

- **Por rol**, en el controlador, con `@PreAuthorize("hasRole('ADMIN')")`.
- **Por propiedad del contenido** (soy el autor, o soy ADMIN), en la **capa de
  servicio**, en `TemaServiceImpl.exigirPermisoSobre(...)`. El controlador solo
  le pasa la identidad que extrajo del token.

Esto último es deliberado: la regla de "el autor manda sobre lo suyo" es una
regla de negocio, no de transporte, y se prueba sin levantar HTTP.

### 401 y 403 no son lo mismo

- **401** no hay identidad: falta el token, o es inválido, o expiró.
- **403** la identidad existe pero no alcanza para esa operación.

Conseguir esta distinción exigió permitir explícitamente el **despacho interno a
`/error`** en la configuración de seguridad. Sin eso, cuando `@PreAuthorize`
deniega el acceso Spring reenvía a `/error`, y como el servicio no mantiene
sesión ese segundo paso llega sin identidad: el `403` se convertía en `401` y la
diferencia se perdía. Las pruebas con `MockMvc` no lo detectan porque no
reproducen ese reenvío; se encontró probando contra el servicio real.

---

## Decisiones técnicas que conviene conocer

### `ddl-auto=validate` contra Oracle, nunca `update`

Las tablas las crean los scripts SQL, que son parte de la entrega. JPA solo
**comprueba** que su mapeo coincida con las tablas reales y no las modifica.

Esa comprobación encontró un problema real: Oracle 23 tiene un tipo `BOOLEAN`
nativo, así que Hibernate esperaba `BOOLEAN` donde el script define `NUMBER(1)`.
De ahí el `@Convert(converter = NumericBooleanConverter.class)` en
`USUARIO.ACTIVO` y `TEMA.CERRADO`: mantiene `NUMBER(1)` en la base, que es
portable, y el mapeo valida igual.

### El `LIKE` de la búsqueda declara su `ESCAPE`

En `TemaRepository.buscar(...)` el `LIKE` lleva `ESCAPE '\'` escrito a mano. Si
no se declara, Hibernate genera `escape ''`, y como **en Oracle la cadena vacía
es NULL**, la comparación entera se vuelve nula y la búsqueda por texto no
devuelve nada. Con un carácter de escape real funciona.

Por lo mismo, los parámetros de texto van envueltos en `CAST(... AS String)`
dentro de la comprobación de nulidad: un parámetro que solo aparece en un
`IS NULL` no tiene tipo que inferir y el driver lo enviaría como un null sin
tipo, que Oracle rechaza.

### Las pruebas de repositorio usan la base configurada

`@DataJpaTest` sustituye el `DataSource` por uno embebido y descarta la URL del
perfil de pruebas. Con eso, H2 dejaba de emular a Oracle y las pruebas
validaban una semántica distinta a la de la base real: fue exactamente lo que
escondió el problema del `LIKE`. De ahí el
`@AutoConfigureTestDatabase(replace = NONE)` en las dos clases de repositorio.

### Perfiles

| Perfil | Base de datos | Para qué |
|---|---|---|
| `local` | H2 en memoria, modo Oracle | Desarrollar y probar en Postman sin red ni credenciales. Siembra datos de ejemplo al arrancar. |
| `test` | H2 en memoria, modo Oracle | Las 77 pruebas automatizadas. |
| `oracle` | Autonomous Database, esquema `FORO` | La verificación final y la entrega. |

El docente de S1 pidió expresamente mantener los perfiles separados y hacer la
prueba final sobre Oracle. Es lo que se hizo: `docs/registros/` contiene la
evidencia de las dos cosas.

### El Wallet no puede estar en la carpeta del proyecto

La ruta del Wallet **no puede contener espacios**: el driver JDBC de Oracle los
elimina al parsear el parámetro `TNS_ADMIN` de la URL y la conexión falla con
`ORA-12263`. La ruta de este proyecto tiene espacios, así que el Wallet vive en
`~/.oracle/wallets/FULLSTACK3`, fuera de los repositorios.
