/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v1.executor;

import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.to.ResultTO;

import java.io.InputStream;
import java.util.Map;

/**
 * @author Dejan Brkic
 */
public interface ProcessContentExecutor {

    ResultTO processContent(final String id, final InputStream input, final boolean isXml, final Map<String, String> params, final String chainName) throws ServiceException;
}
