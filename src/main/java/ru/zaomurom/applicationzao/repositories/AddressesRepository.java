package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.zaomurom.applicationzao.models.client.Addresses;
import ru.zaomurom.applicationzao.models.client.Client;

import java.util.List;

public interface AddressesRepository extends JpaRepository<Addresses, Long> {
    List<Addresses> findByClientId(Long clientId);
    
    List<Addresses> findByClient(Client client);

    @Modifying
    @Query("UPDATE Addresses a SET a.clientsRegion = NULL WHERE a.client.id = :clientId")
    void clearClientsRegionReferences(@Param("clientId") Long clientId);


    @Query("SELECT a FROM Addresses a LEFT JOIN FETCH a.clientsRegion cr LEFT JOIN FETCH cr.region WHERE a.client.id = :clientId")
    List<Addresses> findByClientIdWithRegions(@Param("clientId") Long clientId);
}
