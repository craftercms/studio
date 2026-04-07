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

package org.craftercms.studio.model.site;

import java.util.Collection;

/**
 * Includes the information about content matching a site
 * monitor queries.
 */
public class SiteMonitor {
	private String name;
	private Collection<SiteMonitorPath> paths;

	public Collection<SiteMonitorPath> getPaths() {
		return paths;
	}

	public void setPaths(Collection<SiteMonitorPath> paths) {
		this.paths = paths;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Includes the information about content matching a site monitor query for a configured path pattern
	 *
	 * @param name          the name of the path pattern
	 * @param emails        the emails to which the notifications should be sent
	 * @param emailTemplate the email template to be used for the notifications
	 * @param items         the collection of items matching the path pattern, including the item id, label and url
	 */
	public record SiteMonitorPath(String name, String emails, String emailTemplate, Collection<SiteMonitorItem> items) {
	}

	/**
	 * A matched item, including the item id, label and url
	 * This is a normal class instead of a record to avoid issues
	 * with freemarker
	 */
	public static class SiteMonitorItem {
		private final String id;
		private final String label;
		private final String url;

		public SiteMonitorItem(String id, String label, String url) {
			this.id = id;
			this.label = label;
			this.url = url;
		}

		public String getId() {
			return id;
		}

		public String getLabel() {
			return label;
		}

		public String getUrl() {
			return url;
		}
	}
}
