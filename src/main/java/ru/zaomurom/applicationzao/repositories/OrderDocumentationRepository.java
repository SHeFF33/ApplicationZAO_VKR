package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.order.OrderDocumentation;

public interface OrderDocumentationRepository extends JpaRepository<OrderDocumentation, Long> {
}