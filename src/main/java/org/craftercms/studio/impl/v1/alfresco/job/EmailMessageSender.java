/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.impl.v1.alfresco.job;

import org.craftercms.studio.impl.v1.alfresco.to.EmailMessageQueueTo;
import org.craftercms.studio.impl.v1.alfresco.to.EmailMessageTO;
import org.springframework.mail.javamail.JavaMailSender;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EmailMessageSender implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailMessageSender.class);
	protected JavaMailSender emailService;
	protected EmailMessageQueueTo emailMessages;
	protected String defaultFromAddress;


	public String getDefaultFromAddress() {
		return defaultFromAddress;
	}

	public void setDefaultFromAddress(String defaultFromAddress) {
		this.defaultFromAddress = defaultFromAddress;
	}

	public void initThread() {
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		while(true)
		{
			try
			{
				if(emailMessages.size()>0)
				{
					List<EmailMessageTO> list=emailMessages.getAll();
					int size= list.size();
					for(int counter=0;counter<size;counter++)
					{
						EmailMessageTO emailMessage=list.get(counter);
						emailMessage.preprocessEmail();
						String userEmailAddress=emailMessage.getTo();
						String content= emailMessage.getContent();
						String subject = emailMessage.getSubject();
						String replyTo = emailMessage.getReplyTo();
						String personalFromName=emailMessage.getPersonalFromName();
						boolean success=sendEmail(subject,content,userEmailAddress,replyTo,personalFromName);
						if(success) {
							if (LOGGER.isDebugEnabled()) {
								LOGGER.debug("Successfully sent email to:"+userEmailAddress);
							}
						}
						else {
							LOGGER.error("Could not send email to:"+userEmailAddress);
						}
						emailMessage=null;
					}
				}
				int secs= 30;
				Thread.sleep(secs*1000);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		// TODO Auto-generated method stub

	}

	protected boolean sendEmail(final String subject,final String content,final String userEmailAddress,final String replyTo,final String personalFromName)
	{
		boolean success=true;
		MimeMessagePreparator preparator = new MimeMessagePreparator() {

			public void prepare(MimeMessage mimeMessage) throws Exception {

				mimeMessage.setRecipient(Message.RecipientType.TO,
						new InternetAddress(userEmailAddress));
				InternetAddress[] replyTos= new InternetAddress[1];
				if( (replyTo != null) && (!"".equals(replyTo)) )
				{
					replyTos[0]= new InternetAddress(replyTo);
					mimeMessage.setReplyTo(replyTos);
				}
				InternetAddress fromAddress= new InternetAddress(defaultFromAddress);
				if(personalFromName != null)
					fromAddress.setPersonal(personalFromName);
				mimeMessage.setFrom(fromAddress);
				mimeMessage.setText(content);
				mimeMessage.setSubject(subject);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("sending email to ["+userEmailAddress+"]subject subject :["+subject+"]");
				}
			}
		};
		try {
			emailService.send(preparator);
		}
		catch (MailException ex) {
			// simply log it and go on...
			LOGGER.error("Error sending email notification to:"+userEmailAddress,ex);

			success=false;
		}

		return success;
	}

	public JavaMailSender getEmailService() {
		return emailService;
	}

	public void setEmailService(JavaMailSender emailService) {
		this.emailService = emailService;
	}

	public void setEmailMessages(EmailMessageQueueTo emailMessages)
	{
		this.emailMessages=emailMessages;
	}
}
