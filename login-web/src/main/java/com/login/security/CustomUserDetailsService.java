package com.login.security;

import com.login.entities.User;
import com.login.exceptions.UserDoesNotExistException;
import com.login.repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    UserRepo userRepo;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading Username From DB");
        User user = userRepo.findByEmail(username);
        List<SimpleGrantedAuthority> authorities =new ArrayList<>();
        if (user==null){
            throw new UsernameNotFoundException("User Does Not Exist");
        }
        String password = Optional.ofNullable(user.getPassword()).orElse("");
        return new org.springframework.security.core.userdetails.User(user.getEmail(),password,authorities);
    }
}
