package com.vignesh.springsecuritypg.security;

import com.vignesh.springsecuritypg.model.Authorities;
import com.vignesh.springsecuritypg.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MyUserDetailsService implements UserDetailsService {

/*    @Autowired
    private PasswordEncoder passwordEncoder;*/

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private List<User> users = new ArrayList<>() {{
        add(new User("user", passwordEncoder().encode("password"), Collections.singletonList(Authorities.USER)));
    }};

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return users.stream().filter(u -> u.getUsername().equals(s)).findFirst().orElseThrow(() -> new UsernameNotFoundException(s));
    }
}
