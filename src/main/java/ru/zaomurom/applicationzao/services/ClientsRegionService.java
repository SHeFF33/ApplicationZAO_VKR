package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.zaomurom.applicationzao.models.client.Client;
import ru.zaomurom.applicationzao.models.prices.ClientsRegion;
import ru.zaomurom.applicationzao.models.prices.Region;
import ru.zaomurom.applicationzao.repositories.ClientsRegionRepository;
import ru.zaomurom.applicationzao.repositories.AddressesRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientsRegionService {

    @Autowired
    private ClientsRegionRepository clientsRegionRepository;

    @Autowired
    private AddressesRepository addressesRepository;

    public List<ClientsRegion> findAll() {
        return clientsRegionRepository.findAll();
    }

    public Optional<ClientsRegion> findById(Long id) {
        return clientsRegionRepository.findById(id);
    }

    public ClientsRegion save(ClientsRegion clientsRegion) {
        return clientsRegionRepository.save(clientsRegion);
    }

    public void deleteById(Long id) {
        clientsRegionRepository.deleteById(id);
    }

    public List<Region> findRegionsByClient(Client client) {
        return clientsRegionRepository.findByClient(client)
                .stream()
                .map(ClientsRegion::getRegion)
                .collect(Collectors.toList());
    }

    public List<ClientsRegion> findByClient(Client client) {
        return clientsRegionRepository.findByClient(client);
    }

    @Transactional
    public void deleteByClient(Client client) {
        // First, clear the clients_region_id references from addresses
        addressesRepository.clearClientsRegionReferences(client.getId());
        // Then delete the client regions
        clientsRegionRepository.deleteByClient(client);
    }

    public ClientsRegion findByClientAndRegionId(Client client, Long regionId) {
        return clientsRegionRepository.findByClientAndRegion_Id(client, regionId);
    }
}