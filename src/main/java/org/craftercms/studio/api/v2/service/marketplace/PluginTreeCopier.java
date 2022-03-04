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
package org.craftercms.studio.api.v2.service.marketplace;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.text.StringSubstitutor;
import org.craftercms.studio.api.v2.service.marketplace.registry.FileRecord;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.repository.git.TreeCopier;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SANDBOX_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SITES_REPOS_PATH;

/**
 * Extension of {@link TreeCopier} that collects information about the files to be used for the plugin registry
 *
 * @author joseross
 * @since 4.0.0
 */
public class PluginTreeCopier extends TreeCopier {

    protected final StudioConfiguration studioConfiguration;

    protected final String siteId;

    protected final List<FileRecord> files;

    protected final StringSubstitutor stringSubstitutor;

    protected final Path repoDir;

    /**
     * Indicates if the checksum should be populated for all files
     */
    protected final boolean calculateChecksum;

    public PluginTreeCopier(Path source, Path target, StudioConfiguration studioConfiguration, String siteId,
                            Map<String, String> params, List<FileRecord> files, boolean calculateChecksum) {
        super(source, target);
        this.studioConfiguration = studioConfiguration;
        this.siteId = siteId;
        this.files = files;
        this.calculateChecksum = calculateChecksum;
        this.stringSubstitutor = new StringSubstitutor(params, "${plugin:", "}");
        this.repoDir = Paths.get(studioConfiguration.getProperty(REPO_BASE_PATH),
                studioConfiguration.getProperty(SITES_REPOS_PATH), siteId,
                studioConfiguration.getProperty(SANDBOX_PATH));
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        byte[] content = Files.readAllBytes(file);

        //TODO: Find a better way to do this
        if (file.toString().endsWith(".xml")) {
            content = stringSubstitutor.replace(new String(content, UTF_8)).getBytes(UTF_8);
        }

        Files.write(target.resolve(source.relativize(file)), content, CREATE, TRUNCATE_EXISTING, WRITE);

        FileRecord record = new FileRecord();
        record.setPath(repoDir.relativize(target.resolve(source.relativize(file))).toString());
        if (calculateChecksum) {
            record.setSha512(DigestUtils.sha512Hex(content));
        }
        files.add(record);

        return FileVisitResult.CONTINUE;
    }

}
