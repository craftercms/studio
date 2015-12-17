/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2015 Crafter Software Corporation.
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
import scripts.api.ObjectStateServices;

def result = [:];
def site = params.site;
def state = params.state;

def states = new java.util.ArrayList();

if(state != "ALL") {
    states.add(state);
}
else {
    states.add("NEW_UNPUBLISHED_LOCKED");
    states.add("NEW_UNPUBLISHED_UNLOCKED");
    states.add("NEW_SUBMITTED_WITH_WF_SCHEDULED");
    states.add("NEW_SUBMITTED_WITH_WF_SCHEDULED_LOCKED");
    states.add("NEW_SUBMITTED_WITH_WF_UNSCHEDULED");
    states.add("NEW_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED");
    states.add("NEW_SUBMITTED_NO_WF_SCHEDULED");
    states.add("NEW_SUBMITTED_NO_WF_SCHEDULED_LOCKED");
    states.add("NEW_SUBMITTED_NO_WF_UNSCHEDULED");
    states.add("NEW_PUBLISHING_FAILED");
    states.add("NEW_DELETED");
    states.add("EXISTING_UNEDITED_LOCKED");
    states.add("EXISTING_UNEDITED_UNLOCKED");
    states.add("EXISTING_EDITED_LOCKED");
    states.add("EXISTING_EDITED_UNLOCKED");
    states.add("EXISTING_SUBMITTED_WITH_WF_SCHEDULED");
    states.add("EXISTING_SUBMITTED_WITH_WF_SCHEDULED_LOCKED");
    states.add("EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED");
    states.add("EXISTING_SUBMITTED_WITH_WF_UNSCHEDULED_LOCKED");
    states.add("EXISTING_SUBMITTED_NO_WF_SCHEDULED");
    states.add("EXISTING_SUBMITTED_NO_WF_SCHEDULED_LOCKED");
    states.add("EXISTING_SUBMITTED_NO_WF_UNSCHEDULED");
    states.add("EXISTING_PUBLISHING_FAILED");
    states.add("EXISTING_DELETED");
}
def context = ObjectStateServices.createContext(applicationContext, request);
result.items = ObjectStateServices.getItemStates(context, site, states);
return result;


