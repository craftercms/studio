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
package org.craftercms.cstudio.alfresco.preview;

import java.io.File;
import java.util.Properties;

/**
 * @author Alfonso Vásquez
 */
public class DeployedPreviewFile {

    private String path;
    private File file;
    private Properties metaData;

    public DeployedPreviewFile(String path, File file, Properties metaData) {
        this.path = path;
        this.file = file;
        this.metaData = metaData;
    }

    public String getPath() {
        return path;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return file.getName();
    }

    public boolean isFile() {
        return file.isFile();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public Properties getMetaData() {
        return metaData;
    }

}
