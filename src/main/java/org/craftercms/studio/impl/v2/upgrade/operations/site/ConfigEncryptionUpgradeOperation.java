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

import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;

import java.beans.ConstructorProperties;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of {@link AbstractContentUpgradeOperation} that upgrades encrypted properties in configuration files.
 *
 * @author joseross
 * @since 3.1.9
 */
public class ConfigEncryptionUpgradeOperation extends AbstractContentUpgradeOperation {

    protected static String DEFAULT_ENCRYPTED_PATTERN = "\\$\\{enc:([^}#]+)}";

    protected Pattern encryptedPattern = Pattern.compile(DEFAULT_ENCRYPTED_PATTERN);

    protected TextEncryptor textEncryptor;

    @ConstructorProperties({"studioConfiguration", "textEncryptor"})
    public ConfigEncryptionUpgradeOperation(StudioConfiguration studioConfiguration, TextEncryptor textEncryptor) {
        super(studioConfiguration);
        this.textEncryptor = textEncryptor;
    }

    @Override
    protected boolean shouldBeUpdated(StudioUpgradeContext context, Path file) {
        return true;
    }

    @Override
    protected void updateFile(StudioUpgradeContext context, Path path) throws UpgradeException {
        try {
            // read the whole file
            String content = readFile(path);
            // find all encrypted values
            Matcher matcher = encryptedPattern.matcher(content);
            boolean updateFile = matcher.matches();
            // for each one
            while(matcher.find()) {
                String encryptedValue = matcher.group(1);
                // decrypt it
                String originalValue = textEncryptor.decrypt(encryptedValue);
                // encrypt it again
                String newValue = textEncryptor.encrypt(originalValue);
                // replace it
                content = content.replaceAll(encryptedValue, newValue);

                updateFile = true;
            }

            // update the file if needed
            if (updateFile) {
                writeFile(path, content);
            }
        } catch (Exception e) {
            throw new UpgradeException("Error updating file " + path + " for site " + context, e);
        }
    }


}
