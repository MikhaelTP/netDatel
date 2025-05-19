CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    user_type VARCHAR(20) NOT NULL CHECK (user_type IN ('SUPER_ADMIN', 'CLIENT_ADMIN', 'WORKER', 'AUDITOR', 'PROVIDER')),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    enabled BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    last_login TIMESTAMP,
    attributes JSONB DEFAULT '{}'::jsonb
);


CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    is_default BOOLEAN DEFAULT FALSE,
    hierarchy_level INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

INSERT INTO roles (name, description, hierarchy_level) VALUES
('ROLE_SUPER_ADMIN', 'Administrador del sistema con acceso completo', 100),
('ROLE_CLIENT_ADMIN', 'Administrador de cliente', 80),
('ROLE_MODULE1_USER', 'Usuario con acceso al Módulo 1', 50),
('ROLE_MODULE2_USER', 'Usuario con acceso al Módulo 2', 50),
('ROLE_MODULE3_USER', 'Usuario con acceso al Módulo 3', 50),
('ROLE_AUDITOR', 'Auditor con permisos de evaluación', 60),
('ROLE_PROVIDER', 'Proveedor con acceso limitado', 40);

CREATE TABLE permissions (
    id SERIAL PRIMARY KEY,
    code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    category VARCHAR(50),
    service VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

INSERT INTO permissions (code, name, service, category) VALUES
('admin:client:create', 'Crear clientes', 'admin-service', 'client-management'),
('admin:client:read', 'Ver clientes', 'admin-service', 'client-management'),
('admin:client:update', 'Actualizar clientes', 'admin-service', 'client-management'),
('admin:client:delete', 'Eliminar clientes', 'admin-service', 'client-management'),
('document:folder:create', 'Crear carpetas', 'document-service', 'folder-management'),
('document:file:upload', 'Subir archivos', 'document-service', 'file-management'),
('provider:audit:approve', 'Aprobar auditorías', 'provider-service', 'audit-management');


CREATE TABLE user_roles (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by INTEGER REFERENCES users(id),
    valid_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valid_until TIMESTAMP,
    UNIQUE(user_id, role_id)
);

CREATE TABLE role_permissions (
    id SERIAL PRIMARY KEY,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id INTEGER NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by INTEGER REFERENCES users(id),
    is_deny BOOLEAN DEFAULT FALSE,
    condition_expr VARCHAR(255),
    UNIQUE(role_id, permission_id)
);


CREATE TABLE user_permissions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    permission_id INTEGER NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by INTEGER REFERENCES users(id),
    is_deny BOOLEAN DEFAULT FALSE,
    resource_id VARCHAR(100)
);

CREATE UNIQUE INDEX user_permission_resource_idx ON user_permissions (user_id, permission_id, COALESCE(resource_id, ''));


CREATE TABLE sessions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);


CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(100),
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tipo ENUM para tipos de usuario
CREATE TYPE user_type AS ENUM (
    'SUPER_ADMIN',
    'CLIENT_ADMIN',
    'WORKER',
    'AUDITOR',
    'PROVIDER'
);


-- Índices para búsquedas frecuentes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_user_type ON users(user_type);

-- Índices para relaciones
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_user_permissions_user_id ON user_permissions(user_id);
CREATE INDEX idx_sessions_user_id ON sessions(user_id);

-- Índices para búsquedas en logs
CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_entity_type ON audit_log(entity_type);



-- Procedimientos Almacenados y Funciones
CREATE OR REPLACE FUNCTION has_permission(
    p_user_id INTEGER,
    p_permission_code VARCHAR
) RETURNS BOOLEAN AS $$
DECLARE
    v_permission_id INTEGER;
    v_has_permission BOOLEAN := FALSE;
BEGIN
    -- Obtener ID del permiso
    SELECT id INTO v_permission_id 
    FROM permissions 
    WHERE code = p_permission_code AND is_active = TRUE;
    
    IF v_permission_id IS NULL THEN
        RETURN FALSE;
    END IF;
    
    -- Verificar si el usuario tiene el permiso directamente asignado (y no denegado)
    SELECT EXISTS (
        SELECT 1
        FROM user_permissions
        WHERE user_id = p_user_id
        AND permission_id = v_permission_id
        AND is_deny = FALSE
        AND (resource_id IS NULL)
    ) INTO v_has_permission;
    
    IF v_has_permission THEN
        RETURN TRUE;
    END IF;
    
    -- Verificar si el permiso está denegado explícitamente al usuario
    SELECT EXISTS (
        SELECT 1
        FROM user_permissions
        WHERE user_id = p_user_id
        AND permission_id = v_permission_id
        AND is_deny = TRUE
        AND (resource_id IS NULL)
    ) INTO v_has_permission;
    
    IF v_has_permission THEN
        RETURN FALSE;
    END IF;
    
    -- Verificar si el usuario tiene el permiso a través de algún rol
    SELECT EXISTS (
        SELECT 1
        FROM user_roles ur
        JOIN role_permissions rp ON ur.role_id = rp.role_id
        WHERE ur.user_id = p_user_id
        AND rp.permission_id = v_permission_id
        AND rp.is_deny = FALSE
        AND (ur.valid_until IS NULL OR ur.valid_until > CURRENT_TIMESTAMP)
    ) INTO v_has_permission;
    
    IF v_has_permission THEN
        RETURN TRUE;
    END IF;
    
    -- Verificar si el permiso está denegado explícitamente a través de algún rol
    SELECT EXISTS (
        SELECT 1
        FROM user_roles ur
        JOIN role_permissions rp ON ur.role_id = rp.role_id
        WHERE ur.user_id = p_user_id
        AND rp.permission_id = v_permission_id
        AND rp.is_deny = TRUE
        AND (ur.valid_until IS NULL OR ur.valid_until > CURRENT_TIMESTAMP)
    ) INTO v_has_permission;
    
    IF v_has_permission THEN
        RETURN FALSE;
    END IF;
    
    RETURN FALSE;
END;
$$ LANGUAGE plpgsql;


-- Verificación de Permisos sobre Recursos Específicos
CREATE OR REPLACE FUNCTION has_resource_permission(
    p_user_id INTEGER,
    p_permission_code VARCHAR,
    p_resource_id VARCHAR
) RETURNS BOOLEAN AS $$
DECLARE
    v_permission_id INTEGER;
    v_has_permission BOOLEAN := FALSE;
BEGIN
    -- Obtener ID del permiso
    SELECT id INTO v_permission_id 
    FROM permissions 
    WHERE code = p_permission_code AND is_active = TRUE;
    
    IF v_permission_id IS NULL THEN
        RETURN FALSE;
    END IF;
    
    -- Verificar si el usuario tiene el permiso directamente asignado para el recurso
    SELECT EXISTS (
        SELECT 1
        FROM user_permissions
        WHERE user_id = p_user_id
        AND permission_id = v_permission_id
        AND is_deny = FALSE
        AND resource_id = p_resource_id
    ) INTO v_has_permission;
    
    IF v_has_permission THEN
        RETURN TRUE;
    END IF;
    
    -- Verificar si el permiso está denegado explícitamente al usuario para el recurso
    SELECT EXISTS (
        SELECT 1
        FROM user_permissions
        WHERE user_id = p_user_id
        AND permission_id = v_permission_id
        AND is_deny = TRUE
        AND resource_id = p_resource_id
    ) INTO v_has_permission;
    
    IF v_has_permission THEN
        RETURN FALSE;
    END IF;
    
    -- Verificar el permiso global (sin recurso específico)
    RETURN has_permission(p_user_id, p_permission_code);
END;
$$ LANGUAGE plpgsql;



-- Función para Registrar Auditoría
CREATE OR REPLACE FUNCTION log_audit(
    p_user_id INTEGER,
    p_action VARCHAR,
    p_entity_type VARCHAR,
    p_entity_id VARCHAR,
    p_old_values JSONB,
    p_new_values JSONB,
    p_ip_address VARCHAR
) RETURNS VOID AS $$
BEGIN
    INSERT INTO audit_log (
        user_id,
        action,
        entity_type,
        entity_id,
        old_values,
        new_values,
        ip_address
    ) VALUES (
        p_user_id,
        p_action,
        p_entity_type,
        p_entity_id,
        p_old_values,
        p_new_values,
        p_ip_address
    );
END;
$$ LANGUAGE plpgsql;


-- Estructura de Tokens JWT
json
{
  "alg": "RS256",
  "typ": "JWT",
  "kid": "key-id-1"
}
Payload:
json
{
  "sub": "123",           // ID del usuario
  "name": "John Doe",     // Nombre del usuario
  "email": "jdoe@example.com",
  "roles": [              // Roles asignados
    "ROLE_CLIENT_ADMIN",
    "ROLE_MODULE1_USER"
  ],
  "permissions": [        // Permisos clave incluidos directamente
    "document:folder:create",
    "document:file:upload"
  ],
  "type": "CLIENT_ADMIN", // Tipo de usuario
  "client_id": "456",     // ID del cliente (si aplica)
  "iat": 1684123456,      // Issued At (tiempo de emisión)
  "exp": 1684127056,      // Expiration Time (tiempo de expiración)
  "iss": "identity-service", // Issuer (emisor)
  "jti": "abc123"         // JWT ID (identificador único)
}



