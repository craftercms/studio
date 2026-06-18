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
package org.craftercms.studio.impl.v1.job;

import java.util.List;

import org.craftercms.studio.api.v1.to.EmailMessageQueueTo;
import org.craftercms.studio.api.v1.to.EmailMessageTO;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.MAIL_FROM_DEFAULT;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.MAIL_SMTP_AUTH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;

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

	@SuppressWarnings("unused")
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
					for (EmailMessageTO emailMessage : list) {
						String userEmailAddress = emailMessage.getTo();
						String content = emailMessage.getContent();
						String subject = emailMessage.getSubject();
						boolean success = sendEmail(subject, content, userEmailAddress);
						if (success) {
							logger.debug("Successfully sent email to '{}'", userEmailAddress);
						} else {
							logger.error("Could not send email to '{}'", userEmailAddress);
						}
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

	protected boolean sendEmail(final String subject, final String content, final String userEmailAddress) {
		boolean success = true;
		MimeMessagePreparator preparator = mimeMessage -> {

			mimeMessage.addRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmailAddress));
			InternetAddress fromAddress = new InternetAddress(getDefaultFromAddress());
			mimeMessage.setFrom(fromAddress);
			mimeMessage.setContent(content, "text/html; charset=utf-8");
			mimeMessage.setSubject(subject);
			logger.debug("Sending an email to '{}' with subject '{}'", userEmailAddress, subject);
		};

		try {
			if (isAuthenticatedSMTP()) {
				emailService.send(preparator);
			} else {
				emailServiceNoAuth.send(preparator);
			}
		} catch (MailException e) {
			logger.error("Error sending email to '{}'", userEmailAddress, e);
			success = false;
		}

		return success;
	}

	@SuppressWarnings("unused")
	public void shutdown() {
		if (thread != null) {
			running = false;
			thread.interrupt();
		}
	}

	public boolean isAuthenticatedSMTP() {
		return Boolean.parseBoolean(studioConfiguration.getProperty(MAIL_SMTP_AUTH));
	}

	@SuppressWarnings("unused")
	public void setEmailService(JavaMailSender emailService) {
		this.emailService = emailService;
	}

	@SuppressWarnings("unused")
	public void setEmailMessages(EmailMessageQueueTo emailMessages) {
		this.emailMessages = emailMessages;
	}

	@SuppressWarnings("unused")
	public void setEmailServiceNoAuth(JavaMailSender emailServiceNoAuth) {
		this.emailServiceNoAuth = emailServiceNoAuth;
	}

	public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
		this.studioConfiguration = studioConfiguration;
	}
}
