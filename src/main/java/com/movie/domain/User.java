package com.movie.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.enums.Role;
import com.movie.resource.UserResource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @JsonIgnore //NEVER expose password
    private String password;

    @Column(nullable = false)
    private String mobile;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Ticket> tickets; //

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Document> documents;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    public static User toEntity(UserResource resource){
        return User.builder()
                .id(resource.getUserId())
                .name(resource.getName())
                .password(resource.getPassword())
                .mobile(resource.getMobile())
                .email(resource.getEmail())
                .role(resource.getRole() == null ? Role.USER : resource.getRole())
                .build();
    }

    public static UserResource toResource(User user){
        return UserResource.builder()
                .userId(user.getId())
                .name(user.getName())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }
}