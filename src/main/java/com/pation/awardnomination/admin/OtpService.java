package com.pation.awardnomination.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    public void generateAndSendOtp(User user, String purpose) {
        // Generate a 6-digit random code
        int code = 100000 + secureRandom.nextInt(900000);
        String otpCode = String.valueOf(code);

        // Set OTP and expiration (e.g., 5 minutes)
        user.setOtpCode(otpCode);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        // Send Email
        emailService.sendOtpEmail(user.getEmail(), otpCode, purpose);
    }

    public boolean verifyOtp(Long userId, String otpCode) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getOtpCode() != null && user.getOtpCode().equals(otpCode)) {
                if (user.getOtpExpiry() != null && LocalDateTime.now().isBefore(user.getOtpExpiry())) {
                    // Valid OTP
                    user.setOtpCode(null);
                    user.setOtpExpiry(null);
                    userRepository.save(user);
                    return true;
                }
            }
        }
        return false;
    }
}
