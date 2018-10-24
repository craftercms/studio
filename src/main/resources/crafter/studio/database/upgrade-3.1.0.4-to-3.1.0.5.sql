CREATE TABLE IF NOT EXISTS cluster
(
  `id`                        BIGINT(20)    NOT NULL AUTO_INCREMENT,
  `cluster_id`                VARCHAR(50)   NOT NULL,
  `cluster_member_name`       VARCHAR(50)   NOT NULL,
  `cluster_member_ip`         VARCHAR(50)   NOT NULL,
  `cluster_member_timestamp`  TIMESTAMP     NOT NULL,
  `remote_name`               VARCHAR(50)   NOT NULL,
  `remote_url`                VARCHAR(2000) NOT NULL,
  `authentication_type`       VARCHAR(16)   NOT NULL,
  `remote_username`           VARCHAR(255)  NULL,
  `remote_password`           VARCHAR(255)  NULL,
  `remote_token`              VARCHAR(255)  NULL,
  `remote_private_key`        TEXT          NULL,
  PRIMARY KEY (`id`),
  UNIQUE `uq_cl_cluster_member` (`cluster_id`, `cluster_member_name`),
  INDEX `cluster_cluster_id_idx` (`cluster_id` ASC),
  INDEX `cluster_cluster_member_idx` (`cluster_member_name` ASC)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

UPDATE _meta SET version = '3.1.0.5' ;