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

CREATE TABLE IF NOT EXISTS `user_properties`
(
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT(20) NOT NULL,
  `site_id` BIGINT(20) NOT NULL,
  `property_key` VARCHAR(255) NOT NULL,
  `property_value` TEXT NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY `user_property_ix_user_id` (`user_id`) REFERENCES `user` (`id`),
  FOREIGN KEY `user_property_ix_site_id` (`site_id`) REFERENCES `site` (`id`),
  UNIQUE INDEX `user_property_ix_property_key` (`user_id`, `site_id`, `property_key`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = DYNAMIC ;

UPDATE _meta SET version = '3.2.0.17' ;
