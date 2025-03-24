package ru.zaomurom.applicationzao.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.zaomurom.applicationzao.models.DocumentZAO;
import ru.zaomurom.applicationzao.models.client.*;
import ru.zaomurom.applicationzao.models.order.Order;
import ru.zaomurom.applicationzao.models.order.OrderDocumentation;
import ru.zaomurom.applicationzao.models.order.TCHOrder;
import ru.zaomurom.applicationzao.models.product.*;
import ru.zaomurom.applicationzao.services.*;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressesService addressesService;

    @Autowired
    private ContactsService contactsService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private DocumentationService documentationService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDocumentationService orderDocumentationService;

    @Autowired
    private DocumentTypeService documentTypeService;

    @Autowired
    private DocumentZAOService documentZAOService;

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();

        model.addAttribute("client", client);
        model.addAttribute("addresses", client.getAddresses());
        model.addAttribute("contacts", client.getContacts());

        return "profile";
    }

    @PostMapping("/profile/addAddress")
    public String addAddress(
            @RequestParam int postalcode,
            @RequestParam String country,
            @RequestParam String region,
            @RequestParam String rayon,
            @RequestParam String city,
            @RequestParam String street,
            @RequestParam String home,
            @RequestParam String roomnumber,
            Principal principal
    ) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();

        Addresses address = new Addresses();
        address.setPostalcode(postalcode);
        address.setCountry(country);
        address.setRegion(region);
        address.setRayon(rayon);
        address.setCity(city);
        address.setStreet(street);
        address.setHome(home);
        address.setRoomnumber(roomnumber);
        address.setClient(client);

        addressesService.save(address);

        return "redirect:/profile";
    }

    @PostMapping("/profile/editAddress")
    @ResponseBody
    public ResponseEntity<String> editAddress(@RequestBody List<Map<String, String>> addresses, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();

        for (Map<String, String> addressData : addresses) {
            try {
                Long id = Long.parseLong(addressData.get("id"));
                Addresses address = addressesService.findById(id).orElseThrow(() -> new RuntimeException("Адрес не найден"));

                // Обновляем все поля, если они присутствуют в запросе
                if (addressData.containsKey("postalcode")) {
                    address.setPostalcode(Integer.parseInt(addressData.get("postalcode")));
                }
                if (addressData.containsKey("country")) {
                    address.setCountry(addressData.get("country"));
                }
                if (addressData.containsKey("region")) {
                    address.setRegion(addressData.get("region"));
                }
                if (addressData.containsKey("rayon")) {
                    address.setRayon(addressData.get("rayon"));
                }
                if (addressData.containsKey("city")) {
                    address.setCity(addressData.get("city"));
                }
                if (addressData.containsKey("street")) {
                    address.setStreet(addressData.get("street"));
                }
                if (addressData.containsKey("home")) {
                    address.setHome(addressData.get("home"));
                }
                if (addressData.containsKey("roomnumber")) {
                    address.setRoomnumber(addressData.get("roomnumber"));
                }

                // Обновление контакта
                if (addressData.containsKey("contactId")) {
                    String contactIdStr = addressData.get("contactId");
                    if (contactIdStr != null && !contactIdStr.isEmpty()) {
                        Long contactId = Long.parseLong(contactIdStr);
                        Contacts contact = contactsService.findById(contactId).orElseThrow(() -> new RuntimeException("Контакт не найден"));
                        address.setContact(contact);
                    } else {
                        address.setContact(null);
                    }
                }

                addressesService.save(address);
            } catch (Exception e) {
                System.err.println("Ошибка при обработке адреса: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка: " + e.getMessage());
            }
        }

        return ResponseEntity.ok("Адреса обновлены");
    }

    @PostMapping("/profile/editContact")
    @ResponseBody
    public ResponseEntity<String> editContact(@RequestBody List<Map<String, String>> contacts, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();

        for (Map<String, String> contactData : contacts) {
            try {
                Long id = Long.parseLong(contactData.get("id"));
                Contacts contact = contactsService.findById(id).orElseThrow(() -> new RuntimeException("Контакт не найден"));

                // Обновляем все поля, если они присутствуют в запросе
                if (contactData.containsKey("contactType")) {
                    contact.setContactType(contactData.get("contactType"));
                }
                if (contactData.containsKey("name")) {
                    contact.setName(contactData.get("name"));
                }
                if (contactData.containsKey("phonenumber")) {
                    contact.setPhonenumber(contactData.get("phonenumber"));
                }
                if (contactData.containsKey("email")) {
                    contact.setEmail(contactData.get("email"));
                }

                contactsService.save(contact);
            } catch (Exception e) {
                System.err.println("Ошибка при обработке контакта: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка: " + e.getMessage());
            }
        }

        return ResponseEntity.ok("Контакты обновлены");
    }

    @PostMapping("/profile/addContact")
    public String addContact(
            @RequestParam String contactType,
            @RequestParam String name,
            @RequestParam String phonenumber,
            @RequestParam String email,
            Principal principal
    ) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();

        Contacts contact = new Contacts();
        contact.setContactType(contactType);
        contact.setName(name);
        contact.setPhonenumber(phonenumber);
        contact.setEmail(email);
        contact.setClient(client);

        contactsService.save(contact);

        return "redirect:/profile";
    }

    @GetMapping("/products")
    public String products(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();

        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        model.addAttribute("username", username);
        Long selectedPriceId = client.getSelectedPrice() != null ? client.getSelectedPrice().getId() : null;
        model.addAttribute("selectedPriceId", selectedPriceId);

        return "products";
    }

    @GetMapping("/product/{id}")
    public String productDetails(@PathVariable Long id, Model model, Principal principal) {
        Product product = productService.findById(id).orElse(null);
        if (product != null) {
            model.addAttribute("product", product);
            String username = principal.getName();
            model.addAttribute("username", username);
            User user = userService.findByUsername(username);
            Client client = user.getClient();
            Long selectedPriceId = client.getSelectedPrice() != null ? client.getSelectedPrice().getId() : null;
            model.addAttribute("selectedPriceId", selectedPriceId);
            return "productDetails";
        } else {
            return "error";
        }
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam(required = false) String query, Model model, Principal principal) {
        List<Product> products;
        if (query != null && !query.isEmpty()) {
            products = productService.findAll().stream()
                    .filter(product -> product.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            products = productService.findAll();
        }

        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Long selectedPriceId = client.getSelectedPrice() != null ? client.getSelectedPrice().getId() : null;

        model.addAttribute("products", products);
        model.addAttribute("selectedPriceId", selectedPriceId);

        return "product-grid-fragment :: productGrid";
    }

    @GetMapping("/product/image/{id}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
        ProductImage image = productImageService.findById(id).orElse(null);
        if (image != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(image.getBytes(), headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/products/{productId}/document/{docId}")
    public ResponseEntity<byte[]> getProductDocument(@PathVariable Long docId) {
        Optional<Documentation> docOptional = documentationService.findById(docId);
        if (docOptional.isPresent()) {
            Documentation doc = docOptional.get();

            String mimeType = determineMimeType(doc.getName());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mimeType));
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(doc.getName(), StandardCharsets.UTF_8)
                    .build());

            return new ResponseEntity<>(doc.getBytes(), headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/product/addToCart")
    public String ProductaddToCart(@RequestParam Long productId, @RequestParam Long sumId, @RequestParam String username, @RequestParam int quantity) {
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Cart cart = cartService.findByClient(client);
        if (cart == null) {
            cart = new Cart();
            cart.setClient(client);
        }
        Product product = productService.findById(productId).orElse(null);
        Sum sum = productService.findSumById(sumId).orElse(null);
        if (product != null && sum != null) {
            cart.addProduct(product, quantity, sum);
            cartService.save(cart);
        }
        return "redirect:/products";
    }

    @GetMapping("/orders")
    public String orders(Model model, Principal principal, @RequestParam(required = false) String status) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            model.addAttribute("error", "Пользователь не найден.");
            return "error";
        }
        Client client = user.getClient();
        List<Order> orders = orderService.findByClientAndStatus(client, status);
        model.addAttribute("orders", orders);
        model.addAttribute("username", username);
        model.addAttribute("status", status);

        return "orders";
    }

    @GetMapping("/orderDetails")
    public String orderDetails(Model model, @RequestParam Long id) {
        Order order = orderService.findById(id).orElse(null);
        if (order != null) {
            double totalSum = order.getTchOrders().stream()
                    .mapToDouble(tchOrder -> tchOrder.getQuantity() * tchOrder.getPrice())
                    .sum();

            model.addAttribute("order", order);
            model.addAttribute("totalSum", totalSum);
            return "orderDetails";
        } else {
            return "error";
        }
    }

    @GetMapping("/orders/{orderId}/document/{docId}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long docId) {
        Optional<OrderDocumentation> docOptional = orderDocumentationService.findById(docId);

        if (docOptional.isPresent()) {
            OrderDocumentation doc = docOptional.get();

            String mimeType = determineMimeType(doc.getName());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mimeType));
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(doc.getName(), StandardCharsets.UTF_8)
                    .build());

            return new ResponseEntity<>(doc.getBytes(), headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/documents")
    public String documents(Model model) {
        List<DocumentZAO> documents = documentZAOService.findAll();
        model.addAttribute("documents", documents);
        return "documents";
    }
    @GetMapping("/documents/download/{docId}")
    public ResponseEntity<byte[]> downloadDocumentZAO(@PathVariable Long docId) {
        Optional<DocumentZAO> docOptional = documentZAOService.findById(docId);

        if (docOptional.isPresent()) {
            DocumentZAO doc = docOptional.get();

            String fileName = doc.getDocumentName();
            if (!fileName.contains(".")) {
                String mimeType = determineMimeType(fileName);
                String extension = determineFileExtension(mimeType);
                fileName += extension;
            }

            String mimeType = determineMimeType(fileName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mimeType));
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(fileName, StandardCharsets.UTF_8)
                    .build());

            return new ResponseEntity<>(doc.getDocumentBytes(), headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private String determineMimeType(String fileName) {
        if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".doc")) {
            return "application/msword";
        } else if (fileName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (fileName.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        } else if (fileName.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else {
            return "application/octet-stream";
        }
    }

    private String determineFileExtension(String mimeType) {
        switch (mimeType) {
            case "application/pdf":
                return ".pdf";
            case "application/msword":
                return ".doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return ".docx";
            case "application/vnd.ms-excel":
                return ".xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return ".xlsx";
            default:
                return ".txt";
        }
    }

    @PostMapping("/addToCart")
    public String addToCart(@RequestParam Long productId, @RequestParam Long sumId, @RequestParam String username, @RequestParam int quantity) {
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Cart cart = cartService.findByClient(client);
        if (cart == null) {
            cart = new Cart();
            cart.setClient(client);
        }
        Product product = productService.findById(productId).orElse(null);
        Sum sum = productService.findSumById(sumId).orElse(null);
        if (product != null && sum != null) {
            cart.addProduct(product, quantity, sum);
            product.setQuantity(product.getQuantity() - quantity);
            productService.save(product);
            cartService.save(cart);
        }
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String cart(Model model, Principal principal) {
        try {
            String username = principal.getName();
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }
            Client client = user.getClient();
            Cart cart = cartService.findByClient(client);
            if (cart == null) {
                cart = new Cart();
                cart.setClient(client);
            }
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            String minDateTime = now.format(formatter);
            model.addAttribute("minDateTime", minDateTime);
            model.addAttribute("cart", cart);
            model.addAttribute("username", username);
            model.addAttribute("client", client);

            return "cart";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @PostMapping("/cart/checkout")
    public String checkout(@RequestParam Long addressId, @RequestParam String deliveryDateTime, Principal principal, Model model) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Cart cart = cartService.findByClient(client);

        if (cart == null || cart.getCartItems().isEmpty()) {
            model.addAttribute("error", "Ваша корзина пуста.");
            return "cart";
        }

        List<Addresses> addresses = addressesService.findByClientId(client.getId());
        if (addresses == null || addresses.isEmpty()) {
            model.addAttribute("error", "Нет адресов доставки. Пожалуйста, добавьте адрес перед оформлением заказа.");
            return "cart";
        }

        Addresses selectedAddress = addresses.stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElse(null);

        if (selectedAddress == null) {
            model.addAttribute("error", "Выбранный адрес не найден.");
            return "cart";
        }

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Date parsedDeliveryDateTime;
        try {
            parsedDeliveryDateTime = dateTimeFormat.parse(deliveryDateTime);
        } catch (ParseException e) {
            model.addAttribute("error", "Неверный формат даты и времени. Используйте формат yyyy-MM-dd'T'HH:mm.");
            return "cart";
        }

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        String formattedDeliveryDateTime = formatter.format(parsedDeliveryDateTime);

        double totalSum = cart.getCartItems().stream()
                .mapToDouble(cartItem -> cartItem.getQuantity() * cartItem.getSum().getSumma())
                .sum();

        model.addAttribute("addressId", addressId);
        model.addAttribute("deliveryDate", formattedDeliveryDateTime);
        model.addAttribute("cart", cart);
        model.addAttribute("client", client);
        model.addAttribute("selectedAddress", selectedAddress);
        model.addAttribute("totalSum", totalSum);

        return "confirmOrder";
    }

    @PostMapping("/cart/confirmOrder")
    public String confirmOrder(
            @RequestParam Long addressId,
            @RequestParam String deliveryDate,
            Principal principal,
            Model model
    ) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Cart cart = cartService.findByClient(client);

        if (cart == null || cart.getCartItems().isEmpty()) {
            model.addAttribute("error", "Ваша корзина пуста.");
            return "cart";
        }

        List<Addresses> addresses = addressesService.findByClientId(client.getId());
        if (addresses == null || addresses.isEmpty()) {
            model.addAttribute("error", "Нет адресов доставки. Пожалуйста, добавьте адрес перед оформлением заказа.");
            return "cart";
        }

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        Date parsedDeliveryDate;
        try {
            parsedDeliveryDate = dateTimeFormat.parse(deliveryDate);
        } catch (ParseException e) {
            model.addAttribute("error", "Неверный формат даты и времени. Используйте формат HH:mm:ss dd.MM.yyyy.");
            return "cart";
        }

        Order order = new Order();
        order.setClient(client);
        order.setDeliveryAddress(addresses.stream().filter(a -> a.getId().equals(addressId)).findFirst().orElse(null));
        order.setDeliveryDate(parsedDeliveryDate);
        order.setOrderDate(new Date());
        order.setStatus("В обработке");

        List<TCHOrder> tchOrders = cart.getCartItems().stream().map(cartItem -> {
            TCHOrder tchOrder = new TCHOrder();
            tchOrder.setOrder(order);
            tchOrder.setProduct(cartItem.getProduct());
            tchOrder.setQuantity(cartItem.getQuantity());
            tchOrder.setPrice(cartItem.getSum().getSumma());
            return tchOrder;
        }).toList();

        order.setTchOrders(tchOrders);
        orderService.save(order);

        orderService.addStatusHistory(order, "В обработке", user);

        cart.getCartItems().clear();
        cartService.save(cart);

        return "redirect:/products";
    }

    public Double getProductPrice(Product product, Long selectedPriceId) {
        if (selectedPriceId != null) {
            return product.getSums().stream()
                    .filter(sum -> sum.getPrice().getId().equals(selectedPriceId))
                    .findFirst()
                    .map(Sum::getSumma)
                    .orElse(null);
        }
        return null;
    }

    public Long getProductPriceId(Product product, Long selectedPriceId) {
        if (selectedPriceId != null) {
            return product.getSums().stream()
                    .filter(sum -> sum.getPrice().getId().equals(selectedPriceId))
                    .findFirst()
                    .map(Sum::getId)
                    .orElse(null);
        }
        return null;
    }
    @PostMapping("/cart/increaseQuantity/{itemId}")
    public ResponseEntity<Map<String, Integer>> increaseQuantity(@PathVariable Long itemId, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Cart cart = cartService.findByClient(client);

        if (cart != null) {
            CartItem cartItem = cart.getCartItems().stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);

            if (cartItem != null) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                cartService.saveCartItem(cartItem);
                Map<String, Integer> response = new HashMap<>();
                response.put("quantity", cartItem.getQuantity());
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/cart/decreaseQuantity/{itemId}")
    public ResponseEntity<Map<String, Integer>> decreaseQuantity(@PathVariable Long itemId, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Cart cart = cartService.findByClient(client);

        if (cart != null) {
            CartItem cartItem = cart.getCartItems().stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);

            if (cartItem != null && cartItem.getQuantity() > 1) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
                cartService.saveCartItem(cartItem);
                Map<String, Integer> response = new HashMap<>();
                response.put("quantity", cartItem.getQuantity());
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @DeleteMapping("/cart/deleteItem/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Cart cart = cartService.findByClient(client);

        if (cart != null) {
            CartItem cartItem = cart.getCartItems().stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);

            if (cartItem != null) {
                System.out.println("Deleting cart item: " + cartItem.getId());
                cartService.deleteCartItem(cartItem);
                cart.getCartItems().remove(cartItem);
                cartService.save(cart);
                System.out.println("Cart item deleted successfully.");
                return ResponseEntity.ok().build();
            } else {
                System.out.println("Cart item not found.");
            }
        } else {
            System.out.println("Cart not found.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

}

