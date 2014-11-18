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
package org.craftercms.cstudio.alfresco.to;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeRef;

import org.craftercms.cstudio.alfresco.webscript.constant.CStudioWebScriptConstants;

/**
 * This class contains content asset information that exists in the repository
 * 
 * @author hyanghee
 *
 */
public class ContentAssetInfoTO implements Serializable {

	/**
	 * 
	 */
	protected static final long serialVersionUID = -8114663374929132828L;
	/** asset noderef **/
	protected NodeRef nodeRef;
	/** asset file name **/
	protected String fileName;
	/** asset file extension **/
	protected String fileExtension;
	/** asset file size in KB **/
	protected double size;
	/** the image width if the asset is an image **/
	protected int _width = -1;
	/** the image height if the asset is an image **/
	protected int _height = -1;
	
	protected String sizeUnit =CStudioWebScriptConstants.FILE_SIZE_KB;

	public String getSizeUnit() {
		return sizeUnit;
	}

	public void setSizeUnit(String sizeUnit) {
		this.sizeUnit = sizeUnit;
	}

	/**
	 * @return the nodeRef
	 */
	public NodeRef getNodeRef() {
		return nodeRef;
	}

	/**
	 * @param nodeRef
	 *            the nodeRef to set
	 */
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the fileExtension
	 */
	public String getFileExtension() {
		return fileExtension;
	}

	/**
	 * @param fileExtension
	 *            the fileExtension to set
	 */
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	/**
	 * @return the size
	 */
	public double getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(double size) {
		this.size = size;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return _width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this._width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return _height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this._height = height;
	}

}
