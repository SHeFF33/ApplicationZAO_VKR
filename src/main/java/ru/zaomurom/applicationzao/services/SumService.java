package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.zaomurom.applicationzao.models.product.Sum;
import ru.zaomurom.applicationzao.repositories.SumRepository;

@Service
public class SumService {
    @Autowired
    private SumRepository sumRepository;

    public Sum save(Sum sum) {
        return sumRepository.save(sum);
    }
}
