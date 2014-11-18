/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.dm.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.craftercms.cstudio.alfresco.dm.to.DmContentItemTO;

public class GoLiveQueue implements Serializable {

    private static final long serialVersionUID = 2209675182536998467L;

    protected Map<String, DmContentItemTO> map = new HashMap<String, DmContentItemTO>();
    GoLiveQueue(Set<DmContentItemTO> queue) {
        for (DmContentItemTO itemTO : queue) {
            map.put(itemTO.getUri(),itemTO);
        }
    }

    public GoLiveQueue() { }

    public Set<DmContentItemTO> getQueue() {
        Set<DmContentItemTO>set=new HashSet<DmContentItemTO>();
        Collection<DmContentItemTO> itemTOCollection = map.values();
        for (DmContentItemTO to : itemTOCollection) {
            DmContentItemTO to1 = new DmContentItemTO(to, false);
            set.add(to1);
        }
        return set;
    }

    public boolean remove(String uri) {
        DmContentItemTO to = map.get(uri);
        map.remove(uri);
        return true;
    }

    public boolean add(DmContentItemTO contentItemTO) {

        DmContentItemTO to = new DmContentItemTO(contentItemTO, false);
        map.put(contentItemTO.getUri(), to);
        return true;
    }

    public boolean add(String key,DmContentItemTO contentItemTO) {
        DmContentItemTO to = new DmContentItemTO(contentItemTO,false);
        map.put(key,to);
        return true;
    }

    public boolean contains(DmContentItemTO to) {
        return map.containsKey(to.getUri());
    }

    public boolean contains(String uri) {
        return map.containsKey(uri);
    }
}
