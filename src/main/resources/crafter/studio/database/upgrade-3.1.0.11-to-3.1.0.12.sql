DROP PROCEDURE IF EXISTS dropIndexIfExists ;

CREATE PROCEDURE dropIndexIfExists(
    IN schemaName tinytext,
    IN tableName tinytext,
    IN indexName tinytext)
BEGIN
    IF EXISTS (
            SELECT * FROM information_schema.STATISTICS
            WHERE index_name = indexName
              AND table_name = tableName
              AND table_schema = schemaName
        )
    THEN
        SET @dropIndex=CONCAT('ALTER TABLE ', schemaName, '.', tableName,
                               ' DROP INDEX ', indexName);
        PREPARE statement FROM @dropIndex;
        EXECUTE statement;
    END IF;
END ;

call dropIndexIfExists('crafter', 'site', 'site_id_unique') ;

call dropIndexIfExists('crafter', 'site', 'site_id_idx') ;

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

call addColumnIfNotExists('crafter', 'site', 'site_uuid', 'VARCHAR(50) NOT NULL') ;

call addColumnIfNotExists('crafter', 'site', 'deleted', 'INT NOT NULL DEFAULT 0') ;

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

call addUniqueIfNotExists('crafter', 'site', 'site_id_site_uuid_unique', '(`site_id` ASC, `site_uuid` ASC)') ;

DROP PROCEDURE IF EXISTS addIndexIfNotExists ;

CREATE PROCEDURE addIndexIfNotExists(
    IN schemaName tinytext,
    IN tableName tinytext,
    IN indexName tinytext,
    IN indexDefinition text)
BEGIN
    IF NOT EXISTS (
            SELECT * FROM information_schema.STATISTICS
            WHERE index_name = indexName
              AND table_name = tableName
              AND table_schema = schemaName
        )
    THEN
        SET @addIndex=CONCAT('ALTER TABLE ', schemaName, '.', tableName,
                              ' ADD INDEX ', indexName, ' ', indexDefinition);
        PREPARE statement FROM @addIndex;
        EXECUTE statement;
    END IF;
END ;

call addIndexIfNotExists('crafter', 'site', 'site_id_idx', '(`site_id` ASC)') ;

UPDATE `site` SET site_uuid = UUID() WHERE site_uuid IS NULL OR site_uuid = '' ;

UPDATE _meta SET version = '3.1.0.12' ;