/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.cstudio.alfresco.event;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class EventServiceImpl extends AbstractRegistrableService implements EventService, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventServiceImpl.class);

    protected  ApplicationContext applicationContext;

    private Map<String, List<EventSubscriber>> eventListeners = new FastMap<String, List<EventSubscriber>>();

    @Override
    public void publish(String event, Object... args) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Publishing %s", event));
        }
        List<EventSubscriber> listenersForEvent = getListenersForEvent(event, false);
        if (listenersForEvent != null) {
            for (EventSubscriber listener : listenersForEvent) {
                Object bean = applicationContext.getBean(listener.getBeanName());
                Method method = listener.getMethod();
                try {
                    method.invoke(bean, args);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void subscribe(String event, String listener, Method method) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format("Subscribing %s to %s", listener, event));
        }
        EventSubscriber subscriber = new EventSubscriber(listener, method);
        getListenersForEvent(event, true).add(subscriber);
    }

    protected List<EventSubscriber> getListenersForEvent(String event, boolean create) {
        List<EventSubscriber> listeners = eventListeners.get(event);
        if (listeners == null && create) {
            listeners = new FastList<EventSubscriber>();
            eventListeners.put(event, listeners);
        }
        return listeners;
    }

    @Override
    public void unSubscribe(String event, String listener) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("UnSubscribing %s to %s", listener, event));
        }
        getListenersForEvent(event, false).remove(listener);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void register() {
        getServicesManager().registerService(EventService.class, this);
    }
}
