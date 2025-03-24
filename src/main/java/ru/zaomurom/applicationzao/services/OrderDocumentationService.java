package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.zaomurom.applicationzao.models.order.OrderDocumentation;
import ru.zaomurom.applicationzao.repositories.OrderDocumentationRepository;

import java.util.Optional;

@Service
public class OrderDocumentationService {
    @Autowired
    private OrderDocumentationRepository orderDocumentationRepository;

    public void deleteById(Long id) {
        orderDocumentationRepository.deleteById(id);
    }

    public OrderDocumentation save(OrderDocumentation documentation) {
        return orderDocumentationRepository.save(documentation);
    }

    public Optional<OrderDocumentation> findById(Long id) {
        return orderDocumentationRepository.findById(id);
    }
}
