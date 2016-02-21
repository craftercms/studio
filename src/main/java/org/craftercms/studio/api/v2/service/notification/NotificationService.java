package org.craftercms.studio.api.v2.service.notification;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.tuple.Pair;

/**
 * New Interface for Workflow Notification Service.
 * @author Carlos Ortiz
 */
public interface NotificationService {

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
     * <p>Sends a email to configure emails when a deployment had fail</p>
     * @param name Name of the site which the deployment fail.
     */
    void notifyDeploymentError(final String name);

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
     * @param locale Language of the notification ,if null defaults to English.
     */
    void notifyContentApproval(final String site,final String submitterUser,final List<String> itemsSubmitted,final
    String approver,Locale locale);

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
        itemsSubmitted, final String submitter, final Date scheduleDate,final boolean isADelete,final String
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
     * Checks if Notification Services 2 is enable for the given site
     * @return True if is enable, false otherwise.
     */
    boolean isEnable();
}
