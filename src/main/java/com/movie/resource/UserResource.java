package com.movie.resource;

import com.movie.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResource {
    private Long userId;
    private String name;
    private String password;
    private String mobile;
    private String email;
    private Role role;
}
