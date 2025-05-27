package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.zaomurom.applicationzao.models.prices.PricesOnRegions;
import ru.zaomurom.applicationzao.repositories.PricesOnRegionsRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PricesOnRegionsService {

    @Autowired
    private PricesOnRegionsRepository pricesOnRegionsRepository;

    public List<PricesOnRegions> findAll() {
        return pricesOnRegionsRepository.findAll();
    }

    public Optional<PricesOnRegions> findById(Long id) {
        return pricesOnRegionsRepository.findById(id);
    }

    public PricesOnRegions save(PricesOnRegions pricesOnRegions) {
        return pricesOnRegionsRepository.save(pricesOnRegions);
    }

    public void deleteById(Long id) {
        pricesOnRegionsRepository.deleteById(id);
    }

    public List<PricesOnRegions> findByFilters(Long regionId, Double thickness) {
        if (regionId != null && thickness != null) {
            return pricesOnRegionsRepository.findByRegionIdAndThickness(regionId, thickness);
        } else if (regionId != null) {
            return pricesOnRegionsRepository.findByRegionId(regionId);
        } else if (thickness != null) {
            return pricesOnRegionsRepository.findByThickness(thickness);
        } else {
            return pricesOnRegionsRepository.findAll();
        }
    }
}