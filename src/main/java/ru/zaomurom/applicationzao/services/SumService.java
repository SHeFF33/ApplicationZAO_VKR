package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.zaomurom.applicationzao.models.product.Sum;
import ru.zaomurom.applicationzao.repositories.SumRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SumService {
    @Autowired
    private SumRepository sumRepository;

    public Sum save(Sum sum) {
        return sumRepository.save(sum);
    }
    public void saveAll(List<Sum> sums) {
        sumRepository.saveAll(sums);
    }

    public void deleteById(Long id) {
        sumRepository.deleteById(id);
    }

    public void deleteByProductId(Long productId) {
        sumRepository.deleteByProductId(productId);
    }

    public Optional<Sum> findById(Long aLong) {
        return sumRepository.findById(aLong);
    }
}
