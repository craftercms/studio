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
import org.craftercms.commons.git.utils.AuthenticationType;
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
				  "blueprintId": "test-blueprint"
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

	@Test
	public void testCreateSiteFromRemote() throws JsonProcessingException {
		String requestJson = """
				{
				  "siteId": "test-site",
				  "name": "Test Site",
				  "description": "A site created from a remote repository",
				  "sourceType": "remote",
				  "remoteUrl": "http://example.com/repo.git",
				  "remoteName": "origin",
				  "remoteBranch": "main",
				  "authentication": {
					  "type": "none"
				  }
				}
				""";
		ObjectMapper objectMapper = new ObjectMapper();
		CreateSiteRequest request = objectMapper.readValue(requestJson, CreateSiteRequest.class);
		assertInstanceOf(CreateSiteRequest.RemoteSource.class, request);
		CreateSiteRequest.RemoteSource remoteRequest = (CreateSiteRequest.RemoteSource) request;
		assertEquals("test-site", remoteRequest.getSiteId());
		assertEquals("Test Site", remoteRequest.getName());
		assertEquals("A site created from a remote repository", remoteRequest.getDescription());
		assertEquals("http://example.com/repo.git", remoteRequest.getRemoteUrl());
		assertEquals("origin", remoteRequest.getRemoteName());
		assertEquals("main", remoteRequest.getRemoteBranch());
		assertEquals(AuthenticationType.none, remoteRequest.getAuthentication().getType());
	}

	@Test
	public void testCreateSiteFromRemoteBasicAuth() throws JsonProcessingException {
		String requestJson = """
				{
				  "siteId": "test-site",
				  "name": "Test Site",
				  "description": "A site created from a remote repository",
				  "sourceType": "remote",
				  "remoteUrl": "http://example.com/repo.git",
				  "authentication": {
					  "type": "basic",
					  "username": "user",
					  "password": "pass"
				  }
				}
				""";
		ObjectMapper objectMapper = new ObjectMapper();
		CreateSiteRequest request = objectMapper.readValue(requestJson, CreateSiteRequest.class);
		assertInstanceOf(CreateSiteRequest.RemoteSource.class, request);
		CreateSiteRequest.RemoteSource remoteRequest = (CreateSiteRequest.RemoteSource) request;
		assertEquals(AuthenticationType.basic, remoteRequest.getAuthentication().getType());
		assertEquals("user", remoteRequest.getAuthentication().getUsername());
		assertEquals("pass", remoteRequest.getAuthentication().getPassword());
	}

	@Test
	public void testCreateSiteFromRemoteTokenAuth() throws JsonProcessingException {
		String requestJson = """
				{
				  "siteId": "test-site",
				  "name": "Test Site",
				  "description": "A site created from a remote repository",
				  "sourceType": "remote",
				  "remoteUrl": "http://example.com/repo.git",
				  "authentication": {
					  "type": "token",
					  "token": "the secret token"
				  }
				}
				""";
		ObjectMapper objectMapper = new ObjectMapper();
		CreateSiteRequest request = objectMapper.readValue(requestJson, CreateSiteRequest.class);
		assertInstanceOf(CreateSiteRequest.RemoteSource.class, request);
		CreateSiteRequest.RemoteSource remoteRequest = (CreateSiteRequest.RemoteSource) request;
		assertEquals(AuthenticationType.token, remoteRequest.getAuthentication().getType());
		assertEquals("the secret token", remoteRequest.getAuthentication().getToken());
	}

	@Test
	public void testSingleBranchDefault() throws JsonProcessingException {
		String requestJson = """
				{
				  "siteId": "test-site",
				  "name": "Test Site",
				  "description": "A site created from a remote repository",
				  "sourceType": "remote",
				  "remoteUrl": "http://example.com/repo.git"
				}
				""";
		ObjectMapper objectMapper = new ObjectMapper();
		CreateSiteRequest request = objectMapper.readValue(requestJson, CreateSiteRequest.class);
		assertInstanceOf(CreateSiteRequest.RemoteSource.class, request);
		CreateSiteRequest.RemoteSource remoteRequest = (CreateSiteRequest.RemoteSource) request;
		assertTrue(remoteRequest.isSingleBranch());
	}

}
