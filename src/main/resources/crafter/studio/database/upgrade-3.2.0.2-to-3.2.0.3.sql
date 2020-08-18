DROP PROCEDURE IF EXISTS update_parent_id ;

CREATE PROCEDURE update_parent_id(IN siteId VARCHAR(50), IN rootPath VARCHAR(2000))
BEGIN
    DECLARE v_parent_id BIGINT;
    DECLARE v_parent_path VARCHAR(2000);
    DECLARE v_parent_item_path VARCHAR(2000);
    DECLARE v_finished INTEGER DEFAULT 0;
    DECLARE parent_cursor CURSOR FOR SELECT i.id as parent_id, REPLACE(i.path, '/index.xml','') AS parent_path, i.path
        AS parent_item_path FROM item i WHERE i.site_id = siteId AND (path = rootPath OR path LIKE concat(rootPath, '/%'));
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_finished = 1;
    OPEN parent_cursor;
    update_parent: LOOP
        FETCH parent_cursor INTO v_parent_id, v_parent_path, v_parent_item_path;
        IF v_finished = 1 THEN LEAVE update_parent;
        END IF;
        UPDATE item SET parent_id = v_parent_id WHERE site_id = siteId
            AND path RLIKE (concat(v_parent_path, '/[^/]+/index\.xml|', v_parent_path,'/(?!index\.xml)[^/]+$'));
    END LOOP update_parent;
END ;

CREATE TABLE IF NOT EXISTS `item` (
                                      `id`                      BIGINT          NOT NULL    AUTO_INCREMENT,
                                      `record_last_updated`     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      `site_id`                 BIGINT          NOT NULL,
                                      `path`                    VARCHAR(2048)   NOT NULL,
                                      `preview_url`             VARCHAR(2048)   NOT NULL,
                                      `state`                   BIGINT          NOT NULL,
                                      `owned_by`                BIGINT          NULL,
                                      `created_by`              BIGINT          NOT NULL,
                                      `created_on`              TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
                                      `last_modified_by`        BIGINT             NOT NULL,
                                      `last_modified_on`        TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
                                      `label`                   VARCHAR(256)    NULL,
                                      `content_type_id`         VARCHAR(256)    NULL,
                                      `system_type`             VARCHAR(64)     NULL,
                                      `mime_type`               VARCHAR(64)     NULL,
                                      `disabled`                INT             NOT NULL    DEFAULT 0,
                                      `locale_code`             VARCHAR(16)     NULL,
                                      `translation_source_id`   BIGINT          NULL,
                                      `size`                    INT             NULL,
                                      `parent_id`               BIGINT          NULL,
                                      `commit_id`               VARCHAR(128)    NULL,
                                      PRIMARY KEY (`id`),
                                      FOREIGN KEY item_ix_created_by(`created_by`) REFERENCES `user` (`id`),
                                      FOREIGN KEY item_ix_last_modified_by(`last_modified_by`) REFERENCES `user` (`id`),
                                      FOREIGN KEY item_ix_owned_by(`owned_by`) REFERENCES `user` (`id`),
                                      FOREIGN KEY item_ix_site_id(`site_id`) REFERENCES `site` (`id`),
                                      FOREIGN KEY item_ix_parent(`parent_id`) REFERENCES `item` (`id`) ,
                                      UNIQUE uq_i_site_path (`site_id`, `path`(900))
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `item_translation` (
                                                  `id`                      BIGINT(20) NOT NULL AUTO_INCREMENT,
                                                  `record_last_updated`     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                  `source_id`               BIGINT(20)      NOT NULL,
                                                  `translation_id`          BIGINT(20)      NOT NULL,
                                                  `locale_code`             VARCHAR(16)     NOT NULL,
                                                  `date_translated`         TIMESTAMP       NOT NULL,
                                                  PRIMARY KEY (`id`),
                                                  FOREIGN KEY `item_translation_ix_source`(`source_id`) REFERENCES `item` (`id`),
                                                  FOREIGN KEY `item_translation_ix_translation`(`translation_id`) REFERENCES `item` (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;

UPDATE _meta SET version = '3.2.0.3' ;