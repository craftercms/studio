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
package org.craftercms.cstudio.alfresco.dm.to;

import org.craftercms.cstudio.alfresco.to.TemplateConfigTO;

import java.io.Serializable;
import java.util.List;

public class DmSiteConfigTO implements Serializable {


    private static final long serialVersionUID = -6893641518197372995L;
    /** site name **/
    protected String _name;
    /** sandbox **/
    protected String _sandbox;
    /** root prefix **/
    protected String _rootPrefix;
    /** level descriptor name **/
    protected String _levelDescriptorName;
    /** top level folders **/
    protected List<DmFolderConfigTO> _folders = null;
    /** a list of paths to exclude when traversing file/folder hierarchy **/
    protected List<String> _excludePaths = null;

    /** page path patterns **/
    protected List<String> _pagePatterns = null;
    /** component path patterns **/
    protected List<String> _componentPatterns = null;
    /** assets path patterns **/
    protected List<String> _assetPatterns = null;
    /** document path patterns **/
    protected List<String> _documentPatterns = null;

    /** content to display in widgets **/
    protected List<String> _displayPatterns = null;

    /** periodically index user sandbox? **/
    protected boolean _indexUserSandbox = false;
    /** index life time. default = 1 hour **/
    protected long _indexTimeToLive = 3600000;
    /** use single user sandbox? **/
    protected boolean _useCollaborativeSandbox = false;
    protected boolean _checkForRenamed = false;

    /** common template configuration **/
    protected TemplateConfigTO _templateConfig = null;

    /** WEM Project **/
    protected String _wemProject;


    /**
     * @return the name
     */
    public String getName() {
        return _name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * @return the sandbox
     */
    public String getSandbox() {
        return _sandbox;
    }

    /**
     * @param sandbox
     *            the sandbox to set
     */
    public void setSandbox(String sandbox) {
        this._sandbox = sandbox;
    }

    /**
     * @return the rootPrefix
     */
    public String getRootPrefix() {
        return _rootPrefix;
    }

    /**
     * @param rootPrefix
     *            the rootPrefix to set
     */
    public void setRootPrefix(String rootPrefix) {
        this._rootPrefix = rootPrefix;
    }

    /**
     * @return the folders
     */
    public List<DmFolderConfigTO> getFolders() {
        return _folders;
    }

    /**
     * @param folders
     *            the folders to set
     */
    public void setFolders(List<DmFolderConfigTO> folders) {
        this._folders = folders;
    }

    /**
     * @param pagePatterns the pagePatterns to set
     */
    public void setPagePatterns(List<String> pagePatterns) {
        this._pagePatterns = pagePatterns;
    }

    /**
     * @return the pagePatterns
     */
    public List<String> getPagePatterns() {
        return _pagePatterns;
    }

    /**
     * @param componentPatterns the componentPatterns to set
     */
    public void setComponentPatterns(List<String> componentPatterns) {
        this._componentPatterns = componentPatterns;
    }

    /**
     * @return the componentPatterns
     */
    public List<String> getComponentPatterns() {
        return _componentPatterns;
    }

    /**
     * @param assetPatterns the assetPatterns to set
     */
    public void setAssetPatterns(List<String> assetPatterns) {
        this._assetPatterns = assetPatterns;
    }

    /**
     * @return the assetPatterns
     */
    public List<String> getAssetPatterns() {
        return _assetPatterns;
    }

    /**
     * get the root path of the given category (e.g. /site/website for Pages)
     *
     * @param category
     * @return the category root path
     */
    public String getCategoryRootPath(String category) {
        if (_folders != null) {
            for (DmFolderConfigTO folder : _folders) {
                if (folder.getName().equals(category)) {
                    return folder.getPath();
                }
            }
        }
        return "";
    }

    /**
     * @param levelDescriptorName the levelDescriptorName to set
     */
    public void setLevelDescriptorName(String levelDescriptorName) {
        this._levelDescriptorName = levelDescriptorName;
    }

    /**
     * @return the levelDescriptorName
     */
    public String getLevelDescriptorName() {
        return _levelDescriptorName;
    }


    /**
     * @param indexUserSandbox the indexUserSandbox to set
     */
    public void setIndexUserSandbox(boolean indexUserSandbox) {
        this._indexUserSandbox = indexUserSandbox;
    }

    /**
     * @return the indexUserSandbox
     */
    public boolean isIndexUserSandbox() {
        return _indexUserSandbox;
    }

    /**
     * @param useCollaborativeSandbox the useCollaborativeSandbox to set
     */
    public void setUseCollaborativeSandbox(boolean useCollaborativeSandbox) {
        this._useCollaborativeSandbox = useCollaborativeSandbox;
    }

    /**
     * @return the useCollaborativeSandbox
     */
    public boolean isUseCollaborativeSandbox() {
        return _useCollaborativeSandbox;
    }

    /**
     * @param checkForRenamed the checkForRenamed to set
     */
    public void setCheckForRenamed(boolean checkForRenamed) {
        this._checkForRenamed = checkForRenamed;
    }

    /**
     * @return the useCollaborativeSandbox
     */
    public boolean isCheckForRenamed() {
        return this._checkForRenamed;
    }

    /**
     * @param indexTimeToLive the indexTimeToLive to set
     */
    public void setIndexTimeToLive(long indexTimeToLive) {
        this._indexTimeToLive = indexTimeToLive;
    }

    /**
     * @return the indexTimeToLive
     */
    public long getIndexTimeToLive() {
        return _indexTimeToLive;
    }

    /**
     * @param documentPatterns the documentPatterns to set
     */
    public void setDocumentPatterns(List<String> documentPatterns) {
        this._documentPatterns = documentPatterns;
    }

    /**
     * @return the documentPatterns
     */
    public List<String> getDocumentPatterns() {
        return _documentPatterns;
    }

    /**
     * @return the excludePaths
     */
    public List<String> getExcludePaths() {
        return _excludePaths;
    }

    /**
     * @param excludePaths the excludePaths to set
     */
    public void setExcludePaths(List<String> excludePaths) {
        this._excludePaths = excludePaths;
    }

    /**
     * @param displayPatterns the displayPatterns to set
     */
    public void setDisplayPatterns(List<String> displayPatterns) {
        this._displayPatterns = displayPatterns;
    }

    /**
     * @return the displayPatterns
     */
    public List<String> getDisplayPatterns() {
        return _displayPatterns;
    }

    /**
     * @return the templateConfig
     */
    public TemplateConfigTO getTemplateConfig() {
        if (_templateConfig == null) {
            _templateConfig = new TemplateConfigTO();
        }
        return _templateConfig;
    }

    /**
     * @param templateConfig the templateConfig to set
     */
    public void setTemplateConfig(TemplateConfigTO templateConfig) {
        this._templateConfig = templateConfig;
    }

    public String getWemProject() {
        return this._wemProject;
    }
    
    public void setWemProject(String wemProject) {
        this._wemProject = wemProject;
    }
}
