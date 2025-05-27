package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.prices.Region;

public interface RegionRepository extends JpaRepository<Region, Long> {
    boolean existsByName(String name);
}