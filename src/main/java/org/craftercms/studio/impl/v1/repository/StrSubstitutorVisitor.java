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

package org.craftercms.studio.impl.v1.repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.apache.commons.text.matcher.StringMatcher;
import org.apache.commons.text.matcher.StringMatcherFactory;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

/**
 * Implementation of {@link FileVisitor} that replaces values in the found files
 *
 * @author joseross
 * @since 3.1.4
 */
public class StrSubstitutorVisitor implements FileVisitor<Path> {

    private static final Logger logger = LoggerFactory.getLogger(StrSubstitutorVisitor.class);

    public static final StringMatcher PREFIX = StringMatcherFactory.INSTANCE.stringMatcher("${plugin:");

    protected StringSubstitutor strSubstitutor;

    public StrSubstitutorVisitor(Map<String, String> variables) {
        Map<String, String> escapedVars = new HashMap<>(variables.size());
        variables.forEach((key, value) -> escapedVars.put(key, StringEscapeUtils.escapeXml10(value)));
        strSubstitutor = new StringSubstitutor(StringLookupFactory.INSTANCE.mapStringLookup(escapedVars), PREFIX,
                                               StringSubstitutor.DEFAULT_SUFFIX, StringSubstitutor.DEFAULT_ESCAPE);
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        logger.debug("Replacing parameters in file: {0}", file);
        try (InputStream inputStream = Files.newInputStream(file)) {
            String originalContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            String updatedContent = strSubstitutor.replace(originalContent);
            if (!StringUtils.equals(originalContent, updatedContent)) {
                logger.debug("Updating file {}", file);
                Files.write(file, updatedContent.getBytes(StandardCharsets.UTF_8));
            }
            return FileVisitResult.CONTINUE;
        } catch (IOException e) {
            logger.error("Error reading file {0}", e, file);
            throw e;
        }
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
        logger.error("Error reading file at {0}", exc, file);
        throw exc;
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
        return FileVisitResult.CONTINUE;
    }

}
