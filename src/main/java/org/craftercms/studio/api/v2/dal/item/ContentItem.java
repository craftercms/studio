/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.dal.item;

import org.craftercms.studio.model.rest.Person;

import java.time.ZonedDateTime;

//Consider moving these classes to a package under model, as they are not only related to the DAL.
public class ContentItem {
	private long id;
	private String label;
	private Long parentId;
	private String contentTypeId;
	private String path;
	private String previewUrl;
	private String systemType;
	private String mimeType;
	private long state;
	private Person lockOwner;
	private String localeCode;
	private Long translationSourceId;
	private Person creator;
	private ZonedDateTime dateCreated;
	private Person modifier;
	private ZonedDateTime dateModified;
	private long availableActions;
	private int childrenCount;
	private Boolean savedAsDraft;

	private PublishTargetStatus staging;
	private PublishTargetStatus live;

	public long getAvailableActions() {
		return availableActions;
	}

	public void setAvailableActions(long availableActions) {
		this.availableActions = availableActions;
	}

	public int getChildrenCount() {
		return childrenCount;
	}

	public void setChildrenCount(int childrenCount) {
		this.childrenCount = childrenCount;
	}

	public String getContentTypeId() {
		return contentTypeId;
	}

	public void setContentTypeId(String contentTypeId) {
		this.contentTypeId = contentTypeId;
	}

	public Person getCreator() {
		return creator;
	}

	public void setCreator(Person creator) {
		this.creator = creator;
	}

	public ZonedDateTime getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(ZonedDateTime dateCreated) {
		this.dateCreated = dateCreated;
	}

	public ZonedDateTime getDateModified() {
		return dateModified;
	}

	public void setDateModified(ZonedDateTime dateModified) {
		this.dateModified = dateModified;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLocaleCode() {
		return localeCode;
	}

	public void setLocaleCode(String localeCode) {
		this.localeCode = localeCode;
	}

	public Person getLockOwner() {
		return lockOwner;
	}

	public void setLockOwner(Person lockOwner) {
		this.lockOwner = lockOwner;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public Person getModifier() {
		return modifier;
	}

	public void setModifier(Person modifier) {
		this.modifier = modifier;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPreviewUrl() {
		return previewUrl;
	}

	public void setPreviewUrl(String previewUrl) {
		this.previewUrl = previewUrl;
	}

	public long getState() {
		return state;
	}

	public void setState(long state) {
		this.state = state;
	}

	public String getSystemType() {
		return systemType;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public Long getTranslationSourceId() {
		return translationSourceId;
	}

	public void setTranslationSourceId(Long translationSourceId) {
		this.translationSourceId = translationSourceId;
	}

	public PublishTargetStatus getLive() {
		return live;
	}

	public void setLive(PublishTargetStatus live) {
		this.live = live;
	}

	public PublishTargetStatus getStaging() {
		return staging;
	}

	public void setStaging(PublishTargetStatus staging) {
		this.staging = staging;
	}

	public Boolean getSavedAsDraft() {
		return savedAsDraft;
	}

	public void setSavedAsDraft(Boolean savedAsDraft) {
		this.savedAsDraft = savedAsDraft;
	}
}
