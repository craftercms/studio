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

call addColumnIfNotExists('crafter', 'item', 'previous_path', 'VARCHAR(2048) NULL') ;

CREATE TABLE IF NOT EXISTS workflow
(
    `id`                    BIGINT(20)      NOT NULL AUTO_INCREMENT,
    `item_id`               BIGINT(20)      NOT NULL,
    `target_environment`    VARCHAR(20)     NOT NULL,
    `state`                 VARCHAR(16)     NOT NULL,
    `submitter_id`          BIGINT(20)      NULL,
    `submitter_comment`     TEXT            NULL,
    `reviewer_id`           BIGINT(20)      NULL,
    `reviewer_comment`      TEXT            NULL,
    `schedule`              TIMESTAMP       NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY `workflow_ix_item`(`item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE,
    FOREIGN KEY `workflow_ix_submitter`(`submitter_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY `workflow_ix_reviewer`(`reviewer_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8
    ROW_FORMAT = DYNAMIC ;

UPDATE _meta SET version = '4.0.0.6' ;
