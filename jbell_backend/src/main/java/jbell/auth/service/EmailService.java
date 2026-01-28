package jbell.auth.service;

public interface EmailService {
    String generateCode();
    void sendVerificationMail(String toEmail, String code);
}