DROP PROCEDURE IF EXISTS addColumnIfNotExists ;

CREATE PROCEDURE addColumnIfNotExists(
    IN schemaName tinytext,
    IN tableName tinytext,
    IN columnName tinytext,
    IN columnDefinition text)
  BEGIN
    IF NOT EXISTS (
            SELECT * FROM information_schema.COLUMNS
            WHERE column_name = columnName
              AND table_name = tableName
              AND table_schema = schemaName
        )
    THEN
        SET @addColumn=CONCAT('ALTER TABLE ', schemaName, '.', tableName,
                              ' ADD COLUMN ', columnName, ' ', columnDefinition);
        PREPARE statement FROM @addColumn;
        EXECUTE statement;
    END IF;
  END ;

DROP PROCEDURE IF EXISTS dropColumnIfExists ;

CREATE PROCEDURE dropColumnIfExists(
    IN schemaName tinytext,
    IN tableName tinytext,
    IN columnName tinytext)
  BEGIN
    IF EXISTS (
            SELECT * FROM information_schema.COLUMNS
            WHERE column_name = columnName
              AND table_name = tableName
              AND table_schema = schemaName
        )
    THEN
        SET @dropColumn=CONCAT('ALTER TABLE ', schemaName, '.', tableName,
                              ' DROP COLUMN ', columnName);
        PREPARE statement FROM @dropColumn;
        EXECUTE statement;
    END IF;
  END ;

call dropColumnIfExists(@crafter_schema_name, 'gitlog', 'verified') ;

call dropColumnIfExists(@crafter_schema_name, 'gitlog', 'commit_date') ;

call addColumnIfNotExists(@crafter_schema_name, 'gitlog', 'last_verified_gitlog_commit_id', 'VARCHAR(50) NULL') ;

UPDATE _meta SET version = '3.0.2.1' ;