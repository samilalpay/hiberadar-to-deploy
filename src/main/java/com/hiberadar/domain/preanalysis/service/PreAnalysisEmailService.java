package com.hiberadar.domain.preanalysis.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PreAnalysisEmailService {

    private static final Logger log = LoggerFactory.getLogger(PreAnalysisEmailService.class);
    private final JavaMailSender mailSender;

    public PreAnalysisEmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    public void sendReport(String toEmail, String subject, String body) {
        if (mailSender == null) {
            log.info("Mail sender not configured. Skipping e-mail to {}", toEmail);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send pre-analysis e-mail to {}: {}", toEmail, ex.getMessage());
        }
    }
}
