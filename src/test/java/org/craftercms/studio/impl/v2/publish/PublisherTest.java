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

package org.craftercms.studio.impl.v2.publish;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.dal.SiteDAO;
import org.craftercms.studio.api.v2.dal.publish.PublishDAO;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.event.publish.RequestPublishEvent;
import org.craftercms.studio.api.v2.service.audit.ActivityStreamService;
import org.craftercms.studio.api.v2.task.TaskManager;
import org.craftercms.studio.api.v2.task.TaskProgress;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_PUBLISH_START;
import static org.craftercms.studio.api.v2.dal.Site.State.READY;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PublisherTest {
	static final String DISABLED_PUBLISH_SITE_ID = "disabledSite1";
	static final String SITE_ID = "site2";
	static final long PACKAGE_ID = 123;
	static final long SITE_NUMERIC_ID = 456;

	@Mock
	SiteDAO siteDAO;

	@Mock
	PublishDAO publishDAO;

	@Mock
	Site disabledSite;

	@Mock
	Site site2;

	@Mock
	PublishPackage publishPackage;

	@Mock
	GeneralLockService generalLockService;

	@Mock
	TaskManager taskManager;

	@Mock
	ActivityStreamService activityService;

	@Mock
	ApplicationEventPublisher eventPublisher;

	@Spy
	@InjectMocks
	Publisher publisher;

	@Before
	public void setUp() {
		when(disabledSite.getPublishingEnabled()).thenReturn(false);

		when(site2.getPublishingEnabled()).thenReturn(true);
		when(site2.getSiteId()).thenReturn(SITE_ID);
		when(site2.getId()).thenReturn(SITE_NUMERIC_ID);

		when(siteDAO.getSite(SITE_ID)).thenReturn(site2);
		when(site2.getState()).thenReturn(READY);

		when(siteDAO.getSite(DISABLED_PUBLISH_SITE_ID)).thenReturn(disabledSite);

		when(publishPackage.getSite()).thenReturn(site2);

		when(taskManager.registerTask(any())).then(invocation -> {
			TaskProgress<?, ?> taskProgress = mock(TaskProgress.class);
			when(taskProgress.startStage(any())).thenReturn(mock(TaskProgress.Stage.class));
			return taskProgress;
		});

		publisher.setApplicationEventPublisher(eventPublisher);

		when(publishPackage.getPackageType()).thenReturn(PublishPackage.PackageType.ITEM_LIST);
	}

	@Test
	public void publishDisabledTest() throws ServiceLayerException {
		publisher.handleRequestPublishEvent(new RequestPublishEvent(DISABLED_PUBLISH_SITE_ID, PACKAGE_ID));
		verify(publisher, never()).lockAndPublish(anyLong(), anyLong());
	}

	@Test
	public void lockUnavailableTest() throws ServiceLayerException {
		when(generalLockService.tryLock(StudioUtils.getPublishingLockKey(SITE_ID))).thenReturn(false);
		publisher.handleRequestPublishEvent(new RequestPublishEvent(SITE_ID, PACKAGE_ID));

		verify(generalLockService).tryLock(StudioUtils.getPublishingLockKey(SITE_ID));
		verify(publisher, never()).lockAndPublish(anyLong(), anyLong());
	}

	@Test
	public void notReadySiteTest() throws ServiceLayerException {
		when(site2.getState()).thenReturn(Site.State.INITIALIZING);
		publisher.handleRequestPublishEvent(new RequestPublishEvent(SITE_ID, PACKAGE_ID));

		verify(site2).getState();
		verify(publisher, never()).lockAndPublish(anyLong(), anyLong());
	}

	@Test
	public void lockedPackageTest() throws ServiceLayerException {
		when(generalLockService.tryLock(StudioUtils.getPublishingLockKey(SITE_ID))).thenReturn(true);
		when(site2.getState()).thenReturn(READY);
		when(generalLockService.tryLock(StudioUtils.getPublishPackageLockKey(PACKAGE_ID))).thenReturn(false);

		publisher.handleRequestPublishEvent(new RequestPublishEvent(SITE_ID, PACKAGE_ID));

		verify(publisher).lockAndPublish(anyLong(), anyLong());
		verify(publisher, never()).doPublish(any());
		verify(generalLockService).tryLock(StudioUtils.getPublishPackageLockKey(PACKAGE_ID));
	}

	@Test
	public void doPublishCallTest() throws ServiceLayerException {
		when(generalLockService.tryLock(StudioUtils.getPublishingLockKey(SITE_ID))).thenReturn(true);
		when(site2.getState()).thenReturn(READY);
		when(generalLockService.tryLock(StudioUtils.getPublishPackageLockKey(PACKAGE_ID))).thenReturn(true);
		doNothing().when(publisher).doPublish(any());

		publisher.handleRequestPublishEvent(new RequestPublishEvent(SITE_ID, PACKAGE_ID));

		verify(publisher).doPublish(any());
		verify(generalLockService).tryLock(StudioUtils.getPublishPackageLockKey(PACKAGE_ID));
	}

	@Test
	public void auditPublishTest() throws ServiceLayerException {
		when(publishPackage.getId()).thenReturn(PACKAGE_ID);
		when(publishDAO.getById(SITE_NUMERIC_ID, PACKAGE_ID)).thenReturn(publishPackage);

		when(generalLockService.tryLock(StudioUtils.getPublishingLockKey(SITE_ID))).thenReturn(true);
		when(site2.getState()).thenReturn(READY);
		when(generalLockService.tryLock(StudioUtils.getPublishPackageLockKey(PACKAGE_ID))).thenReturn(true);

		doNothing().when(publisher).auditPublishOperation(any(), any());
		doNothing().when(publisher).doPublishItemList(any(), any(), any());

		publisher.handleRequestPublishEvent(new RequestPublishEvent(SITE_ID, PACKAGE_ID));

		verify(publisher).auditPublishOperation(any(), eq(OPERATION_PUBLISH_START));
	}
}
