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

CREATE TABLE IF NOT EXISTS cluster_remote_repository
(
  `cluster_id`                  BIGINT(20)    NOT NULL,
  `remote_repository_id`        BIGINT(20)    NOT NULL,
  `record_last_updated`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`cluster_id`, `remote_repository_id`),
  FOREIGN KEY cluster_remote_ix_cluster_id(`cluster_id`) REFERENCES `cluster` (`id`)
    ON DELETE CASCADE,
  FOREIGN KEY cluster_remote_ix_remote_id(`remote_repository_id`) REFERENCES `remote_repository` (`id`)
    ON DELETE CASCADE
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

UPDATE _meta SET version = '3.2.0.10' ;