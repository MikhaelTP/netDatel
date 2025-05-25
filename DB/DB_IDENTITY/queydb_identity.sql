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

select * from roles;

UPDATE roles
SET Name = 'ROLE_SUPER_ADMIN'
Where Id = 1;

UPDATE roles
SET hierarchy_level = 100
Where Id = 1;

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





INSERT INTO permissions (code, name, service, category) VALUES
('admin:module:create', 'Crear modulos', 'admin-service', 'client-management'),
('admin:module:read', 'Ver modulos', 'admin-service', 'client-management'),
('admin:module:update', 'Actualizar modulos', 'admin-service', 'client-management'),
('admin:module:delete', 'Eliminar modulos', 'admin-service', 'client-management'),
('admin:client-module:create', 'Crear client-module', 'admin-service', 'client-management'),
('admin:client-module:read', 'Ver client-module', 'admin-service', 'client-management'),
('admin:client-module:update', 'Actualizar client-module', 'admin-service', 'client-management'),
('admin:client-module:delete', 'Eliminar client-module', 'admin-service', 'client-management'),
('admin:client-dashboard:read', 'Crear Dashboard', 'admin-service', 'client-management'),
('admin:notification:create', 'Crear Notificaciones', 'admin-service', 'client-management'),
('admin:notification:read', 'Ver Notificaciones', 'admin-service', 'client-management'),
('admin:notification:update', 'Actualizar Notificaciones', 'admin-service', 'client-management'),
('admin:notification:delete', 'Eliminar Notificaciones', 'admin-service', 'client-management'),
('admin:worker:create', 'Crear Trabajadores', 'admin-service', 'client-management'),
('admin:worker:read', 'Ver Trabajadores', 'admin-service', 'client-management'),
('admin:worker:delete', 'Eliminar Trabajadores', 'admin-service', 'client-management');






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

-- Scripts para simular registros
-- Script de inicialización para Identity Service
-- Ejecutar este script después de crear las tablas

-- 1. Insertar roles básicos del sistema
INSERT INTO roles (name, description, hierarchy_level, is_default, is_active) VALUES
('ROLE_SUPER_ADMIN', 'Administrador del sistema con acceso completo', 100, false, true),
('ROLE_CLIENT_ADMIN', 'Administrador de cliente', 80, false, true),
('ROLE_MODULE1_USER', 'Usuario con acceso al Módulo 1', 50, false, true),
('ROLE_MODULE2_USER', 'Usuario con acceso al Módulo 2', 50, false, true),
('ROLE_MODULE3_USER', 'Usuario con acceso al Módulo 3', 50, false, true),
('ROLE_AUDITOR', 'Auditor con permisos de evaluación', 60, false, true),
('ROLE_PROVIDER', 'Proveedor con acceso limitado', 40, false, true),
('ROLE_WORKER', 'Trabajador estándar', 30, true, true); -- Este es el rol por defecto


Select * from roles;
Select * from permissions;

-- 2. Insertar permisos básicos del sistema
-- Permisos de administración
INSERT INTO permissions (code, name, description, category, service, is_active) VALUES

-- Permisos de gestión de usuarios
('auth:user:create', 'Crear usuarios', 'Permite crear nuevos usuarios', 'user-management', 'identity-service', true),
('auth:user:read', 'Ver usuarios', 'Permite ver información de usuarios', 'user-management', 'identity-service', true),
('auth:user:update', 'Actualizar usuarios', 'Permite modificar información de usuarios', 'user-management', 'identity-service', true),
('auth:user:delete', 'Eliminar usuarios', 'Permite eliminar usuarios', 'user-management', 'identity-service', true),

-- Permisos de roles
('auth:role:create', 'Crear roles', 'Permite crear nuevos roles', 'role-management', 'identity-service', true),
('auth:role:read', 'Ver roles', 'Permite ver roles del sistema', 'role-management', 'identity-service', true),
('auth:role:update', 'Actualizar roles', 'Permite modificar roles', 'role-management', 'identity-service', true),
('auth:role:delete', 'Eliminar roles', 'Permite eliminar roles', 'role-management', 'identity-service', true),
('auth:role:assign', 'Asignar roles', 'Permite asignar roles a usuarios', 'role-management', 'identity-service', true),

-- Permisos de documentos
('document:folder:create', 'Crear carpetas', 'Permite crear carpetas en el sistema', 'folder-management', 'document-service', true),
('document:folder:read', 'Ver carpetas', 'Permite ver carpetas', 'folder-management', 'document-service', true),
('document:folder:update', 'Actualizar carpetas', 'Permite modificar carpetas', 'folder-management', 'document-service', true),
('document:folder:delete', 'Eliminar carpetas', 'Permite eliminar carpetas', 'folder-management', 'document-service', true),
('document:file:upload', 'Subir archivos', 'Permite subir archivos al sistema', 'file-management', 'document-service', true),
('document:file:download', 'Descargar archivos', 'Permite descargar archivos', 'file-management', 'document-service', true),
('document:file:delete', 'Eliminar archivos', 'Permite eliminar archivos', 'file-management', 'document-service', true),

-- Permisos de proveedores
('provider:provider:create', 'Crear proveedores', 'Permite registrar proveedores', 'provider-management', 'provider-service', true),
('provider:provider:read', 'Ver proveedores', 'Permite ver información de proveedores', 'provider-management', 'provider-service', true),
('provider:provider:update', 'Actualizar proveedores', 'Permite modificar proveedores', 'provider-management', 'provider-service', true),
('provider:audit:create', 'Crear auditorías', 'Permite crear auditorías', 'audit-management', 'provider-service', true),
('provider:audit:approve', 'Aprobar auditorías', 'Permite aprobar auditorías', 'audit-management', 'provider-service', true);

-- 3. Crear usuario Super Admin (Marlon)
-- Nota: La contraseña debe ser hasheada con BCrypt. Este es el hash de "Admin123!"
-- En producción, cambiar esta contraseña inmediatamente
Select * from users;
INSERT INTO users (
    username, 
    email, 
    password_hash, 
    user_type, 
    first_name, 
    last_name, 
    enabled, 
    account_non_locked, 
    created_at,
    attributes
) VALUES (
    'marlon.admin',
    'marlon@netdatel.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewKyNiGJJRi8rNp2', -- Admin123!
    'SUPER_ADMIN',
    'Marlon',
    'Administrator',
    true,
    true,
    CURRENT_TIMESTAMP,
    '{"department": "IT", "isFounder": true}'::jsonb
);

SELECT id, username, email, password_hash, enabled, account_non_locked 
FROM users 
WHERE username = 'marlon.admin';

-- Este es un hash BCrypt válido para "admin123"
UPDATE users 
SET password_hash = '$2a$12$LZ.jJUvsIJr7gKdX8vN5H.yVDrOuSXDqrKkWlZ4WjWKWr/OPE7qQm'
WHERE username = 'marlon.admin';

CREATE EXTENSION IF NOT EXISTS pgcrypto;

UPDATE users 
SET password_hash = crypt('admin123', gen_salt('bf', 10))
WHERE username = 'marlon.admin';


-- Verificar que se actualizó
SELECT *, username, password_hash FROM users WHERE username = 'marlon.admin';

-- Prueba de verificación (debería devolver true)
SELECT username, 
       password_hash,
       crypt('admin123', password_hash) = password_hash as password_matches
FROM users 
WHERE username = 'marlon.admin';

UPDATE users 
SET password_hash = '$2a$12$LZ.jJUvsIJr7gKdX8vN5H.yVDrOuSXDqrKkWlZ4WjWKWr/OPE7qQm'
WHERE username = 'client.admin';

UPDATE users 
SET password_hash = '$2a$12$LZ.jJUvsIJr7gKdX8vN5H.yVDrOuSXDqrKkWlZ4WjWKWr/OPE7qQm'
WHERE username = 'marlon.admin';

-- Primero verificar si ya tiene roles asignados
SELECT u.username, r.name as role_name
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'marlon.admin';

Select * from roles;

SELECT u.id, u.username, u.email, u.user_type, u.enabled, u.account_non_locked,
       r.name as role_name
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'marlon.admin';


-- Verifica que se actualizó
SELECT username, password_hash FROM users;

SELECT id, username, email, user_type, enabled, account_non_locked FROM users;

-- 4. Obtener el ID del usuario recién creado y del rol SUPER_ADMIN
-- Asignar rol SUPER_ADMIN al usuario Marlon
Select * from user_roles;
INSERT INTO user_roles (user_id, role_id, granted_at, granted_by)
SELECT 
    u.id,
    r.id,
    CURRENT_TIMESTAMP,
    u.id -- Se auto-asigna el rol
FROM users u
CROSS JOIN roles r
WHERE u.username = 'marlon.admin' 
AND r.name = 'ROLE_SUPER_ADMIN';

-- 5. Asignar todos los permisos al rol SUPER_ADMIN
INSERT INTO role_permissions (role_id, permission_id, granted_at, is_deny)
SELECT 
    r.id,
    p.id,
    CURRENT_TIMESTAMP,
    false
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ROLE_SUPER_ADMIN';

-- 6. Crear un usuario de prueba adicional (Client Admin)
INSERT INTO users (
    username, 
    email, 
    password_hash, 
    user_type, 
    first_name, 
    last_name, 
    enabled, 
    account_non_locked, 
    created_at,
    attributes
) VALUES (
    'client.adminofficial',
    'admin@cliente2.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewKyNiGJJRi8rNp2', -- Admin123!
    'CLIENT_ADMIN',
    'Admin',
    'Cliente',
    true,
    true,
    CURRENT_TIMESTAMP,
    '{"clientId": "1", "company": "Cliente Demo S.A."}'::jsonb
);

-- 7. Asignar rol CLIENT_ADMIN al usuario de prueba
INSERT INTO user_roles (user_id, role_id, granted_at, granted_by)
SELECT 
    u.id,
    r.id,
    CURRENT_TIMESTAMP,
    (SELECT id FROM users WHERE username = 'marlon.admin')
FROM users u
CROSS JOIN roles r
WHERE u.username = 'client.adminofficial' 
AND r.name = 'ROLE_CLIENT_ADMIN';

-- 8. Asignar permisos específicos al rol CLIENT_ADMIN
INSERT INTO role_permissions (role_id, permission_id, granted_at, granted_by, is_deny)
SELECT 
    r.id,
    p.id,
    CURRENT_TIMESTAMP,
    (SELECT id FROM users WHERE username = 'marlon.admin'),
    false
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ROLE_CLIENT_ADMIN'
AND p.code IN (
    'auth:user:create',
    'auth:user:read',
    'auth:user:update',
    'document:folder:create',
    'document:folder:read',
    'document:folder:update',
    'document:file:upload',
    'document:file:download'
);

-- 9. Crear registro inicial en audit_log
INSERT INTO audit_log (
    user_id,
    action,
    entity_type,
    entity_id,
    ip_address,
    timestamp
) VALUES (
    (SELECT id FROM users WHERE username = 'marlon.admin'),
    'SYSTEM_INIT',
    'SYSTEM',
    '1',
    '127.0.0.1',
    CURRENT_TIMESTAMP
);

-- Verificar que los datos se insertaron correctamente
SELECT 'Usuarios creados:' as info;
SELECT id, username, email, user_type FROM users;

SELECT 'Roles creados:' as info;
SELECT id, name, hierarchy_level FROM roles ORDER BY hierarchy_level DESC;

SELECT 'Permisos creados:' as info;
SELECT COUNT(*) as total_permisos FROM permissions;

SELECT 'Asignaciones de roles:' as info;
SELECT u.username, r.name as role 
FROM user_roles ur
JOIN users u ON ur.user_id = u.id
JOIN roles r ON ur.role_id = r.id;


-- 1. Verificar usuarios y sus roles
SELECT 
    u.id,
    u.username, 
    u.user_type,
    u.enabled,
    u.account_non_locked,
    r.name as role_name
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
ORDER BY u.username;

-- 2. Verificar que existen los roles básicos
SELECT * FROM roles ORDER BY hierarchy_level;
SELECT * FROM users
-- 3. Verificar permisos existentes
SELECT * FROM permissions ORDER BY category, code;

-- 1. Verificar el estado actual del usuario
SELECT 
    id,
    username, 
    enabled,
    account_non_locked,
    password_hash IS NOT NULL as has_password
FROM users 
WHERE username = 'marlon.admin';

-- 2. Habilitar al usuario
UPDATE users 
SET 
    enabled = true,
    account_non_locked = true
WHERE username = 'marlon.admin';

-- 3. Verificar que se actualizó correctamente
SELECT 
    username, 
    enabled,
    account_non_locked,
    user_type
FROM users 
WHERE username = 'marlon.admin';


-- Opción 1: Restaurar con hash conocido para 'admin123'
UPDATE users 
SET password_hash = '$2a$10$dXJ3SW6G7P9wuQEXk1lzDOAlp/qKjFp3E5mzUdx8JdE8PVWkv6g3K'
WHERE username = 'marlon.admin';

-- 4. Verificar qué permisos tiene cada rol
SELECT 
    r.name as role_name,
    p.code as permission_code,
    p.name as permission_name,
    p.category
FROM roles r
LEFT JOIN role_permissions rp ON r.id = rp.role_id
LEFT JOIN permissions p ON rp.permission_id = p.id
ORDER BY r.name, p.category, p.code;

-- Creando nuevo usuario
-- =====================================================
-- CREAR SUPER_ADMIN COMPLETO CON TODOS LOS PERMISOS
-- =====================================================

-- 1. Crear el usuario SUPER_ADMIN
-- Contraseña: admin123 (hash BCrypt)
INSERT INTO users (
    username, 
    email, 
    password_hash, 
    user_type, 
    first_name, 
    last_name, 
    enabled, 
    account_non_locked,
    created_at
) VALUES (
    'super.admin3', 
    'super.admin@netdatel33.com', 
    '$2a$10$dXJ3SW6G7P9wuQEXk1lzDOAlp/qKjFp3E5mzUdx8JdE8PVWkv6g3K', 
    'SUPER_ADMIN', 
    'Super', 
    'Administrator', 
    true, 
    true,
    CURRENT_TIMESTAMP
)

-- Actualizando password
UPDATE users 
SET password_hash = crypt('admin123', gen_salt('bf', 10))
WHERE username = 'super.admin3';

SELECT 
    r.id, 
    r.name, 
    r.description,
    COUNT(rp.permission_id) as permissions_count
FROM roles r
LEFT JOIN role_permissions rp ON r.id = rp.role_id
WHERE r.name = 'ROLE_SUPER_ADMIN'
GROUP BY r.id, r.name, r.description;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'ROLE_SUPER_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'super.admin34' AND r.name = 'ROLE_SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- 5. Verificación completa del nuevo usuario
SELECT '=== NUEVO USUARIO CREADO ===' as info;

SELECT 
    u.id,
    u.username,
    u.email,
    u.user_type,
    u.enabled,
    u.account_non_locked,
    u.created_at
FROM users u
WHERE u.username = 'super.admin';

SELECT '=== ROLES ASIGNADOS ===' as info;

select * from users;
select * from roles;

SELECT 
    u.username,
    r.name as role_name,
    r.description
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'super.admin';

SELECT '=== PERMISOS DISPONIBLES ===' as info;

SELECT 
    u.username,
    COUNT(DISTINCT p.id) as total_permissions,
    STRING_AGG(DISTINCT p.category, ', ') as categories
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
JOIN role_permissions rp ON r.id = rp.role_id
JOIN permissions p ON rp.permission_id = p.id
WHERE u.username = 'super.admin'
GROUP BY u.username;

SELECT '=== PERMISOS DETALLADOS ===' as info;

SELECT 
    p.category,
    p.code,
    p.name,
    p.service
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
JOIN role_permissions rp ON r.id = rp.role_id
JOIN permissions p ON rp.permission_id = p.id
WHERE u.username = 'super.admin'
ORDER BY p.category, p.code;

select * from users;

UPDATE users SET enabled = true, account_non_locked = true WHERE username = 'super.admin';