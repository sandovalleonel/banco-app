# Sistema de Integración Bancaria (Banco App)

Este proyecto es una aplicación bancaria Full-Stack que incluye:
- **Base de Datos**: PostgreSQL para el almacenamiento persistente.
- **Back-End**: API REST construida con Java 8, Spring Boot y Maven.
- **Front-End**: Interfaz de usuario SPA desarrollada en Angular 19.

Toda la solución está completamente dockerizada para permitir una fácil compilación, despliegue y actualización utilizando únicamente Docker y Docker Compose.

---

## 🛠️ Requisitos de Sistema

Antes de iniciar, asegúrate de cumplir con los siguientes requisitos en el equipo host:

- **Docker** (Versión 20.10.0 o superior)
- **Docker Compose** (Versión v2.0.0 o superior)
- **Puertos Disponibles**:
  - `5432` (Base de Datos PostgreSQL)
  - `8080` (API Back-End)
  - `4200` (Aplicación Front-End Angular)

---

## 🚀 Despliegue en un Simple Comando

Para levantar toda la infraestructura del proyecto desde la carpeta raíz, sigue los siguientes pasos:

1. **Clona el repositorio** (si aún no lo has hecho) e ingresa a la raíz del proyecto:
   ```bash
   cd banco-app
   ```

2. **Ejecuta el siguiente comando** para compilar y levantar todos los contenedores en segundo plano (modo detached):
   ```bash
   docker compose up -d --build
   ```

Este comando realizará las siguientes acciones automáticamente:
- **Inicializar la Base de Datos**: Creará la base de datos `banco` y ejecutará el script `database/BaseDatos.sql` para crear las tablas, relaciones, triggers de auditoría e inyectar datos de prueba iniciales.
- **Compilar y Levantar el Back-End**: Compilará internamente la aplicación de Spring Boot en un contenedor multi-stage usando Maven y la iniciará una vez que la base de datos esté lista y saludable.
- **Compilar y Levantar el Front-End**: Descargará las dependencias de Angular, compilará el código de producción y servirá la SPA optimizada a través de un servidor web Nginx seguro.

---

## 🔗 URLs de Acceso

Una vez levantado el entorno, puedes acceder a los servicios desde tu navegador u otras herramientas:

- **Front-End (Angular UI)**: [http://localhost:4200](http://localhost:4200)
- **Back-End (Spring Boot API)**: [http://localhost:8080](http://localhost:8080)
- **Documentación API (Swagger UI)**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Base de Datos (PostgreSQL)**: `localhost:5432` (Base: `banco`, Usuario: `postgres`, Password: `postgres`)

---

## 🔄 Procedimiento para Actualizaciones

El despliegue está configurado para permitir actualizaciones independientes del Front-End o del Back-End de forma profesional, reconstruyendo solo lo necesario y minimizando tiempos:

### 1. Actualizar el Front-End (Angular)
Si has realizado cambios en el código de la carpeta `frontend/` y deseas desplegarlos:
```bash
docker compose up -d --build banco-frontend
```

### 2. Actualizar el Back-End (Spring Boot)
Si has realizado cambios en el código de la carpeta `backend/` y deseas desplegarlos:
```bash
docker compose up -d --build banco-backend
```

### 3. Reiniciar la Base de Datos (Limpiar Datos)
Si deseas vaciar y volver a inicializar la base de datos ejecutando el script SQL desde cero:
```bash
docker compose down -v
docker compose up -d --build
```
> ⚠️ **Advertencia**: Esto borrará el volumen persistente de la base de datos y recreará el esquema con los datos de prueba del script.

---

## 📋 Estructura de Servicios en Docker Compose

- **`banco-db`**: Contenedor Postgres que monta el script SQL local en `/docker-entrypoint-initdb.d/` para su ejecución automática en el primer arranque.
- **`banco-backend`**: Contenedor Spring Boot que espera a que `banco-db` pase su control de salud (`healthcheck`) para arrancar de forma segura.
- **`banco-frontend`**: Contenedor Nginx independiente que se compila y levanta de forma asíncrona sin bloquearse por el estado del Back-End.
