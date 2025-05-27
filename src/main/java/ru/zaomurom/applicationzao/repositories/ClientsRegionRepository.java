package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.zaomurom.applicationzao.models.client.Client;
import ru.zaomurom.applicationzao.models.prices.ClientsRegion;

import java.util.List;

public interface ClientsRegionRepository extends JpaRepository<ClientsRegion, Long> {
    List<ClientsRegion> findByClient(Client client);

    @Modifying
    @Transactional
    @Query("DELETE FROM ClientsRegion cr WHERE cr.client = :client")
    void deleteByClient(@Param("client") Client client);

    ClientsRegion findByClientAndRegion_Id(Client client, Long regionId);
}