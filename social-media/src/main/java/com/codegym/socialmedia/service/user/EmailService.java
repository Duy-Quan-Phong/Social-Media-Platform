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

    public void sendNotificationEmail(String to, String senderName, String type) {
        String subject;
        String body;
        switch (type) {
            case "FRIEND_REQUEST" -> { subject = senderName + " đã gửi lời mời kết bạn"; body = senderName + " đã gửi lời mời kết bạn cho bạn. Đăng nhập để chấp nhận."; }
            case "COMMENT_POST" -> { subject = senderName + " đã bình luận bài viết của bạn"; body = senderName + " vừa bình luận bài viết của bạn."; }
            case "LIKE_POST" -> { subject = senderName + " đã thích bài viết của bạn"; body = senderName + " vừa thích bài viết của bạn."; }
            case "MENTION_COMMENT" -> { subject = senderName + " đã nhắc đến bạn"; body = senderName + " vừa nhắc đến bạn trong một bình luận."; }
            case "SHARED_POST" -> { subject = senderName + " đã chia sẻ bài viết của bạn"; body = senderName + " vừa chia sẻ bài viết của bạn."; }
            default -> { subject = "Thông báo mới từ Social Media"; body = "Bạn có thông báo mới từ " + senderName + "."; }
        }
        sendSimpleMessage(to, subject, body);
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
