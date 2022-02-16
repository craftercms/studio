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
package org.craftercms.studio.model.policy;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Represents a content action that needs to be validated
 *
 * @author joseross
 * @since 4.0.0
 */
public class Action {

    public static final String METADATA_FILE_SIZE = "fileSize";
    public static final String METADATA_CONTENT_TYPE = "contentType";

    /**
     * The type of action
     */
    @NotNull
    protected Type type;

    /**
     * The source of the action
     */
    protected String source;

    /**
     * The target of the action
     */
    @NotEmpty
    protected String target;

    /**
     * Indicates if the action is recursive
     */
    protected boolean recursive = false;

    protected Map<String, Object> contentMetadata = emptyMap();

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public Map<String, Object> getContentMetadata() {
        return contentMetadata;
    }

    public void setContentMetadata(Map<String, Object> contentMetadata) {
        this.contentMetadata = contentMetadata;
    }

    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key) {
        return (T) contentMetadata.get(key);
    }

    public boolean containsMetadata(String key) {
        return contentMetadata.containsKey(key);
    }

    @Override
    public String toString() {
        return "Action{" +
                "type=" + type +
                ", source='" + source + '\'' +
                ", target='" + target + '\'' +
                ", recursive=" + recursive +
                '}';
    }

}
