package com.movie.config;

import com.movie.service.UserAuthService;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class MyAuthorityProvider implements AuthenticationProvider {
    @Autowired
    UserAuthService userAuthService;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username=authentication.getName();
        String userPassword=authentication.getCredentials().toString();
        UserDetails myObjUser=userAuthService.loadUserByUsername(username);
        if(myObjUser!=null && passwordEncoder.matches(userPassword, myObjUser.getPassword())){
            return new UsernamePasswordAuthenticationToken(
                    myObjUser,                     // principal (full user object)
                    null,
                    myObjUser.getAuthorities()    // roles/permissions
            );
        }
        throw  new BadCredentialsException("Invalid Credential");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        if(UsernamePasswordAuthenticationToken.class.equals(authentication)){
            return true;
        }
        return false;
    }
}
