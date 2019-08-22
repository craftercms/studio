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

package org.craftercms.studio.model.rest;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents the response of an API operation.
 *
 * @author Dejan Brkic
 * @author avasquez
 */
public class ApiResponse {

    public static final ApiResponse OK =
            new ApiResponse(0, "OK", StringUtils.EMPTY, StringUtils.EMPTY);
    public static final ApiResponse CREATED =
            new ApiResponse(1, "Created", StringUtils.EMPTY, StringUtils.EMPTY);
    public static final ApiResponse DELETED =
            new ApiResponse(2, "Deleted", StringUtils.EMPTY, StringUtils.EMPTY);

    public static final ApiResponse INTERNAL_SYSTEM_FAILURE =
            new ApiResponse(1000, "Internal system failure", "Contact support", StringUtils.EMPTY);
    public static final ApiResponse INVALID_PARAMS =
            new ApiResponse(1001, "Invalid parameter(s)", "Check API and make sure you're sending the correct " +
                                                          "parameters", StringUtils.EMPTY);

    public static final ApiResponse UNAUTHENTICATED =
            new ApiResponse(2000, "Unauthenticated", "Please login first", StringUtils.EMPTY);
    public static final ApiResponse UNAUTHORIZED =
            new ApiResponse(2001, "Unauthorized", "You don't have permission to perform this task, please contact " +
                                                  "your administrator", StringUtils.EMPTY);

    public static final ApiResponse ORG_NOT_FOUND =
            new ApiResponse(3000, "Organization not found", "Check if you sent in the right Org Id", StringUtils.EMPTY);
    public static final ApiResponse ORG_ALREADY_EXISTS =
            new ApiResponse(3001, "Organization already exists", "Try a different organization name",
                            StringUtils.EMPTY);

    public static final ApiResponse GROUP_NOT_FOUND =
            new ApiResponse(4000, "Group not found", "Check if you sent in the right Group Id", StringUtils.EMPTY);
    public static final ApiResponse GROUP_ALREADY_EXISTS =
            new ApiResponse(4001, "Group already exists", "Try a different group name", StringUtils.EMPTY);

    public static final ApiResponse PROJECT_NOT_FOUND =
            new ApiResponse(5000, "Project not found", "Check if you sent in the right Project Id", StringUtils.EMPTY);
    public static final ApiResponse PROJECT_ALREADY_EXISTS =
            new ApiResponse(5001, "Project already exists", "Try a different project name", StringUtils.EMPTY);

    public static final ApiResponse USER_NOT_FOUND =
            new ApiResponse(6000, "User not found", "Check if you're using the correct User ID", StringUtils.EMPTY);
    public static final ApiResponse USER_ALREADY_EXISTS =
            new ApiResponse(6001, "User already exists" , "Try a different username", StringUtils.EMPTY);
    public static final ApiResponse USER_EXTERNALLY_MANAGED =
            new ApiResponse(6002, "User is externally managed", "Update the user in the main identity system " +
                                                                "(e.g. LDAP)", StringUtils.EMPTY);
    public static final ApiResponse USER_PASSWORD_REQUIREMENTS_FAILED =
            new ApiResponse(6003, "User password does not fullfill requirements",
                    "Use password that will fullfill password requirements", StringUtils.EMPTY);

    public static final ApiResponse USER_PASSWORD_DOES_NOT_MATCH =
            new ApiResponse(6004, "User current password does not match",
                    "Use correct current password", StringUtils.EMPTY);

    public static final ApiResponse CONTENT_NOT_FOUND =
            new ApiResponse(7000, "Content not found", "Check if you sent in the right Content Id", StringUtils.EMPTY);
    public static final ApiResponse CONTENT_ALREADY_EXISTS =
            new ApiResponse(7001, "Content already exists", "Advise the user that the content already exists",
                            StringUtils.EMPTY);

    public static final ApiResponse PUBLISHING_DISABLED =
            new ApiResponse(8000, "Publishing is disabled", "Advise the user to enable publishing", StringUtils.EMPTY);

    public static final ApiResponse SEARCH_UNREACHABLE =
            new ApiResponse(9000, "Search is unreachable", "Advise the user that the search engine is not " +
                                                           "reachable", StringUtils.EMPTY);

    public static final ApiResponse LOV_NOT_FOUND =
            new ApiResponse(10000, "LoV not found", "Check if you sent in the right LoV Id", StringUtils.EMPTY);

    public static final ApiResponse CLUSTER_MEMBER_NOT_FOUND =
            new ApiResponse(11000, "Cluster member not found", "Check if you sent in the right Cluster Member Id",
                    StringUtils.EMPTY);

    public static final ApiResponse CLUSTER_MEMBER_ALREADY_EXISTS =
            new ApiResponse(11001, "Cluster member already exists", "Get the list of cluster members to validate",
                    StringUtils.EMPTY);

    public static final ApiResponse REMOTE_REPOSITORY_NOT_FOUND =
            new ApiResponse(12000, "Remote repository not found",
                    "Check if you sent in the right remote repository name", StringUtils.EMPTY);

    public static final ApiResponse REMOTE_REPOSITORY_ALREADY_EXISTS =
            new ApiResponse(12001, "Remote repository already exists",
                    "Get the list of remote repositories to validate", StringUtils.EMPTY);

    public static final ApiResponse PULL_FROM_REMOTE_REPOSITORY_CONFLICT =
            new ApiResponse(12002, "Pull from remote repository resulted in conflict", "Resolve conflicts before " +
                    "continue working with repositroy", StringUtils.EMPTY);

    public static final ApiResponse CMIS_UNREACHABLE =
            new ApiResponse(50000, "CMIS server is unreachable", "Advise the user that the CMIS server is not " +
                                                                 "reachable", StringUtils.EMPTY);

    public static final ApiResponse CMIS_TIMEOUT =
            new ApiResponse(50001, "Request to CMIS server timed out", "Advise the user that the request to CMIS " +
                    "server timed out", StringUtils.EMPTY);

    public static final ApiResponse CMIS_NOT_FOUND =
            new ApiResponse(50002, "CMIS server was not found", "Advise the user that the CMIS server was not " +
                    "found", StringUtils.EMPTY);

    public static final ApiResponse BOX_UNREACHABLE =
            new ApiResponse(51000, "Box is unreachable", "Advise the user that Box is not reachable", StringUtils.EMPTY);

    public static final ApiResponse AWS_UNREACHABLE =
            new ApiResponse(52000, "AWS is unreachable", "Advise the user that AWS engine is not reachable",
                            StringUtils.EMPTY);

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
