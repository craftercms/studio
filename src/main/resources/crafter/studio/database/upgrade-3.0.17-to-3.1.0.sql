ALTER TABLE `group`
  DROP FOREIGN KEY `group_site_fk`,
  DROP INDEX `uq_group_name_siteid` ;

ALTER TABLE `group_user`
  DROP PRIMARY KEY,
  DROP FOREIGN KEY user_ug_foreign_key,
  DROP FOREIGN KEY group_ug_foreign_key,
  DROP COLUMN `id` ;

ALTER TABLE `user`
  DROP PRIMARY KEY ;

ALTER TABLE `user`
  ADD COLUMN `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  ADD COLUMN `record_last_updated` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CHANGE COLUMN `username` `username` VARCHAR(255),
  CHANGE COLUMN `password` `password` VARCHAR(128),
  CHANGE COLUMN `firstname` `first_name` VARCHAR(32),
  CHANGE COLUMN `lastname` `last_name` VARCHAR(32),
  ADD COLUMN `timezone` VARCHAR(16) NULL,
  ADD COLUMN `locale` VARCHAR(16) NULL,
  ADD PRIMARY KEY (`id`),
  ADD INDEX `user_ix_record_last_updated` (`record_last_updated` DESC),
  ADD UNIQUE INDEX `user_ix_username` (`username`),
  ADD INDEX `user_ix_first_name` (`first_name`),
  ADD INDEX `user_ix_last_name` (`last_name`) ;

CREATE TABLE IF NOT EXISTS `organization`
(
  `id`                  BIGINT(20)  NOT NULL AUTO_INCREMENT,
  `record_last_updated` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `org_name`            VARCHAR(32) NOT NULL,
  `org_desc`            TEXT        NULL,
  PRIMARY KEY (`id`),
  INDEX `organization_ix_record_last_updated` (`record_last_updated` DESC),
  UNIQUE INDEX `organization_ix_org_name` (`org_name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

INSERT IGNORE INTO `organization` (id, record_last_updated, org_name, org_desc)
VALUES (1, CURRENT_TIMESTAMP, 'studio', 'studio default organization') ;

CREATE TABLE IF NOT EXISTS `organization_user`
(
  `user_id`   BIGINT(20) NOT NULL,
  `org_id`    BIGINT(20) NOT NULL,
  `record_last_updated` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `org_id`),
  FOREIGN KEY org_member_ix_user_id(user_id) REFERENCES `user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY org_member_ix_org_id(org_id) REFERENCES `organization` (`id`) ON DELETE CASCADE,
  INDEX `org_member_ix_record_last_updated` (`record_last_updated` DESC)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

INSERT IGNORE INTO `organization_user` (user_id, org_id)
SELECT `id`, 1
FROM `user` ;

DROP PROCEDURE IF EXISTS migrate_groups ;

CREATE PROCEDURE migrate_groups()
  BEGIN
    DECLARE v_group_id BIGINT;
    DECLARE v_finished INTEGER DEFAULT 0;
    DECLARE group_cursor CURSOR FOR SELECT `group`.id FROM `group` inner JOIN site ON `group`.site_id = site.id WHERE site.system = 0;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_finished = 1;
    OPEN group_cursor;
    update_group_name: LOOP
      FETCH group_cursor INTO v_group_id;
      IF v_finished = 1 THEN LEAVE update_group_name;
      END IF;
      UPDATE `group` LEFT JOIN `site` ON `group`.site_id = `site`.id SET `group`.name = TRIM(CONCAT(CONCAT(TRIM(`site`.name), '_'), TRIM(`group`.name))) WHERE `group`.id = v_group_id;
    end loop update_group_name;
  end ;

CALL migrate_groups() ;

ALTER TABLE `group`
  ADD COLUMN `record_last_updated` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD COLUMN `org_id` BIGINT(20) NOT NULL DEFAULT 1,
  CHANGE COLUMN `name` `group_name` VARCHAR(32),
  CHANGE COLUMN `description` `group_description` TEXT,
  DROP COLUMN `site_id`,
  DROP COlUMN `externally_managed`,
  ADD INDEX `group_ix_record_last_updated` (`record_last_updated` DESC),
  ADD FOREIGN KEY group_ix_org_id(org_id) REFERENCES `organization` (`id`) ON DELETE CASCADE,
  ADD INDEX `group_ix_group_name` (`group_name`) ;

DROP PROCEDURE IF EXISTS migrate_groups ;

ALTER TABLE `group_user`
  ADD COLUMN `user_id` BIGINT(20) NOT NULL ;

UPDATE `group_user` gu
LEFT JOIN `user` u on gu.username = u.username
SET gu.user_id = u.id ;

ALTER TABLE `group_user`
  DROP COLUMN `username`,
  CHANGE COLUMN `groupid` `group_id` BIGINT(20) NOT NULL,
  ADD COLUMN `record_last_updated` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD PRIMARY KEY (`user_id`, `group_id`),
  ADD FOREIGN KEY group_member_ix_user_id(`user_id`) REFERENCES `user` (`id`)
    ON DELETE CASCADE,
  ADD FOREIGN KEY group_member_ix_group_id(`group_id`) REFERENCES `group` (`id`)
    ON DELETE CASCADE,
  ADD INDEX `group_member_ix_record_last_updated` (`record_last_updated` DESC) ;

UPDATE _meta SET version = '3.1.0' ;