-- Tipos ENUM
CREATE TYPE file_status AS ENUM ('ACTIVE', 'DELETED', 'ARCHIVED');
CREATE TYPE view_status AS ENUM ('NEW', 'VIEWED', 'DOWNLOADED', 'NOT_DOWNLOADED');
CREATE TYPE view_color AS ENUM ('BLUE', 'AMBER', 'GREEN', 'RED');
CREATE TYPE action_type AS ENUM ('VIEW', 'DOWNLOAD', 'CREATE', 'UPDATE', 'DELETE');
CREATE TYPE download_status AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED');


CREATE TABLE client_spaces (
    id SERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL,
    module_id INTEGER NOT NULL,
    storage_path VARCHAR(255) NOT NULL,
    total_quota_bytes BIGINT NOT NULL,
    used_bytes BIGINT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by INTEGER NOT NULL,
    updated_by INTEGER,
    UNIQUE(client_id, module_id)
);

CREATE TABLE folders (
    id SERIAL PRIMARY KEY,
    client_space_id INTEGER NOT NULL REFERENCES client_spaces(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_id INTEGER REFERENCES folders(id) ON DELETE CASCADE,
    path VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER NOT NULL,
    updated_at TIMESTAMP,
    updated_by INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    attributes JSONB DEFAULT '{}'::jsonb,
    UNIQUE(client_space_id, parent_id, name)
);

CREATE TABLE files (
    id SERIAL PRIMARY KEY,
    folder_id INTEGER NOT NULL REFERENCES folders(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(127) NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    hash_value VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'DELETED', 'ARCHIVED')),
    view_status VARCHAR(20) DEFAULT 'NEW' CHECK (view_status IN ('NEW', 'VIEWED', 'DOWNLOADED', 'NOT_DOWNLOADED')),
    view_status_color VARCHAR(20) DEFAULT 'BLUE' CHECK (view_status_color IN ('BLUE', 'AMBER', 'GREEN', 'RED')),
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_viewed_date TIMESTAMP,
    last_downloaded_date TIMESTAMP,
    uploaded_by INTEGER NOT NULL,
    version INTEGER DEFAULT 1,
    metadata JSONB DEFAULT '{}'::jsonb,
    UNIQUE(folder_id, name)
);

CREATE TABLE file_versions (
    id SERIAL PRIMARY KEY,
    file_id INTEGER NOT NULL REFERENCES files(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    hash_value VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER NOT NULL,
    change_comments TEXT,
    UNIQUE(file_id, version_number)
);

CREATE TABLE folder_permissions (
    id SERIAL PRIMARY KEY,
    folder_id INTEGER NOT NULL REFERENCES folders(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL,
    can_read BOOLEAN DEFAULT FALSE,
    can_write BOOLEAN DEFAULT FALSE,
    can_delete BOOLEAN DEFAULT FALSE,
    can_download BOOLEAN DEFAULT FALSE,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by INTEGER NOT NULL,
    valid_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valid_until TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    UNIQUE(folder_id, user_id)
);

CREATE TABLE file_permissions (
    id SERIAL PRIMARY KEY,
    file_id INTEGER NOT NULL REFERENCES files(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL,
    can_read BOOLEAN DEFAULT FALSE,
    can_write BOOLEAN DEFAULT FALSE,
    can_delete BOOLEAN DEFAULT FALSE,
    can_download BOOLEAN DEFAULT FALSE,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by INTEGER NOT NULL,
    valid_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valid_until TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    UNIQUE(file_id, user_id)
);


CREATE TABLE file_comments (
    id SERIAL PRIMARY KEY,
    file_id INTEGER NOT NULL REFERENCES files(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    parent_comment_id INTEGER REFERENCES file_comments(id) ON DELETE CASCADE,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE file_access_history (
    id SERIAL PRIMARY KEY,
    file_id INTEGER REFERENCES files(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL,
    action_type VARCHAR(50) NOT NULL CHECK (action_type IN ('VIEW', 'DOWNLOAD', 'CREATE', 'UPDATE', 'DELETE')),
    action_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    device_info VARCHAR(255),
    additional_info JSONB
);


CREATE TABLE batch_downloads (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    folder_id INTEGER NOT NULL REFERENCES folders(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    total_files INTEGER,
    processed_files INTEGER DEFAULT 0,
    download_url VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    expiration_time TIMESTAMP,
    file_size_bytes BIGINT,
    include_subfolders BOOLEAN DEFAULT TRUE,
    error_message TEXT
);

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Índices para búsquedas frecuentes
CREATE INDEX idx_client_spaces_client_id ON client_spaces(client_id);
CREATE INDEX idx_client_spaces_module_id ON client_spaces(module_id);

CREATE INDEX idx_folders_client_space_id ON folders(client_space_id);
CREATE INDEX idx_folders_parent_id ON folders(parent_id);
CREATE INDEX idx_folders_path ON folders(path);
CREATE INDEX idx_folders_path_gin ON folders USING gin(path gin_trgm_ops);

CREATE INDEX idx_files_folder_id ON files(folder_id);
CREATE INDEX idx_files_name ON files(name);
CREATE INDEX idx_files_view_status ON files(view_status);
CREATE INDEX idx_files_uploaded_by ON files(uploaded_by);
CREATE INDEX idx_files_mime_type ON files(mime_type);
CREATE INDEX idx_files_upload_date ON files(upload_date);

CREATE INDEX idx_file_versions_file_id ON file_versions(file_id);

CREATE INDEX idx_folder_permissions_folder_id ON folder_permissions(folder_id);
CREATE INDEX idx_folder_permissions_user_id ON folder_permissions(user_id);

CREATE INDEX idx_file_permissions_file_id ON file_permissions(file_id);
CREATE INDEX idx_file_permissions_user_id ON file_permissions(user_id);

CREATE INDEX idx_file_comments_file_id ON file_comments(file_id);
CREATE INDEX idx_file_comments_user_id ON file_comments(user_id);
CREATE INDEX idx_file_comments_parent_comment_id ON file_comments(parent_comment_id);

CREATE INDEX idx_file_access_history_file_id ON file_access_history(file_id);
CREATE INDEX idx_file_access_history_user_id ON file_access_history(user_id);
CREATE INDEX idx_file_access_history_action_date ON file_access_history(action_date);
CREATE INDEX idx_file_access_history_action_type ON file_access_history(action_type);

CREATE INDEX idx_batch_downloads_user_id ON batch_downloads(user_id);
CREATE INDEX idx_batch_downloads_folder_id ON batch_downloads(folder_id);
CREATE INDEX idx_batch_downloads_status ON batch_downloads(status);

-- Índices para búsqueda de texto
CREATE INDEX idx_files_name_trgm ON files USING gin(name gin_trgm_ops);
CREATE INDEX idx_files_metadata_gin ON files USING gin(metadata jsonb_path_ops);

-- Funcion para Calcular Tamaño de Carpeta

CREATE OR REPLACE FUNCTION calculate_folder_size(p_folder_id INTEGER)
RETURNS BIGINT AS $$
DECLARE
    v_total_size BIGINT := 0;
    v_subfolder_id INTEGER;
BEGIN
    -- Calcular tamaño de archivos directos
    SELECT COALESCE(SUM(file_size), 0)
    INTO v_total_size
    FROM files
    WHERE folder_id = p_folder_id
    AND status = 'ACTIVE';
    
    -- Calcular tamaño de subcarpetas recursivamente
    FOR v_subfolder_id IN
        SELECT id FROM folders WHERE parent_id = p_folder_id
    LOOP
        v_total_size := v_total_size + calculate_folder_size(v_subfolder_id);
    END LOOP;
    
    RETURN v_total_size;
END;
$$ LANGUAGE plpgsql;

-- Función para comprobar Permisos
CREATE OR REPLACE FUNCTION has_file_permission(
    p_user_id INTEGER,
    p_file_id INTEGER,
    p_permission VARCHAR
) RETURNS BOOLEAN AS $$
DECLARE
    v_folder_id INTEGER;
    v_has_permission BOOLEAN := FALSE;
    v_permission_field VARCHAR;
BEGIN

-- Verificar permiso directo sobre el archivo
   v_permission_field := 'can_' || lower(p_permission);
   
   EXECUTE format('
       SELECT EXISTS (
           SELECT 1
           FROM document.file_permissions
           WHERE file_id = %L
           AND user_id = %L
           AND %I = TRUE
           AND is_active = TRUE
           AND (valid_until IS NULL OR valid_until > CURRENT_TIMESTAMP)
       )', p_file_id, p_user_id, v_permission_field) INTO v_has_permission;
   
   IF v_has_permission THEN
       RETURN TRUE;
   END IF;
   
   -- Obtener el folder_id del archivo
   SELECT folder_id 
   INTO v_folder_id 
   FROM files 
   WHERE id = p_file_id;
   
   -- Verificar si tiene permiso en la carpeta
   RETURN has_folder_permission(p_user_id, v_folder_id, p_permission);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION has_folder_permission(
   p_user_id INTEGER,
   p_folder_id INTEGER,
   p_permission VARCHAR
) RETURNS BOOLEAN AS $$
DECLARE
   v_parent_id INTEGER;
   v_has_permission BOOLEAN := FALSE;
   v_permission_field VARCHAR;
BEGIN
   -- Verificar permiso directo sobre la carpeta
   v_permission_field := 'can_' || lower(p_permission);
   
   EXECUTE format('
       SELECT EXISTS (
           SELECT 1
           FROM document.folder_permissions
           WHERE folder_id = %L
           AND user_id = %L
           AND %I = TRUE
           AND is_active = TRUE
           AND (valid_until IS NULL OR valid_until > CURRENT_TIMESTAMP)
       )', p_folder_id, p_user_id, v_permission_field) INTO v_has_permission;
   
   IF v_has_permission THEN
       RETURN TRUE;
   END IF;
   
   -- Verificar si tiene permiso en la carpeta padre (recursivamente)
   SELECT parent_id 
   INTO v_parent_id 
   FROM olders 
   WHERE id = p_folder_id;
   
   IF v_parent_id IS NOT NULL THEN
       RETURN has_folder_permission(p_user_id, v_parent_id, p_permission);
   END IF;
   
   RETURN FALSE;
END;
$$ LANGUAGE plpgsql;


-- Desactivar el trigger existente
-- DROP TRIGGER IF EXISTS trg_update_file_view_status ON file_access_history;

-- Crear la función corregida
CREATE OR REPLACE FUNCTION update_file_view_status() 
RETURNS TRIGGER AS $$
BEGIN
    -- Si es una acción de visualización
    IF NEW.action_type = 'VIEW' THEN
        
        UPDATE files
        SET view_status = 'VIEWED',
            view_status_color = 'AMBER',
            last_viewed_date = CURRENT_TIMESTAMP
        WHERE id = NEW.file_id
          AND (last_viewed_date IS NULL OR view_status = 'NEW');
        
    -- Si es una acción de descarga
    ELSIF NEW.action_type = 'DOWNLOAD' THEN
        
        UPDATE files
        SET view_status = 'DOWNLOADED',
            view_status_color = 'GREEN',
            last_downloaded_date = CURRENT_TIMESTAMP
        WHERE id = NEW.file_id;
        
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Recrear el trigger
CREATE TRIGGER trg_update_file_view_status
AFTER INSERT ON file_access_history
FOR EACH ROW
EXECUTE FUNCTION update_file_view_status();

-- Función para Descargas Masivas
CREATE OR REPLACE FUNCTION initiate_batch_download(
    p_user_id INTEGER,
    p_folder_id INTEGER,
    p_include_subfolders BOOLEAN DEFAULT TRUE
) RETURNS INTEGER AS $$
DECLARE
    v_batch_id INTEGER;
    v_total_files INTEGER;
BEGIN
    -- Contar archivos a descargar
    IF p_include_subfolders THEN
        WITH RECURSIVE folder_tree AS (
            SELECT id FROM folders WHERE id = p_folder_id
            UNION ALL
            SELECT f.id FROM folders f
            JOIN folder_tree ft ON f.parent_id = ft.id
        )
        SELECT COUNT(*) INTO v_total_files
        FROM files
        WHERE folder_id IN (SELECT id FROM folder_tree)
        AND status = 'ACTIVE';
    ELSE
        SELECT COUNT(*) INTO v_total_files
        FROM files
        WHERE folder_id = p_folder_id
        AND status = 'ACTIVE';
    END IF;
    
    -- Crear registro de descarga masiva
    INSERT INTO batch_downloads (
        user_id,
        folder_id,
        status,
        total_files,
        include_subfolders,
        expiration_time
    ) VALUES (
        p_user_id,
        p_folder_id,
        'PENDING',
        v_total_files,
        p_include_subfolders,
        CURRENT_TIMESTAMP + INTERVAL '24 hours'
    ) RETURNING id INTO v_batch_id;
    
    -- Actualizar estado de archivos no descargados
    IF p_include_subfolders THEN
        WITH RECURSIVE folder_tree AS (
            SELECT id FROM folders WHERE id = p_folder_id
            UNION ALL
            SELECT f.id FROM folders f
            JOIN folder_tree ft ON f.parent_id = ft.id
        )
        UPDATE files
        SET view_status = 'NOT_DOWNLOADED',
            view_status_color = 'RED'
        WHERE folder_id IN (SELECT id FROM folder_tree)
        AND status = 'ACTIVE'
        AND view_status <> 'DOWNLOADED';
    ELSE
        UPDATE files
        SET view_status = 'NOT_DOWNLOADED',
            view_status_color = 'RED'
        WHERE folder_id = p_folder_id
        AND status = 'ACTIVE'
        AND view_status <> 'DOWNLOADED';
    END IF;
    
    RETURN v_batch_id;
END;
$$ LANGUAGE plpgsql;

--  Función para Actualizar Espacio Utilizado
CREATE OR REPLACE FUNCTION update_client_space_usage(p_client_space_id INTEGER)
RETURNS VOID AS $$
DECLARE
    v_used_bytes BIGINT := 0;
BEGIN
    -- Calcular uso total de espacio
    WITH RECURSIVE folder_tree AS (
        SELECT id FROM folders WHERE client_space_id = p_client_space_id
    )
    SELECT COALESCE(SUM(file_size), 0)
    INTO v_used_bytes
    FROM files
    WHERE folder_id IN (SELECT id FROM folder_tree)
    AND status = 'ACTIVE';
    
    -- Actualizar el espacio utilizado
    UPDATE client_spaces
    SET used_bytes = v_used_bytes,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_client_space_id;
END;
$$ LANGUAGE plpgsql;

-- Trigger para actualizar automáticamente el espacio utilizado
CREATE OR REPLACE FUNCTION trigger_update_space_usage()
RETURNS TRIGGER AS $$
DECLARE
    v_client_space_id INTEGER;
BEGIN
    -- Obtener el client_space_id
    SELECT client_space_id INTO v_client_space_id
    FROM folders
    WHERE id = COALESCE(NEW.folder_id, OLD.folder_id);
    
    -- Actualizar el espacio utilizado
    PERFORM update_client_space_usage(v_client_space_id);
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_files_space_usage
AFTER INSERT OR UPDATE OR DELETE ON files
FOR EACH STATEMENT
EXECUTE FUNCTION trigger_update_space_usage();



-- Consultas para ver registros
SELECT * FROM client_spaces ORDER BY created_at DESC;
SELECT * FROM folders ORDER BY created_at DESC;
SELECT * FROM files ORDER BY Id DESC;
SELECT * FROM file_access_history ORDER BY Id DESC;    -- file_access_history


