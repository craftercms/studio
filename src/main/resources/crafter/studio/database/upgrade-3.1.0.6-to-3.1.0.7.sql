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

call addColumnIfNotExists('crafter', 'cluster', 'git_remote_name', 'VARCHAR(50) NOT NULL') ;

call addColumnIfNotExists('crafter', 'cluster', 'local_ip', 'VARCHAR(40) NOT NULL') ;

call addColumnIfNotExists('crafter', 'cluster', 'state', 'VARCHAR(50) NOT NULL') ;

DROP PROCEDURE IF EXISTS addUniqueIfNotExists ;

CREATE PROCEDURE addUniqueIfNotExists(
    IN schemaName tinytext,
    IN tableName tinytext,
    IN uniqueName tinytext,
    IN uniqueDefinition text)
BEGIN
    IF NOT EXISTS (
            SELECT * FROM information_schema.STATISTICS
            WHERE index_name = uniqueName
              AND table_name = tableName
              AND table_schema = schemaName
        )
    THEN
        SET @addUnique=CONCAT('ALTER TABLE ', schemaName, '.', tableName,
                              ' ADD UNIQUE ', uniqueName, ' ', uniqueDefinition);
        PREPARE statement FROM @addUnique;
        EXECUTE statement;
    END IF;
END ;

call addUniqueIfNotExists('crafter', 'cluster', 'uq_cl_git_remote_name', '(`git_remote_name`)') ;

UPDATE _meta SET version = '3.1.0.7' ;