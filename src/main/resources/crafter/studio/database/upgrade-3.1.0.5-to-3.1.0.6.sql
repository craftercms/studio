DROP TABLE IF EXISTS cluster ;

CREATE TABLE cluster
(
  `id`                  BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `git_url`             VARCHAR(500) NOT NULL,
  `git_auth_type`       VARCHAR(16)   NOT NULL,
  `git_username`        VARCHAR(255)  NULL,
  `git_password`        VARCHAR(255)  NULL,
  `git_token`           VARCHAR(255)  NULL,
  `git_private_key`     TEXT          NULL,
  PRIMARY KEY (`id`),
  UNIQUE `uq_cl_git_url` (`git_url`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

UPDATE _meta SET version = '3.1.0.6' ;