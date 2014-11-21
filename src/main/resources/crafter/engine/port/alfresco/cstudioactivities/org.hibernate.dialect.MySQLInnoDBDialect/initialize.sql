CREATE TABLE `cstudio_activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `modified_date` datetime NOT NULL,
  `creation_date` datetime NOT NULL,
  `summary` text NOT NULL,
  `summary_format` varchar(255) NOT NULL,
  `content_id` text NOT NULL,
  `site_network` varchar(255) NOT NULL,
  `activity_type` varchar(255) NOT NULL,
  `content_type` varchar(255) NOT NULL,
  `post_user_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `cstudio_activity_user_idx` (`post_user_id`),
  KEY `cstudio_activity_site_idx` (`site_network`),
  KEY `cstudio_activity_content_idx` (`content_id`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8;