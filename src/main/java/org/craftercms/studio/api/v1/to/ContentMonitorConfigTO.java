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

import java.util.Collection;
import static java.util.Collections.emptyList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * Site content monitor configuration
 */
public class ContentMonitorConfigTO {
	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "monitor")
	private final Collection<ContentMonitorTO> monitors;

	public ContentMonitorConfigTO() {
		this.monitors = emptyList();
	}

	@SuppressWarnings("unused")
	public ContentMonitorConfigTO(Collection<ContentMonitorTO> contentMonitors) {
		this.monitors = contentMonitors;
	}

	public Collection<ContentMonitorTO> getMonitors() {
		return monitors;
	}

	public record ContentMonitorTO(String name, String query,
								   List<MonitorPathTO> paths) {
	}

	public record MonitorPathTO(String name, String pattern,
								String emailTemplate, String emails,
								String locale) {
	}
}
