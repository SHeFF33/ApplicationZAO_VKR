package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.client.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
}