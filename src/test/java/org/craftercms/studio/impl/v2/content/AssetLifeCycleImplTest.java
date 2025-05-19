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


import org.craftercms.studio.api.v1.asset.Asset;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessingConfigReader;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorPipeline;
import org.craftercms.studio.api.v1.asset.processing.AssetProcessorPipelineResolver;
import org.craftercms.studio.api.v1.asset.processing.ProcessorPipelineConfiguration;
import org.craftercms.studio.api.v1.exception.AssetProcessingException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.content.ContentLoader;
import org.craftercms.studio.api.v2.content.LifeCycleContent;
import org.craftercms.studio.api.v2.content.LifeCycleContent.ContentLifeCycleItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AssetLifeCycleImplTest {

	@Mock
	private AssetProcessorPipelineResolver pipelineResolver;

	@Mock
	private AssetProcessingConfigReader configReader;

	@Mock
	private ContentLoader contentLoader;

	@Mock
	private LifeCycleContent lifeCycleContent;

	@Mock
	private AssetProcessorPipeline pipeline;

	@InjectMocks
	private AssetLifeCycleImpl assetLifeCycle;

	private final String siteId = "site1";
	private final String configPath = "/config/path";

	@Before
	public void setUp() {
		assetLifeCycle = new AssetLifeCycleImpl(pipelineResolver, configReader, configPath);
	}

	@Test
	public void testExecuteWithMissingConfig() throws ServiceLayerException {
		when(contentLoader.getContentRaw(siteId, configPath)).thenReturn(null);

		assetLifeCycle.execute(siteId, lifeCycleContent, contentLoader);

		verifyNoInteractions(configReader, pipelineResolver, lifeCycleContent);
	}

	@Test
	public void testExecuteWithEmptyConfig() throws ServiceLayerException {
		InputStream configIn = mock(InputStream.class);
		when(contentLoader.getContentRaw(siteId, configPath)).thenReturn(configIn);
		when(configReader.readConfig(configIn)).thenReturn(emptyList());

		assetLifeCycle.execute(siteId, lifeCycleContent, contentLoader);

		verify(configReader, times(1)).readConfig(configIn);
		verifyNoInteractions(pipelineResolver, lifeCycleContent);
	}

	@Test
	public void testExecuteWithValidPipelines() throws ServiceLayerException {
		InputStream configIn = mock(InputStream.class);
		ProcessorPipelineConfiguration pipelineConfig = mock(ProcessorPipelineConfiguration.class);
		Asset outputAsset = mock(Asset.class);
		when(outputAsset.getRepoPath()).thenReturn("path/to/output");
		when(outputAsset.getFilePath()).thenReturn(Path.of("temp/location/output"));

		ContentLifeCycleItem item = mock(ContentLifeCycleItem.class);
		when(item.filePath()).thenReturn(Path.of("temp/location/input"));
		when(item.repoPath()).thenReturn("/path/to/asset");

		Map<String, ContentLifeCycleItem> lifeCycleContentItems = new HashMap<>();
		lifeCycleContentItems.put(item.repoPath(), item);

		when(contentLoader.getContentRaw(siteId, configPath)).thenReturn(configIn);
		when(configReader.readConfig(configIn)).thenReturn(List.of(pipelineConfig));
		when(lifeCycleContent.getRepoPath()).thenReturn("/path/to/asset");
		when(lifeCycleContent.getItems()).thenReturn(lifeCycleContentItems);
		doAnswer(a -> {
			lifeCycleContentItems.put(a.getArgument(0), new ContentLifeCycleItem(a.getArgument(0), a.getArgument(1)));
			return null;
		}).when(lifeCycleContent).write(anyString(), any(Path.class));
		doReturn(item).when(lifeCycleContent).get("/path/to/asset");
		when(pipelineResolver.getPipeline(pipelineConfig)).thenReturn(pipeline);
		doReturn(List.of(outputAsset)).when(pipeline).processAsset(eq(pipelineConfig), any());

		assetLifeCycle.execute(siteId, lifeCycleContent, contentLoader);

		verify(pipelineResolver, times(1)).getPipeline(pipelineConfig);
		verify(pipeline, times(1)).processAsset(eq(pipelineConfig), any());
		verify(lifeCycleContent, times(1)).write(outputAsset.getRepoPath(), outputAsset.getFilePath());

		assertTrue("Lifecycle items should contain output asset", lifeCycleContent.getItems().containsKey(outputAsset.getRepoPath()));
	}

	@Test
	public void testExecuteWithPipelineException() throws AssetProcessingException {
		InputStream configIn = mock(InputStream.class);
		ProcessorPipelineConfiguration pipelineConfig = mock(ProcessorPipelineConfiguration.class);
		ContentLifeCycleItem item = mock(ContentLifeCycleItem.class);

		when(contentLoader.getContentRaw(siteId, configPath)).thenReturn(configIn);
		when(configReader.readConfig(configIn)).thenReturn(List.of(pipelineConfig));
		when(lifeCycleContent.getRepoPath()).thenReturn("/path/to/asset");
		doReturn(item).when(lifeCycleContent).get("/path/to/asset");
		when(pipelineResolver.getPipeline(pipelineConfig)).thenReturn(pipeline);
		doThrow(AssetProcessingException.class).when(pipeline).processAsset(eq(pipelineConfig), any());

		assertThrows(ServiceLayerException.class, () -> assetLifeCycle.execute(siteId, lifeCycleContent, contentLoader));

		verify(pipelineResolver, times(1)).getPipeline(pipelineConfig);
		verify(pipeline, times(1)).processAsset(eq(pipelineConfig), any());
	}

}
