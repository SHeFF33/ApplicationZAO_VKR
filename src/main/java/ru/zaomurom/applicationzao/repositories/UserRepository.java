package ru.zaomurom.applicationzao.repositories;

import ru.zaomurom.applicationzao.models.client.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User findByEmail(String email);
    List<User> findByIsAdminTrueAndEmailIsNotNull();
    List<User> findByIsAdminTrue();
}