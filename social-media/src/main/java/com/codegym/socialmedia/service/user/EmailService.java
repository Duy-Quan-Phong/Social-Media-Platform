package com.codegym.socialmedia.service.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;



    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String subject = "Yêu cầu Đặt lại Mật khẩu";
        // Giả định ứng dụng chạy trên localhost:8080. Cần thay đổi khi triển khai thực tế.
        String resetUrl = "http://localhost:8080/reset-password?token=" + token;
        String text = "Xin chào người dùng face-book-clone\n\n"
                + "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình.\n"
                + "Vui lòng nhấp vào liên kết dưới đây để đặt lại mật khẩu:\n"
                + resetUrl + "\n\n"
                + "Liên kết này sẽ hết hạn sau 24 giờ.\n"
                + "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n"
                + "Trân trọng,\n";
        sendSimpleMessage(to, subject, text);
    }
}
