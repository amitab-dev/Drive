ALTER TABLE files ADD file_key TEXT;
ALTER TABLE files RENAME COLUMN name TO file_name;