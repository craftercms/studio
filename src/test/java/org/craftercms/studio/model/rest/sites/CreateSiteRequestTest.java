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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CreateSiteRequestTest {

	@Test
	public void testCreateSiteFromBlueprint() throws JsonProcessingException {
		String requestJson = """
				{
				  "siteId": "test-site",
				  "name": "Test Site",
				  "description": "A site created from a blueprint",
				  "sourceType": "blueprint",
				  "blueprint": "test-blueprint"
				}
				""";
		ObjectMapper objectMapper = new ObjectMapper();
		CreateSiteRequest request = objectMapper.readValue(requestJson, CreateSiteRequest.class);
		assertInstanceOf(CreateSiteRequest.BlueprintSource.class, request);
		CreateSiteRequest.BlueprintSource blueprintRequest = (CreateSiteRequest.BlueprintSource) request;
		assertEquals("test-site", blueprintRequest.getSiteId());
		assertEquals("Test Site", blueprintRequest.getName());
		assertEquals("A site created from a blueprint", blueprintRequest.getDescription());
		assertEquals("test-blueprint", blueprintRequest.getBlueprintId());
	}

}
