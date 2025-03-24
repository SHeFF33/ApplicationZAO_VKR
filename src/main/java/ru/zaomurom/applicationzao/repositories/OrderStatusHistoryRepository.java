package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.zaomurom.applicationzao.models.order.Order;
import ru.zaomurom.applicationzao.models.order.OrderStatusHistory;

import java.util.Date;
import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> , JpaSpecificationExecutor<OrderStatusHistory> {
    List<OrderStatusHistory> findByOrder(Order order);
    List<OrderStatusHistory> findAll(Specification<OrderStatusHistory> spec);
}
