/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.utils;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.service.objectstate.State;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.UUID;

public final class SqlStatementGenerator {

    public static final String ITEM_STATE_INSERT =
            "INSERT INTO item_state (object_id, site, path, state, system_processing) " +
                    "VALUES ('#{objectId}', '#{site}', '#{path}', '#{state}', 0) ;";

    public static final String ITEM_STATE_UPSERT =
            "INSERT INTO item_state (object_id, site, path, state, system_processing) " +
                    "VALUES ('#{objectId}', '#{site}', '#{path}', '#{state}', 0) " +
                    "ON DUPLICATE KEY UPDATE path = '#{path}', state = '#{state}', system_processing = 0 ;";

    public static final String ITEM_STATE_MOVE =
            "UPDATE item_state SET path = '#{newPath}'  WHERE site = '#{site}' AND path = '#{oldPath}' ;";

    public static final String ITEM_STATE_TRANSITION =
            "Update item_state SET state = CASE " +
                    "WHEN state = 'NEW_UNPUBLISHED_LOCKED' THEN 'NEW_UNPUBLISHED_UNLOCKED' " +
                    "WHEN state = 'NEW_UNPUBLISHED_UNLOCKED' THEN 'NEW_UNPUBLISHED_UNLOCKED' " +
                    "WHEN state = 'NEW_SUBMITTED_WITH_WF_SCHEDULED' THEN 'NEW_UNPUBLISHED_UNLOCKED' " +
                    "WHEN state = 'NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED' THEN 'NEW_UNPUBLISHED_UNLOCKED' " +
                    "WHEN state = 'NEW_SUBMITTED_WITH_WF_UNSCHEDULED' THEN 'NEW_UNPUBLISHED_UNLOCKED' " +
                    "WHEN state = 'NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED' THEN 'NEW_UNPUBLISHED_UNLOCKED' " +
                    "WHEN state = 'NEW_SUBMITTED_NO_WF_SCHEDULED' THEN 'NEW_UNPUBLISHED_UNLOCKED' " +
                    "WHEN state = 'NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED' THEN 'NEW_UNPUBLISHED_UNLOCKED' " +
                    "WHEN state = 'NEW_SUBMITTED_NO_WF_UNSCHEDULED' THEN 'NEW_UNPUBLISHED_UNLOCKED' " +
                    "WHEN state = 'NEW_PUBLISHING_FAILED' THEN 'NEW_UNPUBLISHED_UNLOCKED' " +
                    "WHEN state = 'NEW_DELETED' THEN state " +
                    "WHEN state = 'EXISTING_UNEDITED_LOCKED' THEN 'EXISTING_EDITED_UNLOCKED' " +
                    "WHEN state = 'EXISTING_UNEDITED_UNLOCKED' THEN 'EXISTING_EDITED_UNLOCKED' " +
                    "WHEN state = 'EXISTING_EDITED_LOCKED' THEN 'EXISTING_EDITED_UNLOCKED' " +
                    "WHEN state = 'EXISTING_EDITED_UNLOCKED' THEN 'EXISTING_EDITED_UNLOCKED' " +
                    "WHEN state = 'EXISTING_SUBMITTED_WITH_WF_SCHEDULED' THEN 'EXISTING_EDITED_UNLOCKED' " +
                    "WHEN state = 'EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED' THEN 'EXISTING_EDITED_UNLOCKED' " +
                    "WHEN state = 'EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED' THEN 'EXISTING_EDITED_UNLOCKED' " +
                    "WHEN state = 'EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED' THEN 'EXISTING_EDITED_UNLOCKED' " +
                    "WHEN state = 'EXISTING_SUBMITTED_NO_WF_SCHEDULED' THEN 'EXISTING_EDITED_UNLOCKED' " +
                    "WHEN state = 'EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED' THEN 'EXISTING_EDITED_UNLOCKED' " +
                    "WHEN state = 'EXISTING_SUBMITTED_NO_WF_UNSCHEDULED' THEN 'EXISTING_EDITED_UNLOCKED' " +
                    "WHEN state = 'EXISTING_PUBLISHING_FAILED' THEN 'EXISTING_EDITED_UNLOCKED' " +
                    "WHEN state = 'EXISTING_DELETED' THEN state " +
                    "ELSE state END WHERE site = '#{site}' AND path = '#{path}' ; ";

    public static final String ITEM_STATE_DELETE =
            "DELETE FROM item_state WHERE site = '#{site}' AND path = BINARY '#{path}' ;";

    public static final String ITEM_METADATA_INSERT =
            "INSERT INTO item_metadata (site, path, modifier, modified, creator, commit_id) " +
                    "VALUES ('#{site}', '#{path}', '#{modifier}', '#{modified}', '#{creator}', '#{commitId}') ;";

    public static final String ITEM_METADATA_UPSERT =
            "INSERT INTO item_metadata (site, path, modifier, modified, creator, commit_id) " +
                    "VALUES ('#{site}', '#{path}', '#{modifier}', '#{modified}', '#{creator}', '#{commitId}') " +
                    "ON DUPLICATE KEY UPDATE path = '#{path}', modifier = '#{modifier}', creator = '#{creator}', " +
                    "commit_id = '#{commitId}' ;";

    public static final String ITEM_METADATA_UPDATE =
            "UPDATE item_metadata SET path = '#{path}', modifier = '#{modifier}', creator = '#{creator}', " +
                    "commit_id = '#{commitId}' WHERE  site = '#{site}' AND path = '#{path}' ;";

    public static final String ITEM_METADATA_MOVE =
            "UPDATE item_metadata SET path = '#{newPath}' WHERE site = '#{site}' AND path = ''#{oldPath}' ;";

    public static final String ITEM_METADATA_DELETE =
            "DELETE FROM item_metadata WHERE site = '#{site}' AND path = BINARY '#{path}' ;" ;

    public static final String DEPENDENCIES_INSERT =
            "INSERT INTO dependency (site, source_path, target_path, type) " +
                    "VALUES ('#{site}', '#{sourcePath}', '#{targetPath}', '#{type}') ;";

    public static final String DEPENDENCIES_DELETE_SOURCE =
            "DELETE FROM dependency WHERE source_path = '#{path}' AND site = '#{site}' ;" ;

    public static final String DEPENDENCIES_DELETE =
            "DELETE FROM dependency WHERE site = '#{site}' AND (source_path = '#{path}' OR target_path = '#{path}') ;";

    public static String insertItemStateRow(String site, String path) {
        String sql =  StringUtils.replace(ITEM_STATE_INSERT,"#{objectId}", UUID.randomUUID().toString());
        sql = StringUtils.replace(sql, "#{site}", StringUtils.replace(site,"'", "''"));
        sql = StringUtils.replace(sql,"#{path}", StringUtils.replace(path, "'", "''"));
        sql = StringUtils.replace(sql,"#{state}", State.NEW_UNPUBLISHED_UNLOCKED.name());
        return sql;
    }

    public static String upsertItemStateRow(String site, String path) {
        String sql =  StringUtils.replace(ITEM_STATE_UPSERT,"#{objectId}", UUID.randomUUID().toString());
        sql = StringUtils.replace(sql, "#{site}", StringUtils.replace(site,"'", "''"));
        sql = StringUtils.replace(sql,"#{path}", StringUtils.replace(path, "'", "''"));
        sql = StringUtils.replace(sql,"#{state}", State.NEW_UNPUBLISHED_UNLOCKED.name());
        return sql;
    }

    public static String moveItemStateRow(String site, String oldPath, String newPath) {
        String sql =  StringUtils.replace(ITEM_STATE_MOVE,"#{site}", StringUtils.replace(site,"'", "''"));
        sql = StringUtils.replace(sql,"#{oldPath}", StringUtils.replace(oldPath, "'", "''"));
        sql = StringUtils.replace(sql,"#{newPath}", StringUtils.replace(newPath, "'", "''"));
        return sql;
    }

    public static String transitionSaveItemStateRow(String site, String path) {
        String sql =  StringUtils.replace(ITEM_STATE_TRANSITION,"#{site}", StringUtils.replace(site,"'", "''"));
        sql = StringUtils.replace(sql,"#{path}", StringUtils.replace(path, "'", "''"));
        return sql;
    }

    public static String deleteItemStateRow(String site, String path) {
        String sql =  StringUtils.replace(ITEM_STATE_DELETE,"#{site}", StringUtils.replace(site,"'", "''"));
        sql = StringUtils.replace(sql,"#{path}", StringUtils.replace(path, "'", "''"));
        return sql;
    }

    public static String insertItemMetadataRow(String siteId, String path, String creator, ZonedDateTime dateTime,
                                               String commitId) {
        Timestamp sqlTs = new Timestamp(dateTime.toInstant().toEpochMilli());
        String sql = StringUtils.replace(ITEM_METADATA_INSERT,"#{site}", StringUtils.replace(siteId,"'", "''"));
        sql = StringUtils.replace(sql, "#{path}", StringUtils.replace(path,"'", "''"));
        sql = StringUtils.replace(sql, "#{modifier}", StringUtils.replace(creator, "'", "''"));
        sql = StringUtils.replace(sql, "#{modified}", sqlTs.toString());
        sql = StringUtils.replace(sql, "#{creator}", StringUtils.replace(creator, "'", "''"));
        sql = StringUtils.replace(sql, "#{commitId}", StringUtils.replace(commitId, "'", "''"));
        return sql;
    }

    public static String upsertItemMetadataRow(String siteId, String path, String creator, ZonedDateTime dateTime,
                                               String commitId) {
        Timestamp sqlTs = new Timestamp(dateTime.toInstant().toEpochMilli());
        String sql = StringUtils.replace(ITEM_METADATA_UPSERT,"#{site}", StringUtils.replace(siteId,"'", "''"));
        sql = StringUtils.replace(sql, "#{path}", StringUtils.replace(path,"'", "''"));
        sql = StringUtils.replace(sql, "#{modifier}", StringUtils.replace(creator, "'", "''"));
        sql = StringUtils.replace(sql, "#{modified}", sqlTs.toString());
        sql = StringUtils.replace(sql, "#{creator}", StringUtils.replace(creator, "'", "''"));
        sql = StringUtils.replace(sql, "#{commitId}", StringUtils.replace(commitId, "'", "''"));
        return sql;
    }

    public static String updateItemMetadataRow(String siteId, String path, String creator, ZonedDateTime dateTime,
                                               String commitId) {
        Timestamp sqlTs = new Timestamp(dateTime.toInstant().toEpochMilli());
        String sql = StringUtils.replace(ITEM_METADATA_UPDATE,"#{site}", StringUtils.replace(siteId,"'", "''"));
        sql = StringUtils.replace(sql, "#{path}", StringUtils.replace(path,"'", "''"));
        sql = StringUtils.replace(sql, "#{modifier}", StringUtils.replace(creator, "'", "''"));
        sql = StringUtils.replace(sql, "#{modified}", sqlTs.toString());
        sql = StringUtils.replace(sql, "#{creator}", StringUtils.replace(creator, "'", "''"));
        sql = StringUtils.replace(sql, "#{commitId}", StringUtils.replace(commitId, "'", "''"));
        return sql;
    }

    public static String moveItemMetadataRow(String site, String oldPath, String newPath) {
        String sql =  StringUtils.replace(ITEM_METADATA_MOVE,"#{site}", StringUtils.replace(site,"'", "''"));
        sql = StringUtils.replace(sql,"#{oldPath}", StringUtils.replace(oldPath, "'", "''"));
        sql = StringUtils.replace(sql,"#{newPath}", StringUtils.replace(newPath, "'", "''"));
        return sql;
    }

    public static String deleteItemMetadataRow(String site, String path) {
        String sql =  StringUtils.replace(ITEM_METADATA_DELETE,"#{site}", StringUtils.replace(site,"'", "''"));
        sql = StringUtils.replace(sql,"#{path}", StringUtils.replace(path, "'", "''"));
        return sql;
    }

    public static String insertDependencyRow(String siteId, String sourcePath, String targetPath, String type) {
        String sql = StringUtils.replace(DEPENDENCIES_INSERT, "#{site}", StringUtils.replace(siteId, "'", "''"));
        sql = StringUtils.replace(sql, "#{sourcePath}", StringUtils.replace(sourcePath, "'", "''"));
        sql = StringUtils.replace(sql, "#{targetPath}", StringUtils.replace(targetPath,"'", "''"));
        sql = StringUtils.replace(sql, "#{type}", StringUtils.replace(type, "'", "''"));
        return sql;
    }

    public static String deleteDependencySourcePathRows(String siteId, String sourcePath) {
        String sql = StringUtils.replace(DEPENDENCIES_DELETE_SOURCE, "#{site}", StringUtils.replace(siteId, "'", "''"));
        sql = StringUtils.replace(sql, "#{path}", StringUtils.replace(sourcePath, "'", "''"));
        return sql;
    }

    public static String deleteDependencyRows(String siteId, String sourcePath) {
        String sql = StringUtils.replace(DEPENDENCIES_DELETE, "#{site}", StringUtils.replace(siteId, "'", "''"));
        sql = StringUtils.replace(sql, "#{path}", StringUtils.replace(sourcePath, "'", "''"));
        return sql;
    }

    private SqlStatementGenerator() {}
}
