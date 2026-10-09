ALTER TABLE favoritos ADD COLUMN lista_id INT;
ALTER TABLE favoritos ADD CONSTRAINT fk_lista_id FOREIGN KEY (lista_id) REFERENCES listas(id);
