
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

package scripts.api.impl.objectstate;

class SpringObjectStateServices {

    def context = null

    /**
     * constructor
     *
     * @param context - service context
     */
    def SpringObjectStateServices(context) {
        this.context = context
    }

    def getItemStates(site, states) {
        def springBackedService = this.context.applicationContext.get("cstudioObjectStateService");
        return springBackedService.getObjectStateByStates(site, states);
    }

    def setObjectState(site, path, state, systemprocessing) {
        def springBackedService = this.context.applicationContext.get("cstudioObjectStateService");
        return springBackedService.setObjectState(site, path, state, systemprocessing);
    }
}
