package lk.londontec.fin_mate.security;

import lk.londontec.fin_mate.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

public class AppUserPrincipal implements UserDetails {

    private final User user;

    public AppUserPrincipal(User user) {
        this.user = user;
    }

    public Long getUserId() {
        return user.getId();
    }

    @Override
    public java.util.Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // no roles yet — add if you need admin/user distinction later
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
