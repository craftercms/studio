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

package org.craftercms.studio.api.v1.service.deployment;

public class UploadFailedException extends Exception {

    private static final long serialVersionUID = 7407643924342258309L;

    public UploadFailedException(String site, String target, String url) {
        super();
        this._site = site;
        this._target = target;
        this._url = url;
    }

    public UploadFailedException(String site, String target, String url, Throwable throwable) {
        super(throwable);
        this._site = site;
        this._target = target;
        this._url = url;
    }

    public String getSite() { return _site; }
    public String getTarget() { return _target; }
    public String getUrl() { return _url; }

    protected String _site;
    protected String _target;
    protected String _url;
}
