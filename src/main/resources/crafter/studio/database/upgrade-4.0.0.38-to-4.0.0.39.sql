/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

CREATE TABLE IF NOT EXISTS `activity_stream` (
    `id`                        BIGINT(20)      NOT NULL AUTO_INCREMENT,
    `site_id`                   BIGINT(20)      NOT NULL,
    `user_id`                   BIGINT(20)      NOT NULL,
    `action`                    VARCHAR(32)     NOT NULL,
    `action_timestamp`          TIMESTAMP       NOT NULL,
    `item_id`                   BIGINT(20)      NULL,
    `package_id`                VARCHAR(50)     NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY `activity_user_idx` (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY `activity_site_idx` (`site_id`) REFERENCES `site`(`id`),
    INDEX `activity_action_idx` (`action` ASC)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;

call addColumnIfNotExists('crafter', 'user', 'avatar', 'TEXT NULL') ;

call addColumnIfNotExists('crafter', 'workflow', 'label', 'VARCHAR(256) NULL') ;

call addColumnIfNotExists('crafter', 'publish_request', 'label', 'VARCHAR(256) NULL') ;

UPDATE _meta SET version = '4.0.0.39' ;
