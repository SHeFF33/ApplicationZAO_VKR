package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.prices.PricesOnRegions;

import java.util.List;

public interface PricesOnRegionsRepository extends JpaRepository<PricesOnRegions, Long> {
    List<PricesOnRegions> findByRegionId(Long regionId);
    List<PricesOnRegions> findByThickness(Double thickness);
    List<PricesOnRegions> findByRegionIdAndThickness(Long regionId, Double thickness);
}