package org.icatproject.topcat.utils;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@LocalBean
public class MailBean {
	private static final Logger logger = LoggerFactory.getLogger(MailBean.class);

	@Resource(name = "mail/topcat")
	private Session session;

	public void send(String email, String subject, String body) {
		System.out.println("Sending email to " + email);
		Message msg = new MimeMessage(session);
		try {
			msg.setSubject(subject);
			msg.setText(body);
			msg.setRecipients(RecipientType.TO, InternetAddress.parse(email));

			Transport.send(msg);

			logger.debug("Email sent to " + email);
		} catch (MessagingException e) {
			logger.debug(e.getMessage());
		}
	}

}
