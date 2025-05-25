-- Tipos ENUM
CREATE TYPE client_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');
CREATE TYPE module_status AS ENUM ('ACTIVE', 'INACTIVE', 'PENDING', 'EXPIRED');
CREATE TYPE administrator_status AS ENUM ('PENDING', 'ACTIVE', 'INACTIVE');
CREATE TYPE notification_status AS ENUM ('PENDING', 'SENT', 'FAILED', 'DELIVERED');
CREATE TYPE target_type AS ENUM ('CLIENT', 'ADMINISTRATOR', 'WORKERS');


-- Tabla de clientes
CREATE TABLE clients (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    ruc VARCHAR(20) UNIQUE NOT NULL,
    business_name VARCHAR(200) NOT NULL,
    commercial_name VARCHAR(200),
    taxpayer_type VARCHAR(50),
    activity_start_date DATE,
    fiscal_address VARCHAR(300),
    economic_activity VARCHAR(300),
    contact_number VARCHAR(50),
    status client_status NOT NULL DEFAULT 'ACTIVE',
    notified BOOLEAN DEFAULT FALSE,
    allocated_storage BIGINT DEFAULT 0,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_update_date TIMESTAMP,
    created_by INTEGER NOT NULL,
    notes TEXT
);


-- Tabla de representantes legales
CREATE TABLE legal_representatives (
    id SERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    document_type VARCHAR(30) NOT NULL,
    document_number VARCHAR(30) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    position VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER NOT NULL,
    UNIQUE(client_id, document_number)
);


-- Tabla de módulos
CREATE TABLE modules (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    version VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_update TIMESTAMP,
    features JSONB DEFAULT '{}'::jsonb
);

-- Insertar datos iniciales de módulos
INSERT INTO modules (code, name, description) VALUES
('MOD1', 'Gestión Documental', 'Sistema de gestión documental con permisos granulares.'),
('MOD2', 'Gestión Documental Avanzada', 'Sistema de gestión documental con carga masiva de usuarios mediante Excel.'),
('MOD3', 'Gestión de Proveedores', 'Sistema jerárquico para gestión de proveedores y auditorías.');



-- Tabla de asignación cliente-módulos
CREATE TABLE client_modules (
    id SERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    module_id INTEGER NOT NULL REFERENCES modules(id) ON DELETE RESTRICT,
    start_date DATE NOT NULL,
    end_date DATE,
    status module_status NOT NULL DEFAULT 'ACTIVE',
    max_user_accounts INTEGER NOT NULL DEFAULT 10,
    specific_storage_limit BIGINT,
    activation_date TIMESTAMP,
    deactivation_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER NOT NULL,
    last_update TIMESTAMP,
    updated_by INTEGER,
    configuration JSONB DEFAULT '{}'::jsonb,
    UNIQUE(client_id, module_id)
);


-- Tabla de administradores de cliente
CREATE TABLE client_administrators (
    id SERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    email VARCHAR(100) NOT NULL,
    dni VARCHAR(20) NOT NULL,
    identity_user_id INTEGER UNIQUE,
    status administrator_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER NOT NULL,
    notification_sent BOOLEAN DEFAULT FALSE,
    notification_date TIMESTAMP,
    UNIQUE(client_id, email)
);


-- Tabla de historial de clientes
CREATE TABLE client_history (
    id SERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    action VARCHAR(50) NOT NULL,
    previous_status client_status,
    new_status client_status,
    change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by INTEGER NOT NULL,
    notes TEXT,
    details JSONB
);


-- Tabla de notificaciones
CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    client_id INTEGER REFERENCES clients(id) ON DELETE CASCADE,
    target_type target_type NOT NULL,
    target_id INTEGER,
    notification_type VARCHAR(50) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    send_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status notification_status NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    last_retry TIMESTAMP,
    created_by INTEGER NOT NULL
);


-- Tabla de pre-registro de trabajadores
CREATE TABLE workers_registration (
    id SERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    email VARCHAR(100) NOT NULL,
    dni VARCHAR(20) NOT NULL,
    identity_user_id INTEGER,
    is_registered BOOLEAN DEFAULT FALSE,
    registration_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER NOT NULL,
    notification_sent BOOLEAN DEFAULT FALSE,
    notification_date TIMESTAMP,
    UNIQUE(client_id, email)
);




-- Índices para búsquedas frecuentes
CREATE INDEX idx_clients_code ON clients(code);
CREATE INDEX idx_clients_ruc ON clients(ruc);
CREATE INDEX idx_clients_business_name ON clients(business_name);
CREATE INDEX idx_clients_status ON clients(status);


-- Índices para relaciones
CREATE INDEX idx_legal_representatives_client_id ON legal_representatives(client_id);
CREATE INDEX idx_client_modules_client_id ON client_modules(client_id);
CREATE INDEX idx_client_modules_module_id ON client_modules(module_id);
CREATE INDEX idx_client_modules_status ON client_modules(status);
CREATE INDEX idx_client_administrators_client_id ON client_administrators(client_id);
CREATE INDEX idx_client_administrators_email ON client_administrators(email);
CREATE INDEX idx_client_history_client_id ON client_history(client_id);
CREATE INDEX idx_notifications_client_id ON notifications(client_id);
CREATE INDEX idx_workers_registration_client_id ON workers_registration(client_id);


-- Índices para búsquedas por fechas
CREATE INDEX idx_client_modules_start_date ON client_modules(start_date);
CREATE INDEX idx_client_modules_end_date ON client_modules(end_date);
CREATE INDEX idx_client_history_change_date ON client_history(change_date);
CREATE INDEX idx_notifications_send_date ON notifications(send_date);


-- Procedimientos
CREATE OR REPLACE FUNCTION generate_client_code() 
RETURNS VARCHAR AS $$
DECLARE
    v_prefix VARCHAR := 'CLI';
    v_year VARCHAR := to_char(CURRENT_DATE, 'YY');
    v_month VARCHAR := to_char(CURRENT_DATE, 'MM');
    v_sequence INTEGER;
    v_code VARCHAR;
BEGIN
    -- Obtener secuencia para el mes actual
    SELECT COALESCE(MAX(SUBSTRING(code, 9, 4)::INTEGER), 0) + 1
    INTO v_sequence
    FROM clients
    WHERE code LIKE v_prefix || v_year || v_month || '%';
    
    -- Formatear código
    v_code := v_prefix || v_year || v_month || LPAD(v_sequence::TEXT, 4, '0');
    
    RETURN v_code;
END;
$$ LANGUAGE plpgsql;

-- Función para registrar Historial de Cliente
CREATE OR REPLACE FUNCTION log_client_history(
    p_client_id INTEGER,
    p_action VARCHAR,
    p_previous_status client_status,
    p_new_status client_status,
    p_changed_by INTEGER,
    p_notes TEXT,
    p_details JSONB
) RETURNS VOID AS $$
BEGIN
    INSERT INTO client_history (
        client_id,
        action,
        previous_status,
        new_status,
        changed_by,
        notes,
        details
    ) VALUES (
        p_client_id,
        p_action,
        p_previous_status,
        p_new_status,
        p_changed_by,
        p_notes,
        p_details
    );
END;
$$ LANGUAGE plpgsql;

-- Función para Verificar Módulos Activos
CREATE OR REPLACE FUNCTION has_active_module(
    p_client_id INTEGER,
    p_module_code VARCHAR
) RETURNS BOOLEAN AS $$
DECLARE
    v_has_module BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM admin.client_modules cm
        JOIN admin.modules m ON cm.module_id = m.id
        WHERE cm.client_id = p_client_id
        AND m.code = p_module_code
        AND cm.status = 'ACTIVE'
        AND (cm.end_date IS NULL OR cm.end_date >= CURRENT_DATE)
    ) INTO v_has_module;
    
    RETURN v_has_module;
END;
$$ LANGUAGE plpgsql;

-- Función para actualizar el estado de Módulos Expirados
CREATE OR REPLACE FUNCTION update_expired_modules() 
RETURNS INTEGER AS $$
DECLARE
    v_count INTEGER := 0;
BEGIN
    UPDATE client_modules
    SET status = 'EXPIRED',
        last_update = CURRENT_TIMESTAMP
    WHERE status = 'ACTIVE'
    AND end_date < CURRENT_DATE;
    
    GET DIAGNOSTICS v_count = ROW_COUNT;
    
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;

-- Trigger para Actualizar Fecha de Última Modificación
CREATE OR REPLACE FUNCTION update_last_update_date()
RETURNS TRIGGER AS $$
BEGIN
    NEW.last_update_date := CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_update_client_last_update
BEFORE UPDATE ON clients
FOR EACH ROW
EXECUTE FUNCTION update_last_update_date();


select * from modules;
DELETE FROM MODULES 
wHERE id IN (5);

