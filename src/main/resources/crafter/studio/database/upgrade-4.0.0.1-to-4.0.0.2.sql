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

CREATE TABLE IF NOT EXISTS `refresh_token`
(
    `user_id` BIGINT(20) PRIMARY KEY,
    `token` VARCHAR(50) NOT NULL,
    `last_updated_on` TIMESTAMP,
    `created_on` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY `refresh_token_ix_user_id` (`user_id`) REFERENCES `user` (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `access_token`
(
    `id`      BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT(20),
    `label`   VARCHAR(2550) NOT NULL,
    `enabled` BOOLEAN DEFAULT true,
    `last_updated_on` TIMESTAMP,
    `created_on` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `expires_at` TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY `access_token_ix_user_id` (`user_id`) REFERENCES `user` (`id`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;

UPDATE _meta SET version = '4.0.0.2' ;
