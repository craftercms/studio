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

package org.craftercms.studio.impl.v1.service.event;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.ebus.*;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventServiceImpl implements EventService, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);

    protected ApplicationContext applicationContext;

    private Map<String, List<EventSubscriber>> eventListeners = new HashMap<String, List<EventSubscriber>>();


    @Override
    @ValidateParams
    public void publish(@ValidateStringParam(name = "event") String event, Object... args) {
        logger.debug(String.format("Publishing %s", event));

        List<EventSubscriber> listenersForEvent = getListenersForEvent(event, false);
        if (listenersForEvent != null) {
            for (EventSubscriber listener : listenersForEvent) {
                Object bean = applicationContext.getBean(listener.getBeanName());
                Method method = listener.getMethod();
                try {
                    method.invoke(bean, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.error("Error invoking listeners method for Publishing event: " + listener.getBeanName() + " - " + listener.getMethod());
                }
            }
        }
    }

    protected List<EventSubscriber> getListenersForEvent(String event, boolean create) {
        List<EventSubscriber> listeners = eventListeners.get(event);
        if (listeners == null && create) {
            listeners = new ArrayList<EventSubscriber>();
            eventListeners.put(event, listeners);
        }
        return listeners;
    }

    @Override
    @ValidateParams
    public void subscribe(@ValidateStringParam(name = "event") String event, @ValidateStringParam(name = "listener") String listener, Method method) {
        logger.info(String.format("Subscribing %s to %s", listener, event));
        EventSubscriber subscriber = new EventSubscriber(listener, method);
        getListenersForEvent(event, true).add(subscriber);
    }

    @Override
    @ValidateParams
    public void unSubscribe(@ValidateStringParam(name = "event") String event, @ValidateStringParam(name = "listener") String listener) {
        logger.debug(String.format("UnSubscribing %s to %s", listener, event));
        List<EventSubscriber> listeners = getListenersForEvent(event, false);
        for (EventSubscriber subscriber : listeners) {
            if (StringUtils.equals(subscriber.getBeanName(), listener)) {
                listeners.remove(subscriber);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
