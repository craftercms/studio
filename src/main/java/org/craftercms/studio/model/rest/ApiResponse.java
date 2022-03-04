/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.model.rest;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents the response of an API operation.
 *
 * @author Dejan Brkic
 * @author avasquez
 */
public class ApiResponse {

    // 1 - 1000
    public static final ApiResponse OK =
            new ApiResponse(0, "OK", StringUtils.EMPTY, StringUtils.EMPTY);
    public static final ApiResponse CREATED =
            new ApiResponse(1, "Created", StringUtils.EMPTY, StringUtils.EMPTY);
    public static final ApiResponse DELETED =
            new ApiResponse(2, "Deleted", StringUtils.EMPTY, StringUtils.EMPTY);

    // 1000 - 2000
    public static final ApiResponse INTERNAL_SYSTEM_FAILURE =
            new ApiResponse(1000, "Internal system failure", "Contact support", StringUtils.EMPTY);
    public static final ApiResponse INVALID_PARAMS = new ApiResponse(1001, "Invalid parameter(s)",
            "Check API and make sure you're sending the correct parameters", StringUtils.EMPTY);
    public static final ApiResponse DEPRECATED = new ApiResponse(1002, "Deprecated",
            "This API has been deprecated", StringUtils.EMPTY);

    // 2000 - 3000
    public static final ApiResponse UNAUTHENTICATED =
            new ApiResponse(2000, "Unauthenticated", "Please login first", StringUtils.EMPTY);
    public static final ApiResponse UNAUTHORIZED = new ApiResponse(2001, "Unauthorized",
            "You don't have permission to perform this task, please contact your administrator", StringUtils.EMPTY);

    // 3000 - 4000
    public static final ApiResponse ORG_NOT_FOUND = new ApiResponse(3000, "Organization not found",
            "Check if you sent in the right Org Id", StringUtils.EMPTY);
    public static final ApiResponse ORG_ALREADY_EXISTS = new ApiResponse(3001, "Organization already exists",
            "Try a different organization name", StringUtils.EMPTY);

    // 4000 - 5000
    public static final ApiResponse GROUP_NOT_FOUND = new ApiResponse(4000, "Group not found",
            "Check if you sent in the right Group Id", StringUtils.EMPTY);
    public static final ApiResponse GROUP_ALREADY_EXISTS = new ApiResponse(4001, "Group already exists",
            "Try a different group name", StringUtils.EMPTY);

    // 5000 - 6000
    public static final ApiResponse SITE_NOT_FOUND = new ApiResponse(5000, "Site not found",
            "Check if you sent in the right Site Id", StringUtils.EMPTY);
    public static final ApiResponse SITE_ALREADY_EXISTS = new ApiResponse(5001, "Site already exists",
            "Try a different site name", StringUtils.EMPTY);

    // 6000 - 7000
    public static final ApiResponse USER_NOT_FOUND = new ApiResponse(6000, "User not found",
            "Check if you're using the correct User ID", StringUtils.EMPTY);
    public static final ApiResponse USER_ALREADY_EXISTS = new ApiResponse(6001, "User already exists" ,
            "Try a different username", StringUtils.EMPTY);
    public static final ApiResponse USER_EXTERNALLY_MANAGED = new ApiResponse(6002, "User is externally managed",
            "Update the user in the main identity system (e.g. LDAP)", StringUtils.EMPTY);
    public static final ApiResponse USER_PASSWORD_REQUIREMENTS_FAILED =
            new ApiResponse(6003, "User password does not fulfill requirements",
                    "Use password that will fulfill password requirements", StringUtils.EMPTY);
    public static final ApiResponse USER_PASSWORD_DOES_NOT_MATCH =
            new ApiResponse(6004, "User current password does not match",
                    "Use correct current password", StringUtils.EMPTY);

    // 7000 - 8000
    public static final ApiResponse CONTENT_NOT_FOUND = new ApiResponse(7000, "Content not found",
            "Check if you sent in the right Content Id", StringUtils.EMPTY);
    public static final ApiResponse CONTENT_ALREADY_EXISTS = new ApiResponse(7001, "Content already exists",
            "Edit the existing item or delete it before creating it again", StringUtils.EMPTY);
    public static final ApiResponse CONTENT_ALREADY_LOCKED = new ApiResponse(7002, "Content already locked",
            "The user that locked the item or the administrator must unlock the item first", StringUtils.EMPTY);
    public static final ApiResponse CONTENT_ALREADY_UNLOCKED = new ApiResponse(7003, "Content already unlocked",
            "The item is already unlocked", StringUtils.EMPTY);

    // 8000 - 9000
    public static final ApiResponse PUBLISHING_DISABLED = new ApiResponse(8000, "Publishing is disabled",
            "Advise the user to enable publishing", StringUtils.EMPTY);

    // 9000 - 10000
    public static final ApiResponse SEARCH_UNREACHABLE = new ApiResponse(9000, "Search is unreachable",
            "Advise the user that the search engine is not reachable", StringUtils.EMPTY);

    // 10000 - 11000
    public static final ApiResponse LOV_NOT_FOUND = new ApiResponse(10000, "LoV not found",
            "Check if you sent in the right LoV Id", StringUtils.EMPTY);

    // 11000 - 12000
    public static final ApiResponse CLUSTER_MEMBER_NOT_FOUND = new ApiResponse(11000, "Cluster member not found",
            "Check if you sent in the right Cluster Member Id", StringUtils.EMPTY);
    public static final ApiResponse CLUSTER_MEMBER_ALREADY_EXISTS =
            new ApiResponse(11001, "Cluster member already exists",
                    "Get the list of cluster members to validate", StringUtils.EMPTY);

    // 12000 - 13000
    public static final ApiResponse REMOTE_REPOSITORY_NOT_FOUND = new ApiResponse(12000, "Remote repository not found",
                    "Check if you sent in the right remote repository name", StringUtils.EMPTY);
    public static final ApiResponse REMOTE_REPOSITORY_ALREADY_EXISTS =
            new ApiResponse(12001, "Remote repository already exists",
                    "Get the list of remote repositories to validate", StringUtils.EMPTY);
    public static final ApiResponse PULL_FROM_REMOTE_REPOSITORY_CONFLICT =
            new ApiResponse(12002, "Pull from remote repository resulted in conflict",
                    "Resolve conflicts before continuing work with repository", StringUtils.EMPTY);
    public static final ApiResponse ADD_REMOTE_INVALID =
            new ApiResponse(12003, "Remote is invalid. Not added to remote repositories",
                    "Add new remote repository with valid parameters.", StringUtils.EMPTY);
    public static final ApiResponse REMOVE_REMOTE_FAILED =
            new ApiResponse(12004, "Failed to remove remote repository",
                    "Contact your system administrator.", StringUtils.EMPTY);
    public static final ApiResponse PUSH_TO_REMOTE_FAILED =
            new ApiResponse(12005, "Push to remote repository failed",
                    "Check your repository settings or contact your system administrator.", StringUtils.EMPTY);
    public static final ApiResponse REMOTE_REPOSITORY_NOT_REMOVABLE =
            new ApiResponse(12006, "Failed to remove remote repository",
                    "Remote repository is cluster node repository. Can't be removed.", StringUtils.EMPTY);
    public static final ApiResponse REMOTE_REPOSITORY_AUTHENTICATION_FAILED =
            new ApiResponse(12007, "Remote repository authentication failed",
                    "Recreate the remote repository with the correct authentication credentials " +
                    "and make sure you have write access.", StringUtils.EMPTY);

    // 40000 - 41000
    public static final ApiResponse MARKETPLACE_NOT_INITIALIZED =
            new ApiResponse(40000, "Marketplace service is not initialized",
                    "Contact your system administrator.", StringUtils.EMPTY);

    public static final ApiResponse MARKETPLACE_UNREACHABLE =
            new ApiResponse(40001, "Marketplace server is unreachable",
                    "Check the configuration to make sure the Marketplace URL is correct", StringUtils.EMPTY);

    public static final ApiResponse PLUGIN_ALREADY_INSTALLED =
            new ApiResponse(40002, "Plugin is already installed",
                    "Check that the site id, plugin id and plugin version are correct", StringUtils.EMPTY);

    public static final ApiResponse PLUGIN_INSTALLATION_ERROR =
            new ApiResponse(40003, "Error installing plugin",
                    "Check the plugin requirements", StringUtils.EMPTY);

    // 50000 - 51000
    public static final ApiResponse CMIS_UNREACHABLE = new ApiResponse(50000, "CMIS server is unreachable",
            "Advise the user that the CMIS server is not reachable", StringUtils.EMPTY);
    public static final ApiResponse CMIS_TIMEOUT = new ApiResponse(50001, "Request to CMIS server timed out",
            "Advise the user that the request to CMIS server timed out", StringUtils.EMPTY);
    public static final ApiResponse CMIS_NOT_FOUND = new ApiResponse(50002, "CMIS server was not found",
            "Advise the user that the CMIS server was not found", StringUtils.EMPTY);
    public static final ApiResponse CMIS_STUDIO_PATH_NOT_FOUND =
            new ApiResponse(50003, "Target path does not exist in site repository",
                    "Check if you sent in the right Studio Path", StringUtils.EMPTY);

    // 51000 - 52000
    public static final ApiResponse BOX_UNREACHABLE = new ApiResponse(51000, "Box is unreachable",
            "Advise the user that Box is not reachable", StringUtils.EMPTY);

    // 52000 - 53000
    public static final ApiResponse AWS_UNREACHABLE = new ApiResponse(52000, "AWS is unreachable",
            "Advise the user that AWS engine is not reachable", StringUtils.EMPTY);

    private int code;
    private String message;
    private String remedialAction;
    private String documentationUrl;

    public ApiResponse(ApiResponse response) {
        code = response.code;
        message = response.message;
        remedialAction = response.remedialAction;
        documentationUrl = response.documentationUrl;
    }

    private ApiResponse(int code, String message, String remedialAction, String documentationUrl) {
        this.code = code;
        this.message = message;
        this.remedialAction = remedialAction;
        this.documentationUrl = documentationUrl;
    }

    /**
     * Returns the response code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Sets the response code.
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Returns the detailed message of the response.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the detailed message of the response.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns what the user can do in order to address the issue indicated by the response.
     */
    public String getRemedialAction() {
        return remedialAction;
    }

    /**
     * Sets what the user can do in order to address the issue indicated by the response.
     */
    public void setRemedialAction(String remedialAction) {
        this.remedialAction = remedialAction;
    }

    /**
     * Returns the URL to documentation related to the response.
     */
    public String getDocumentationUrl() {
        return documentationUrl;
    }

    /**
     * Sets the URL to documentation related to the response.
     */
    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
               "code=" + code +
               ", message='" + message + '\'' +
               ", remedialAction='" + remedialAction + '\'' +
               ", documentationUrl='" + documentationUrl + '\'' +
               '}';
    }

}
