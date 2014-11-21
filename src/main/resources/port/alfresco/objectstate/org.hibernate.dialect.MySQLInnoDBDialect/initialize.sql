CREATE TABLE `cstudio_objectstate` (
        `object_id` varchar(255) NOT NULL,
        `site` varchar(50) NOT NULL,
        `path` varchar(2000) NOT NULL,
        `state` varchar(255) NOT NULL,
        `system_processing` bit(1) NOT NULL,
        PRIMARY KEY (`object_id`),
        KEY `cstudio_objectstate_object_idx` (`object_id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
