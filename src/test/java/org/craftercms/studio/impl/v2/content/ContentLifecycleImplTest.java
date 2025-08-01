/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.content;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.script.ScriptExecutor;
import org.craftercms.studio.api.v2.content.ContentLoader;
import org.craftercms.studio.api.v2.content.LifecycleContent;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import javax.script.ScriptException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.DmConstants.KEY_APPLICATION_CONTEXT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_LIFECYCLE_INCLUDED_BEANS;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_LIFECYCLE_INCLUDE_APPLICATION_CONTEXT;
import static org.craftercms.studio.api.v2.content.LifecycleContent.LifecycleOperation.UPDATE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONTENT_PROCESSOR_CONTENT_LIFE_CYCLE_SCRIPT_LOCATION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ContentLifecycleImplTest {

	private static final String SITE_ID = "test-site1";

	@Mock
	private StudioConfiguration studioConfiguration;

	@Mock
	private ScriptExecutor scriptExecutor;

	@Mock
	private ContentLoader contentLoader;

	@Mock
	private ApplicationContext applicationContext;

	@Mock
	private LifecycleContent lifecycleContent;

	@InjectMocks
	@Spy
	private ContentLifecycleImpl contentLifecycle;

	static Logger mockLogger;
	static MockedStatic<LoggerFactory> loggerFactoryMockedStatic;

	@BeforeClass
	public static void setUpClass() {
		loggerFactoryMockedStatic = mockStatic(LoggerFactory.class);
		mockLogger = mock(Logger.class);
		loggerFactoryMockedStatic.when(() -> LoggerFactory.getLogger(ContentLifecycleImpl.class)).thenReturn(mockLogger);
	}

	@AfterClass
	public static void tearDownClass() {
		// Close the static mock
		loggerFactoryMockedStatic.close();
	}

	@Before
	public void setUp() {
		doReturn("/config/studio/content-types").when(studioConfiguration).getProperty(CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_BASE_PATH);
		doReturn("/config/studio/content-types/{content-type}/controller.groovy").when(studioConfiguration).getProperty(CONTENT_PROCESSOR_CONTENT_LIFE_CYCLE_SCRIPT_LOCATION);
		contentLifecycle.setApplicationContext(applicationContext);
	}

	@Test
	public void testExecuteWithEmptyContentType() throws ServiceLayerException {
		when(lifecycleContent.getContentType()).thenReturn("");
		when(lifecycleContent.getRepoPath()).thenReturn("/path");

		contentLifecycle.execute(SITE_ID, lifecycleContent, contentLoader);

		verifyNoInteractions(scriptExecutor);
	}

	@Test
	public void testExecuteWithUnknownContentType() throws ServiceLayerException {
		when(lifecycleContent.getContentType()).thenReturn("unknown");
		when(lifecycleContent.getRepoPath()).thenReturn("/path");

		contentLifecycle.execute(SITE_ID, lifecycleContent, contentLoader);

		verifyNoInteractions(scriptExecutor);
	}

	@Test
	public void testExecuteWithMissingScript() throws Exception {
		when(lifecycleContent.getContentType()).thenReturn("type");
		when(lifecycleContent.getRepoPath()).thenReturn("/path");
		when(contentLoader.getContentRaw(eq(SITE_ID), anyString())).thenReturn(null);

		contentLifecycle.execute(SITE_ID, lifecycleContent, contentLoader);

		verifyNoInteractions(scriptExecutor);
	}

	@Test
	public void testExecuteWithEmptyScript() throws Exception {
		when(lifecycleContent.getContentType()).thenReturn("type");
		when(lifecycleContent.getRepoPath()).thenReturn("/path");
		InputStream emptyScript = new ByteArrayInputStream("".getBytes());
		when(contentLoader.getContentRaw(eq(SITE_ID), anyString())).thenReturn(emptyScript);

		contentLifecycle.execute(SITE_ID, lifecycleContent, contentLoader);

		verifyNoInteractions(scriptExecutor);
	}

	@Test
	public void testExecuteWithValidScript() throws Exception {
		when(lifecycleContent.getContentType()).thenReturn("type");
		when(lifecycleContent.getRepoPath()).thenReturn("/path");
		InputStream scriptStream = new ByteArrayInputStream("print('Hello World')".getBytes());
		when(contentLoader.getContentRaw(eq(SITE_ID), anyString())).thenReturn(scriptStream);

		Map<String, Object> model = Map.of("key", "value");
		doReturn(model).when(contentLifecycle).buildModel(SITE_ID, lifecycleContent, contentLoader);

		contentLifecycle.execute(SITE_ID, lifecycleContent, contentLoader);

		verify(scriptExecutor, times(1)).executeScriptString(eq(SITE_ID), anyString(), eq(model));
	}

	@Test
	public void testExecuteWithScriptExecutionException() throws Exception {
		when(lifecycleContent.getContentType()).thenReturn("type");
		when(lifecycleContent.getRepoPath()).thenReturn("/path");
		InputStream scriptStream = new ByteArrayInputStream("print('Hello World')".getBytes());
		when(contentLoader.getContentRaw(eq(SITE_ID), anyString())).thenReturn(scriptStream);

		Map<String, Object> model = Map.of("key", "value");
		doReturn(model).when(contentLifecycle).buildModel(SITE_ID, lifecycleContent, contentLoader);

		doThrow(ScriptException.class).when(scriptExecutor).executeScriptString(eq(SITE_ID), anyString(), eq(model));

		assertThrows(ServiceLayerException.class, () -> contentLifecycle.execute(SITE_ID, lifecycleContent, contentLoader));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testApplicationContextDisabled() throws ServiceLayerException, ScriptException {
		doNothing().when(contentLifecycle).addSpringBeans(any());
		when(lifecycleContent.getContentType()).thenReturn("type");
		when(lifecycleContent.getRepoPath()).thenReturn("/path");
		when(lifecycleContent.getOperation()).thenReturn(UPDATE);
		doReturn(false).when(studioConfiguration).getProperty(CONTENT_LIFECYCLE_INCLUDE_APPLICATION_CONTEXT, Boolean.class, false);
		InputStream scriptStream = new ByteArrayInputStream("print('Hello World')".getBytes());
		when(contentLoader.getContentRaw(eq(SITE_ID), anyString())).thenReturn(scriptStream);

		ArgumentCaptor<Map<String, Object>> modelCaptor = ArgumentCaptor.forClass(Map.class);
		contentLifecycle.execute(SITE_ID, lifecycleContent, contentLoader);

		verify(scriptExecutor, times(1)).executeScriptString(eq(SITE_ID), anyString(), modelCaptor.capture());

		assertFalse("Application context variable should be disabled", modelCaptor.getValue().containsKey(KEY_APPLICATION_CONTEXT));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAvailableBeans() throws ScriptException, ServiceLayerException {
		doReturn("type").when(lifecycleContent).getContentType();
		doReturn("/path").when(lifecycleContent).getRepoPath();
		doReturn(UPDATE).when(lifecycleContent).getOperation();
		ArrayList<String> beanNames = new ArrayList<>(List.of("bean1", "bean2", "bean3"));
		doReturn(false).when(studioConfiguration).getProperty(CONTENT_LIFECYCLE_INCLUDE_APPLICATION_CONTEXT, Boolean.class, false);
		doReturn(beanNames.toArray(new String[0])).when(studioConfiguration).getArray(CONTENT_LIFECYCLE_INCLUDED_BEANS, String.class);
		doReturn(new Object()).when(applicationContext).getBean(ArgumentMatchers.<String>argThat(beanNames::contains));
		InputStream scriptStream = new ByteArrayInputStream("print('Hello World')".getBytes());
		when(contentLoader.getContentRaw(eq(SITE_ID), anyString())).thenReturn(scriptStream);

		ArgumentCaptor<Map<String, Object>> modelCaptor = ArgumentCaptor.forClass(Map.class);
		contentLifecycle.execute(SITE_ID, lifecycleContent, contentLoader);

		verify(scriptExecutor, times(1)).executeScriptString(eq(SITE_ID), anyString(), modelCaptor.capture());

		assertTrue("Model should contain all configured beans", beanNames.stream().allMatch(modelCaptor.getValue()::containsKey));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAvailableBeanDoesNotExist() throws ScriptException, ServiceLayerException {
		when(lifecycleContent.getContentType()).thenReturn("type");
		when(lifecycleContent.getRepoPath()).thenReturn("/path");
		when(lifecycleContent.getOperation()).thenReturn(UPDATE);
		List<String> beanNames = List.of("bean1", "bean2", "bean3");
		List<String> existingBeans = List.of("bean1", "bean2");
		doReturn(false).when(studioConfiguration).getProperty(CONTENT_LIFECYCLE_INCLUDE_APPLICATION_CONTEXT, Boolean.class, false);
		doReturn(beanNames.toArray(new String[0])).when(studioConfiguration).getArray(CONTENT_LIFECYCLE_INCLUDED_BEANS, String.class);
		doReturn(new Object()).when(applicationContext).getBean(ArgumentMatchers.<String>argThat(name -> "bean1".equals(name) || "bean2".equals(name)));
		doThrow(NoSuchBeanDefinitionException.class).when(applicationContext).getBean("bean3");

		InputStream scriptStream = new ByteArrayInputStream("print('Hello World')".getBytes());
		when(contentLoader.getContentRaw(eq(SITE_ID), anyString())).thenReturn(scriptStream);

		ArgumentCaptor<Map<String, Object>> modelCaptor = ArgumentCaptor.forClass(Map.class);
		contentLifecycle.execute(SITE_ID, lifecycleContent, contentLoader);

		verify(scriptExecutor, times(1)).executeScriptString(eq(SITE_ID), anyString(), modelCaptor.capture());

		assertTrue("Model should contain existing configured beans", existingBeans.stream().allMatch(modelCaptor.getValue()::containsKey));
		assertFalse("Model should not contain bean3", modelCaptor.getValue().containsKey("bean3"));
	}

	@Test
	public void testGetScriptPathInvalidContentType() throws ServiceLayerException {
		doCallRealMethod()
				.when(contentLifecycle)
				.getScriptPath(any(), any());

		when(lifecycleContent.getContentType()).thenReturn("../../../../invalid/location");

		contentLifecycle.execute(SITE_ID, lifecycleContent, contentLoader);

		verify(mockLogger).error(argThat(msg -> msg.contains("Invalid script path")));
	}
}

