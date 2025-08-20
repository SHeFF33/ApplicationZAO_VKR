package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.zaomurom.applicationzao.models.prices.ClientsRegion;
import ru.zaomurom.applicationzao.models.prices.Region;
import ru.zaomurom.applicationzao.repositories.ClientsRegionRepository;
import ru.zaomurom.applicationzao.repositories.PricesOnRegionsJDRepository;
import ru.zaomurom.applicationzao.repositories.PricesOnRegionsRepository;
import ru.zaomurom.applicationzao.repositories.RegionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RegionService {

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private ClientsRegionRepository clientsRegionRepository;
    
    @Autowired
    private PricesOnRegionsRepository pricesOnRegionsRepository;
    
    @Autowired
    private PricesOnRegionsJDRepository pricesOnRegionsJDRepository;

    public List<Region> findAll() {
        return regionRepository.findAll();
    }

    public Optional<Region> findById(Long id) {
        return regionRepository.findById(id);
    }

    public Optional<Region> findByName(String name) {
        return regionRepository.findByName(name);
    }

    public Region save(Region region) {
        return regionRepository.save(region);
    }

    @Transactional
    public void deleteById(Long id) {
        // Сначала удаляем все прайсы, связанные с этим регионом
        pricesOnRegionsRepository.deleteByRegionId(id);
        pricesOnRegionsJDRepository.deleteByRegionId(id);
        // Затем удаляем связи клиентов с регионом
        clientsRegionRepository.deleteByRegionId(id);
        // Наконец удаляем сам регион
        regionRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return regionRepository.existsByName(name);
    }

    public List<ClientsRegion> findRegionsByClientId(Long clientId) {
        return clientsRegionRepository.findByClientId(clientId);
    }

}