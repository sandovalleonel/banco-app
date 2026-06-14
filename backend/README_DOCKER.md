# Guía de Despliegue con Docker - Banco API

Esta guía contiene las instrucciones paso a paso para levantar los contenedores de la aplicación y la base de datos PostgreSQL utilizando **Docker** y **Docker Compose**.

---

## Requisitos Previos

- Tener instalado **Docker Desktop** (en Windows/Mac) o **Docker Engine** y **Docker Compose** (en Linux).
- Tener libres los puertos `8080` (para la API) y `5432` (para la base de datos).

---

## Cómo Iniciar la Aplicación

1. **Abrir la terminal** en la raíz del proyecto (`c:\local\banco-app\backend`).
2. **Levantar los contenedores**:
   Ejecuta el siguiente comando para compilar la imagen de la aplicación y levantar los servicios en segundo plano:
   ```bash
   docker compose up -d --build
   ```
   *Nota: La primera vez descargará las imágenes base de Maven y PostgreSQL y demorará un poco más.*

3. **Verificar que los contenedores estén corriendo**:
   ```bash
   docker compose ps
   ```

4. **Ver los logs en tiempo real**:
   Para monitorear el inicio de Spring Boot y verificar las conexiones, ejecuta:
   ```bash
   docker compose logs -f
   ```

---

## Acceso a la Aplicación y Base de Datos

- **Endpoints de la API**: Disponibles en `http://localhost:8080` (ej: `http://localhost:8080/clientes`, `http://localhost:8080/cuentas`).
- **Base de Datos PostgreSQL**:
  - **Host**: `localhost` (o `db` desde dentro de la red docker).
  - **Puerto**: `5432`
  - **Base de Datos**: `banco`
  - **Usuario**: `postgres`
  - **Contraseña**: `postgres`

---

## Detener la Aplicación

Para apagar los contenedores y liberar los puertos sin borrar los datos de la base de datos:
```bash
docker compose down
```

Para apagar los contenedores y **borrar** todos los datos guardados en el volumen de PostgreSQL:
```bash
docker compose down -v
```
