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

package org.craftercms.studio.impl.v2.utils.spring.security;

import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.beans.ConstructorProperties;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Utility class that wraps multiple {@link RegexRequestMatcher}s with an {@link OrRequestMatcher}
 *
 * @author joseross
 */
public class OrRegexRequestMatcher implements RequestMatcher {

    protected RequestMatcher requestMatcher;

    @ConstructorProperties({"patterns"})
    public OrRegexRequestMatcher(String... patterns) {
        requestMatcher = new OrRequestMatcher(
                Stream.of(patterns)
                        .map(pattern -> new RegexRequestMatcher(pattern, null))
                        .collect(toList())
        );
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return requestMatcher.matches(request);
    }
}