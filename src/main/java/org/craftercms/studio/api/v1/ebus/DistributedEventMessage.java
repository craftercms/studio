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

package org.craftercms.studio.api.v1.ebus;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DistributedEventMessage {

    protected String eventKey;
    protected Class messageClass;
    protected Object message;

    public String getEventKey() { return eventKey; }
    public void setEventKey(String eventKey) { this.eventKey = eventKey; }

    public Object getMessage() { return message; }
    public void setMessage(Object message) { this.message = message; }

    public Class getMessageClass() { return messageClass; }
    public void setMessageClass(Class messageClass) { this.messageClass = messageClass; }

    public <T> T getMessage(Class<T> clazz) { return clazz.cast(message); }

    public DistributedEventMessage() { }

    @JsonCreator
    public DistributedEventMessage(@JsonProperty("eventKey") String eventKey, @JsonProperty("messageClass") Class messageClass, @JsonProperty("message") Object message) {
        super();
        this.eventKey = eventKey;
        this.messageClass = messageClass;
        this.message = message;
    }
}
