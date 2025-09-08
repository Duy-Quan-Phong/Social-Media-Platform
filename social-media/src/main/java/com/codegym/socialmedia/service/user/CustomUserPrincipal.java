package com.codegym.socialmedia.service.user;

import com.codegym.socialmedia.general_interface.UserPrincipalInfo;
import com.codegym.socialmedia.model.account.Role;
import com.codegym.socialmedia.model.account.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomUserPrincipal implements UserDetails, UserPrincipalInfo {
    private final User user;
    private Collection<? extends GrantedAuthority> roles;
    public CustomUserPrincipal(User user) {
        this.user = user;
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role a : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(a.getName()));
        }
        this.roles = authorities;
    }

    public long getId(){
        return user.getId();
    }

    @Override
    public String getAvatarUrl() {
        return user.getProfilePicture();
    }

    @Override
    public String getFullName() {
        return user.getFirstName() + " " + user.getLastName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getAccountStatus() == User.AccountStatus.ACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    public User getUser() {
        return user;
    }


    public String getName(){
        return user.getUsername();
    }
}
