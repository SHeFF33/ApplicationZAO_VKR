package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.zaomurom.applicationzao.models.product.Product;
import ru.zaomurom.applicationzao.models.product.Sum;
import ru.zaomurom.applicationzao.models.prices.PricesOnRegions;
import ru.zaomurom.applicationzao.repositories.PricesOnRegionsRepository;
import ru.zaomurom.applicationzao.repositories.SumRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SumService {
    private final SumRepository sumRepository;
    private final PricesOnRegionsRepository pricesOnRegionsRepository;

    @Autowired
    public SumService(SumRepository sumRepository, PricesOnRegionsRepository pricesOnRegionsRepository) {
        this.sumRepository = sumRepository;
        this.pricesOnRegionsRepository = pricesOnRegionsRepository;
    }

    public Sum save(Sum sum) {
        return sumRepository.save(sum);
    }

    public List<Sum> calculateAutomaticPrices(Product product, Date period) {
        List<Sum> calculatedSums = new ArrayList<>();
        
        // Находим все цены для данной толщины
        List<PricesOnRegions> prices = pricesOnRegionsRepository.findByThickness(product.getTolsh());
        
        if (prices.isEmpty()) {
            throw new RuntimeException("Не найдены цены для толщины " + product.getTolsh() + " мм");
        }

        // Для каждого региона рассчитываем цену
        for (PricesOnRegions priceOnRegion : prices) {
            String regionName = priceOnRegion.getRegion().getName();
            
            // Ищем существующую цену для этого региона
            Optional<Sum> existingSum = sumRepository.findByProductAndRegionName(product, regionName);
            
            Sum sum;
            if (existingSum.isPresent()) {
                // Обновляем существующую цену
                sum = existingSum.get();
            } else {
                // Создаем новую цену
                sum = new Sum();
                sum.setProduct(product);
                sum.setRegionName(regionName);
            }
            
            sum.setPeriod(period);
            double finalPrice = priceOnRegion.getPricePerSquareMeter() * product.getVolume();
            sum.setSumma(Math.round(finalPrice * 100.0) / 100.0);
            sum.setPricesOnRegions(priceOnRegion);
            
            // Сохраняем цену
            calculatedSums.add(sumRepository.save(sum));
        }

        return calculatedSums;
    }

    public void deleteByProductId(Long productId) {
        sumRepository.deleteByProductId(productId);
    }

    public void deleteById(Long id) {
        sumRepository.deleteById(id);
    }

    public Optional<Sum> findById(Long id) {
        return sumRepository.findById(id);
    }
}
