package com.movie.resource;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResource {
    private String email;
    private String password;
}
