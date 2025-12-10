package com.codegym.socailmedia.service.user;

import com.codegym.socailmedia.ErrAccountException;
import com.codegym.socailmedia.model.account.User;
import com.codegym.socailmedia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = null;

        // 1. Thử tìm lần 1
        if (username.contains("@")) {
            // Nếu có @ thì ưu tiên tìm bằng email
            user = userRepository.findByEmail(username).orElse(null); // <-- Thêm .orElse(null)
        } else {
            // Ngược lại tìm bằng username
            user = userRepository.findByUsername(username).orElse(null); // <-- Thêm .orElse(null)
        }

        // 2. Nếu chưa thấy, thử tìm lại bằng cách còn lại (Fallback)
        if (user == null) {
            if (username.contains("@")) {
                user = userRepository.findByUsername(username).orElse(null); // <-- Thêm .orElse(null)
            } else {
                user = userRepository.findByEmail(username).orElse(null); // <-- Thêm .orElse(null)
            }
        }

        // 3. Nếu vẫn không thấy -> Báo lỗi
        if (user == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng với tên đăng nhập hoặc email: " + username);
        }

        // 4. Kiểm tra trạng thái tài khoản
        if (!user.isActive()) {
            throw new ErrAccountException("Tài khoản đã bị vô hiệu hóa ");
        }

        if (user.getAccountStatus() == User.AccountStatus.BANNED) {
            throw new ErrAccountException("Tài khoản đã bị cấm ");
        }

        if (user.getAccountStatus() == User.AccountStatus.SUSPENDED) {
            throw new ErrAccountException("Tài khoản đã bị tạm khóa ");
        }

        return new CustomUserPrincipal(user);
    }
}