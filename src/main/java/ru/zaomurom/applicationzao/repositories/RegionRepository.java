package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.prices.Region;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {
    boolean existsByName(String name);
    Optional<Region> findByName(String name);
}