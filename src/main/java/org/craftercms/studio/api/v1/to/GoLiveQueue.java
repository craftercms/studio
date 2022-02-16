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
package org.craftercms.studio.api.v1.to;

import java.io.Serializable;
import java.util.*;

public class GoLiveQueue implements Serializable {

    private static final long serialVersionUID = 2209675182536998467L;

    protected Map<String, ContentItemTO> map = new HashMap<String, ContentItemTO>();

    GoLiveQueue(Set<ContentItemTO> queue) {
        for (ContentItemTO itemTO : queue) {
            map.put(itemTO.getUri(),itemTO);
        }
    }

    public GoLiveQueue() { }

    public Set<ContentItemTO> getQueue() {
        Set<ContentItemTO> set = new HashSet<ContentItemTO>();
        Collection<ContentItemTO> itemTOCollection = map.values();
        for (ContentItemTO to : itemTOCollection) {
            ContentItemTO to1 = new ContentItemTO(to, false);
            set.add(to1);
        }
        return set;
    }

    public boolean remove(String uri) {
        ContentItemTO to = map.get(uri);
        map.remove(uri);
        return true;
    }

    public boolean add(ContentItemTO contentItemTO) {

        ContentItemTO to = new ContentItemTO(contentItemTO, false);
        map.put(contentItemTO.getUri(), to);
        return true;
    }

    public boolean add(String key,ContentItemTO contentItemTO) {
        ContentItemTO to = new ContentItemTO(contentItemTO,false);
        map.put(key,to);
        return true;
    }

    public boolean contains(ContentItemTO to) {
        return map.containsKey(to.getUri());
    }

    public boolean contains(String uri) {
        return map.containsKey(uri);
    }
}
