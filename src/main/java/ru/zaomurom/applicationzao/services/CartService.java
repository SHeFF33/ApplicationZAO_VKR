package ru.zaomurom.applicationzao.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.zaomurom.applicationzao.models.client.Client;
import ru.zaomurom.applicationzao.models.product.Cart;
import ru.zaomurom.applicationzao.models.product.CartItem;
import ru.zaomurom.applicationzao.repositories.CartItemRepository;
import ru.zaomurom.applicationzao.repositories.CartRepository;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    public Cart findByClient(Client client) {
        return cartRepository.findByClient(client);
    }

    public void save(Cart cart) {
        cartRepository.save(cart);
    }

    public void saveCartItem(CartItem cartItem) {
        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void deleteCartItem(CartItem cartItem) {
        cartItemRepository.delete(cartItem);
    }
}
