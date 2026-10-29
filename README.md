# foro-temas

Microservicio de **contenido del foro** —categorías, temas y comentarios— del
caso *Sistema de Foros en Línea*. Spring Boot sobre **Oracle Cloud Autonomous
Database**.

Es **uno de los dos microservicios** del caso. El otro,
[`foro-usuarios`](#el-otro-microservicio), vive en su propio repositorio y se
encarga de las cuentas, los roles y el inicio de sesión.

El foro trata sobre **cubos Rubik y twisty puzzles**, con seis categorías.

| | |
|---|---|
| Puerto | `8082` |
| Responsabilidad | Categorías; publicación, búsqueda y edición de temas; comentarios |
| Tablas propias | `CATEGORIA`, `TEMA`, `COMENTARIO` |
| Rol en la pareja | **Valida** el JWT que el otro microservicio emite |

**Asignatura:** Desarrollo Full Stack III (DSY2205) · Experiencia 1, Semana 2
· Actividad sumativa *Programando nuestro BackEnd*.

---

## 1. Requisitos

| Herramienta | Versión |
|---|---|
| Java | 21 LTS (Temurin) |
| Maven | 3.9.16, incluido como *wrapper* |
| Spring Boot | 3.5.16 |
| Base de datos | Oracle Autonomous Database, esquema `FORO` |

No hace falta instalar Maven: el proyecto trae su propio `mvnw`.

---

## 2. Estructura

```
foro-temas/
├── src/main/java/com/foro/temas/
│   ├── controller/      CategoriaController, TemaController, ComentarioController, EstadoController
│   ├── service/         contrato + impl con la lógica de negocio y las reglas de autor
│   ├── repository/      CategoriaRepository, TemaRepository, ComentarioRepository (JPA)
│   ├── model/           Categoria, Tema, Comentario
│   ├── dto/             entrada y salida, incluida la paginación
│   ├── security/        validación del JWT, filtro, configuración
│   ├── exception/       excepciones propias + manejador global
│   └── config/          datos de ejemplo del perfil local
├── src/test/java/...    43 pruebas automatizadas
├── scripts/
│   ├── 00_crear_esquema_foro.sql   CREATE USER FORO + permisos (como ADMIN)
│   ├── 01_crear_tablas.sql         CATEGORIA, TEMA, COMENTARIO + secuencias + índices
│   ├── 02_datos_iniciales.sql      6 categorías, 9 temas, 13 comentarios
│   ├── 99_eliminar_objetos.sql     para recrear desde cero
│   ├── consultas_demo.sql          SELECT para evidenciar los datos en Oracle
│   ├── sql.sh + EjecutarSql.java   ejecuta un .sql por JDBC
│   ├── entorno-oracle.sh           credenciales desde el Llavero de macOS
│   ├── run.sh                      arranca contra Oracle
│   └── test.sh                     ejecuta las pruebas
├── postman/             colección de demostración (cubre los dos servicios)
└── docs/
    ├── arquitectura.md  diagramas y decisiones de diseño de la pareja
    └── registros/       evidencias de ejecución
```

---

## 3. Base de datos

Las tablas viven en un **esquema propio llamado `FORO`**, no bajo `ADMIN`. Este
microservicio crea y puebla **solo sus tres tablas**; las de cuentas las crea
`foro-usuarios` con su propio script.

| Tabla | Registros iniciales |
|---|---|
| `CATEGORIA` | 6 |
| `TEMA` | 9 |
| `COMENTARIO` | 13 |

Las seis categorías: Cubo 3x3 · Big cubes · Métodos y algoritmos · Puzzles no
cúbicos · Competencias WCA · Hardware y mods.

### `TEMA.AUTOR_ID` no tiene clave foránea, y es a propósito

`TEMA` y `COMENTARIO` guardan el **ID y el nombre del autor que vienen firmados
en el JWT**, sin relación JPA ni clave foránea hacia `USUARIO`, que pertenece al
otro microservicio. Es el patrón habitual entre microservicios: cada uno es
dueño de sus tablas y replica el mínimo dato ajeno que necesita. El nombre se
guarda además del ID precisamente para no tener que preguntarle al otro servicio
cada vez que se lista un tema.

Dentro de este microservicio sí hay claves foráneas: `TEMA → CATEGORIA` y
`COMENTARIO → TEMA`, esta última con `ON DELETE CASCADE`.

### Creación desde cero

```bash
# 1. Una sola vez, como ADMIN: crea el esquema y sus permisos.
#    Antes de ejecutarlo, reemplaza __CLAVE_FORO__ en el script.
#    Este paso es común a los dos microservicios: basta hacerlo una vez.
./scripts/sql.sh admin scripts/00_crear_esquema_foro.sql

# 2. Como FORO: las tablas de este microservicio.
./scripts/sql.sh foro scripts/01_crear_tablas.sql

# 3. Como FORO: sus datos iniciales.
./scripts/sql.sh foro scripts/02_datos_iniciales.sql
```

Los scripts también se pueden pegar directamente en **Oracle Database Actions**.

> `sql.sh` ejecuta el SQL por JDBC porque en el equipo de desarrollo no hay
> `sqlplus` ni `sqlcl` instalados.

### Credenciales

No están en el repositorio. `scripts/entorno-oracle.sh` las lee del **Llavero de
macOS**:

```bash
read -s -p "Password FORO: " P && \
  security add-generic-password -a "$USER" -s foro-online-db -w "$P" -U && \
  unset P
```

El **Wallet** vive en `~/.oracle/wallets/FULLSTACK3`, fuera del proyecto: su
ruta no puede contener espacios o el driver JDBC falla con `ORA-12263`.

---

## 4. Ejecución

```bash
./scripts/run.sh               # contra Oracle, puerto 8082
./mvnw spring-boot:run         # en local con H2 en memoria, sin credenciales
```

El perfil `local` es el de por omisión y siembra al arrancar las mismas 6
categorías, 9 temas y 13 comentarios de ejemplo.

```bash
curl http://localhost:8082/api/estado
```

Responde con el **motor real de base de datos**, que es la forma rápida de
distinguir si se está corriendo contra H2 o contra Oracle.

> Para **escribir** en el foro hace falta un token, y los tokens los emite
> `foro-usuarios`. Leer el foro, en cambio, es público y no necesita el otro
> servicio arriba.

---

## 5. API REST

| Método | Ruta | Descripción | Acceso | Respuestas |
|---|---|---|---|---|
| `GET` | `/api/categorias` | Categorías con su cantidad de temas | público | `200` |
| `GET` | `/api/categorias/{id}` | Una categoría | público | `200` · `404` |
| `POST` | `/api/categorias` | Crea una categoría | `ADMIN` | `201` · `400` · `403` · `409` |
| `GET` | `/api/temas` | Busca temas. Filtros `?categoria=` `?texto=` `?autor=`, paginado con `?pagina=` y `?tamano=` | público | `200` |
| `GET` | `/api/temas/{id}` | Detalle con sus comentarios | público | `200` · `404` |
| `POST` | `/api/temas` | Publica un tema | autenticado | `201` · `400` · `401` · `404` |
| `PUT` | `/api/temas/{id}` | Edita un tema | autor o `ADMIN` | `200` · `400` · `403` · `404` |
| `DELETE` | `/api/temas/{id}` | Elimina un tema y sus comentarios | autor o `ADMIN` | `204` · `403` · `404` |
| `GET` | `/api/temas/{id}/comentarios` | Comentarios del tema | público | `200` · `404` |
| `POST` | `/api/temas/{id}/comentarios` | Comenta un tema | autenticado | `201` · `400` · `401` · `404` |
| `DELETE` | `/api/comentarios/{id}` | Elimina un comentario | autor o `ADMIN` | `204` · `403` · `404` |
| `GET` | `/api/estado` | Estado del servicio y de la conexión | público | `200` |

### Búsqueda de temas

Los tres filtros son opcionales y **se combinan entre sí**. Se resuelven en una
sola consulta JPQL que desactiva por su cuenta las condiciones que no llegan, de
modo que las ocho combinaciones posibles no necesitan ocho métodos:

| Petición | Resultado |
|---|---|
| `/api/temas` | todos, paginados de 10 en 10 |
| `/api/temas?texto=cfop` | los que llevan «cfop» en el título, sin distinguir mayúsculas |
| `/api/temas?autor=feliks` | los publicados por ese usuario |
| `/api/temas?categoria=3` | los de esa categoría |
| `/api/temas?texto=gan&autor=feliks` | los dos filtros a la vez |

La respuesta siempre viene envuelta con los datos de paginación:

```json
{
  "contenido": [ ... ],
  "pagina": 0,
  "tamano": 10,
  "totalElementos": 9,
  "totalPaginas": 1
}
```

### Ejemplos

```bash
# Leer el foro no necesita token
curl "http://localhost:8082/api/categorias"
curl "http://localhost:8082/api/temas?texto=cfop"
curl "http://localhost:8082/api/temas?autor=feliks&categoria=1&pagina=0&tamano=5"

# Publicar sí. El token lo emite foro-usuarios.
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"nombreUsuario":"feliks","contrasena":"Cubo12345*"}' \
  | python3 -c 'import json,sys; print(json.load(sys.stdin)["token"])')

# El autor no se envía en el cuerpo: sale del token.
curl -X POST http://localhost:8082/api/temas \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"categoriaId":1,"titulo":"Mi primer sub-20","contenido":"Por fin bajé de 20 segundos."}'
```

---

## 6. Seguridad y roles

- **No emite tokens ni consulta la tabla `USUARIO`.** Comparte con
  `foro-usuarios` el secreto de firma y con eso valida por sí mismo que el JWT
  es auténtico y está vigente. No hay una llamada de red por petición.
- **Leer es público**, escribir exige token, crear categorías exige `ADMIN`.
- **Reglas por rol** en el controlador con `@PreAuthorize`; la regla de
  propiedad —*«soy el autor, o soy ADMIN»*— vive en la **capa de servicio**,
  porque es una regla de negocio y no de transporte.
- **CORS** configurado para los puertos locales, de cara al frontend Angular de
  la semana 3.

`401` y `403` significan cosas distintas y el servicio las distingue: `401` es
que no hay identidad, `403` que la identidad existe pero no alcanza.

La tabla completa de privilegios está en
[`docs/arquitectura.md`](docs/arquitectura.md#roles-y-privilegios).

---

## 7. Pruebas

```bash
./scripts/test.sh
```

**43 pruebas** sobre H2 en modo Oracle, de modo que validan el mismo mapeo JPA
que se usa contra la base real sin depender de la red:

| Clase | Pruebas | Cobertura |
|---|---|---|
| `TemaRepositoryTest` | 11 | Mapeo JPA, borrado en cascada, búsqueda con filtros, conteo agrupado |
| `ForoServiceImplTest` | 15 | CRUD de categorías, temas y comentarios; reglas de autor y `ADMIN` |
| `ForoControllerTest` | 16 | Los doce endpoints, la validación del token y los privilegios |
| `ForoTemasApplicationTests` | 1 | Arranque del contexto con seguridad y JPA |

Las pruebas de controlador **firman sus propios tokens** con el mismo secreto que
usa `foro-usuarios`, así que la autorización se prueba de punta a punta sin
levantar el otro servicio.

---

## 8. Postman

1. **File ▸ Import** → `postman/foro-online.postman_collection.json`
2. Arrancar **los dos** microservicios, cada uno desde su repositorio
3. **Run collection**, o ejecutar las peticiones **00 a 24** en orden

La colección cubre los dos servicios porque la demostración los recorre juntos:
el token se obtiene en `foro-usuarios` y se usa aquí. Es el mismo archivo en los
dos repositorios.

---

## 9. El otro microservicio

El caso se resuelve con dos microservicios en repositorios separados:

| Repositorio | Puerto | Responsabilidad |
|---|---|---|
| `foro-usuarios` | 8081 | Cuentas, roles e inicio de sesión |
| **`foro-temas`** (este) | 8082 | Categorías, temas y comentarios |

Se comunican por el JWT: el otro servicio lo firma, este lo valida con el mismo
secreto compartido. **No hay llamadas de red entre ellos**, así que este
servicio sigue sirviendo el foro aunque `foro-usuarios` esté caído; lo único que
no se podrá hacer mientras tanto es iniciar sesión ni publicar.

Los detalles están en [`docs/arquitectura.md`](docs/arquitectura.md).

---

## 10. Evidencias

En [`docs/registros/`](docs/registros):

| Archivo | Contenido |
|---|---|
| `01_verificacion_oracle.txt` | Recorrido de punta a punta contra Oracle sobre los dos servicios: los cuatro verbos REST, los privilegios de los roles y los casos de error |
| `02_esquema_oracle.txt` | Estado del esquema `FORO`, conteo por tabla y la matriz de privilegios verificada en vivo |
| `03_pruebas_automatizadas.txt` | Salida de las pruebas de este microservicio |
| `04_coleccion_postman.txt` | Las 25 peticiones de la colección ejecutadas en orden contra Oracle |
