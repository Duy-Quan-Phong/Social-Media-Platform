package com.codegym.socialmedia.service.user;

import com.codegym.socialmedia.general_interface.UserPrincipalInfo;
import com.codegym.socialmedia.model.account.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;
public class CustomOAuth2User extends DefaultOAuth2User implements UserPrincipalInfo {

    private final String avatarUrl;
    private final String fullName;
    private final String username;
    private final String email;
    private final long id;
    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes,
                            String nameAttributeKey,
                            String avatarUrl,
                            String fullName,
                            String username,
                            String email,long id) {
        super(authorities, attributes, nameAttributeKey);
        this.avatarUrl = avatarUrl;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getName() {
        return this.username;
    }

    public String getEmail() {
        return email;
    }
}