/*
<<<<<<< HEAD
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
=======
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
>>>>>>> 8218f769775ee0a146c7c1591dc01b12e8b2d643
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

CREATE TABLE IF NOT EXISTS workflow_package
(
    `id`                    VARCHAR(50)     NOT NULL,
    `site_id`               BIGINT(20)      NOT NULL,
    `label`                 VARCHAR(256)    NOT NULL,
    `status`                VARCHAR(20)     NOT NULL,
    `author`                BIGINT(20)      NULL,
    `reviewer`              BIGINT(20)      NULL,
    `schedule`              TIMESTAMP       NULL,
    `publishing_target`     VARCHAR(50)     NOT NULL,
    `author_comment`        TEXT            NULL,
    `reviewer_comment`      TEXT            NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY `workflow_package_ix_site`(`site_id`) REFERENCES `site` (`id`) ON DELETE CASCADE,
    FOREIGN KEY `workflow_package_ix_author`(`author`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY `workflow_package_ix_reviewer`(`reviewer`) REFERENCES `user` (`id`) ON DELETE CASCADE
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS workflow_package_item
(
    `id`                        BIGINT(20)          NOT NULL AUTO_INCREMENT,
    `workflow_package_id`       VARCHAR(50)         NOT NULL,
    `item_id`                   BIGINT(20)          NOT NULL,
    `commit_id`                 VARCHAR(128)        NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY `workflow_package_item_ix_package`(`workflow_package_id`) REFERENCES `workflow_package` (`id`) ON DELETE CASCADE,
    FOREING KEY `workflow_package_item_ix_item`(`item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS publishing_queue
(
    `id`                        BIGINT(20)          NOT NULL AUTO_INCREMENT,
    `workflow_package_id`       VARCHAR(50)         NULL,
    `site_id`                   BIGINT(20)          NOT NULL,
    `status`                    VARCHAR(50)         NOT NULL,
    `operation`                 VARCHAR(20)         NULL,
    `publishing_target`         VARCHAR(50)         NOT NULL,
    `schedule`                  TIMESTAMP           NOT NULL,
    `initiator`                 BIGINT(20)          NOT NULL,
    `publishing_comment`        TEXT                NULL,
    `completed_on`              TIMESTAMP           NULL,
    `number_of_retries`         INT                 NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    FOREIGN KEY `publishing_queue_ix_package`(`workflow_package_id`) REFERENCES `workflow_package` (`id`) ON DELETE CASCADE,
    FOREIGN KEY `publisging_queue_ix_site`(`site_id`) REFERENCES `site` (`id`) ON DELETE CASCADE
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS publishing_queue_parameters
(
    `id`                        BIGINT(20)          NOT NULL AUTO_INCREMENT,
    `publishing_queue_id`       BIGINT(20)          NOT NULL,
    `operation`                 VARCHAR(20)         NULL,
    `parameter_type`            VARCHAR(50)         NOT NULL,
    `parameter_value`           TEXT            NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY `publishing_queue_parameter_ix_queue`(`publishing_queue_id`) REFERENCES `publishing_queue` (`id`) ON DELETE CASCADE
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;

UPDATE _meta SET version = '4.0.0.40' ;
