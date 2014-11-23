CREATE TABLE `cstudio_DEPENDENCY` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `site` varchar(35) NOT NULL,
  `source_path` text NOT NULL,
  `target_path` text NOT NULL,
  `type` varchar(15) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `cstudio_dependency_site_idx` (`site`),
  KEY `cstudio_dependency_sourcepath_idx` (`source_path`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8;