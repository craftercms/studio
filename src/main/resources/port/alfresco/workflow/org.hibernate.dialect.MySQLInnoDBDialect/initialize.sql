CREATE TABLE `workflow_job` (
        `id` varchar(50) NOT NULL,
        `site` varchar(120) NOT NULL,
        `process_name` varchar(250) NOT NULL,
        `created_date` datetime DEFAULT NULL,
        `modified_date` datetime DEFAULT NULL,
        `state` varchar(250) NOT NULL,
        PRIMARY KEY (`id`),
        KEY `workflow_job_idx` (`id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `workflow_job_property` (
        `job_id` varchar(50) NOT NULL,
        `name` varchar(120) NOT NULL,
        `value` varchar(250) NOT NULL,
        PRIMARY KEY (`job_id`, `name`),
        KEY `workflow_job_property_idx` (`job_id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `workflow_item` (
        `id` varchar(50) NOT NULL,
        `job_id` varchar(50) NOT NULL,
        `path` text NOT NULL,
        `percent_complete` int NOT NULL,
        PRIMARY KEY (`id`),
        KEY `workflow_item_idx` (`id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

