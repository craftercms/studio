/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.api.v1.constant;

/**
 * CStudio Xml constants
 *
 * @author hyanghee
 *
 */
public class StudioXmlConstants {

	/** xml document root names **/
	public static final String DOCUMENT_MODEL_DATA = "model-data";

	/** xml document element names for readiness **/
	public static final String DOCUMENT_ELM_ITEM = "item";
	public static final String DOCUMENT_ELM_ITEMS = "items";

	/** xml document element names for cstudio **/
	public static final String DOCUMENT_ELM_INTERNAL_TITLE = "internal-name";
	public static final String DOCUMENT_ELM_CONTENT_TYPE = "content-type";
    public static final String DOCUMENT_ELM_DISPLAY_TEMPLATE = "display-template";
	public static final String DOCUMENT_ELM_FILE_NAME = "file-name";
	public static final String DOCUMENT_ELM_CREATED_BY = "createdby";
	public static final String DOCUMENT_ELM_MODIFIED_BY = "modifiedby";
	public static final String DOCUMENT_ELM_CREATED_DATE= "createddate";
	public static final String DOCUMENT_ELM_MODIFIED_DATE= "modifieddate";


	/** xml document element for taxonomy **/
	public static final String DOCUMENT_CATEGORY = "category";
	public static final String DOCUMENT_CATEGORY_TYPE = "type";
	public static final String DOCUMENT_CATEGORY_NAME = "name";
	public static final String DOCUMENT_CATEGORY_ID = "id";
	public static final String DOCUMENT_CATEGORY_DESCRIPTION = "description";
	public static final String DOCUMENT_CATEGORY_ORDER = "order";
	public static final String DOCUMENT_CATEGORY_ICON_PATH = "iconpath";
	public static final String DOCUMENT_CATEGORY_IS_LIVE = "islive";
	public static final String DOCUMENT_CATEGORY_DISABLED = "disabled";
	public static final String DOCUMENT_CATEGORY_PARENT = "parent";
	public static final String DOCUMENT_CATEGORY_PARENT_PRODUCT = "product";
	public static final String DOCUMENT_CATEGORY_PARENT_PRODUCT_ID = "productid";
	public static final String DOCUMENT_CATEGORY_PARENT_PRODUCT_FAMILY = "productfamily";
	public static final String DOCUMENT_CATEGORY_PARENT_PRODUCT_FAMILY_ID = "productfamilyid";
	public static final String DOCUMENT_CATEGORY_PARENT_GEO = "geo";
	public static final String DOCUMENT_CATEGORY_PARENT_GEO_ID = "geoid";
	public static final String DOCUMENT_CATEGORY_PARENT_LANGUAGE = "language";
	public static final String DOCUMENT_CATEGORY_PARENT_LANGUAGE_ID = "languageid";
	public static final String DOCUMENT_CATEGORY_PARENT_BANNER_SECTION = "bannersection";
	public static final String DOCUMENT_CATEGORY_PARENT_BANNER_SECTION_ID = "bannersectionid";

	/** xml document attribute names **/
	public static final String DOCUMENT_ATTR_DESCRIPTION = "description";
	public static final String DOCUMENT_ATTR_ID = "id";
	public static final String DOCUMENT_ATTR_LABEL = "label";
	public static final String DOCUMENT_ATTR_NAME = "name";
	public static final String DOCUMENT_ATTR_URI = "uri";
	public static final String DOCUMENT_ATTR_VALUE = "value";

	/** xml type element names **/

	/** xml document root and element names for roles-mapping and permissions-mapping xmls */
	public static final String DOCUMENT_ROLE_MAPPINGS = "role-mappings";
	public static final String DOCUMENT_PERMISSIONS = "permissions";
	public static final String DOCUMENT_ELM_ALLOWED_PERMISSIONS = "allowed-permissions/permission";
	public static final String DOCUMENT_ELM_GROUPS_NODE = "groups/group";
	public static final String DOCUMENT_ELM_USER_NODE = "users/user";
	public static final String DOCUMENT_ELM_PERMISSION_ROLE = "role";
	public static final String DOCUMENT_ELM_PERMISSION_RULE = "rule";
	public static final String DOCUMENT_ELM_SITE = "site";
	public static final String DOCUMENT_ATTR_REGEX= "@regex";
	public static final String DOCUMENT_ATTR_PERMISSIONS_NAME= "@name";
	public static final String DOCUMENT_ATTR_SITE_ID= "@id";

    // Deployment config
    public static final String DOCUMENT_ELM_ENDPOINT_ROOT = "endpoint";
    public static final String DOCUMENT_ELM_ENDPOINT_NAME = "name";
    public static final String DOCUMENT_ELM_ENDPOINT_TYPE = "type";
    public static final String DOCUMENT_ELM_ENDPOINT_SERVER_URL = "server-url";
    public static final String DOCUMENT_ELM_ENDPOINT_PASSWORD = "password";
    public static final String DOCUMENT_ELM_ENDPOINT_TARGET = "target";
    public static final String DOCUMENT_ELM_ENDPOINT_SITE_ID = "site-id";
    public static final String DOCUMENT_ELM_ENDPOINT_SEND_METADATA = "send-metadata";
    public static final String DOCUMENT_ELM_ENDPOINT_EXCLUDE_PATTERN = "exclude";
    public static final String DOCUMENT_ELM_ENDPOINT_INCLUDE_PATTERN = "include";
    public static final String DOCUMENT_ELM_ENDPOINT_PATTERN = "pattern";
    public static final String DOCUMENT_ELM_ENDPOINT_BUCKET_SIZE = "bucket-size";
    public static final String DOCUMENT_ELM_ENDPOINT_STATUS_URL = "status-url";
    public static final String DOCUMENT_ELM_ENDPOINT_VERSION_URL = "version-url";
    public static final String DOCUMENT_ELM_ENDPOINT_ORDER = "order";
}
