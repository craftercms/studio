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

package org.craftercms.studio.api.v2.dal;

import java.util.List;
import java.util.Map;

public interface RemoteRepositoryDAO {

    RemoteRepository getRemoteRepository(Map params);

    void insertRemoteRepository(Map params);

    void deleteRemoteRepositoryForSite(Map params);

    void deleteRemoteRepository(Map params);

    List<RemoteRepository> listRemoteRepositories(Map params);

    void deleteRemoteRepositoriesForSite(Map params);
}
