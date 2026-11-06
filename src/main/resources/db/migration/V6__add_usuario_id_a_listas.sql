-- Alinear tipos de id (listas.id y favoritos.lista_id) con Long (BIGINT) para Hibernate ddl-auto=validate
ALTER TABLE favoritos DROP CONSTRAINT fk_lista_id;
ALTER TABLE listas ALTER COLUMN id TYPE BIGINT;
ALTER TABLE favoritos ALTER COLUMN lista_id TYPE BIGINT;
ALTER TABLE favoritos
    ADD CONSTRAINT fk_lista_id
    FOREIGN KEY (lista_id) REFERENCES listas(id);

-- Asociar cada lista a un usuario dueño
ALTER TABLE listas ADD COLUMN usuario_id BIGINT;

ALTER TABLE listas
    ADD CONSTRAINT fk_listas_usuario_id
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id);

-- Backfill para listas preexistentes (ej. 'Sin clasificar' creada en V4)
INSERT INTO usuarios (email, password_hash, rol)
SELECT 'sistema@demo.local', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi6M2W8rV6H7h5mJ5Y6qZ8Y5K8v6W2e', 'ADMIN'
WHERE EXISTS (SELECT 1 FROM listas WHERE usuario_id IS NULL)
  AND NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'sistema@demo.local');

UPDATE listas
SET usuario_id = (SELECT id FROM usuarios ORDER BY id LIMIT 1)
WHERE usuario_id IS NULL;

ALTER TABLE listas ALTER COLUMN usuario_id SET NOT NULL;

