/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.annotation;

import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class StudioAnnotationUtils {

    /**
     * Get annotation value from an annotation class instance
     * @param pjp proceeding join point object
     * @param method method to read
     * @param annotationClass annotation class
     * @param returnType return of annotation value type
     * @return annotation value
     */
    public static <T> T getAnnotationValue(final ProceedingJoinPoint pjp, final Method method, Class<?> annotationClass, Class<T> returnType) {
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Object[] params = pjp.getArgs();
        for (int i = 0; i < paramAnnotations.length; i++) {
            for (Annotation a : paramAnnotations[i]) {
                if (annotationClass.isInstance(a)) {
                    return returnType.cast(params[i]);
                }
            }
        }
        return null;
    }
}
