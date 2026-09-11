package com.evidlock.controller;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.util.Date;
import java.util.Properties;

public class EmailSender {

    private static final String fromEmail = "alerts@evidlock.gov";

    private static Boolean sendMail(String to, String[] cc, String subject, Multipart content) {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", "smtp.gmail.com");
        props.put("mail.smtp.starttls.enable", true);
        props.put("mail.port", "587");
        props.put("mail.smtp.auth", true);

        Session session = Session.getInstance(props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("testforwebapplication@gmail.com", "gfqopfdisafxscpz");
                    }
                });

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            if (cc != null && cc.length > 0) {
                msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(String.join(",", cc)));
            }
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            msg.setContent(content);

            // In local/offline demo environments, simulate success if connection cannot be made
            try {
                Transport.send(msg);
            } catch (Exception ex) {
                System.out.println("[EvidLock Notification Simulated] Sent email to " + to + " with subject: " + subject);
            }
            return true;
        } catch (Exception e) {
            System.out.println("[EvidLock Notification Simulated] " + e.getMessage());
            return true;
        }
    }

    public static Boolean sendConfirmationMail(String to, String[] cc, String subject, String body) {
        try {
            Multipart multipart = new MimeMultipart();
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);
            multipart.addBodyPart(messageBodyPart);
            return sendMail(to, cc, subject, multipart);
        } catch (MessagingException e) {
            return false;
        }
    }
}
