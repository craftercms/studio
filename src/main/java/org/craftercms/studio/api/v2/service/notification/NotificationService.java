/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;

import java.util.Collection;
import java.util.List;

/**
 * New Interface for Workflow Notification Service.
 * @author Carlos Ortiz
 */
public interface NotificationService {

    /** Notification Message Keys **/
    /** Action Completed Message Keys **/
    String COMPLETE_GO_LIVE = "go-live";
    String COMPLETE_REJECT = "reject";
    String COMPLETE_SCHEDULE_GO_LIVE = "schedule-to-go-live";
    String COMPLETE_SUBMIT_TO_GO_LIVE = "submit-to-go-live";
    String COMPLETE_DELETE = "delete";

    /**
     * <p>Sends a email to configure emails when a deployment had fail</p>
     * @param site Name of the site which the deployment fail.
     * @param throwable Throwable error which break the deployment. (Can be null)
     * @param filesUnableToPublish List of files that where unable to publish (can be null)
     *
     */
    // TODO: fix for new publishing system
//    void notifyDeploymentError(final String site, final Throwable throwable, List<PublishRequest> filesUnableToPublish);

    /**
     * Process and Sends a generic email.
     * @param site Site of the Content.
     * @param toUsers List of recipients.
     * @param key key of the message wanted
     * @param params parameters of the message this params will be used to process the message string.
     */
    @SuppressWarnings("unchecked")
    void notify(final String site , final List<String> toUsers ,final String key, final Pair<String,Object>...params);

    /**
     * Send a notification message to the submitter of a package that has been approved
     *
     * @param publishPackage package that was approved
     * @param itemsSubmitted list of publish items to include in the message
     */
    void notifyPackageApproval(PublishPackage publishPackage, final Collection<String> itemsSubmitted);

    /**
     * Send a notification message to the submitter of a package that has been rejected
     * @param publishPackage package that was rejected
     * @param itemsSubmitted list of publish items to include in the message
     */
    void notifyPackageRejection(PublishPackage publishPackage, final Collection<String> itemsSubmitted);

    /**
     * Send a notification message to the configured approver 
     * @param publishPackage
     * @param itemsSubmitted
     */
    void notifyPackageSubmission(PublishPackage publishPackage, final Collection<String> itemsSubmitted);

    /**
     * Gets and process notification message
     * @param site Site of the Content.
     * @param type Type of the message wanted.
     * @param key key of the message wanted
     * @param params parameters of the message this params will be used to process the message string.
     * @return <p>the message in the given locale and processed with the given variables. </p><p>If message not found
     * either by key/locale it will <b>return a default string</b>) </p>
     */
    @SuppressWarnings("unchecked")
    String getNotificationMessage(final String site , final NotificationMessageType type, final String key,
                                  final Pair<String,Object>...params);

    /**
     * Send email to admin that repository has merged conflict
     *
     * @param site site with merge conflict
     * @param filesUnableToMerge files unable to merge
     */
    @Valid
    void notifyRepositoryMergeConflict(@ValidateStringParam String site,
                                       List<String> filesUnableToMerge);
}
