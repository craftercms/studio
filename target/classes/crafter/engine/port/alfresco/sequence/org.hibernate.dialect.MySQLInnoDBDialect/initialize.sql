CREATE TABLE `cstudio_SEQUENCE` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `namespace` varchar(20) NOT NULL,
  `sql_generator` bigint(20) NOT NULL,
  `step` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `cstudio_sequence_namespace_idx` (`namespace`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;