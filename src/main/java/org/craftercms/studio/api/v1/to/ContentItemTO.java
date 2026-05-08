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
package org.craftercms.studio.api.v1.to;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains content item metadata
 * used by the UI
 */
public class ContentItemTO implements Serializable {

	private static final long serialVersionUID = 110010823228368718L;
	// properties
	public String name;
	public String internalName;
	public String contentType;
	public String uri;
	public String path;
	public String browserUri;
	public boolean navigation;
	public boolean floating;
	public boolean hideInAuthoring;
	public boolean previewable;
	public String lockOwner;
	public String user;
	public String userFirstName;
	public String userLastName;
	public String nodeRef;
	public String metaDescription;
	public String site;

	// what it is
	public boolean page;
	public boolean component;
	public boolean document;
	public boolean asset;
	public boolean isContainer;
	public boolean container;

	// special states
	public boolean disabled;
	public boolean savedAsDraft;

	// workflow states
	public boolean submitted;
	public boolean submittedForDeletion;
	public boolean scheduled;
	public boolean published;
	public boolean deleted;
	public boolean inProgress;
	public boolean live;
	public boolean staged;
	public boolean inFlight;

	// duplicate properties (these are probable getters)
	public boolean isDisabled;
	public boolean isSavedAsDraft;
	public boolean isInProgress;
	public boolean isLive;
	public boolean isStaged;
	public boolean isSubmittedForDeletion;
	public boolean isScheduled;
	public boolean isPublished;
	public boolean isNavigation;
	public boolean isDeleted;
	public boolean isNew;
	public boolean isSubmitted;
	public boolean isFloating;
	public boolean isPage;
	public boolean isPreviewable;
	public boolean isComponent;
	public boolean isDocument;
	public boolean isAsset;
	public boolean isInFlight;

	// Added by Dejan needs of services porting and UI
	public ZonedDateTime eventDate;
	public String endpoint;
	public String timezone;
	public int numOfChildren;
	public ZonedDateTime scheduledDate;
	public ZonedDateTime publishedDate;
	public String mandatoryParent;
	public boolean isLevelDescriptor = false;
	public String categoryRoot;
	public ZonedDateTime lastEditDate;
	public String form;
	public List<RenderingTemplateTO> renderingTemplates = new ArrayList<>();

	public boolean folder;
	protected String submissionComment;
	protected List<ContentItemTO> components;
	protected List<ContentItemTO> documents;
	protected List<ContentItemTO> levelDescriptors;
	protected List<ContentItemTO> pages;
	protected boolean isNewFile = false;
	protected boolean isReference = false;
	protected String parentPath = null;
	protected List<DmOrderTO> orders;

	public List<ContentItemTO> children = new ArrayList<>();

	public double size;
	public String sizeUnit;

	public String mimeType;
	public String environment;
	public String submittedToEnvironment;
	public String packageId;

	public ContentItemTO() {
	}

	// Copy constructors
	public ContentItemTO(ContentItemTO item) {
		this(item, false);
	}

	public ContentItemTO(ContentItemTO item, boolean cloneChildren) {
		this.name = item.name;
		this.internalName = item.internalName;
		this.contentType = item.contentType;
		this.uri = item.uri;
		this.path = item.path;
		this.browserUri = item.browserUri;
		this.navigation = item.navigation;
		this.floating = item.floating;
		this.hideInAuthoring = item.hideInAuthoring;
		this.previewable = item.previewable;
		this.lockOwner = item.lockOwner;
		this.user = item.user;
		this.userFirstName = item.userFirstName;
		this.userLastName = item.userLastName;
		this.nodeRef = item.nodeRef;
		this.metaDescription = item.metaDescription;
		this.site = item.site;
		this.page = item.page;
		this.component = item.component;
		this.document = item.document;
		this.asset = item.asset;
		this.isContainer = item.isContainer;
		this.container = item.container;
		this.disabled = item.disabled;
		this.savedAsDraft = item.savedAsDraft;
		this.submitted = item.submitted;
		this.submittedForDeletion = item.submittedForDeletion;
		this.scheduled = item.scheduled;
		this.published = item.published;

		this.deleted = item.deleted;
		this.inProgress = item.inProgress;
		this.live = item.live;
		this.staged = item.staged;
		this.inFlight = item.inFlight;
		this.isDisabled = item.isDisabled;
		this.isSavedAsDraft = item.isSavedAsDraft;
		this.isInProgress = item.isInProgress;
		this.isLive = item.isLive;
		this.isStaged = item.isStaged;
		this.isSubmittedForDeletion = item.isSubmittedForDeletion;
		this.isScheduled = item.isScheduled;
		this.isPublished = item.isPublished;
		this.isNavigation = item.isNavigation;
		this.isDeleted = item.isDeleted;
		this.isNew = item.isNew;
		this.isSubmitted = item.isSubmitted;
		this.isFloating = item.isFloating;
		this.isPage = item.isPage;
		this.isPreviewable = item.isPreviewable;
		this.isComponent = item.isComponent;
		this.isDocument = item.isDocument;
		this.isAsset = item.isAsset;
		this.isInFlight = item.isInFlight;
		if (item.eventDate != null) {
			this.eventDate = item.eventDate.withZoneSameInstant(ZoneOffset.UTC);
		} else {
			this.eventDate = item.eventDate;
		}
		this.endpoint = item.endpoint;
		this.timezone = item.timezone;
		this.numOfChildren = item.numOfChildren;
		if (item.scheduledDate != null) {
			this.scheduledDate = item.scheduledDate.withZoneSameInstant(ZoneOffset.UTC);
		} else {
			this.scheduledDate = item.scheduledDate;
		}
		if (item.publishedDate != null) {
			this.publishedDate = item.publishedDate.withZoneSameInstant(ZoneOffset.UTC);
		} else {
			this.publishedDate = item.publishedDate;
		}

		this.mandatoryParent = item.mandatoryParent;
		this.isLevelDescriptor = item.isLevelDescriptor;
		this.categoryRoot = item.categoryRoot;
		if (item.lastEditDate != null) {
			this.lastEditDate = item.lastEditDate.withZoneSameInstant(ZoneOffset.UTC);
		} else {
			this.lastEditDate = item.lastEditDate;
		}
		this.form = item.form;
		this.renderingTemplates = item.renderingTemplates;

		this.folder = item.folder;
		this.submissionComment = item.submissionComment;
		this.components = item.components;
		this.documents = item.documents;
		this.levelDescriptors = item.levelDescriptors;
		this.pages = item.pages;
		this.isNewFile = item.isNewFile;
		this.isReference = item.isReference;
		this.parentPath = item.parentPath;
		this.orders = item.orders;
		this.mimeType = item.mimeType;
		this.environment = item.environment;
		this.submittedToEnvironment = item.submittedToEnvironment;
		this.packageId = item.packageId;

		if (cloneChildren) {
			if (item.children != null) {
				this.children = new ArrayList<>(item.children.size());
				for (ContentItemTO child : item.children) {
					this.children.add(new ContentItemTO(child));
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInternalName() {
		return internalName;
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getBrowserUri() {
		return browserUri;
	}

	public void setBrowserUri(String browserUri) {
		this.browserUri = browserUri;
	}

	public boolean isNavigation() {
		return navigation;
	}

	public void setNavigation(boolean navigation) {
		this.navigation = navigation;
	}

	public boolean isFloating() {
		return floating;
	}

	public void setFloating(boolean floating) {
		this.floating = floating;
	}

	public boolean isHideInAuthoring() {
		return hideInAuthoring;
	}

	public void setHideInAuthoring(boolean hideInAuthoring) {
		this.hideInAuthoring = hideInAuthoring;
	}

	public boolean isPreviewable() {
		return previewable;
	}

	public void setPreviewable(boolean previewable) {
		this.previewable = previewable;
	}

	public String getLockOwner() {
		return lockOwner;
	}

	public void setLockOwner(String lockOwner) {
		this.lockOwner = lockOwner;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUserFirstName() {
		return userFirstName;
	}

	public void setUserFirstName(String userFirstName) {
		this.userFirstName = userFirstName;
	}

	public String getUserLastName() {
		return userLastName;
	}

	public void setUserLastName(String userLastName) {
		this.userLastName = userLastName;
	}

	public String getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(String nodeRef) {
		this.nodeRef = nodeRef;
	}

	public String getMetaDescription() {
		return metaDescription;
	}

	public void setMetaDescription(String metaDescription) {
		this.metaDescription = metaDescription;
	}

	public boolean isPage() {
		return page;
	}

	public void setPage(boolean page) {
		this.page = page;
	}

	public boolean isComponent() {
		return component;
	}

	public void setComponent(boolean component) {
		this.component = component;
	}

	public boolean isDocument() {
		return document;
	}

	public void setDocument(boolean document) {
		this.document = document;
	}

	public boolean isAsset() {
		return asset;
	}

	public void setAsset(boolean asset) {
		this.asset = asset;
	}

	public ZonedDateTime getEventDate() {
		return eventDate;
	}

	public void setEventDate(ZonedDateTime eventDate) {
		if (eventDate != null) {
			this.eventDate = eventDate.withZoneSameInstant(ZoneOffset.UTC);
		} else {
			this.eventDate = eventDate;
		}
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public int getNumOfChildren() {
		return numOfChildren;
	}

	public void setNumOfChildren(int numOfChildren) {
		this.numOfChildren = numOfChildren;
	}

	public ZonedDateTime getScheduledDate() {
		return scheduledDate;
	}

	public void setScheduledDate(ZonedDateTime scheduledDate) {
		if (scheduledDate != null) {
			this.scheduledDate = scheduledDate.withZoneSameInstant(ZoneOffset.UTC);
		} else {
			this.scheduledDate = scheduledDate;
		}
	}

	public ZonedDateTime getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(ZonedDateTime publishedDate) {
		if (publishedDate != null) {
			this.publishedDate = publishedDate.withZoneSameInstant(ZoneOffset.UTC);
		} else {
			this.publishedDate = publishedDate;
		}
	}

	public String getMandatoryParent() {
		return mandatoryParent;
	}

	public void setMandatoryParent(String mandatoryParent) {
		this.mandatoryParent = mandatoryParent;
	}

	public boolean isLevelDescriptor() {
		return isLevelDescriptor;
	}

	public void setLevelDescriptor(boolean isLevelDescriptor) {
		this.isLevelDescriptor = isLevelDescriptor;
	}

	public List<ContentItemTO> getChildren() {
		return children;
	}

	public void setChildren(List<ContentItemTO> children) {
		this.children = children;
	}

	public boolean isContainer() {
		return isContainer;
	}

	public void setContainer(boolean isContainer) {
		this.isContainer = isContainer;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isSubmitted() {
		return submitted;
	}

	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}

	public boolean isSubmittedForDeletion() {
		return submittedForDeletion;
	}

	public void setSubmittedForDeletion(boolean submittedForDeletion) {
		this.submittedForDeletion = submittedForDeletion;
	}

	public boolean isScheduled() {
		return scheduled;
	}

	public void setScheduled(boolean scheduled) {
		this.scheduled = scheduled;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public boolean isInProgress() {
		return inProgress;
	}

	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
	}

	public boolean isLive() {
		return live;
	}

	public void setLive(boolean live) {
		this.live = live;
	}

	public boolean isStaged() {
		return staged;
	}

	public void setStaged(boolean staged) {
		this.staged = staged;
	}

	public String getCategoryRoot() {
		return categoryRoot;
	}

	public void setCategoryRoot(String categoryRoot) {
		this.categoryRoot = categoryRoot;
	}

	public ZonedDateTime getLastEditDate() {
		return lastEditDate;
	}

	public void setLastEditDate(ZonedDateTime lastEditDate) {
		if (lastEditDate != null) {
			this.lastEditDate = lastEditDate.withZoneSameInstant(ZoneOffset.UTC);
		} else {
			this.lastEditDate = lastEditDate;
		}
	}

	public String getForm() {
		return form;
	}

	public void setForm(String form) {
		this.form = form;
	}

	public boolean isFolder() {
		return folder;
	}

	public void setFolder(boolean folder) {
		this.folder = folder;
	}

	public String getSubmissionComment() {
		return submissionComment;
	}

	public void setSubmissionComment(String submissionComment) {
		this.submissionComment = submissionComment;
	}

	public List<ContentItemTO> getComponents() {
		return components;
	}

	public void setComponents(List<ContentItemTO> components) {
		this.components = components;
	}

	public List<ContentItemTO> getDocuments() {
		return documents;
	}

	public void setDocuments(List<ContentItemTO> documents) {
		this.documents = documents;
	}

	public List<ContentItemTO> getLevelDescriptors() {
		return levelDescriptors;
	}

	public void setLevelDescriptors(List<ContentItemTO> levelDescriptors) {
		this.levelDescriptors = levelDescriptors;
	}

	public boolean isNewFile() {
		return isNewFile;
	}

	public void setNewFile(boolean isNewFile) {
		this.isNewFile = isNewFile;
	}

	public List<ContentItemTO> getPages() {
		return pages;
	}

	public void setPages(List<ContentItemTO> pages) {
		this.pages = pages;
	}

	public boolean isReference() {
		return isReference;
	}

	public void setReference(boolean isReference) {
		this.isReference = isReference;
	}

	public String getParentPath() {
		return parentPath;
	}

	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}

	public List<DmOrderTO> getOrders() {
		return orders;
	}

	public void setOrders(List<DmOrderTO> orders) {
		this.orders = orders;
	}

	public boolean isInFlight() {
		return this.inFlight;
	}

	public void setInFlight(boolean inFlight) {
		this.inFlight = inFlight;
	}

	public Double getOrder(String orderName) {
		if (orderName != null && orders != null) {
			for (DmOrderTO order : orders) {
				if (orderName.equalsIgnoreCase(order.getId())) {
					return order.getOrder();
				}
			}
		}
		return -1.0;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public String getSizeUnit() {
		return sizeUnit;
	}

	public void setSizeUnit(String sizeUnit) {
		this.sizeUnit = sizeUnit;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getSubmittedToEnvironment() {
		return submittedToEnvironment;
	}

	public void setSubmittedToEnvironment(String submittedToEnvironment) {
		this.submittedToEnvironment = submittedToEnvironment;
	}

	public String getPackageId() {
		return packageId;
	}

	public void setPackageId(String packageId) {
		this.packageId = packageId;
	}

}
