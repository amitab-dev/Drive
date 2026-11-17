ALTER TABLE files ADD file_key TEXT;
ALTER TABLE files RENAME COLUMN name TO file_name;
ALTER TABLE files ADD CONSTRAINT unique_key UNIQUE (file_key);
ALTER TABLE files ADD COLUMN parent_id UUID NULL;

-- Self referencing fk

ALTER TABLE files
    ADD CONSTRAINT fk_file_parent
    FOREIGN KEY (parent_id) REFERENCES files (id)
    ON DELETE RESTRICT;

CREATE INDEX idx_files_owner_parent
    ON files (owner, parent_id);

ALTER TABLE files ALTER COLUMN file_key  DROP NOT NULL;
ALTER TABLE files ALTER COLUMN file_size DROP NOT NULL;
ALTER TABLE files ALTER COLUMN mime_type DROP NOT NULL;