CREATE TABLE `cstudio_pagenavigationordersequence` (
  `folder_id` varchar(100) NOT NULL,
  `site` varchar(50) NOT NULL,
  `path` varchar(255) NOT NULL,
  `max_count` float NOT NULL,
  PRIMARY KEY (`folder_id`),
  KEY `cstudio_pagenavigationorder_folder_idx` (`folder_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;