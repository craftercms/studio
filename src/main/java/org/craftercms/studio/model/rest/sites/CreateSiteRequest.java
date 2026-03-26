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

package org.craftercms.studio.model.rest.sites;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.craftercms.commons.validation.annotations.param.ValidSiteId;

import java.util.Map;

/**
 * Holds the information needed to create a site
 */
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		property = "sourceType"
)
@JsonSubTypes({
		@JsonSubTypes.Type(value = CreateSiteRequest.RemoteSource.class, name = "remote"),
		@JsonSubTypes.Type(value = CreateSiteRequest.BlueprintSource.class, name = "blueprint")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public sealed abstract class CreateSiteRequest permits CreateSiteRequest.RemoteSource, CreateSiteRequest.BlueprintSource {
	@NotEmpty
	@ValidSiteId
	private String siteId;
	@NotEmpty
	@Size(max = 255)
	private String name;
	@Size(max = 4000)
	private String description;
	@Size(max = 255)
	private String sandboxBranch;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSandboxBranch() {
		return sandboxBranch;
	}

	public void setSandboxBranch(String sandboxBranch) {
		this.sandboxBranch = sandboxBranch;
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/**
	 * CreateSiteRequest with a remote repository as source
	 */
	public static final class RemoteSource extends CreateSiteRequest {
		@NotEmpty
		private String repositoryUrl;

		public String getRepositoryUrl() {
			return repositoryUrl;
		}

		public void setRepositoryUrl(String repositoryUrl) {
			this.repositoryUrl = repositoryUrl;
		}

		@Override
		public String toString() {
			return "RemoteSource{" +
					"siteId='" + getSiteId() + '\'' +
					", name='" + getName() + '\'' +
					", description='" + getDescription() + '\'' +
					", sandboxBranch='" + getSandboxBranch() + '\'' +
					", repositoryUrl='" + repositoryUrl + '\'' +
					'}';
		}
	}

	/**
	 * CreateSiteRequest with a blueprint as source
	 */
	public static final class BlueprintSource extends CreateSiteRequest {
		@NotEmpty
		private String blueprintId;
		private Map<String,String> siteParams;

		public String getBlueprintId() {
			return blueprintId;
		}

		public void setBlueprintId(String blueprintId) {
			this.blueprintId = blueprintId;
		}

		public Map<String, String> getSiteParams() {
			return siteParams;
		}

		public void setSiteParams(Map<String, String> siteParams) {
			this.siteParams = siteParams;
		}

		@Override
		public String toString() {
			return "BlueprintSource{" +
					"siteId='" + getSiteId() + '\'' +
					", name='" + getName() + '\'' +
					", description='" + getDescription() + '\'' +
					", sandboxBranch='" + getSandboxBranch() + '\'' +
					", blueprint='" + blueprintId + '\'' +
					", siteParams=" + siteParams +
					'}';
		}
	}
}
