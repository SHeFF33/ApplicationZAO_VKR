package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.zaomurom.applicationzao.models.product.Product;
import ru.zaomurom.applicationzao.models.product.Sum;
import ru.zaomurom.applicationzao.models.product.NomenclatureType;
import ru.zaomurom.applicationzao.models.prices.PricesOnRegions;
import ru.zaomurom.applicationzao.models.prices.PricesOnRegionsJD;
import ru.zaomurom.applicationzao.repositories.PricesOnRegionsRepository;
import ru.zaomurom.applicationzao.repositories.PricesOnRegionsJDRepository;
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
    private final PricesOnRegionsJDRepository pricesOnRegionsJDRepository;

    @Autowired
    public SumService(SumRepository sumRepository, PricesOnRegionsRepository pricesOnRegionsRepository, 
                     PricesOnRegionsJDRepository pricesOnRegionsJDRepository) {
        this.sumRepository = sumRepository;
        this.pricesOnRegionsRepository = pricesOnRegionsRepository;
        this.pricesOnRegionsJDRepository = pricesOnRegionsJDRepository;
    }

    public Sum save(Sum sum) {
        return sumRepository.save(sum);
    }

    public List<Sum> calculateAutomaticPrices(Product product, Date period) {
        List<Sum> calculatedSums = new ArrayList<>();
        
        // Сначала удаляем все существующие цены для этого продукта
        sumRepository.deleteByProductId(product.getId());
        
        // Определяем тип номенклатуры и выбираем соответствующие прайсы
        boolean isRailway = NomenclatureType.RAILWAY.equals(product.getNomenclatureType());
        
        if (isRailway) {
            // Используем ЖД прайсы
            List<PricesOnRegionsJD> prices = pricesOnRegionsJDRepository.findByThickness(product.getTolsh());
            
            if (prices.isEmpty()) {
                throw new RuntimeException("Не найдены ЖД цены для толщины " + product.getTolsh() + " мм");
            }

            // Для каждого региона рассчитываем цену
            for (PricesOnRegionsJD priceOnRegion : prices) {
                String regionName = priceOnRegion.getRegion().getName();
                
                // Создаем новую цену
                Sum sum = new Sum();
                sum.setProduct(product);
                sum.setRegionName(regionName);
                sum.setPeriod(period);
                double finalPrice = priceOnRegion.getPricePerSquareMeter() * product.getVolume();
                sum.setSumma(Math.round(finalPrice * 100.0) / 100.0);
                sum.setPricesOnRegionsJD(priceOnRegion);
                
                // Сохраняем цену
                calculatedSums.add(sumRepository.save(sum));
            }
        } else {
            // Используем Авто прайсы
            List<PricesOnRegions> prices = pricesOnRegionsRepository.findByThickness(product.getTolsh());
            
            if (prices.isEmpty()) {
                throw new RuntimeException("Не найдены Авто цены для толщины " + product.getTolsh() + " мм");
            }

            // Для каждого региона рассчитываем цену
            for (PricesOnRegions priceOnRegion : prices) {
                String regionName = priceOnRegion.getRegion().getName();
                
                // Создаем новую цену
                Sum sum = new Sum();
                sum.setProduct(product);
                sum.setRegionName(regionName);
                sum.setPeriod(period);
                double finalPrice = priceOnRegion.getPricePerSquareMeter() * product.getVolume();
                sum.setSumma(Math.round(finalPrice * 100.0) / 100.0);
                sum.setPricesOnRegions(priceOnRegion);
                
                // Сохраняем цену
                calculatedSums.add(sumRepository.save(sum));
            }
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
