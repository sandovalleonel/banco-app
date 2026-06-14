-- =============================================================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS Y DATOS DE PRUEBA (PostgreSQL)
-- Archivo: BaseDatos.sql
-- =============================================================================

-- 1. ELIMINACIÓN DE TABLAS EN ORDEN DE JERARQUÍA (Por si se vuelve a ejecutar)
DROP TABLE IF EXISTS movimientos_historico CASCADE;
DROP TABLE IF EXISTS movimientos CASCADE;
DROP TABLE IF EXISTS cuenta CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;
DROP TABLE IF EXISTS persona CASCADE;
DROP TABLE IF EXISTS configuracion CASCADE;

-- 2. TABLA DE CONFIGURACIÓN DE PARÁMETROS DE NEGOCIO
CREATE TABLE configuracion (
    clave VARCHAR(50) PRIMARY KEY,
    valor VARCHAR(250) NOT NULL,
    descripcion VARCHAR(250)
);

-- Inyección del parámetro solicitado para el control de cupos diarios
INSERT INTO configuracion (clave, valor, descripcion) 
VALUES ('LIMITE_DIARIO_RETIRO', '1000.00', 'Monto máximo acumulado que un cliente puede retirar en un mismo día');


-- 3. TABLA PERSONA (Clase Base)
CREATE TABLE persona (
    id BIGSERIAL PRIMARY KEY,
    identificacion VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    genero VARCHAR(20),
    edad INT,
    direccion VARCHAR(250),
    telefono VARCHAR(20)
);


-- 4. TABLA CLIENTE (Hereda de Persona mediante estrategia JOINED)
CREATE TABLE cliente (
    cliente_id BIGINT PRIMARY KEY,
    contrasena VARCHAR(250) NOT NULL, -- En producción usar hashes (ej. BCrypt)
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_cliente_persona FOREIGN KEY (cliente_id) REFERENCES persona(id) ON DELETE CASCADE
);


-- 5. TABLA CUENTA
CREATE TABLE cuenta (
    numero_cuenta VARCHAR(20) PRIMARY KEY,
    tipo_cuenta VARCHAR(20) NOT NULL, -- Valores esperados: 'Ahorro' o 'Corriente'
    saldo_inicial NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    cliente_id BIGINT NOT NULL,
    CONSTRAINT fk_cuenta_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(cliente_id) ON DELETE RESTRICT
);


-- 6. TABLA MOVIMIENTOS
CREATE TABLE movimientos (
    id BIGSERIAL PRIMARY KEY,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_movimiento VARCHAR(20) NOT NULL, -- Valores esperados: 'Débito' o 'Crédito'
    valor NUMERIC(12, 2) NOT NULL,        -- Negativo para débitos (retiros), positivo para créditos (depósitos)
    saldo NUMERIC(12, 2) NOT NULL,        -- Saldo disponible RESULTANTE después de aplicar la transacción
    numero_cuenta VARCHAR(20) NOT NULL,
    CONSTRAINT fk_movimientos_cuenta FOREIGN KEY (numero_cuenta) REFERENCES cuenta(numero_cuenta) ON DELETE CASCADE
);


-- 7. TABLA DE AUDITORÍA / HISTÓRICO
CREATE TABLE movimientos_historico (
    historico_id BIGSERIAL PRIMARY KEY,
    movimiento_id BIGINT,
    fecha_movimiento TIMESTAMP,
    tipo_movimiento VARCHAR(20),
    valor NUMERIC(12, 2),
    saldo NUMERIC(12, 2),
    numero_cuenta VARCHAR(20),
    -- Campos de auditoría
    auditoria_operacion VARCHAR(10) NOT NULL, -- INSERT, UPDATE, DELETE
    auditoria_fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    auditoria_usuario VARCHAR(100) NOT NULL
);


-- 8. FUNCIÓN Y TRIGGER DE AUDITORÍA (Corregido con ELSIF)
CREATE OR REPLACE FUNCTION fn_auditar_movimientos()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO movimientos_historico (
            movimiento_id, fecha_movimiento, tipo_movimiento, valor, saldo, numero_cuenta, 
            auditoria_operacion, auditoria_usuario
        )
        VALUES (
            NEW.id, NEW.fecha, NEW.tipo_movimiento, NEW.valor, NEW.saldo, NEW.numero_cuenta, 
            'INSERT', CURRENT_USER
        );
        RETURN NEW;
        
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO movimientos_historico (
            movimiento_id, fecha_movimiento, tipo_movimiento, valor, saldo, numero_cuenta, 
            auditoria_operacion, auditoria_usuario
        )
        VALUES (
            NEW.id, NEW.fecha, NEW.tipo_movimiento, NEW.valor, NEW.saldo, NEW.numero_cuenta, 
            'UPDATE', CURRENT_USER
        );
        RETURN NEW;
        
    ELSIF (TG_OP = 'DELETE') THEN
        INSERT INTO movimientos_historico (
            movimiento_id, fecha_movimiento, tipo_movimiento, valor, saldo, numero_cuenta, 
            auditoria_operacion, auditoria_usuario
        )
        VALUES (
            OLD.id, OLD.fecha, OLD.tipo_movimiento, OLD.valor, OLD.saldo, OLD.numero_cuenta, 
            'DELETE', CURRENT_USER
        );
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger asociado a la tabla movimientos
CREATE TRIGGER trg_auditoria_movimientos
AFTER INSERT OR UPDATE OR DELETE ON movimientos
FOR EACH ROW
EXECUTE FUNCTION fn_auditar_movimientos();


-- =============================================================================
-- INSERCIÓN DE DATOS DE PRUEBA (Casos de Uso de las Capturas)
-- =============================================================================

--- ----------------------------------------------------------------------------
--- CASO 1: Creación de Usuarios/Clientes
--- ----------------------------------------------------------------------------

-- Cliente 1: Jose Lema
INSERT INTO persona (id, identificacion, nombre, genero, edad, direccion, telefono)
VALUES (1, '1711111111', 'Jose Lema', 'Masculino', 30, 'Otavalo sn y principal', '098254785');
INSERT INTO cliente (cliente_id, contrasena, estado) 
VALUES (1, '1234', TRUE);

-- Cliente 2: Marianela Montalvo
INSERT INTO persona (id, identificacion, nombre, genero, edad, direccion, telefono)
VALUES (2, '1722222222', 'Marianela Montalvo', 'Femenino', 28, 'Amazonas y NNUU', '097548965');
INSERT INTO cliente (cliente_id, contrasena, estado) 
VALUES (2, '5678', TRUE);

-- Cliente 3: Juan Osorio
INSERT INTO persona (id, identificacion, nombre, genero, edad, direccion, telefono)
VALUES (3, '1733333333', 'Juan Osorio', 'Masculino', 35, '13 junio y Equinoccial', '098874587');
INSERT INTO cliente (cliente_id, contrasena, estado) 
VALUES (3, '1245', TRUE);

-- Ajustar secuenciador de la tabla persona para que no choque con los IDs manuales
SELECT setval('persona_id_seq', (SELECT MAX(id) FROM persona));


--- ----------------------------------------------------------------------------
--- CASO 2: Creación de Cuentas de Usuario
--- ----------------------------------------------------------------------------

-- Cuenta Ahorros de Jose Lema (Saldo Inicial: 2000)
INSERT INTO cuenta (numero_cuenta, tipo_cuenta, saldo_inicial, estado, cliente_id)
VALUES ('478758', 'Ahorro', 2000.00, TRUE, 1);

-- Cuenta Corriente de Marianela Montalvo (Saldo Inicial: 100)
INSERT INTO cuenta (numero_cuenta, tipo_cuenta, saldo_inicial, estado, cliente_id)
VALUES ('225487', 'Corriente', 100.00, TRUE, 2);

-- Cuenta Ahorros de Juan Osorio (Saldo Inicial: 0)
INSERT INTO cuenta (numero_cuenta, tipo_cuenta, saldo_inicial, estado, cliente_id)
VALUES ('495878', 'Ahorros', 0.00, TRUE, 3);

-- Segunda Cuenta (Ahorros) de Marianela Montalvo (Saldo Inicial: 540)
INSERT INTO cuenta (numero_cuenta, tipo_cuenta, saldo_inicial, estado, cliente_id)
VALUES ('496825', 'Ahorros', 540.00, TRUE, 2);

-- Cuenta Corriente Extra de Jose Lema (Caso 3 del ejercicio)
INSERT INTO cuenta (numero_cuenta, tipo_cuenta, saldo_inicial, estado, cliente_id)
VALUES ('585545', 'Corriente', 1000.00, TRUE, 1);


--- ----------------------------------------------------------------------------
--- CASO 3: Historial Inicial de Movimientos (Estados de Cuenta Base)
--- ----------------------------------------------------------------------------

-- Nota: Como Senior, los saldos en movimientos reflejan el cálculo post-operación.
-- Movimiento 1: Retiro de 575 en la cuenta de Jose Lema (478758). Saldo inicial 2000 -> Restante 1425
INSERT INTO movimientos (fecha, tipo_movimiento, valor, saldo, numero_cuenta)
VALUES ('2026-02-10 09:00:00', 'Débito', -575.00, 1425.00, '478758');

-- Movimiento 2: Depósito de 600 en cuenta Corriente de Marianela (225487). Saldo inicial 100 -> Restante 700
INSERT INTO movimientos (fecha, tipo_movimiento, valor, saldo, numero_cuenta)
VALUES ('2026-02-10 10:15:00', 'Crédito', 600.00, 700.00, '225487');

-- Movimiento 3: Depósito de 150 en cuenta Ahorros de Juan Osorio (495878). Saldo inicial 0 -> Restante 150
INSERT INTO movimientos (fecha, tipo_movimiento, valor, saldo, numero_cuenta)
VALUES ('2026-02-12 11:30:00', 'Crédito', 150.00, 150.00, '495878');

-- Movimiento 4: Retiro de 540 en cuenta Ahorros de Marianela (496825). Saldo inicial 540 -> Restante 0
INSERT INTO movimientos (fecha, tipo_movimiento, valor, saldo, numero_cuenta)
VALUES ('2026-02-08 14:00:00', 'Débito', -540.00, 0.00, '496825');