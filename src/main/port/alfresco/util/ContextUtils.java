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
package org.craftercms.cstudio.alfresco.util;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.springframework.aop.framework.Advised;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class ContextUtils {

    public static Map<String, List<Method>> findServicesWithMethodAnnotation(ApplicationContext applicationContext, Class<? extends Annotation> annotation) {
        Map<String, List<Method>> beanMap = new FastMap<String, List<Method>>();
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        if (allBeanNames != null) {
            for (String beanName : allBeanNames) {
                Object listener = applicationContext.getBean(beanName);
                Class<?> listenerType = listener.getClass();
                if (Advised.class.isAssignableFrom(listenerType)) {
                    listenerType = ((Advised) listener).getTargetSource().getTargetClass();
                }
                Method[] methods = listenerType.getMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(annotation)) {
                        List<Method> methodList = beanMap.get(beanName);
                        if (methodList == null) {
                            methodList = new FastList<Method>();
                            beanMap.put(beanName, methodList);
                        }
                        methodList.add(method);
                    }
                }
            }
        }
        return beanMap;
    }
}
