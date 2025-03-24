package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.order.Order;
import ru.zaomurom.applicationzao.models.client.Client;

import java.util.Date;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByClient(Client client);
    List<Order> findByClientAndStatus(Client client, String status);
    List<Order> findAll(Specification<Order> spec);
}
