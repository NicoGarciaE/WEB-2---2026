# TP3 · Seguridad con JWT, Autorización, CI y Despliegue

Evolución del proyecto de **Catálogo, Listas y Favoritos** (TP1–TP2) incorporando autenticación stateless con **JWT**, autorización por **propiedad de recursos y rol (`USER` / `ADMIN`)**, pruebas unitarias y de integración con **Testcontainers**, pipeline de **Integración Continua (GitHub Actions)** y despliegue en nube con configuración por variables de entorno.

## Dominio

La jerarquía de entidades es `Usuario → Lista → Favorito`:

1. **`Usuario`**: se registra (`POST /api/auth/registro`), inicia sesión (`POST /api/auth/login`) con contraseña hasheada mediante BCrypt y tiene rol `USER` (por defecto) o `ADMIN`.
2. **`Lista`**: pertenece a un `Usuario` (`@ManyToOne` hacia `UsuarioEntity`, columna `usuario_id`).
3. **`Favorito`**: pertenece a una `Lista` (`@ManyToOne` hacia `ListaEntity`, columna `lista_id`) y referencia un producto del catálogo externo.

- Un usuario `USER` solo puede listar, ver y modificar **sus propias listas** (y los favoritos asociados a ellas). Si intenta operar sobre una lista ajena recibe `403 Forbidden`.
- Un usuario `ADMIN` puede acceder a las listas y favoritos de cualquier usuario.
- El catálogo `/api/productos/**`, `/api/auth/**` y `/health` son **públicos**.

---

## Variables de entorno

Definidas en `src/main/resources/application.properties` con valores por defecto para desarrollo local:

| Variable | Por defecto (local) | Descripción |
|---|---|---|
| `PORT` | `8080` | Puerto HTTP del servidor embebido |
| `DB_URL` | `jdbc:postgresql://localhost:5432/webii_tp2` | URL JDBC de PostgreSQL |
| `DB_USERNAME` | `webii_tp2` | Usuario de PostgreSQL |
| `DB_PASSWORD` | `webii_tp2` | Contraseña de PostgreSQL |
| `JWT_SECRET` | *(clave dev de 64 caracteres)* | Secreto HMAC-SHA256 para firmar/validar JWT (mín. 32 caracteres) |
| `JWT_EXPIRATION_MS` | `86400000` | Tiempo de expiración del JWT en milisegundos (24 h) |
| `DUMMYJSON_BASE_URL` | `https://dummyjson.com` | URL base del catálogo externo |

---

## Cómo levantar el proyecto en local

Requiere **Java 25**. Usar siempre el wrapper de Maven (`./mvnw` o `.\mvnw.cmd`).

### 1. Base de datos PostgreSQL

**Opción A — Docker:**
```bash
docker compose up -d
```

**Opción B — PostgreSQL local:**
```sql
CREATE ROLE webii_tp2 WITH LOGIN PASSWORD 'webii_tp2' SUPERUSER;
CREATE DATABASE webii_tp2 OWNER webii_tp2;
```

### 2. Ejecutar la aplicación

```powershell
# Windows (PowerShell)
.\mvnw.cmd spring-boot:run
```
```bash
# macOS / Linux
./mvnw spring-boot:run
```

Al iniciar, **Flyway** aplica automáticamente las migraciones `V1` a `V6` y **Hibernate** valida el esquema (`ddl-auto=validate`). La aplicación queda escuchando en `http://localhost:8080`.

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html` (incluye el botón **Authorize** para cargar el Bearer token JWT).
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`.

---

## Endpoints principales

| Método | Path | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/health` | Público | Estado verificable del servicio (`{"status":"ok","service":"demo"}`) |
| `GET` | `/ping` | Público | Responde `pong` |
| `GET` | `/api/productos?limit=&skip=` | Público | Catálogo paginado de solo lectura (DummyJSON) |
| `GET` | `/api/productos/{id}` | Público | Detalle de un producto (`404` si no existe) |
| `POST` | `/api/auth/registro` | Público | Registra usuario con contraseña hasheada y rol `USER` (`201` / `409`) |
| `POST` | `/api/auth/login` | Público | Valida credenciales y emite JWT con `id` y `rol` (`200` / `401`) |
| `GET` / `POST` | `/api/listas` | Bearer JWT | Lista las listas propias (o todas si `ADMIN`) / crea lista asociada al usuario autenticado |
| `GET` / `DELETE` | `/api/listas/{id}` | Bearer JWT | Obtiene o elimina lista vacía (`403` si es ajena y no es `ADMIN`, `409` si no está vacía) |
| `GET` | `/api/listas/{id}/favoritos` | Bearer JWT | Favoritos de una lista (`403` si es ajena y no es `ADMIN`) |
| `POST` | `/api/listas/{origenId}/mover-favoritos` | Bearer JWT | Mueve favoritos a otra lista propia y elimina la lista origen (`@Transactional`) |
| `GET` / `POST` | `/api/favoritos` | Bearer JWT | Lista favoritos propios (o todos si `ADMIN`) / crea favorito en lista propia |
| `GET` / `PUT` / `DELETE` | `/api/favoritos/{id}` | Bearer JWT | Obtiene, actualiza o elimina un favorito validando propiedad de su lista |

---

## Cómo correr los tests (Consigna 5)

El `pom.xml` separa la ejecución en dos fases mediante **Maven Surefire** y **Maven Failsafe**:

### 1. Pruebas unitarias (`*Test` — Surefire)
No requieren PostgreSQL ni Docker; usan mocks para validar las reglas de negocio y seguridad en aislamiento (`ListaServiceImplTest`, `UsuarioServiceImplTest`):

```powershell
# Windows
.\mvnw.cmd test
```
```bash
# Linux / macOS
./mvnw test
```

### 2. Pruebas unitarias + de integración (`*IT` — Failsafe + Testcontainers)
Requiere **Docker** corriendo. Testcontainers levanta automáticamente un contenedor limpio `postgres:17`, Flyway ejecuta las migraciones `V1`–`V6` desde cero, y se prueban los flujos HTTP reales con `AuthControllerIT` y `ListaControllerIT` (`200` login válido, `401` login inválido, `401` sin token, `200` lista propia, `403` lista ajena, `200` acceso `ADMIN`):

```powershell
# Windows
.\mvnw.cmd verify
```
```bash
# Linux / macOS
./mvnw verify
```

---

## Integración Continua — GitHub Actions (Consigna 6)

El archivo `.github/workflows/ci.yml` se dispara automáticamente en cada `push` y `pull_request`:

1. Levanta un entorno limpio `ubuntu-latest` (que ya cuenta con Docker preinstalado, sin ninguna base local preexistente).
2. Instala **JDK 25 (Temurin)** con caché de dependencias Maven.
3. Asegura permisos de ejecución sobre `./mvnw` (además de estar registrado en Git como `100755` mediante `git update-index --chmod=+x mvnw`).
4. Ejecuta `./mvnw -B verify`, corriendo tanto las pruebas unitarias (`*Test`) como las de integración (`*IT`) con Testcontainers y las migraciones de Flyway contra una base PostgreSQL limpia.

### Cómo leer el resultado de CI
1. Ir al repositorio en GitHub → pestaña **Actions**.
2. Seleccionar el workflow **CI** y abrir la corrida correspondiente al último commit.
3. Entrar al job **Build, Unit Tests & Integration Tests (Testcontainers)** y desplegar el paso **Run Maven verify**:
   - Si todas las migraciones, pruebas unitarias y de integración pasan, el paso muestra `BUILD SUCCESS` y el check del commit queda en verde (✅).
   - Si alguna prueba o migración falla, Maven devuelve código distinto de cero (`BUILD FAILURE`), detiene el workflow, marca el commit en rojo (❌) y muestra en el log exactamente qué clase/método `*Test` o `*IT` falló.

---

## Pasos de despliegue en Render (Consigna 7)

El repositorio incluye un `Dockerfile` multi-stage (compila con `eclipse-temurin:25-jdk` y ejecuta con `eclipse-temurin:25-jre`) listo para desplegar en **Render** (o cualquier proveedor compatible con contenedores):

1. **Crear la base de datos PostgreSQL en Render:**
   - En el dashboard de Render: **New + → PostgreSQL**.
   - Elegir plan **Free** y crear la instancia.
   - Una vez disponible (`Available`), copiar de la sección *Connections* los datos internos (`Hostname`, `Port`, `Database`, `Username`, `Password`).

2. **Crear el Web Service en Render:**
   - En el dashboard de Render: **New + → Web Service** y conectar el repositorio de GitHub (rama `tp3-entrega` o `main`).
   - **Language / Runtime:** `Docker` (detecta automáticamente el `Dockerfile` de la raíz).
   - **Instance Type:** `Free`.

3. **Configurar las variables de entorno en el Web Service (*Environment*):**
   - `DB_URL`: `jdbc:postgresql://<INTERNAL_HOST>:5432/<DATABASE_NAME>` *(importante incluir el prefijo `jdbc:` adelante de `postgresql://`)*.
   - `DB_USERNAME`: `<USERNAME_DE_RENDER_POSTGRES>`
   - `DB_PASSWORD`: `<PASSWORD_DE_RENDER_POSTGRES>`
   - `JWT_SECRET`: una cadena aleatoria segura de al menos 32–64 caracteres para firmar los tokens en producción.
   - *(Render inyecta `PORT` automáticamente, que Spring toma desde `server.port=${PORT:8080}`).*

4. **Verificación posterior al despliegue:**
   - **Logs del servicio (pestaña *Logs* en Render):** verificar que Flyway aplicó las 6 migraciones (`Successfully applied 6 migrations to schema "public", now at version v6`) y que Spring Boot inició sin errores (`Started DemoApplication`).
   - **Endpoint de estado público:** abrir `https://<tu-servicio>.onrender.com/health` → responde `200 OK` con `{"status":"ok","service":"demo"}`.
   - **Validación de seguridad en la URL pública:**
     - `GET https://<tu-servicio>.onrender.com/api/listas` sin header `Authorization` → responde `401 Unauthorized`.
     - `POST /api/auth/registro` + `POST /api/auth/login` → copiar el `token` devuelto y llamar `GET /api/listas` con `Authorization: Bearer <token>` → responde `200 OK`.

---

## Dónde vive la regla de autorización y por qué (Consigna 4.5)

La validación de **propiedad de recursos y rol** (`USER` solo accede/modifica sus propias listas y favoritos; `ADMIN` puede acceder a cualquiera) vive en la **capa de Servicio** (`ListaServiceImpl` y `FavoritoServiceImpl`), mientras que la **autenticación** y las reglas generales de rutas públicas vs. autenticadas viven en `JwtAuthenticationFilter` y `SecurityConfig`.

**¿Por qué en el Service y no en un Filtro ni en el Controller?**
1. **No puede resolverse únicamente en un Filtro HTTP:** el filtro conoce la ruta (`/api/listas/{id}`) y la identidad del token (`id`, `rol`), pero no sabe a qué usuario pertenece esa lista sin consultar `ListaRepository`. Además, diferenciar `404 Not Found` (la lista no existe) de `403 Forbidden` (existe pero es de otro usuario) exige buscar primero la entidad de dominio.
2. **Responsabilidad única del Controller:** los `@RestController` solo traducen HTTP y validan DTOs de entrada (`@Valid`), sin alojar lógica de negocio ni reglas del dominio.
3. **Centralización sin posibilidad de bypass:** al vivir `verificarAcceso(...)` en el Service, cualquier operación compuesta (como `moverFavoritos(origenId, destinoId)`, que valida propiedad tanto sobre la lista origen como sobre la destino, o `FavoritoServiceImpl` al operar sobre una lista) aplica siempre la misma regla sin duplicar consultas.
4. **Testeabilidad unitaria directa:** permite probar todos los casos de autorización (`USER` dueño, `USER` ajeno → `AccesoDenegadoException`, `ADMIN`) en pruebas unitarias puras (`ListaServiceImplTest`) con mocks de repositorios, sin necesidad de levantar Spring MVC.

---

## Documentación heredada del TP2

### Puertos y Adaptadores (TP2 · Consigna 4)
**¿Qué cambió?**
Para migrar de memoria a PostgreSQL tuvimos que crear tres componentes nuevos en la capa de persistencia: `FavoritoEntity`, `FavoritoJpaRepository` y `FavoritoRepositoryAdapter`, eliminando `InMemoryFavoritoRepository`.

**¿Qué NO cambió y por qué?**
En el TP2, `FavoritoService`, `FavoritoController` y los DTOs permanecieron intactos gracias a la **Arquitectura Hexagonal (Puertos y Adaptadores)**: `FavoritoRepository` actúa como puerto (contrato) desacoplando la lógica de negocio del mecanismo concreto de persistencia.

### Evolución del Esquema (TP2 · Consigna 6)
Se resuelve siempre agregando nuevas migraciones (`V4`, `V5`, `V6`) porque en Flyway y en entornos productivos las migraciones previas ya fueron aplicadas y su checksum queda registrado en `flyway_schema_history`; editar una migración ya corrida rompería la validación e integridad de bases existentes.

### Transacciones (TP2 · Consigna 7)
Si sacáramos `@Transactional` en `moverFavoritos` y una escritura fallara a mitad del proceso (por ejemplo, tras reasignar favoritos pero antes de borrar la lista origen), la base perdería consistencia violando la **Atomicidad (ACID)**. `@Transactional` garantiza que todas las operaciones del caso de uso se confirmen juntas o se reviertan por completo.
