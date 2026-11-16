ALTER TABLE files ADD file_key TEXT;
ALTER TABLE files RENAME COLUMN name TO file_name;
ALTER TABLE files ADD CONSTRAINT unique_key UNIQUE (file_key);