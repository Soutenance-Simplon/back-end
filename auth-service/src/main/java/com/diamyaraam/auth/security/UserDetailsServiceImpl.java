package com.diamyaraam.auth.security;

import com.diamyaraam.auth.entity.User;
import com.diamyaraam.auth.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String telephone) throws UsernameNotFoundException {

        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Aucun compte trouvé avec ce numéro : " + telephone
                ));

        String role = user.getRole() != null
                ? "ROLE_" + user.getRole().getNomRole()
                : "ROLE_USER";

        return org.springframework.security.core.userdetails.User
                .withUsername(telephone)
                .password(user.getPassword())
                .authorities(new SimpleGrantedAuthority(role))
                .accountLocked(user.isAccountLocked())
                .disabled(!user.isActive())
                .build();
    }
}
