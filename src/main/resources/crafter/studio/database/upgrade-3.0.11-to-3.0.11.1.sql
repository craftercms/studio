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

call dropColumnIfExists('crafter', 'remote_repository', 'remote_branch') ;

UPDATE `audit` SET `source` = 'API' WHERE `source` = 'UI' ;

UPDATE _meta SET version = '3.0.11.1' ;