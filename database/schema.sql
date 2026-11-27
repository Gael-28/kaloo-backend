-- Kaloo - PostgreSQL (Railway)
-- Puedes ejecutar esto en Railway Query o psql. El backend también crea estas tablas al iniciar.

CREATE TABLE IF NOT EXISTS establishments (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    address TEXT NOT NULL,
    phone TEXT NOT NULL DEFAULT '',
    email TEXT NOT NULL DEFAULT '',
    opening_hours TEXT NOT NULL DEFAULT '',
    description TEXT,
    manager_name TEXT NOT NULL DEFAULT '',
    manager_phone TEXT NOT NULL DEFAULT '',
    lost_and_found_note TEXT
);

CREATE TABLE IF NOT EXISTS user_streaks (
    username TEXT PRIMARY KEY,
    current_streak INT NOT NULL DEFAULT 0,
    longest_streak INT NOT NULL DEFAULT 0,
    total_check_ins INT NOT NULL DEFAULT 0,
    last_check_in DATE
);

CREATE TABLE IF NOT EXISTS lost_items (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    color TEXT,
    image_base64 TEXT,
    date_found DATE,
    status TEXT NOT NULL DEFAULT 'ENCONTRADO'
);

-- Datos iniciales (opcionales)
INSERT INTO establishments (name, address, phone, email, opening_hours, description, manager_name, manager_phone, lost_and_found_note)
SELECT 'Kaloo Gym', 'Calle Ejemplo 123', '555-0100', 'contacto@kaloo.example',
       'Lun a Vie 06:00-22:00, Sáb 08:00-14:00', 'Establecimiento deportivo',
       'Gerente Kaloo', '555-0111', 'Para checar objetos perdidos, llama al gerente en horario del establecimiento.'
WHERE NOT EXISTS (SELECT 1 FROM establishments);
