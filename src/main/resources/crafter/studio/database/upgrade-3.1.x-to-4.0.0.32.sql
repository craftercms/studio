ALTER TABLE `publish_request`
ADD COLUMN `submission_type` VARCHAR(32) NULL DEFAULT NULL AFTER `contenttypeclass`,
ADD COLUMN `label` VARCHAR(256) NULL DEFAULT NULL AFTER `package_id`,
ADD COLUMN `published_on` TIMESTAMP NULL DEFAULT NULL AFTER `label` ;

ALTER TABLE `site`
DROP COLUMN `search_engine`,
DROP COLUMN `publishing_status_message`,
DROP COLUMN `status` ;

CREATE TABLE IF NOT EXISTS `user_properties` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT(20) NOT NULL,
  `site_id` BIGINT(20) NOT NULL,
  `property_key` VARCHAR(255) NOT NULL,
  `property_value` TEXT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `user_property_ix_property_key` (`user_id` ASC, `site_id` ASC, `property_key` ASC),
  INDEX `user_property_ix_site_id` (`site_id` ASC),
  CONSTRAINT `user_property_ix_user_id`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`),
  CONSTRAINT `user_property_ix_site_id`
    FOREIGN KEY (`site_id`)
    REFERENCES `site` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `item` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `site_id` BIGINT(20) NOT NULL,
  `path` VARCHAR(2048) NOT NULL,
  `preview_url` VARCHAR(2048) NULL DEFAULT NULL,
  `state` BIGINT(20) NOT NULL,
  `locked_by` BIGINT(20) NULL DEFAULT NULL,
  `created_by` BIGINT(20) NULL DEFAULT NULL,
  `created_on` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_modified_by` BIGINT(20) NULL DEFAULT NULL,
  `last_modified_on` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_published_on` TIMESTAMP NULL DEFAULT NULL,
  `label` VARCHAR(256) NULL DEFAULT NULL,
  `content_type_id` VARCHAR(256) NULL DEFAULT NULL,
  `system_type` VARCHAR(64) NULL DEFAULT NULL,
  `mime_type` VARCHAR(96) NULL DEFAULT NULL,
  `locale_code` VARCHAR(16) NULL DEFAULT NULL,
  `translation_source_id` BIGINT(20) NULL DEFAULT NULL,
  `size` INT(11) NULL DEFAULT NULL,
  `parent_id` BIGINT(20) NULL DEFAULT NULL,
  `commit_id` VARCHAR(128) NULL DEFAULT NULL,
  `previous_path` VARCHAR(2048) NULL DEFAULT NULL,
  `ignored` INT(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uq_i_site_path` (`site_id` ASC, `path`(900) ASC),
  INDEX `item_ix_created_by` (`created_by` ASC),
  INDEX `item_ix_last_modified_by` (`last_modified_by` ASC),
  INDEX `item_ix_locked_by` (`locked_by` ASC),
  INDEX `item_ix_parent` (`parent_id` ASC),
  CONSTRAINT `item_ix_created_by`
    FOREIGN KEY (`created_by`)
    REFERENCES `user` (`id`),
  CONSTRAINT `item_ix_last_modified_by`
    FOREIGN KEY (`last_modified_by`)
    REFERENCES `user` (`id`),
  CONSTRAINT `item_ix_locked_by`
    FOREIGN KEY (`locked_by`)
    REFERENCES `user` (`id`),
  CONSTRAINT `item_ix_site_id`
    FOREIGN KEY (`site_id`)
    REFERENCES `site` (`id`),
  CONSTRAINT `item_ix_parent`
    FOREIGN KEY (`parent_id`)
    REFERENCES `item` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `item_translation` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `record_last_updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `source_id` BIGINT(20) NOT NULL,
  `translation_id` BIGINT(20) NOT NULL,
  `locale_code` VARCHAR(16) NOT NULL,
  `date_translated` TIMESTAMP NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `item_translation_ix_source` (`source_id` ASC),
  INDEX `item_translation_ix_translation` (`translation_id` ASC),
  CONSTRAINT `item_translation_ix_source`
    FOREIGN KEY (`source_id`)
    REFERENCES `item` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `item_translation_ix_translation`
    FOREIGN KEY (`translation_id`)
    REFERENCES `item` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `workflow` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `item_id` BIGINT(20) NOT NULL,
  `target_environment` VARCHAR(20) NOT NULL,
  `state` VARCHAR(16) NOT NULL,
  `submitter_id` BIGINT(20) NULL DEFAULT NULL,
  `submitter_comment` TEXT NULL DEFAULT NULL,
  `reviewer_id` BIGINT(20) NULL DEFAULT NULL,
  `reviewer_comment` TEXT NULL DEFAULT NULL,
  `schedule` TIMESTAMP NULL DEFAULT NULL,
  `publishing_package_id` VARCHAR(50) NULL DEFAULT NULL,
  `submission_type` VARCHAR(32) NULL DEFAULT NULL,
  `notify_submitter` INT(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `workflow_ix_item` (`item_id` ASC),
  INDEX `workflow_ix_submitter` (`submitter_id` ASC),
  INDEX `workflow_ix_reviewer` (`reviewer_id` ASC),
  CONSTRAINT `workflow_ix_item`
    FOREIGN KEY (`item_id`)
    REFERENCES `item` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `workflow_ix_submitter`
    FOREIGN KEY (`submitter_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `workflow_ix_reviewer`
    FOREIGN KEY (`reviewer_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `refresh_token` (
  `user_id` BIGINT(20) NULL DEFAULT NULL,
  `token` VARCHAR(50) NOT NULL,
  `last_updated_on` TIMESTAMP NOT NULL,
  `created_on` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
ROW_FORMAT = DYNAMIC ;

CREATE TABLE IF NOT EXISTS `access_token` (
  `id` BIGINT(20) NULL DEFAULT NULL AUTO_INCREMENT,
  `user_id` BIGINT(20) NULL DEFAULT NULL,
  `label` VARCHAR(2550) NOT NULL,
  `enabled` BOOLEAN NULL DEFAULT true,
  `last_updated_on` TIMESTAMP NOT NULL,
  `created_on` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
ROW_FORMAT = DYNAMIC ;

UPDATE site SET state = 'READY' WHERE deleted = 0 ;

UPDATE site SET state = 'DELETED' WHERE deleted = 1 ;

UPDATE site set state = 'INITIALIZING' WHERE state = 'CREATING' ;

INSERT IGNORE INTO `user` (record_last_updated, username, password, first_name, last_name,
                           externally_managed, timezone, locale, email, enabled, deleted)
VALUES (CURRENT_TIMESTAMP, 'git_repo_user', '',
        'Git Repo', 'User', 0, 'EST5EDT', 'en/US', 'evalgit@example.com', 1, 0) ;

CREATE PROCEDURE populateItemTable(IN siteId VARCHAR(50))
BEGIN
	DECLARE siteIdInt BIGINT;
    DECLARE created_on TIMESTAMP DEFAULT now();


	SELECT id INTO siteIdInt FROM site WHERE site_id = siteId AND deleted = 0;

	DELETE FROM item WHERE site_id = siteIdInt;

 	INSERT INTO item
             (site_id, path, state, locked_by, created_by, created_on, last_modified_by, last_modified_on, commit_id)
    SELECT siteIdInt as site_id, item_path as path,
            (CASE WHEN item_state = 'NEW_UNPUBLISHED_LOCKED' THEN
                        1 + 2 + 8 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'NEW_UNPUBLISHED_UNLOCKED' THEN
                        1 + 2 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'NEW_SUBMITTED_WITH_WF_SCHEDULED' THEN
                        1 + 2 + 32 + 64 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED' THEN
                        1 + 2 + 32 + 64 + 8 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'NEW_SUBMITTED_WITH_WF_UNSCHEDULED' THEN
                        1 + 2 + 32 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED' THEN
                        1 + 2 + 32 + 8 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'NEW_SUBMITTED_NO_WF_SCHEDULED' THEN
                        1 + 2 + 64 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED' THEN
                        1 + 2 + 64 + 8 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'NEW_SUBMITTED_NO_WF_UNSCHEDULED' THEN
                        1 + 2 + 128 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'NEW_PUBLISHING_FAILED' THEN
                        1 + 2 + 128 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'NEW_DELETED' THEN
                        1 + 4 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'EXISTING_UNEDITED_LOCKED' THEN
                        8 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'EXISTING_UNEDITED_UNLOCKED' THEN
                        16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'EXISTING_EDITED_LOCKED' THEN
                        2 + 8 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'EXISTING_EDITED_UNLOCKED' THEN
                        2 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'EXISTING_SUBMITTED_WITH_WF_SCHEDULED' THEN
                        2 + 32 + 64 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED' THEN
                        2 + 32 + 64 + 8 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED' THEN
                        2 + 32 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED' THEN
                        2 + 32 + 8 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'EXISTING_SUBMITTED_NO_WF_SCHEDULED' THEN
                        2 + 64 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED' THEN
                        2 + 64 + 8 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'EXISTING_SUBMITTED_NO_WF_UNSCHEDULED' THEN
                        2 + 128 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'EXISTING_PUBLISHING_FAILED' THEN
                        2 + 128 + 16 * item_sys_process + 256 * dest + 512 * staging + 1024 * live
                        WHEN item_state = 'EXISTING_DELETED' THEN
                        4 + 16 * item_sys_process
                    ELSE 0 END) as state,
            (SELECT id from user WHERE username = item_owner LIMIT 1) as locked_by,
            (SELECT id from user WHERE username = item_creator LIMIT 1) as created_by, created_on,
            (SELECT id FROM user WHERE username = item_modifier LIMIT 1) as last_modified_by,
            item_modified as last_modified_on,
            item_commit_id as commit_id
    FROM
    (SELECT im.path as item_path, ist.state as item_state, ist.system_processing as item_sys_process,
	   im.lockowner as item_owner, im.creator as item_creator, im.modifier as item_modifier,
	   im.modified as item_modified, im.commit_id as item_commit_id,
	   submittedtoenvironment as item_wf_env,
			IFNULL((SELECT if(state = 'COMPLETED' AND environment = 'live', 1, 0)
				FROM publish_request WHERE site = siteId and path = item_path ORDER BY scheduleddate DESC LIMIT 1), 0) as live,
            IFNULL((SELECT if(state = 'COMPLETED' AND environment = 'staging', 1, 0)
				FROM publish_request WHERE site = siteId and path = item_path ORDER BY scheduleddate DESC LIMIT 1), 0) as staging,
			IF(submittedtoenvironment = 'live', 1, 0) AS dest
	FROM item_state ist
		LEFT OUTER JOIN item_metadata im ON ist.site = im.site AND ist.path = im.path
	WHERE ist.site = siteId AND im.path NOT LIKE '%.keep' AND im.path NOT LIKE '%.DS_Store'
	UNION
	SELECT im.path as item_path, ist.state as item_state, ist.system_processing as item_sys_process,
		   im.lockowner as item_owner, im.creator as item_creator, im.modifier as item_modifier,
		   im.modified as item_modified, im.commit_id as item_commit_id,
		   submittedtoenvironment as item_wf_env,
			IFNULL((SELECT if(state = 'COMPLETED' AND environment = 'live', 1, 0)
				FROM publish_request WHERE site = siteId and path = item_path ORDER BY scheduleddate DESC LIMIT 1), 0) as live,
            IFNULL((SELECT if(state = 'COMPLETED' AND environment = 'staging', 1, 0)
				FROM publish_request WHERE site = siteId and path = item_path ORDER BY scheduleddate DESC LIMIT 1), 0) as staging,
			IF(submittedtoenvironment = 'live', 1, 0) AS dest
	FROM item_state ist
		RIGHT OUTER JOIN item_metadata im ON ist.site = im.site AND ist.path = im.path
	WHERE ist.site = siteId AND im.path NOT LIKE '%.keep' AND im.path NOT LIKE '%.DS_Store'
    ) AS item_states;

    SELECT COUNT(1) FROM item WHERE site_id = siteIdInt;
END ;

CREATE PROCEDURE populateItemParentId(IN siteId BIGINT)
BEGIN
    UPDATE item,
        (SELECT id, max(potential_parent_path) as calculated_parent_path,
						(SELECT p.id FROM item p WHERE (p.path = max(potential_parent_path)) and p.site_id = siteId) AS calculated_parent_id
        FROM
            (SELECT candidates.id, candidates.path, candidates.parent_id,
                    (SELECT p.id FROM item p WHERE (p.path = candidates.parent_path) AND p.site_id = siteId) AS potential_parent_id,
                    candidates.parent_path as potential_parent_path
            FROM (
					SELECT id, parent_id, path,
							reverse(substr(reverse(trim('/index.xml' from path)), locate('/', reverse(trim('/index.xml' from path)))+1)) AS parent_path
					FROM item
					WHERE site_id = siteId
				UNION
					SELECT id, parent_id, path,
							concat(reverse(substr(reverse(trim('/index.xml' from path)), locate('/', reverse(trim('/index.xml' from path)))+1)), '/index.xml') AS parent_path
					FROM item
					WHERE site_id = siteId
				) AS candidates
			) AS mapped
        WHERE potential_parent_id IS NOT NULL
        GROUP BY id
        ) AS updates
    SET item.parent_id = updates.calculated_parent_id
    WHERE item.id = updates.id;
END ;

CREATE PROCEDURE migrateWorkflow(IN siteId VARCHAR(50))
BEGIN
    DECLARE v_site_id BIGINT;
    DECLARE v_item_id BIGINT;
    DECLARE v_path VARCHAR(2048);
    DECLARE v_renamed INT;
    DECLARE v_previous_path VARCHAR(2048);
    DECLARE v_state VARCHAR(255);
    DECLARE v_state_str VARCHAR(255);
    DECLARE v_submitter VARCHAR(255);
    DECLARE v_submitter_id BIGINT;
    DECLARE v_submitter_comment TEXT;
    DECLARE v_reviewer VARCHAR(255);
    DECLARE v_reviewer_id BIGINT;
    DECLARE v_reviewer_comment TEXT;
    DECLARE v_schedule TIMESTAMP;
    DECLARE v_notify_submitter INT;
    DECLARE v_last_modified_by BIGINT;
    DECLARE v_last_modified_on TIMESTAMP;
    DECLARE v_target_environment VARCHAR(255);
    DECLARE v_publishing_package_id VARCHAR(50);

    DECLARE v_finished INTEGER DEFAULT 0;
    DECLARE item_cursor CURSOR FOR
        SELECT ist.path as item_path, ist.state as item_state, im.renamed as item_renamed,
               im.oldurl as item_previous_path, im.submittedtoenvironment as item_target_environment,
               im.submittedby as item_submitter, im.submissioncomment as item_submitter_comment,
               im.launchdate as item_scheduled, im.sendemail as item_notify_submitter
        FROM item_state ist LEFT OUTER JOIN item_metadata im ON ist.site = im.site AND ist.path = im.path
        WHERE ist.site = siteId
        UNION
        SELECT ist.path as item_path, ist.state as item_state, im.renamed as item_renamed,
               im.oldurl as item_previous_path, im.submittedtoenvironment as item_target_environment,
               im.submittedby as item_submitter, im.submissioncomment as item_submitter_comment,
               im.launchdate as item_scheduled, im.sendemail as item_notify_submitter
        FROM item_state ist RIGHT OUTER JOIN item_metadata im ON ist.site = im.site AND ist.path = im.path
        WHERE ist.site = siteId;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_finished = 1;

    OPEN item_cursor;
    update_item: LOOP
        -- Init all variables used inside the loop to avoid possible issues with previous iterations
        SET v_path = NULL;
        SET v_state = NULL;
        SET v_renamed = NULL;
        SET v_previous_path = NULL;
        SET v_target_environment = NULL;
        SET v_submitter = NULL;
        SET v_submitter_comment = NULL;
        SET v_schedule = NULL;
        SET v_notify_submitter = 0;
        SET v_site_id = NULL;
        SET v_item_id = NULL;
        SET v_submitter_id = NULL;
        SET v_reviewer_id = NULL;
        SET v_reviewer_comment = NULL;
        SET v_publishing_package_id = NULL;

        FETCH item_cursor INTO v_path, v_state, v_renamed, v_previous_path, v_target_environment, v_submitter,
            v_submitter_comment, v_schedule, v_notify_submitter;

        IF v_notify_submitter IS NULL THEN
            SET v_notify_submitter = 0;
        END IF;

        IF v_finished = 1 THEN
            LEAVE update_item;
        END IF;

        SELECT id INTO v_site_id FROM site WHERE site_id = siteId AND deleted = 0;

        SELECT id INTO v_item_id FROM item WHERE site_id = v_site_id AND path = v_path;

        CASE
            WHEN v_state = 'NEW_SUBMITTED_WITH_WF_SCHEDULED' THEN SELECT 'OPENED' INTO v_state_str;
            WHEN v_state = 'NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED' THEN SELECT 'OPENED' INTO v_state_str;
            WHEN v_state = 'NEW_SUBMITTED_WITH_WF_UNSCHEDULED' THEN SELECT 'OPENED' INTO v_state_str;
            WHEN v_state = 'NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED' THEN SELECT 'OPENED' INTO v_state_str;
            WHEN v_state = 'NEW_SUBMITTED_NO_WF_SCHEDULED' THEN SELECT 'APPROVED' INTO v_state_str;
            WHEN v_state = 'NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED' THEN SELECT 'APPROVED' INTO v_state_str;
            WHEN v_state = 'NEW_SUBMITTED_NO_WF_UNSCHEDULED' THEN SELECT 'APPROVED' INTO v_state_str;
            WHEN v_state = 'EXISTING_SUBMITTED_WITH_WF_SCHEDULED' THEN SELECT 'OPENED' INTO v_state_str;
            WHEN v_state = 'EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED' THEN SELECT 'OPENED' INTO v_state_str;
            WHEN v_state = 'EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED' THEN SELECT 'OPENED' INTO v_state_str;
            WHEN v_state = 'EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED' THEN SELECT 'OPENED' INTO v_state_str;
            WHEN v_state = 'EXISTING_SUBMITTED_NO_WF_SCHEDULED' THEN SELECT 'APPROVED' INTO v_state_str;
            WHEN v_state = 'EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED' THEN SELECT 'APPROVED' INTO v_state_str;
            WHEN v_state = 'EXISTING_SUBMITTED_NO_WF_UNSCHEDULED' THEN SELECT 'APPROVED' INTO v_state_str;
            ELSE SET v_state_str = NULL;
        END CASE;

        SELECT a.id INTO v_submitter_id FROM
            (SELECT id FROM user WHERE username = v_submitter UNION
             SELECT id FROM user WHERE username = v_submitter LIMIT 1) as a;

        SELECT a.id INTO v_reviewer_id FROM
            (SELECT id FROM user WHERE username = v_reviewer UNION
             SELECT id FROM user WHERE username = v_reviewer LIMIT 1) as a;

        SELECT submissioncomment, package_id INTO v_reviewer_comment, v_publishing_package_id
        FROM publish_request
        WHERE state = 'READY_FOR_LIVE' AND site = siteId and path = v_path ORDER BY scheduleddate DESC LIMIT 1;

        IF v_renamed > 0 THEN
            UPDATE item SET previous_path = v_previous_path WHERE id = v_item_id;
        END IF;

        IF v_state_str IS NOT NULL THEN
            INSERT INTO workflow (item_id, target_environment, state, submitter_id, submitter_comment, notify_submitter,
                                  schedule, reviewer_id, reviewer_comment, publishing_package_id)
            VALUES (v_item_id, v_target_environment, v_state_str, v_submitter_id, v_submitter_comment,
                    v_notify_submitter, v_schedule, v_reviewer_id, v_reviewer_comment, v_publishing_package_id);
        END IF;

        set v_finished = 0;
    end loop update_item;
    SELECT COUNT(1) FROM item WHERE site_id = v_site_id;
END ;
