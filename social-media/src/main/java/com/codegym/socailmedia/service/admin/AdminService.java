package com.codegym.socailmedia.service.admin;

import com.codegym.socailmedia.model.account.User;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface AdminService {
    Page<User> getAllUsers(int page, int size);
    void blockUser(Long userId);
    Map<String, Long> getVisitStatistics();
    Map<String, Long> getNewUserStatistics();
}


