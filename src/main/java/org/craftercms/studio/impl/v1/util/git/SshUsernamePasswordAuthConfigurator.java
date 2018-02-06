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

import com.jcraft.jsch.Session;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;

/**
 * {@link GitAuthenticationConfigurator} that configures the {@code TransportCommand} to use SSH with username/password authentication.
 * The user name is expected to be part of the Git SSH URL, while the password is provided separately and injected to this class.
 *
 * @author avasquez
 */
public class SshUsernamePasswordAuthConfigurator extends AbstractSshAuthConfigurator {

    protected String password;

    public SshUsernamePasswordAuthConfigurator(String password) {
        this.password = password;
    }

    @Override
    protected SshSessionFactory createSessionFactory() {
        return new JschConfigSessionFactory() {

            @Override
            protected void configure(OpenSshConfig.Host hc, Session session) {
                session.setPassword(password);
                setHostKeyType(hc, session);
            }


        };
    }

}
