package com.yo.apihotelbooking.schemas.domain;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import jakarta.persistence.*;
import com.yo.apihotelbooking.schemas.AuditableEntity;
import org.springframework.security.core.userdetails.UserDetails;
import com.yo.apihotelbooking.schemas.enums.UserRole;
import lombok.Data;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User extends AuditableEntity implements UserDetails {
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    @Column(nullable = false, unique = true, length = 100)
private String username;
    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.CUSTOMER;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

@Override
   public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email; 
    }
    public String getRealUser() {
        return username; 
    }
@Override
    public String getPassword() {
        return password;
    }
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return isActive; }


}
