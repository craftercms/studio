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

/**
 * Holds the result of a site policy validation
 *
 * @author joseross
 * @since 4.0.0
 */
public class ValidationResult {

    public static ValidationResult allowed(Action action) {
        return allowedWithModifications(action, null);
    }

    public static ValidationResult allowedWithModifications(Action action, String modifiedValue) {
        ValidationResult result = new ValidationResult();
        result.allowed = true;
        result.target = action.target;
        result.type = action.type;
        result.modifiedValue = modifiedValue;
        return result;
    }

    public static ValidationResult notAllowed(Action action) {
        ValidationResult result = new ValidationResult();
        result.target = action.target;
        result.type = action.type;
        return result;
    }

    /**
     * The type of the action
     */
    protected Type type;

    /**
     * The target of the action
     */
    protected String target;

    /**
     * Indicates if the action is allowed
     */
    protected boolean allowed;

    /**
     * Indicates the modified target if there is one
     */
    protected String modifiedValue;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public String getModifiedValue() {
        return modifiedValue;
    }

    public void setModifiedValue(String modifiedValue) {
        this.modifiedValue = modifiedValue;
    }

}
