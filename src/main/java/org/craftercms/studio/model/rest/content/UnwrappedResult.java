/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.model.rest.content;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.craftercms.studio.model.rest.Result;

/**
 * Convenience generic decorator that unwraps (this means the result properties will be serialized
 * as if they were properties of its containing object) the result entity.
 * This is useful when the rest response should contain the same attributes
 * as the service result, additional to the {@link org.craftercms.studio.model.rest.ApiResponse}.
 *
 * @param <T> the type of the result entity
 */
public class UnwrappedResult<T> extends Result {
    @JsonUnwrapped
    private final T result;

    public UnwrappedResult(T result) {
        this.result = result;
    }

    public T getResult() {
        return result;
    }

    public static <T> UnwrappedResult<T> of(T result) {
        return new UnwrappedResult<>(result);
    }
}
