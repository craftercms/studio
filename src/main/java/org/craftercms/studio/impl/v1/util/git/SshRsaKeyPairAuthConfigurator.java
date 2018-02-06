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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.util.FS;

/**
 * {@link GitAuthenticationConfigurator} that configures the {@code TransportCommand} to use SSH with RSA key pair authentication.
 * The file path of the private key and it's passphrase can be provided, but are not necessary, specially when the private key has
 * already been loaded into the SSH agent.
 *
 * @author avasquez
 */
public class SshRsaKeyPairAuthConfigurator extends AbstractSshAuthConfigurator {

    protected String privateKeyPath;
    protected String passphrase;

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    @Override
    protected SshSessionFactory createSessionFactory() {
        return new JschConfigSessionFactory() {

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch defaultJSch = super.createDefaultJSch(fs);

                if (StringUtils.isNotEmpty(privateKeyPath)) {
                    if (StringUtils.isNotEmpty(passphrase)) {
                        defaultJSch.addIdentity(privateKeyPath, passphrase);
                    } else {
                        defaultJSch.addIdentity(privateKeyPath);
                    }
                }

                return defaultJSch;
            }

            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                setHostKeyType(host, session);
            }

        };
    }

}
