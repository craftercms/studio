/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.dal.security;

import java.util.HashMap;
import java.util.Map;

public enum PerformableAction {

    READ(1);

    private int value;

    private static Map<Integer, PerformableAction> map = new HashMap<Integer, PerformableAction>();

    private PerformableAction(int value) {
        this.value = value;
    }

    static {
        for (PerformableAction performableAction : PerformableAction.values()) {
            map.put(performableAction.value, performableAction);
        }
    }

    public static PerformableAction valueOf(int performableAction) {
        return map.get(performableAction);
    }

    public int getValue() {
        return value;
    }
}
