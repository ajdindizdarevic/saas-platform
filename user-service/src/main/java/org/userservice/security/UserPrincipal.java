package org.userservice.security;

import lombok.Getter;
import org.userservice.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapts the {@link User} domain object to Spring Security's {@link UserDetails}.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final UUID   id;
    private final String email;
    private final String username;
    private final String password;
    private final String tenantId;
    private final String tenantSchema;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(User user) {
        this.id           = user.getId();
        this.email        = user.getEmail();
        this.username     = user.getUsername();
        this.password     = user.getPassword();
        this.tenantId     = user.getTenantId();
        this.tenantSchema = user.getTenantSchema();
        this.active       = user.isActive();
        this.authorities  = buildAuthorities(user);
    }

    public static UserPrincipal of(User user) {
        return new UserPrincipal(user);
    }

    // ── UserDetails ───────────────────────────────────────────────────────────

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword()                                     { return password; }
    @Override public String getUsername()                                     { return username; }
    @Override public boolean isAccountNonExpired()                            { return true; }
    @Override public boolean isAccountNonLocked()                             { return active; }
    @Override public boolean isCredentialsNonExpired()                        { return true; }
    @Override public boolean isEnabled()                                      { return active; }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Set<SimpleGrantedAuthority> buildAuthorities(User user) {
        return user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().name()))
                .collect(Collectors.toSet());
    }

    public Set<String> getRoleNames() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}