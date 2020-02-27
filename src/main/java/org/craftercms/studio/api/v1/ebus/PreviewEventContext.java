/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v1.ebus;

import java.io.Serializable;

public class PreviewEventContext extends EventContext implements Serializable {

    private static final long serialVersionUID = 6915982832841499939L;

    private boolean waitTillDeploymentIsDone;

    public PreviewEventContext() {
    }

    public PreviewEventContext(boolean waitTillDeploymentIsDone) {
        this.waitTillDeploymentIsDone = waitTillDeploymentIsDone;
    }

    public boolean isWaitTillDeploymentIsDone() {
        return waitTillDeploymentIsDone;
    }

}
