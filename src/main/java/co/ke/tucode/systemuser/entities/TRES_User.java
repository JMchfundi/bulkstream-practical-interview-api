package co.ke.tucode.systemuser.entities;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonBackReference;
import co.ke.finsis.entity.OfficerRegistration;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class TRES_User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @NotEmpty
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @NotEmpty
    private String password;

    @Column(nullable = true)
    private String access;

    @Column(nullable = false)
    private String user_signature;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Spring Security overrides
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
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
