package ru.zaomurom.applicationzao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.zaomurom.applicationzao.models.client.Client;
import ru.zaomurom.applicationzao.models.product.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByClient(Client client);
}
