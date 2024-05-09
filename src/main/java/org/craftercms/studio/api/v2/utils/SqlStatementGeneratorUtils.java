/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Objects;

import static org.apache.commons.lang.StringUtils.removeEnd;
import static org.craftercms.studio.api.v1.constant.DmConstants.SLASH_INDEX_FILE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;

public final class SqlStatementGeneratorUtils {

    public static final String ITEM_INSERT =
            "INSERT INTO item (site_id, path, preview_url, state, locked_by, created_by, created_on, last_modified_by," +
                    " last_modified_on, last_published_on, label, content_type_id, system_type, mime_type," +
                    " locale_code, translation_source_id, size, parent_id, ignored)" +
                    " VALUES (#{siteId}, '#{path}', '#{previewUrl}', #{state}, #{lockedBy}, #{createdBy}," +
                    " '#{createdOn}', #{lastModifiedBy}, '#{lastModifiedOn}', '#{lastPublishedOn}', '#{label}'," +
                    " '#{contentTypeId}', '#{systemType}', '#{mimeType}', '#{localeCode}'," +
                    " #{translationSourceId}, #{size}, #{parentId}, #{ignoredAsInt})" +
                    " ON DUPLICATE KEY UPDATE site_id = #{siteId}, path = '#{path}', preview_url = '#{previewUrl}'," +
                    " state = #{state}, locked_by = #{lockedBy}, last_modified_by = #{lastModifiedBy}," +
                    " last_modified_on = '#{lastModifiedOn}', last_published_on = '#{lastPublishedOn}'," +
                    " label = '#{label}', content_type_id = '#{contentTypeId}', system_type = '#{systemType}'," +
                    " mime_type = '#{mimeType}', locale_code = '#{localeCode}'," +
                    " translation_source_id = #{translationSourceId}, size = #{size}, parent_id = #{parentId}," +
                    " ignored = #{ignoredAsInt} ;";

    public static final String ITEM_UPDATE =
            "UPDATE item SET preview_url = '#{previewUrl}'," +
                    " state = (state | #{onStatesBitMap}) & ~#{offStatesBitMap}," +
                    " last_modified_by = #{lastModifiedBy}," +
                    " last_modified_on = '#{lastModifiedOn}', label = '#{label}', content_type_id = '#{contentTypeId}'," +
                    " system_type = '#{systemType}', mime_type = '#{mimeType}', size = #{size}, locked_by = null, " +
                    " ignored = #{ignoredAsInt} WHERE site_id = #{siteId} " +
                    " and path = '#{path}' ;";

    public static final String ITEM_DELETE =
            "DELETE FROM item WHERE site_id = #{siteId} and path = '#{path}' ;";

    public static final String ITEM_MOVE =
            "UPDATE item SET path = REPLACE(path, '#{oldPath}', '#{newPath}'), locked_by = null," +
                    " state = (state | #{onStatesBitMap}) & ~#{offStatesBitMap}" +
                    " WHERE site_id = #{siteId} AND (path = '#{oldPath}' OR path LIKE '#{oldPath}/%') ;";

    public static final String ITEM_UPDATE_PARENT_ID =
            "SELECT id, @itemId := id, path FROM item WHERE site_id = #{siteId} AND path = '#{itemPath}' ;\n\n" +
                    "SELECT id , @parentId := id, path FROM item WHERE site_id = #{siteId} AND (path = " +
                    "'#{parentPath}/index.xml' or path = '#{parentPath}') ORDER BY PATH desc LIMIT 1 ;\n\n" +
                    "UPDATE item SET parent_id = @parentId WHERE id = @itemId ;\n\nSET @itemId = NULL ;\n\n" +
                    "SET @parentId = NULL ;" ;

    public static final String UPDATE_NEW_PAGE_CHILDREN = "UPDATE item, " +
            "(SELECT child.id AS childId, " +
            "(SELECT i.id FROM item i WHERE i.site_id = #{siteId} AND i.path = concat('#{path}', '/index.xml')) AS newParentId " +
            "FROM item child INNER JOIN item parent ON child.parent_id = parent.id " +
            "WHERE child.site_id = #{siteId} AND parent.path = '#{path}') AS updates " +
            "SET item.parent_id = updates.newParentId " +
            "WHERE item.id = updates.childId ;\n\n";

    public static final String UPDATE_DELETED_PAGE_CHILDREN = "UPDATE item, " +
            "(SELECT child.id AS childId, " +
            "(SELECT i.id FROM item i WHERE i.site_id = #{siteId} AND i.path = '#{folderPath}') AS newParentId " +
            "FROM item child INNER JOIN item parent ON child.parent_id = parent.id " +
            "WHERE child.site_id = #{siteId} AND parent.path = concat('#{folderPath}', '/index.xml')) AS updates " +
            "SET item.parent_id = updates.newParentId " +
            "WHERE item.id = updates.childId ;\n\n";

    public static final String DEPENDENCIES_INSERT =
            "INSERT INTO dependency (site, source_path, target_path, type, valid) " +
                    "VALUES ('#{site}', '#{sourcePath}', '#{targetPath}', '#{type}', #{valid}) ;\n\n";

    public static final String DEPENDENCIES_DELETE_SOURCE =
            "DELETE FROM dependency WHERE source_path = '#{path}' AND site = '#{site}' ;\n\n";

    public static final String DEPENDENCIES_DELETE =
            "DELETE FROM dependency WHERE site = '#{site}' AND source_path = '#{path}' ;\n\n" +
            "UPDATE dependency SET valid = 0 WHERE site = '#{site}' AND target_path = '#{path}' ;\n\n";
    private static final String DEPENDENCIES_INVALIDATE =
            "UPDATE dependency SET valid = 0 WHERE site = '#{site}' AND target_path = '#{path}' ;\n\n";
    private static final String DEPENDENCIES_VALIDATE =
            "UPDATE dependency SET valid = 1 WHERE site = '#{site}' AND  target_path = '#{path}' ;\n\n";

    public static String insertItemRow(long siteId, String path, String previewUrl, long state, Long lockedBy,
                                       Long createdBy, ZonedDateTime createdOn, Long lastModifiedBy,
                                       ZonedDateTime lastModifiedOn, ZonedDateTime lastPublishedOn, String label,
                                       String contentTypeId, String systemType, String mimeType, String localeCode,
                                       Long translationSourceId, Long size, Long parentId) {
        Timestamp sqlTsCreated = new Timestamp(createdOn.toInstant().toEpochMilli());
        Timestamp sqlTsLastModified = new Timestamp(lastModifiedOn.toInstant().toEpochMilli());
        Timestamp sqlTsLastPublished = Objects.isNull(lastPublishedOn) ?
                null : new Timestamp(lastPublishedOn.toInstant().toEpochMilli());
        int ignoredAsInt = 0;
        String fileName = FilenameUtils.getName(path);
        if (ArrayUtils.contains(IGNORE_FILES, fileName)) {
            ignoredAsInt = 1;
        }
        String sql = StringUtils.replace(ITEM_INSERT, "#{siteId}", Long.toString(siteId));
        sql = StringUtils.replace(sql,"#{path}", StringUtils.replace(path, "'", "''"));
        if (StringUtils.isEmpty(previewUrl)) {
            sql = StringUtils.replace(sql, "'#{previewUrl}'", "NULL");
        } else {
            sql = StringUtils.replace(sql, "#{previewUrl}", StringUtils.replace(previewUrl, "'", "''"));
        }
        sql = StringUtils.replace(sql,"#{state}", Long.toString(state));
        sql = StringUtils.replace(sql,"#{lockedBy}", Objects.isNull(lockedBy) ? "NULL" : Long.toString(lockedBy));
        sql = StringUtils.replace(sql,"#{createdBy}", Objects.isNull(createdBy) ? "NULL" : Long.toString(createdBy));
        sql = StringUtils.replace(sql, "#{createdOn}", sqlTsCreated.toString());
        sql = StringUtils.replace(sql,"#{lastModifiedBy}", Objects.isNull(lastModifiedBy) ? "NULL" :
                Long.toString(lastModifiedBy));
        sql = StringUtils.replace(sql, "#{lastModifiedOn}", sqlTsLastModified.toString());
        if (Objects.isNull(sqlTsLastPublished)) {
            sql = StringUtils.replace(sql, "'#{lastPublishedOn}'", "NULL");
        } else {
            sql = StringUtils.replace(sql, "#{lastPublishedOn}", sqlTsLastPublished.toString());
        }
        sql = StringUtils.replace(sql,"#{label}", StringUtils.replace(label, "'", "''"));
        if (StringUtils.isEmpty(contentTypeId)) {
            sql = StringUtils.replace(sql,"'#{contentTypeId}'", "NULL");
        } else {
            sql = StringUtils.replace(sql,"#{contentTypeId}", StringUtils.replace(contentTypeId, "'", "''"));
        }
        sql = StringUtils.replace(sql,"#{systemType}", StringUtils.replace(systemType, "'", "''"));
        if (StringUtils.isEmpty(mimeType)) {
            sql = StringUtils.replace(sql, "'#{mimeType}'", "NULL");
        } else {
            sql = StringUtils.replace(sql, "#{mimeType}", StringUtils.replace(mimeType, "'", "''"));
        }
        sql = StringUtils.replace(sql,"#{localeCode}", StringUtils.replace(localeCode, "'", "''"));
        sql = StringUtils.replace(sql,"#{translationSourceId}", Objects.isNull(translationSourceId) ? "NULL" :
                Long.toString(translationSourceId));
        sql = StringUtils.replace(sql,"#{size}", Long.toString(size));
        sql = StringUtils.replace(sql,"#{parentId}", Objects.isNull(parentId) ? "NULL" :
                Long.toString(parentId));
        sql = StringUtils.replace(sql,"#{ignoredAsInt}", Integer.toString(ignoredAsInt));
        return sql;
    }

    public static String updateItemRow(long siteId, String path, String previewUrl, long onStatesBitMap,
                                       long offStatesBitMap, Long lastModifiedBy, ZonedDateTime lastModifiedOn,
                                       String label, String contentTypeId, String systemType, String mimeType,
                                       Long size) {
        Timestamp sqlTsLastModified = new Timestamp(lastModifiedOn.toInstant().toEpochMilli());
        int ignoredAsInt = 0;
        String fileName = FilenameUtils.getName(path);
        if (ArrayUtils.contains(IGNORE_FILES, fileName)) {
            ignoredAsInt = 1;
        }
        String sql = StringUtils.replace(ITEM_UPDATE, "#{siteId}", Long.toString(siteId));
        sql = StringUtils.replace(sql,"#{path}", StringUtils.replace(path, "'", "''"));
        if (StringUtils.isEmpty(previewUrl)) {
            sql = StringUtils.replace(sql, "'#{previewUrl}'", "NULL");
        } else {
            sql = StringUtils.replace(sql, "#{previewUrl}", StringUtils.replace(previewUrl, "'", "''"));
        }
        sql = StringUtils.replace(sql,"#{onStatesBitMap}", Long.toString(onStatesBitMap));
        sql = StringUtils.replace(sql,"#{offStatesBitMap}", Long.toString(offStatesBitMap));
        sql = StringUtils.replace(sql,"#{lastModifiedBy}", Objects.isNull(lastModifiedBy) ? "NULL" :
                Long.toString(lastModifiedBy));
        sql = StringUtils.replace(sql, "#{lastModifiedOn}", sqlTsLastModified.toString());
        sql = StringUtils.replace(sql,"#{label}", StringUtils.replace(label, "'", "''"));
        if (StringUtils.isEmpty(contentTypeId)) {
            sql = StringUtils.replace(sql,"'#{contentTypeId}'", "NULL");
        } else {
            sql = StringUtils.replace(sql,"#{contentTypeId}", StringUtils.replace(contentTypeId, "'", "''"));
        }
        sql = StringUtils.replace(sql,"#{systemType}", StringUtils.replace(systemType, "'", "''"));
        if (StringUtils.isEmpty(mimeType)) {
            sql = StringUtils.replace(sql, "'#{mimeType}'", "NULL");
        } else {
            sql = StringUtils.replace(sql, "#{mimeType}", StringUtils.replace(mimeType, "'", "''"));
        }
        sql = StringUtils.replace(sql,"#{size}", Long.toString(size));
        sql = StringUtils.replace(sql,"#{ignoredAsInt}", Integer.toString(ignoredAsInt));
        return sql;
    }

    public static String deleteItemRow(Long siteId, String path) {
        String sql =  StringUtils.replace(ITEM_DELETE,"#{siteId}", Long.toString(siteId));
        sql = StringUtils.replace(sql,"#{path}", StringUtils.replace(path, "'", "''"));
        return sql;
    }

    public static String moveItemRow(String site, String oldPath, String newPath, long onStatesBitMap,
                                     long offStatesBitMap) {
        String sql =  StringUtils.replace(ITEM_MOVE,"#{site}", StringUtils.replace(site,"'", "''"));
        sql = StringUtils.replace(sql,"#{oldPath}", StringUtils.replace(oldPath, "'", "''"));
        sql = StringUtils.replace(sql,"#{newPath}", StringUtils.replace(newPath, "'", "''"));
        sql = StringUtils.replace(sql,"#{onStatesBitMap}", Long.toString(onStatesBitMap));
        sql = StringUtils.replace(sql,"#{offStatesBitMap}", Long.toString(offStatesBitMap));
        return sql;
    }

    public static String updateParentId(long siteId, String itemPath, String parentPath) {
        String sql = StringUtils.replace(ITEM_UPDATE_PARENT_ID, "#{siteId}", Long.toString(siteId));
        sql = StringUtils.replace(sql,"#{itemPath}", StringUtils.replace(itemPath, "'", "''"));
        sql = StringUtils.replace(sql,"#{parentPath}", StringUtils.replace(parentPath, "'", "''"));
        return sql;
    }

    /**
     * Generates the sql statements to update a new page children.
     * This should be called when a new page (index.xml) is created in an already existing folder
     *
     * @param siteId the site id
     * @param path   the page to the content (including index.xml)
     * @return the sql statement
     */
    public static String updateNewPageChildren(long siteId, String path) {
        String folderPath = removeEnd(path, SLASH_INDEX_FILE);
        String sql = StringUtils.replace(UPDATE_NEW_PAGE_CHILDREN, "#{siteId}", Long.toString(siteId));
        sql = StringUtils.replace(sql, "#{path}", folderPath);
        return sql;
    }

    /**
     * Generates the sql statements to update a deleted page children.
     * This should be called when a page (index.xml) is deleted via git but its children still exists
     *
     * @param siteId     the site id
     * @param folderPath the folder path to the deleted page
     * @return the sql statement
     */
    public static String updateDeletedPageChildren(long siteId, String folderPath) {
        String sql = StringUtils.replace(UPDATE_DELETED_PAGE_CHILDREN, "#{siteId}", Long.toString(siteId));
        sql = StringUtils.replace(sql, "#{folderPath}", folderPath);
        return sql;
    }

    public static String insertDependencyRow(String siteId, String sourcePath, String targetPath, String type, boolean valid) {
        String sql = StringUtils.replace(DEPENDENCIES_INSERT, "#{site}", StringUtils.replace(siteId, "'", "''"));
        sql = StringUtils.replace(sql, "#{sourcePath}", StringUtils.replace(sourcePath, "'", "''"));
        sql = StringUtils.replace(sql, "#{targetPath}", StringUtils.replace(targetPath,"'", "''"));
        sql = StringUtils.replace(sql, "#{type}", StringUtils.replace(type, "'", "''"));
        sql = StringUtils.replace(sql, "#{valid}", valid ? "1" : "0");
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

    /**
     * Generate the necessary sql statements to invalidate dependencies for a given target path
     *
     * @param siteId     the site id
     * @param targetPath the target path of the dependency records to mark as invalid
     * @return the sql statement
     */
    public static String invalidateDependencies(String siteId, String targetPath) {
        String sql = StringUtils.replace(DEPENDENCIES_INVALIDATE, "#{site}", siteId)
                .replace("#{path}", targetPath);
        return sql;
    }

    /**
     * Generate the necessary sql statements to validate dependencies for a given target path
     *
     * @param siteId     the site id
     * @param targetPath the target path of the dependency records to mark as valid
     * @return the sql statement
     */
    public static String validateDependencies(String siteId, String targetPath) {
        String sql = StringUtils.replace(DEPENDENCIES_VALIDATE, "#{site}", siteId)
                .replace("#{path}", targetPath);
        return sql;
    }

    private SqlStatementGeneratorUtils() {}
}
