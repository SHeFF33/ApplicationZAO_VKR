package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.client.Addresses;

import java.util.List;

public interface AddressesRepository extends JpaRepository<Addresses, Long> {
    List<Addresses> findByClientId(Long clientId);
}
