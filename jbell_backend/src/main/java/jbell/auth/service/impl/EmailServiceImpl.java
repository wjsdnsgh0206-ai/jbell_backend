package jbell.auth.service.impl;

import jbell.auth.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 6자리 난수 생성
    @Override
    public String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    // 실제 메일 발송 로직
    @Override
    public void sendVerificationMail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom("wjsdnsgh0206@gmail.com"); // 발신자 설정 (생략 가능하나 권장)
        message.setSubject("[Jbell] 회원가입 인증번호입니다.");
        message.setText("안녕하세요. Jbell 서비스입니다.\n\n" +
                "인증번호는 [" + code + "] 입니다.\n" +
                "3분 이내에 가입 페이지에 입력해 주세요.");
        
        mailSender.send(message);
    }
}