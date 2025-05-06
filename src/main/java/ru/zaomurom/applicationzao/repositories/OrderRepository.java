package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.zaomurom.applicationzao.models.order.Order;
import ru.zaomurom.applicationzao.models.client.Client;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = {"trucks"})
    Optional<Order> findWithTrucksById(Long id);

    @EntityGraph(attributePaths = {"tchOrders", "tchOrders.product"})
    Optional<Order> findWithTchOrdersById(Long id);
    List<Order> findByClientId(Long clientId);

    List<Order> findByClient(Client client);
    List<Order> findByClientAndStatus(Client client, String status);
    List<Order> findAll(Specification<Order> spec);
}
