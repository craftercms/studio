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
package org.craftercms.studio.api.v1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ServicesManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServicesManager.class);

    protected Map<Class, Object> _servicesMap = new HashMap<Class, Object>();
    public Map<Class, Object> getServicesMap() {
        return _servicesMap;
    }
    public void setServicesMap(Map<Class, Object> servicesMap) {
        this._servicesMap = servicesMap;
    }

    public void registerService(Class clazz, Object service) {
        if (_servicesMap == null) {
            _servicesMap = new HashMap<Class, Object>();
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Registering service: " + clazz.getName());
        }
        _servicesMap.put(clazz, service);
    }

    public <T> T getService(Class<T> clazz) {
        return clazz.cast(_servicesMap.get(clazz));
    }
}
