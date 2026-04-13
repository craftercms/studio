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

package org.craftercms.studio.api.v2.service.notification;

import jakarta.validation.Valid;
import org.apache.commons.lang3.tuple.Pair;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v2.dal.publish.PublishItem;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;

import java.util.Collection;
import java.util.List;

/**
 * New Interface for Workflow Notification Service.
 *
 * @author Carlos Ortiz
 */
public interface NotificationService {

	/**
	 * <p>Sends a email to configured emails when a publishing package had fail</p>
	 *
	 * @param publishPackage the package that was being published
	 * @param throwable      throwable error which break the deployment. (Can be null)
	 * @param failedItems    list of publish items that where unable to publish (can be null)
	 */
	void notifyPublishError(PublishPackage publishPackage, Throwable throwable, Collection<PublishItem> failedItems);

	/**
	 * Process and Sends a generic email.
	 *
	 * @param site    Site of the Content.
	 * @param toUsers List of recipients.
	 * @param key     key of the message wanted
	 * @param params  parameters of the message this params will be used to process the message string.
	 */
	@SuppressWarnings("unchecked")
	void notify(final String site, final Collection<String> toUsers, final String key, final Pair<String, Object>... params);

	/**
	 * Send a notification message to the submitter of a package that has been approved
	 *
	 * @param publishPackage package that was approved
	 * @param itemsSubmitted list of publish items to include in the message
	 */
	void notifyPackageApproval(PublishPackage publishPackage, final Collection<String> itemsSubmitted);

	/**
	 * Send a notification message to the submitter of a package that has been rejected
	 *
	 * @param publishPackage package that was rejected
	 * @param itemsSubmitted list of publish items to include in the message
	 */
	void notifyPackageRejection(PublishPackage publishPackage, final Collection<String> itemsSubmitted);

	/**
	 * Send a notification message to the configured approver
	 *
	 * @param publishPackage package that was submitted
	 * @param itemsSubmitted list of publish items to include in the message
	 */
	void notifyPackageSubmission(PublishPackage publishPackage, final Collection<String> itemsSubmitted);

	/**
	 * Send email to admin that repository has merged conflict
	 *
	 * @param site               site with merge conflict
	 * @param filesUnableToMerge files unable to merge
	 */
	@Valid
	void notifyRepositoryMergeConflict(@ValidateStringParam String site,
					   List<String> filesUnableToMerge);
}
