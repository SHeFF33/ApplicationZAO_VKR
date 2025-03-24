package ru.zaomurom.applicationzao.restcontrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.zaomurom.applicationzao.models.client.Client;
import ru.zaomurom.applicationzao.models.client.User;
import ru.zaomurom.applicationzao.models.order.Order;
import ru.zaomurom.applicationzao.models.product.Product;
import ru.zaomurom.applicationzao.services.ClientService;
import ru.zaomurom.applicationzao.services.OrderService;
import ru.zaomurom.applicationzao.services.ProductService;
import ru.zaomurom.applicationzao.services.UserService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/client")
public class RestClientController {
    @Autowired
    private ClientService clientService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/profile")
    public ResponseEntity<Client> getProfile(Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        return new ResponseEntity<>(client, HttpStatus.OK);
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        List<Product> products = productService.findAll();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getOrders(Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        List<Order> orders = orderService.findByClient(client);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PostMapping("/addToCart")
    public ResponseEntity<Void> addToCart(@RequestParam Long productId, @RequestParam Long sumId, @RequestParam int quantity, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
