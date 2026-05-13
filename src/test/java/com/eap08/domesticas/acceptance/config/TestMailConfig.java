package com.eap08.domesticas.acceptance.config;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

@TestConfiguration
public class TestMailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSender() {
            @Override
            public MimeMessage createMimeMessage() {
                return new MimeMessage((Session) null);
            }

            @Override
            public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
                try {
                    return new MimeMessage(null, contentStream);
                } catch (Exception ex) {
                    throw new MailParseException(ex);
                }
            }

            @Override
            public void send(MimeMessage mimeMessage) throws MailException {
                // no-op
            }

            @Override
            public void send(MimeMessage... mimeMessages) throws MailException {
                // no-op
            }

            @Override
            public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
                // no-op
            }

            @Override
            public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
                // no-op
            }

            @Override
            public void send(SimpleMailMessage simpleMessage) throws MailException {
                // no-op
            }

            @Override
            public void send(SimpleMailMessage... simpleMessages) throws MailException {
                // no-op
            }
        };
    }
}
