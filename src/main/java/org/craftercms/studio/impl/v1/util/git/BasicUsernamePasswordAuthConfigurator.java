/*
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
package org.craftercms.studio.impl.v1.util.git;

import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * {@link GitAuthenticationConfigurator} that uses basic username/password authentication.
 *
 * @author avasquez
 */
public class BasicUsernamePasswordAuthConfigurator implements GitAuthenticationConfigurator {

    private String username;
    private String password;

    public BasicUsernamePasswordAuthConfigurator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void configureAuthentication(TransportCommand command) {
        command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
    }

}
