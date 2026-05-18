package kz.testmanagement.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public void sendTestFinished(String email, String testTitle, String result) {
        if (email == null || email.isBlank()) {
            return;
        }
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.info("Mail sender is not configured. Notification skipped for {}", email);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Test result: " + testTitle);
        message.setText(result);
        sender.send(message);
    }
}
