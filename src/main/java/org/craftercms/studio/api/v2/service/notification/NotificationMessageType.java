package org.craftercms.studio.api.v2.service.notification;

/**
 * Describe the types of notifications available.
 * @author Carlos Ortiz.
 */
public enum NotificationMessageType {
    /**
     * Describes messages of why content are been rejected.
     */
    CannedMessages,
    /**
     * Messages show on workflow dialogs.
     */
    CompleteMessages,
    /**
     * Notification Email type.
     */
    EmailMessage,
    /**
     * Other General Messages.
     */
    GeneralMessages
}
