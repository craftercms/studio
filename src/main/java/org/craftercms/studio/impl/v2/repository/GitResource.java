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

package org.craftercms.studio.impl.v2.repository;

import org.eclipse.jgit.lib.ObjectLoader;
import org.springframework.core.io.AbstractResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of {@link AbstractResource} that wraps an {@link ObjectLoader} from JGit
 *
 * @author joseross
 * @since 4.0.0
 */
public class GitResource extends AbstractResource {

    protected final ObjectLoader objectLoader;

    public GitResource(ObjectLoader objectLoader) {
        this.objectLoader = objectLoader;
    }

    @Override
    public String getDescription() {
        return objectLoader.toString();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return objectLoader.openStream();
    }

    @Override
    public long contentLength() {
        return objectLoader.getSize();
    }

    @Override
    public long lastModified() throws IOException {
        return -1;
    }

}
