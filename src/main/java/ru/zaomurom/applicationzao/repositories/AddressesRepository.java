package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.zaomurom.applicationzao.models.client.Addresses;

import java.util.List;

public interface AddressesRepository extends JpaRepository<Addresses, Long> {
    List<Addresses> findByClientId(Long clientId);

    @Modifying
    @Query("UPDATE Addresses a SET a.clientsRegion = NULL WHERE a.client.id = :clientId")
    void clearClientsRegionReferences(@Param("clientId") Long clientId);
}
