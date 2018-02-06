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

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;

/**
 * {@link GitAuthenticationConfigurator} that configures the {@code TransportCommand} to use SSH, but without providing
 * any authentication functionality. Actual authentication functionality is provided by subclasses.
 *
 * @author avasquez
 */
public abstract class AbstractSshAuthConfigurator implements GitAuthenticationConfigurator {

    public static final String KEY_TYPE_CONFIG = "server_host_key";

    @Override
    public void configureAuthentication(TransportCommand command) {
        SshSessionFactory sessionFactory = createSessionFactory();

        command.setTransportConfigCallback(transport -> ((SshTransport) transport).setSshSessionFactory(sessionFactory));
    }

    /*
     * Iterates through the known hosts (host key repository). If one of the know hosts matches the current host we're trying
     * to connect too,
     */
    protected void setHostKeyType(OpenSshConfig.Host host, Session session) {
        HostKey[] hostKeys = session.getHostKeyRepository().getHostKey();
        for(HostKey hostKey : hostKeys) {
            if(hostKey.getHost().contains(host.getHostName())) {
                session.setConfig(KEY_TYPE_CONFIG, hostKey.getType());
            }
        }
    }

    protected abstract SshSessionFactory createSessionFactory();

}
