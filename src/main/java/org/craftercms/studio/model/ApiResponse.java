/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.model;

import org.apache.commons.lang3.StringUtils;

public class ApiResponse {

    public static final ApiResponse CODE_0 = new ApiResponse(0, "OK", StringUtils.EMPTY, StringUtils.EMPTY);
    public static final ApiResponse CODE_1 = new ApiResponse(1, "Created", StringUtils.EMPTY, StringUtils.EMPTY);
    public static final ApiResponse CODE_2 = new ApiResponse(2, "Deleted", StringUtils.EMPTY, StringUtils.EMPTY);

    public static final ApiResponse CODE_1000 =
            new ApiResponse(1000, "Internal system failure", "Contact support", StringUtils.EMPTY);
    public static final ApiResponse CODE_1001 = new ApiResponse(1001, "Invalid parameter(s)",
                    "Check API and make sure you're sending the correct parameters", StringUtils.EMPTY);

    public static final ApiResponse CODE_2000 = new ApiResponse(2000, "Unauthenticated", "Please login first", StringUtils.EMPTY);
    public static final ApiResponse CODE_2001 = new ApiResponse(2001, "Unauthorized",
            "You don't have permission to perform this task, please contact your administrator", StringUtils.EMPTY);

    public static final ApiResponse CODE_3000 = new ApiResponse(3000, "Organization not found",
            "Check if you sent in the right Org Id", StringUtils.EMPTY);
    public static final ApiResponse CODE_3001 = new ApiResponse(3001, "Organization already exists",
            "Try a different organization name", StringUtils.EMPTY);

    public static final ApiResponse CODE_4000 = new ApiResponse(4000, "Group not found",
            "Check if you sent in the right Group Id", StringUtils.EMPTY);

    public static final ApiResponse CODE_5000 = new ApiResponse(5000, "Project not found",
            "Check if you sent in the right Project Id", StringUtils.EMPTY);
    public static final ApiResponse CODE_5001 = new ApiResponse(5001, "Project already exists",
            "Try a different project name", StringUtils.EMPTY);

    public static final ApiResponse CODE_6000 = new ApiResponse(6000, "User not found",
            "Check if you're using the correct User ID", StringUtils.EMPTY);
    public static final ApiResponse CODE_6001 = new ApiResponse(6001, "User already exists" ,
            "Try a different username", StringUtils.EMPTY);
    public static final ApiResponse CODE_6002 = new ApiResponse(6002, "User is externally managed",
            "Update the user in the main identity system (e.g. LDAP)", StringUtils.EMPTY);

    public static final ApiResponse CODE_7000 = new ApiResponse(7000, "Content not found",
            "Check if you sent in the right Content Id", StringUtils.EMPTY);
    public static final ApiResponse CODE_7001 = new ApiResponse(7001, "Content already exists",
            "Advise the user that the content already exists", StringUtils.EMPTY);

    public static final ApiResponse CODE_8000 = new ApiResponse(8000, "Publishing is disabled",
            "Advise the user to enable publishing", StringUtils.EMPTY);

    public static final ApiResponse CODE_9000 = new ApiResponse(9000, "Search is unreachable",
            "Advise the user that the search engine is not reachable", StringUtils.EMPTY);

    public static final ApiResponse CODE_10000 = new ApiResponse(10000, "LoV not found",
            "Check if you sent in the right LoV Id", StringUtils.EMPTY);

    public static final ApiResponse CODE_50000 = new ApiResponse(50000, "CMIS server is unreachable",
            "Advise the user that the CMIS server is not reachable", StringUtils.EMPTY);

    public static final ApiResponse CODE_51000 = new ApiResponse(51000, "Box is unreachable",
            "Advise the user that Box is not reachable", StringUtils.EMPTY);

    public static final ApiResponse CODE_52000 = new ApiResponse(52000, "AWS is unreachable",
            "Advise the user that AWS engine is not reachable", StringUtils.EMPTY);

    private int code;
    private String message;
    private String remedialAction;
    private String documentationUrl;

    public ApiResponse() {
    }

    private ApiResponse(int code, String message, String remedialAction, String documentationUrl) {
        this.code = code;
        this.message = message;
        this.remedialAction = remedialAction;
        this.documentationUrl = documentationUrl;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRemedialAction() {
        return remedialAction;
    }

    public void setRemedialAction(String remedialAction) {
        this.remedialAction = remedialAction;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }
}
