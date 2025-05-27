package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.zaomurom.applicationzao.models.prices.Region;
import ru.zaomurom.applicationzao.repositories.RegionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RegionService {

    @Autowired
    private RegionRepository regionRepository;

    public List<Region> findAll() {
        return regionRepository.findAll();
    }

    public Optional<Region> findById(Long id) {
        return regionRepository.findById(id);
    }

    public Region save(Region region) {
        return regionRepository.save(region);
    }

    public void deleteById(Long id) {
        regionRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return regionRepository.existsByName(name);
    }
}