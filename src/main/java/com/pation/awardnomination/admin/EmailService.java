package com.pation.awardnomination.admin;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendOtpEmail(String toEmail, String otpCode, String purpose) {
        // Since we don't have a real SMTP server configured, we mock the email by logging it.
        logger.info("\n=======================================================");
        logger.info("📩 MOCK EMAIL SERVICE - MESSAGE DISPATCHED");
        logger.info("=======================================================");
        logger.info("To: {}", toEmail);
        logger.info("Subject: Your {} Verification Code", purpose);
        logger.info("Body:");
        logger.info("Hello,");
        logger.info("Here is your one-time password (OTP) for {}:", purpose);
        logger.info("           -->  {}  <--", otpCode);
        logger.info("This code will expire in 5 minutes.");
        logger.info("=======================================================\n");
    }
}
