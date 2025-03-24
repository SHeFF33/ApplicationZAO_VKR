package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.zaomurom.applicationzao.models.client.Price;
import ru.zaomurom.applicationzao.repositories.PriceRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PriceService {
    @Autowired
    private PriceRepository priceRepository;

    public Price save(Price price) {
        return priceRepository.save(price);
    }

    public List<Price> findAll() {
        return priceRepository.findAll();
    }

    public Optional<Price> findById(Long id) {
        return priceRepository.findById(id);
    }
}
