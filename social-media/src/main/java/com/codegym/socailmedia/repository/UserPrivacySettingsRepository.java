package com.codegym.socailmedia.repository;

import com.codegym.socailmedia.model.account.UserPrivacySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPrivacySettingsRepository extends JpaRepository<UserPrivacySettings, Long> {

    UserPrivacySettings findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}