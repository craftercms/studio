/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v1.job;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.to.EmailMessageQueueTo;
import org.craftercms.studio.api.v1.to.EmailMessageTO;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.util.List;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.MAIL_FROM_DEFAULT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.MAIL_SMTP_AUTH;

public class EmailMessageSender implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(EmailMessageSender.class);
    protected JavaMailSender emailService;
    protected JavaMailSender emailServiceNoAuth;
    protected EmailMessageQueueTo emailMessages;
    protected StudioConfiguration studioConfiguration;
    private Thread thread;
    private boolean running;

    public String getDefaultFromAddress() {
        return studioConfiguration.getProperty(MAIL_FROM_DEFAULT);
    }

    public void initThread() {
        thread = new Thread(this);
        running = true;
        thread.start();
    }

    @Override
    public void run() {
        while (running) {
            try {
                if (emailMessages.size() > 0) {
                    List<EmailMessageTO> list = emailMessages.getAll();
                    int size = list.size();
                    for (int counter = 0; counter < size; counter++) {
                        EmailMessageTO emailMessage = list.get(counter);
                        emailMessage.preprocessEmail();
                        String userEmailAddress = emailMessage.getTo();
                        String content = emailMessage.getContent();
                        String subject = emailMessage.getSubject();
                        String replyTo = emailMessage.getReplyTo();
                        String personalFromName = emailMessage.getPersonalFromName();
                        boolean success = sendEmail(subject, content, userEmailAddress, replyTo, personalFromName);
                        if (success) {
                            logger.debug("Successfully sent email to:" + userEmailAddress);
                        } else {
                            logger.error("Could not send email to:" + userEmailAddress);
                        }
                        emailMessage = null;
                    }
                }
                int secs = 30;
                Thread.sleep(secs * 1000L);

            } catch (InterruptedException e) {
                logger.warn("Interrupted while Thread.sleep()", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    protected boolean sendEmail(final String subject, final String content, final String userEmailAddress,
                                final String replyTo, final String personalFromName) {
        boolean success = true;
        MimeMessagePreparator preparator = new MimeMessagePreparator() {

            public void prepare(MimeMessage mimeMessage) throws Exception {

                mimeMessage.addRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmailAddress));
                InternetAddress[] replyTos = new InternetAddress[1];
                if ((replyTo != null) && (!"".equals(replyTo))) {
                    replyTos[0] = new InternetAddress(replyTo);
                    mimeMessage.setReplyTo(replyTos);
                }
                InternetAddress fromAddress = new InternetAddress(getDefaultFromAddress());
                if (personalFromName != null)
                    fromAddress.setPersonal(personalFromName);
                mimeMessage.setFrom(fromAddress);
                mimeMessage.setContent(content, "text/html; charset=utf-8");
                mimeMessage.setSubject(subject);
                logger.debug("sending email to [" + userEmailAddress + "]subject subject :[" + subject + "]");
            }
        };
        try {
            if (isAuthenticatedSMTP()) {
                emailService.send(preparator);
            } else {
                emailServiceNoAuth.send(preparator);
            }
        } catch (MailException ex) {
            // simply log it and go on...
            logger.error("Error sending email notification to:" + userEmailAddress, ex);

            success = false;
        }

        return success;
    }

    public void shutdown() {
        if (thread != null) {
            running = false;
            thread.interrupt();
        }
    }

    public boolean isAuthenticatedSMTP() {
        boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(MAIL_SMTP_AUTH));
        return toReturn;
    }

    public JavaMailSender getEmailService() {
        return emailService;
    }

    public void setEmailService(JavaMailSender emailService) {
        this.emailService = emailService;
    }

    public void setEmailMessages(EmailMessageQueueTo emailMessages) {
        this.emailMessages = emailMessages;
    }

    public JavaMailSender getEmailServiceNoAuth() {
        return emailServiceNoAuth;
    }

    public void setEmailServiceNoAuth(JavaMailSender emailServiceNoAuth) {
        this.emailServiceNoAuth = emailServiceNoAuth;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }
}
