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

package org.craftercms.studio.impl.v1.service.dependency;

import org.apache.commons.io.IOUtils;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.dependency.DependencyResolver;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.content.ContentService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.toInputStream;
import static org.craftercms.studio.api.v1.constant.StudioConstants.MODULE_STUDIO;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RelativePageDependencyResolverTest {
	public static final String SITE_ID = "mySite";
	public static final Resource RESOLVER_CONFIG_CONTENT = new ClassPathResource("repo-bootstrap/global/configuration/dependency/resolver-config.xml");
	public static final String SITE_CONFIG_FILE_NAME = "dependency/resolver-config.xml";
	public static final String CONFIG_BASE_PATH = "/configuration/dependency";
	public static final String DEFAULT_CONFIG_FILE_NAME = "resolver-config.xml";
	public static final String PAGE_PATH = "/site/website/page/test/index.xml";

	@Mock
	private StudioConfiguration studioConfiguration;

	@Mock
	private ConfigurationService configurationService;

	@Mock
	private ContentService contentService;

	@InjectMocks
	private RegexDependencyResolver dependencyResolver;

	@Before
	public void setUp() throws IOException, DocumentException, ServiceLayerException {
		try (InputStream is = RESOLVER_CONFIG_CONTENT.getInputStream()) {
			Document doc = DocumentHelper.parseText(IOUtils.toString(is, UTF_8));

			when(configurationService.getConfigurationAsDocument(SITE_ID, MODULE_STUDIO, SITE_CONFIG_FILE_NAME, null))
					.thenReturn(doc);
		}
		when(studioConfiguration.getProperty(CONFIGURATION_SITE_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME))
				.thenReturn(SITE_CONFIG_FILE_NAME);
		when(studioConfiguration.getProperty(CONFIGURATION_DEFAULT_DEPENDENCY_RESOLVER_CONFIG_BASE_PATH))
				.thenReturn(CONFIG_BASE_PATH);
		when(studioConfiguration.getProperty(CONFIGURATION_DEFAULT_DEPENDENCY_RESOLVER_CONFIG_FILE_NAME))
				.thenReturn(DEFAULT_CONFIG_FILE_NAME);
	}

	@Test
	public void testRelativePageDependency() throws ContentNotFoundException {
		String content = """
				<html>
				<body>
				    <a href="/articles/page1">Link to A</a>
				    <a href="/site/website/page1/index.xml">Link to A</a>
				</body>
				</html>
				""";
		when(contentService.getContent(SITE_ID, PAGE_PATH)).thenReturn(toInputStream(content, UTF_8));

		Map<String, Set<DependencyResolver.ResolvedDependency>> deps = dependencyResolver.resolve(SITE_ID, PAGE_PATH);

		assertNotNull(deps);

		Set<DependencyResolver.ResolvedDependency> pageDeps = deps.get("page");

		assertEquals(pageDeps.size(), 2);
		assertTrue(pageDeps.stream().anyMatch(d -> d.path().equals("/site/website/page1/index.xml")));
		assertTrue(pageDeps.stream().anyMatch(d -> d.path().equals("/site/website/articles/page1/index.xml")));
	}

	@Test
	public void testHrefStartsWithReservedPath() throws ContentNotFoundException {
		String content = """
				<html>
				<body>
				    <a href="/sitemap/custom/path">Link to A</a>
				</body>
				</html>
				""";
		when(contentService.getContent(SITE_ID, PAGE_PATH)).thenReturn(toInputStream(content, UTF_8));
		Map<String, Set<DependencyResolver.ResolvedDependency>> deps = dependencyResolver.resolve(SITE_ID, PAGE_PATH);
		assertNotNull(deps);
		Set<DependencyResolver.ResolvedDependency> pageDeps = deps.get("page");
		assertEquals(pageDeps.size(), 1);
		assertTrue(pageDeps.stream().anyMatch(d -> d.path().equals("/site/website/sitemap/custom/path/index.xml")));
	}

	@Test
	public void testQueryStringHrefs() throws ContentNotFoundException {
		String content = """
				<html>
				<body>
				    <a href="/page1/path?param=shouldNotBeIncluded">Link to A</a>
				</body>
				</html>
				""";
		when(contentService.getContent(SITE_ID, PAGE_PATH)).thenReturn(toInputStream(content, UTF_8));
		Map<String, Set<DependencyResolver.ResolvedDependency>> deps = dependencyResolver.resolve(SITE_ID, PAGE_PATH);
		assertNotNull(deps);
		Set<DependencyResolver.ResolvedDependency> pageDeps = deps.get("page");
		assertEquals(pageDeps.size(), 1);
		assertTrue(pageDeps.stream().anyMatch(d -> d.path().equals("/site/website/page1/path/index.xml")));
	}

	@Test
	public void testFragmentsHrefs() throws ContentNotFoundException {
		String content = """
				<html>
				<body>
				    <a href="/page1/path#fragmentShouldNotBeIncluded">Link to A</a>
				</body>
				</html>
				""";
		when(contentService.getContent(SITE_ID, PAGE_PATH)).thenReturn(toInputStream(content, UTF_8));
		Map<String, Set<DependencyResolver.ResolvedDependency>> deps = dependencyResolver.resolve(SITE_ID, PAGE_PATH);
		assertNotNull(deps);
		Set<DependencyResolver.ResolvedDependency> pageDeps = deps.get("page");
		assertEquals(pageDeps.size(), 1);
		assertTrue(pageDeps.stream().anyMatch(d -> d.path().equals("/site/website/page1/path/index.xml")));
	}
}
