package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.client.Price;

public interface PriceRepository extends JpaRepository<Price, Long> {
}