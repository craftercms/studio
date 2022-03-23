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
package org.craftercms.studio.api.v2.event;

import org.craftercms.studio.model.rest.Person;
import org.springframework.security.core.Authentication;

/**
 * Base class for all events
 *
 * @author joseross
 * @since 4.0.0
 */
public abstract class StudioEvent {

    /**
     * The current time in ms when the event was triggered
     */
    protected final long timestamp;

    /**
     * The user that triggered the event, may be null for system events
     */
    protected final Person user;

    public StudioEvent(Authentication authentication) {
        this(Person.from(authentication));
    }

    public StudioEvent(Person person) {
        timestamp = System.currentTimeMillis();
        this.user = person;
    }

    public StudioEvent() {
        this((Person) null);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Person getUser() {
        return user;
    }

}
