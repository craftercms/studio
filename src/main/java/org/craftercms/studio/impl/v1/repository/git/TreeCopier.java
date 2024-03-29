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

package org.craftercms.studio.impl.v1.repository.git;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by Sumer Jabri
 */
public class TreeCopier  implements FileVisitor<Path> {
    private static final Logger logger = LoggerFactory.getLogger(TreeCopier.class);
    protected final Path source;
    protected final Path target;

    public TreeCopier(Path source, Path target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        // TODO: SJ: What does this method actually do?
        CopyOption[] options = new CopyOption[0];

        Path newDir = target.resolve(source.relativize(dir));
        try {
            Files.copy(dir, newDir, options);
        } catch (FileAlreadyExistsException e) {
            // ignore
        } catch (IOException e) {
            logger.error("Failed to copy files from '{}' to '{}' with options '{}'", dir, newDir, options, e);
            return SKIP_SUBTREE;
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        CopyOption[] options = new CopyOption[] { REPLACE_EXISTING };
        try {
            Files.copy(file, target.resolve(source.relativize(file)), options);
        } catch (IOException e) {
            logger.error("Failed to copy '{}' to '{}' with options '{}'",
                    source, target.resolve(source.relativize(file)), options, e);
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
        if (e instanceof FileSystemLoopException) {
            logger.error("File system loop detected for file '{}'", file, e);
        } else {
            logger.error("Failed to copy file '{}'", file, e);
        }
        return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return CONTINUE;
    }
}