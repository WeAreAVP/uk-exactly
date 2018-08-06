/* 
 * Exactly
 * Author: Nouman Tayyab (nouman@weareavp.com)
 * Author: Rimsha Khalid (rimsha@weareavp.com)
 * Version: 0.1.6
 * Requires: JDK 1.7 or higher
 * Description: This tool transfers digital files to the UK Exactly
 * Support: info@weareavp.com
 * License: Apache 2.0
 * Copyright: University of Kentucky (http://www.uky.edu). All Rights Reserved
 *
 */
package uk.sipperfly.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import static uk.sipperfly.ui.Exactly.GACOM;

/**
 * This class creates and sends email to the archive via SMTP over TLS.
 *
 * @author  Rimsha Khalid
 */
class MailSender {

	String host;
	String username;
	String password;
	String port;
	String protocol;
	String to;
	String from;
	String body;
	String subject;
	List<String> attachments = new ArrayList<>();
	boolean debug;
	Properties props;
	Session session;

	/**
	 * Constructor for the MailSender
	 *
	 * @param host The SMTP host to send mail to
	 * @param username Valid user name for the host
	 * @param password Valid password for the host
	 * @param debug Toggle debug output
	 * @param port Valid port for the host
	 * @param protocol Valid protocol for the host
	 */
	public MailSender(String host, String username, String password, boolean debug, String port, String protocol) {
		if (host == null || host.length() < 1 || username == null || username.length() < 1
				|| password == null || password.length() < 1
				|| port == null || port.length() < 1
				|| protocol == null || protocol.length() < 1) {
			throw new IllegalArgumentException();
		}

		this.host = host;
		this.debug = debug;
		this.username = username;
		this.password = password;
		this.port = port;
		this.protocol = protocol;
	}

	/**
	 * Sets the properties of the message.
	 * There is	only one message perinstance.
	 *
	 * @param from Valid email address of the sender
	 * @param to Valid email address of the recipient
	 * @param subject Subject for the message
	 * @param body The body of the message in a single string
	 */
	public void SetMessage(String from, String to, String subject, String body) {
		if (from == null || from.length() < 1
				|| to == null || to.length() < 1
				|| subject == null || subject.length() < 1
				|| body == null || body.length() < 1) {
			throw new IllegalArgumentException();
		}

		this.from = from;
		this.to = to;
		this.subject = subject;
		this.body = body;
	}

	/**
	 * Add the filename to a list of MIME attachments.
	 * Does not verify the file is accessible.
	 *
	 * @param filename The name of the file
	 */
	public void AttachFile(String filename) {
		if (filename == null || filename.length() < 1) {
			throw new IllegalArgumentException();
		}

		if (!attachments.contains(filename)) {
			attachments.add(filename);
		}
	}

	/**
	 * Sends the email with the given attachments.
	 *
	 * @return The result of the operation as text
	 */
	public String Send() {
		String result = "Unfinished";
//		props = System.getProperties();
//		props.put("mail.smtp.host", host);
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.starttls.enable", "true");
//		props.put("mail.smtp.port", "587");
//		props.put("mail.smtp.ssl.trust", host);
		this.smtpConfig();
		session = Session.getInstance(props, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		session.setDebug(debug);

		try {
			// create a message
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			InternetAddress[] address = {new InternetAddress(to)};
			msg.setRecipients(Message.RecipientType.TO, address);
			msg.setSubject(subject);

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(body);

			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);

			// create the second message part
			if (attachments.size() > 0) {
				// attach the file to the message
				for (String filename : attachments) {
					MimeBodyPart mbp2 = new MimeBodyPart();
					try {
						mbp2.attachFile(filename);
					} catch (IOException ex) {
						Logger.getLogger(GACOM).log(Level.SEVERE, null, ex);
					}
					mp.addBodyPart(mbp2);
				}
			}
			// add the Multipart to the message
			msg.setContent(mp);

			// set the Date: header
			msg.setSentDate(new Date());

			// send the message
			Transport.send(msg);

			result = "Mail sent successfully.";
		} catch (MessagingException mex) {
			Logger.getLogger(MailSender.class.getName()).log(Level.SEVERE, null, mex);
			mex.printStackTrace();
			Exception ex = null;
			if ((ex = mex.getNextException()) != null) {
				ex.printStackTrace();
			}
			result = mex.getMessage();
		}

		return result;
	}

	/**
	 * Validates that the user can log onto the SMTP mail server over TLS.
	 *
	 * @return True if the logon was successful, false otherwise
	 */
	public boolean Validate() {

		//		props = System.getProperties();
		this.smtpConfig();
		props.setProperty("mail.transport.protocol", "smtp");
		//		props.put("mail.smtp.auth", "true");
		//		props.put("mail.smtp.starttls.enable", "true");
		//		props.put("mail.smtp.host", "smtp.gmail.com");
		//		props.put("mail.smtp.host", host);
		//		props.put("mail.smtp.ssl.trust", host);
		//		props.put("mail.smtp.ssl.trust", host);

		try {
			session = Session.getInstance(props, new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
			session.getTransport().connect();
		} catch (javax.mail.MessagingException ex) {
			Logger.getLogger(MailSender.class.getName()).log(Level.SEVERE, null, ex);
			System.out.println(ex.toString());
			return false;
		}
		return true;

	}

	/**
	 * SMTP configuration
	 */
	private void smtpConfig() {
		props = System.getProperties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", port);
		if (protocol.equals("TLS")) {
			props.put("mail.smtp.starttls.enable", "true");
		} else if (protocol.equals("SSL")) {
			props.put("mail.smtp.socketFactory.port", "465");
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		}
//		props.put("mail.smtp.ssl.trust", host);
	}
}
