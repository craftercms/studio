/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.api.v1.to;

import org.craftercms.studio.api.v1.constant.DmConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.regex.Matcher;

/**
 * represents the DM full path
 *
 * @author hyanghee
 * @author Dejan Brkic
 *
 */
public class DmPathTO implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(DmPathTO.class);


    private static final long serialVersionUID = 1678837872209101222L;

    /** store name e.g.  **/
    protected String _storeName = "workspace";
    /** wem projects root folder **/
    protected String _wemProjectsRoot;
    /** wem project **/
    protected String _wemProjectName;
    /** site name **/
    protected String _siteName;

    /** area type (sandbox) **/
    protected String _areaName;
    public String getAreaName() {
        return _areaName;
    }
    public void setAreaName(String _areaName) {
        this._areaName = _areaName;
    }

    /** relative path within the web project e.g. /site/website/index.xml **/
    protected String _relativePath;
    /** name of the content at the path **/
    protected String _name;

    /**
     * default constructor
     */
    public DmPathTO() {}

    /**
     * constructor
     *
     * @param dmPath Full Path of the Content
     */
    public DmPathTO(String dmPath) {
        final Matcher m = DmConstants.DM_MULTI_REPO_PATH_PATTERN.matcher(dmPath);
        if (m.matches()) {
            _wemProjectsRoot = m.group(1).length() != 0 ? m.group(1) : DmConstants.DM_WEM_PROJECTS_FOLDER;
            _wemProjectName = m.group(2).length() != 0 ? m.group(2) : "";
            _siteName = m.group(3).length() != 0 ? m.group(3) : "";
            _areaName = m.group(4).length() != 0 ? m.group(4) : DmConstants.DM_WORK_AREA_REPO_FOLDER;
            _relativePath = m.group(5).length() != 0 ? m.group(5) : "/";
            int index = _relativePath.lastIndexOf("/");
            if (index > 0 && index < (_relativePath.length() + 1)) {
                _name = _relativePath.substring(index + 1);
            }
        }

    }

    /**
     * constructor
     *
     * @param storeName
     * @param wemProjectsRoot
     * @param wemProjectName
     * @param siteName
     * @param areaName
     * @param relativePath
     */
    public DmPathTO(String storeName, String wemProjectsRoot, String wemProjectName, String siteName, String areaName, String relativePath) {
        this._storeName = storeName;
        this._wemProjectsRoot = wemProjectsRoot;
        this._wemProjectName = wemProjectName;
        this._siteName = siteName;
        this._areaName = areaName;
        this._relativePath = relativePath;
        if (_relativePath != null) {
            int index = _relativePath.lastIndexOf("/");
            if (index > 0 && index < (_relativePath.length() + 1)) {
                _name = _relativePath.substring(index + 1);
            }
        }
    }

    /**
     * @return the storeName
     */
    public String getStoreName() {
        return _storeName;
    }

    /**
     * @param storeName
     *            the storeName to set
     */
    public void setStoreName(String storeName) {
        this._storeName = storeName;
    }

    /**
     * @return the defaultWebApp
     */
    public String getDmSitePath() {
        StringBuilder sb = new StringBuilder("/");
        sb.append(this._wemProjectsRoot).append("/");
        sb.append(this._wemProjectName).append("/");
        sb.append(this._siteName).append("/");
        sb.append(this._areaName);
        return sb.toString();
    }

    /**
     * @return the relativePath
     */
    public String getRelativePath() {
        return _relativePath;
    }

    /**
     * @param relativePath
     *            the relativePath to set
     */
    public void setRelativePath(String relativePath) {
        this._relativePath = relativePath;
    }

    /*
      * (non-Javadoc)
      * @see java.lang.Object#toString()
      */
    public String toString() {
        StringBuilder sb = new StringBuilder("/");
        sb.append(this._wemProjectsRoot).append("/");
        sb.append(this._wemProjectName).append("/");
        sb.append(this._siteName).append("/");
        sb.append(this._areaName);
        sb.append(this._relativePath);
        return sb.toString();
    }

    /**
     * return the full path without the store name
     *
     * @return asset path
     */
    public String getAssetPath() {
        StringBuilder sb = new StringBuilder("/");
        sb.append(this._wemProjectsRoot).append("/");
        sb.append(this._wemProjectName).append("/");
        sb.append(this._siteName).append("/");
        sb.append(this._areaName);
        sb.append(this._relativePath);
        return sb.toString();
    }
    /**
     * @return the name
     */

    public String getName() {
        return _name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this._name = name;
    }

    public String getSiteName() {
        return this._siteName;
    }
}
