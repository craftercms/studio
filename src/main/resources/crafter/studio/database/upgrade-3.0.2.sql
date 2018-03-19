ALTER TABLE `gitlog` DROP COLUMN `verified` ;

ALTER TABLE `gitlog` DROP COLUMN `commit_date` ;

ALTER TABLE `site` ADD COLUMN `last_verified_gitlog_commit_id` VARCHAR(50) NULL ;

CREATE TABLE IF NOT EXISTS site_remote
(
  `id`                    BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `site_id`               VARCHAR(50)   NOT NULL,
  `remote_name`           VARCHAR(255)   NOT NULL,
  `remote_url`            VARCHAR(255)   NOT NULL,
  `authentication_type`   VARCHAR(255)   NOT NULL,
  `remote_username`       VARCHAR(255)   NULL,
  `remote_password`       VARCHAR(255)   NULL,
  `remote_token`          VARCHAR(255)   NULL,
  `remote_private_key`    TEXT           NULL,
  `salt`                  VARCHAR(255)   NULL,
  PRIMARY KEY (`id`),
  INDEX `siteremote_site_idx` (`site_id` ASC)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

UPDATE _meta SET version = '3.0.10' ;