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

package org.craftercms.studio.impl.v2.upgrade.operations.site;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.io.IOUtils;
import org.craftercms.commons.lang.RegexUtils;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.craftercms.studio.impl.v2.upgrade.operations.AbstractUpgradeOperation;

/**
 * Base implementation of {@link org.craftercms.commons.upgrade.UpgradeOperation} for all site content upgrades
 *
 * <p>Supported YAML properties:</p>
 * <ul>
 *     <li><strong>includedPaths</strong>: (required) list of patterns to check for files in the repository</li>
 * </ul>
 *
 * @author joseross
 */
public abstract class AbstractContentUpgradeOperation extends AbstractUpgradeOperation {

    private static final Logger logger = LoggerFactory.getLogger(AbstractContentUpgradeOperation.class);

    public static final String CONFIG_KEY_INCLUDED_PATHS = "includedPaths";

    /**
     * List of patterns to check for files in the repository
     */
    protected List<String> includedPaths;

    public AbstractContentUpgradeOperation(StudioConfiguration studioConfiguration) {
        super(studioConfiguration);
    }

    @Override
    protected void doInit(final HierarchicalConfiguration config) {
        includedPaths = config.getList(String.class, CONFIG_KEY_INCLUDED_PATHS);
    }

    @Override
    public void doExecute(final StudioUpgradeContext context) throws UpgradeException {
        var site = context.getTarget();
        try {
            List<Path> includedPaths = findIncludedPaths(context);
            List<Path> filteredPaths = filterPaths(context, includedPaths);
            if (CollectionUtils.isNotEmpty(filteredPaths)) {
                for (Path file : filteredPaths) {
                    updateFile(context, file);
                    trackChangedFiles(context.getRelativePath(file));
                }
            }
        } catch (IOException e) {
            throw new UpgradeException("Error reading content for site " + site, e);
        }
    }

    /**
     * Finds all files in the given site that match any of the given patterns
     * @param context the current upgrade context
     * @return the list of matching files
     * @throws IOException if there is any error finding the files
     */
    protected List<Path> findIncludedPaths(StudioUpgradeContext context) throws IOException {
        if(CollectionUtils.isNotEmpty(includedPaths)) {
            Path repo = context.getRepositoryPath();
            ListFileVisitor fileVisitor = new ListFileVisitor(repo, includedPaths);
            Files.walkFileTree(repo, fileVisitor);
            return fileVisitor.getMatchedPaths();
        }
        return null;
    }

    /**
     * Filters the given list checking if the files match the update conditions
     * @param context the current upgrade context
     * @param matchedPaths the list of files to filter
     * @return the filtered list of files
     * @throws UpgradeException if there is any error filtering the files
     */
    protected List<Path> filterPaths(StudioUpgradeContext context, List<Path> matchedPaths) throws UpgradeException {
        if(CollectionUtils.isNotEmpty(matchedPaths)) {
            List<Path> filteredPaths = new LinkedList<>();
            for(Path path : matchedPaths) {
                if(shouldBeUpdated(context, path)) {
                    filteredPaths.add(path);
                }
            }
            return filteredPaths;
        }
        return null;
    }

    /**
     * Indicates if the given file should be updated by this class
     * @param context the current upgrade context
     * @param file the file to check
     * @return true if the file should be updated
     * @throws UpgradeException if there is any error checking the file
     */
    protected abstract boolean shouldBeUpdated(StudioUpgradeContext context, Path file) throws UpgradeException;

    /**
     * Performs any needed updates on the content of the given file
     * @param context the current upgrade context
     * @param path the file to update
     * @throws UpgradeException if there is any error updating the file
     */
    protected abstract void updateFile(StudioUpgradeContext context, Path path) throws UpgradeException;

    /**
     * Performs a read operation from the file system
     * @param path the file to read
     * @return the content of the file
     * @throws UpgradeException if there is any error reading the file
     */
    protected String readFile(final Path path) throws UpgradeException {
        logger.debug("Reading content for file {0}", path);
        try (InputStream is = Files.newInputStream(path)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UpgradeException("Error reading file " + path, e);
        }
    }

    /**
     * Performs a write operation in the file system without committing any changes in the repository
     * @param path the file to write
     * @param content the content to write
     * @throws UpgradeException is there is any error writing the content
     */
    protected void writeFile(final Path path, final String content) throws UpgradeException {
        logger.debug("Writing content for file {0}", path);
        try (OutputStream os = Files.newOutputStream(path)) {
            IOUtils.write(content, os, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UpgradeException("Error writing file " + path , e);
        }
    }

    /**
     * Implementation of {@link FileVisitor} that collects files matching any of the given patterns
     * @author joseross
     */
    public static class ListFileVisitor implements FileVisitor<Path> {

        /**
         * Starting path that should be ignored for checking the patterns
         */
        protected Path rootPath;

        /**
         * List of patterns to check the files
         */
        protected List<String> includedPaths;

        /**
         * List of files that match any of the patterns
         */
        protected List<Path> matchedPaths = new LinkedList<>();

        public ListFileVisitor(final Path rootPath, final List<String> includedPaths) {
            this.rootPath = rootPath;
            this.includedPaths = includedPaths;
        }

        public List<Path> getMatchedPaths() {
            return matchedPaths;
        }

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
            String relativePath = rootPath.relativize(file).toString();
            if(RegexUtils.matchesAny(relativePath, includedPaths)) {
                matchedPaths.add(file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException exc) {
            logger.error("Could not read file " + file, exc);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
            return FileVisitResult.CONTINUE;
        }
    }

}
