# Microservicio Plazoleta

Guia minima para ejecutar el servicio en local.

## Requisitos

- Java 21
- MySQL 8+ en ejecucion local
- Git (opcional)

## Configuracion minima

La configuracion principal esta en `src/main/resources/application.yml`.

Valores relevantes por defecto:
- Puerto del servicio: `8081`
- Base de datos: `plazoleta_db` en `localhost:3306`
- Usuario DB: `root`
- Password DB: `admin`
- URL de servicio externo de usuarios: `http://localhost:8082`

Si tu entorno usa otros valores, actualiza `src/main/resources/application.yml` antes de iniciar.

## Levantar el servicio

Desde la raiz del proyecto:

```bash
./gradlew bootRun
```

En Windows (PowerShell):

```powershell
.\gradlew.bat bootRun
```

La API quedara disponible en:
- `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`

## Ejecutar pruebas

```bash
./gradlew test
```

En Windows (PowerShell):

```powershell
.\gradlew.bat test
```

Las pruebas usan H2 en memoria segun `src/test/resources/application.yml`.
