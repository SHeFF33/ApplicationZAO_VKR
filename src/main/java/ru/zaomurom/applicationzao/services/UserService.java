    package ru.zaomurom.applicationzao.services;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.stereotype.Service;
    import ru.zaomurom.applicationzao.models.client.User;
    import ru.zaomurom.applicationzao.repositories.UserRepository;

    import java.util.ArrayList;
    import java.util.List;

    @Service
    public class UserService implements UserDetailsService {
        @Autowired
        private UserRepository userRepository;

        public User findByUsername(String username) {
            return userRepository.findByUsername(username);
        }

        public User save(User user) {
            return userRepository.save(user);
        }

        public List<User> findAll() {
            return userRepository.findAll();
        }

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            User user = findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }
            List<GrantedAuthority> authorities = new ArrayList<>();
            if (user.isAdmin()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
            return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
        }
    }
