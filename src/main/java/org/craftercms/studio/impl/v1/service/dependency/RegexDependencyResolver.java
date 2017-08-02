/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2017 Crafter Software Corporation.
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

package org.craftercms.studio.impl.v1.service.dependency;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DependencyResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexDependencyResolver implements DependencyResolver {

    private final static Logger logger = LoggerFactory.getLogger(RegexDependencyResolver.class);

    protected Map<String, List<String>> mimetypeRegexMap;
    protected ContentService contentService;

    @Override
    public Set<String> resolve(String site, String path, String mimetype, InputStream content) throws IOException {
        String contentString = IOUtils.toString(content);
        return resolve(site, path, mimetype, contentString);
    }

    @Override
    public Set<String> resolve(String site, String path, String mimetype, String content) {
        Set<String> toRet = new HashSet<String>();
        List<String> regexPatterns = mimetypeRegexMap.get(mimetype);
        if (CollectionUtils.isNotEmpty(regexPatterns)) {
            for (String regexPattern : regexPatterns) {
                Pattern pattern = Pattern.compile(regexPattern);
                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    String matchedPath = matcher.group();
                    if (contentService.contentExists(site, matchedPath)) {
                        toRet.add(matchedPath);
                    } else {
                        logger.info("Found reference to " + matchedPath + " in content at " + path + " but content does not exist in referenced path for site " + site);
                    }
                }
            }
        }
        return toRet;
    }

    public Map<String, List<String>> getMimetypeRegexMap() { return mimetypeRegexMap; }
    public void setMimetypeRegexMap(Map<String, List<String>> mimetypeRegexMap) { this.mimetypeRegexMap = mimetypeRegexMap; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }
}
