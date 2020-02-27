/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.tuple.Pair;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;

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
     * @param locale Language of the notification ,if null defaults to English.
     *
     */
    void notifyDeploymentError(final String site, final Throwable throwable, List<String> filesUnableToPublish,
                               Locale locale);

    /**
     * <p>Sends a email to configure emails when a deployment had fail</p>
     * @param name Name of the site which the deployment fail.
     * @param throwable Throwable error which break the deployment. (Can be null)
     */
    void notifyDeploymentError(final String name, final Throwable throwable);

    /**
     * Process and Sends a generic email.
     * @param site Site of the Content.
     * @param toUsers List of recipients.
     * @param key key of the message wanted
     * @param locale Language of the message ,if null defaults to English.
     * @param params parameters of the message this params will be use to process the message string.
     */
    @SuppressWarnings("unchecked")
    void notify(final String site , final List<String> toUsers ,final String key, final Locale
        locale, final Pair<String,Object>...params);

    /**
     * Reloads the current configuration of the notification Service.
     * @param site Site to reload the notification service configuration.
     */
    void reloadConfiguration(final String site);

    /**
     * Sends Notification when content was approve.
     * @param site Site of the Content.
     * @param submitterUser User that submit the content to approval.
     * @param itemsSubmitted List of Item paths that where approve (can be null)
     * @param approver User that approve the content.
     * @param scheduleDate
     * @param locale Language of the notification ,if null defaults to English.
     */
    void notifyContentApproval(final String site, final String submitterUser, final List<String> itemsSubmitted,
                               final String approver, final ZonedDateTime scheduleDate, Locale locale);

    /**
     * Gets and process notification message
     * @param site Site of the Content.
     * @param type Type of the message wanted.
     * @param key key of the message wanted
     * @param locale Language of the message ,if null defaults to English.
     * @param params parameters of the message this params will be use to process the message string.
     * @return <p>the message in the given locale and processed with the given variables. </p><p>If message not found
     * either by key/locale it will <b>return a default string</b>) </p>
     */
    @SuppressWarnings("unchecked")
    String getNotificationMessage(final String site , final NotificationMessageType type, final String key, final Locale
        locale, final Pair<String,Object>...params);

    /**
     * Send to all given users a notification of content that need to be review.
     * @param site Site of the Content.
     * @param usersToNotify List of users (username) to be notify.
     * @param itemsSubmitted List of Item paths that where approve (can be null)
     * @param submitter User (username) that is submitting the content.
     * @param scheduleDate When the content should go live (null if now (or as soon is approve)).
     * @param locale Language of the message ,if null defaults to English.
     * @param isADelete Is this submission a delete one.
     */
    void notifyApprovesContentSubmission(final String site, final List<String> usersToNotify, final List<String>
        itemsSubmitted, final String submitter, final ZonedDateTime scheduleDate,final boolean isADelete,final String
        submissionComments,final Locale locale);

    /**
     * Notifies to the submitter that the content has been rejected.
     * @param site Site of the Content.
     * @param submittedBy User that submitted the rejected content.
     * @param rejectedItems Items that where rejected
     * @param rejectionReason  why the content was rejected.
     * @param userThatRejects User that is rejecting the content.
     * @param locale Language of the message ,if null defaults to English.
     */
    void notifyContentRejection(final String site,final String submittedBy,final List<String> rejectedItems,final
                                String rejectionReason, final String userThatRejects,final Locale locale);

    /**
     * Send email to admin that repository has merge conflict
     *
     * @param site site with merge conflict
     * @param filesUnableToMerge files unable to merge
     * @param locale language of the message ,if null defaults to English.
     */
    @ValidateParams
    void notifyRepositoryMergeConflict(@ValidateStringParam(name = "site") String site,
                                       List<String> filesUnableToMerge, Locale locale);
}
