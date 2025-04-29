package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.zaomurom.applicationzao.models.order.OrderDocumentation;
import ru.zaomurom.applicationzao.models.product.Documentation;
import ru.zaomurom.applicationzao.repositories.DocumentationRepository;

import java.util.Optional;

@Service
public class DocumentationService {
    @Autowired
    private  DocumentationRepository documentationRepository;

    public Documentation save(Documentation documentation) {
        return documentationRepository.save(documentation);
    }

    public Optional<Documentation> findById(Long id) {
        return documentationRepository.findById(id);
    }
    public void deleteById(Long id) {
        documentationRepository.deleteById(id);
    }

    public void deleteByProductId(Long productId) {
        documentationRepository.deleteByProductId(productId);
    }

}
