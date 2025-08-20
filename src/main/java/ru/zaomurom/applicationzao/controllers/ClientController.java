package ru.zaomurom.applicationzao.controllers;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.zaomurom.applicationzao.dto.CartDistributionDTO;
import ru.zaomurom.applicationzao.dto.ManualDistributionRequest;
import ru.zaomurom.applicationzao.models.DocumentZAO;
import ru.zaomurom.applicationzao.models.Quota;
import ru.zaomurom.applicationzao.models.Station;
import ru.zaomurom.applicationzao.models.client.*;
import ru.zaomurom.applicationzao.models.order.*;
import ru.zaomurom.applicationzao.models.prices.Region;
import ru.zaomurom.applicationzao.models.product.*;
import ru.zaomurom.applicationzao.repositories.TchOrderRepository;
import ru.zaomurom.applicationzao.services.*;
import ru.zaomurom.applicationzao.models.prices.ClientsRegion;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private DocumentZAOService documentZAOService;

    @Autowired
    private TchOrderRepository tchOrderRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ClientsRegionService clientsRegionService;

    @Autowired
    private QuotaService quotaService;

    @Autowired
    private StationService stationService;

    @Autowired
    private RegionService regionService;

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();

        model.addAttribute("client", client);
        model.addAttribute("addresses", client.getAddresses());
        model.addAttribute("contacts", client.getContacts());
        
        // Получаем регионы с дедупликацией
        List<Region> clientRegions = clientsRegionService.findRegionsByClient(client)
                .stream()
                .distinct() // Убираем дубликаты
                .collect(Collectors.toList());
        model.addAttribute("clientRegions", clientRegions);
        
        model.addAttribute("username", username);
        model.addAttribute("stations", stationService.findAll());

        // Найти актуальную квоту на сегодня
        LocalDate today = LocalDate.now();
        List<Quota> quotas = quotaService.findByClient(client);
        Quota activeQuota = quotas.stream()
            .filter(q -> (q.getStartDate() == null || !today.isBefore(q.getStartDate()))
                      && (q.getEndDate() == null || !today.isAfter(q.getEndDate())))
            .findFirst()
            .orElse(null);
        model.addAttribute("activeQuota", activeQuota);

        return "profile";
    }

    @PostMapping("/profile/addAddress")
    public String addAddress(@RequestParam Integer postalcode,
                         @RequestParam String country,
                         @RequestParam Long regionId,
                         @RequestParam(required = false) Long stationId,
                         @RequestParam(required = false) String rayon,
                         @RequestParam String city,
                         @RequestParam String street,
                         @RequestParam String home,
                         @RequestParam(required = false) String roomnumber,
                         @RequestParam String schedule,
                         @RequestParam(required = false) String specialRequirements,
                         @RequestParam(required = false) Long contactId,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
    try {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();

        Addresses address = new Addresses();
        address.setPostalcode(postalcode);
        address.setCountry(country);
        address.setRayon(rayon);
        address.setCity(city);
        address.setStreet(street);
        address.setHome(home);
        address.setRoomnumber(roomnumber);
        address.setSchedule(schedule);
        address.setSpecialRequirements(specialRequirements);
        address.setClient(client);

        // Установка региона
        Region region = regionService.findById(regionId).orElse(null);
        if (region != null) {
            ClientsRegion clientsRegion = new ClientsRegion();
            clientsRegion.setClient(client);
            clientsRegion.setRegion(region);
            clientsRegionService.save(clientsRegion);
            address.setClientsRegion(clientsRegion);
        }

        // Установка станции
        if (stationId != null) {
            Station station = stationService.findById(stationId).orElse(null);
            if (station != null) {
                address.setStation(station);
            }
        }

        // Установка контакта
        if (contactId != null) {
            Contacts contact = contactsService.findById(contactId).orElse(null);
            if (contact != null) {
                address.setContact(contact);
            }
        }

        addressesService.save(address);
        redirectAttributes.addFlashAttribute("successMessage", "Адрес успешно добавлен");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при добавлении адреса: " + e.getMessage());
    }
    return "redirect:/profile";
}

@PostMapping("/profile/editAddress")
public String editAddress(@RequestParam Long id,
                          @RequestParam Integer postalcode,
                          @RequestParam String country,
                          @RequestParam Long regionId,
                          @RequestParam(required = false) Long stationId,
                          @RequestParam(required = false) String rayon,
                          @RequestParam String city,
                          @RequestParam String street,
                          @RequestParam String home,
                          @RequestParam(required = false) String roomnumber,
                          @RequestParam String schedule,
                          @RequestParam(required = false) String specialRequirements,
                          @RequestParam(required = false) Long contactId,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
    try {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();

        Optional<Addresses> optionalAddress = addressesService.findById(id);
        if (optionalAddress.isPresent()) {
            Addresses address = optionalAddress.get();
            
            // Проверяем, что адрес принадлежит текущему клиенту
            if (!address.getClient().getId().equals(client.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Доступ запрещен");
                return "redirect:/profile";
            }

            address.setPostalcode(postalcode);
            address.setCountry(country);
            address.setRayon(rayon);
            address.setCity(city);
            address.setStreet(street);
            address.setHome(home);
            address.setRoomnumber(roomnumber);
            address.setSchedule(schedule);
            address.setSpecialRequirements(specialRequirements);

            // Установка региона
            Region region = regionService.findById(regionId).orElse(null);
            if (region != null) {
                ClientsRegion clientsRegion = new ClientsRegion();
                clientsRegion.setClient(client);
                clientsRegion.setRegion(region);
                clientsRegionService.save(clientsRegion);
                address.setClientsRegion(clientsRegion);
            }

            // Установка станции
            if (stationId != null) {
                Station station = stationService.findById(stationId).orElse(null);
                if (station != null) {
                    address.setStation(station);
                }
            } else {
                address.setStation(null);
            }

            // Установка контакта
            if (contactId != null) {
                Contacts contact = contactsService.findById(contactId).orElse(null);
                if (contact != null) {
                    address.setContact(contact);
                }
            } else {
                address.setContact(null);
            }

            addressesService.save(address);
            redirectAttributes.addFlashAttribute("successMessage", "Адрес успешно обновлен");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Адрес не найден");
        }
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении адреса: " + e.getMessage());
    }
    return "redirect:/profile";
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

                // Validate and update fields
                if (contactData.containsKey("contactType")) {
                    contact.setContactType(contactData.get("contactType"));
                }
                if (contactData.containsKey("name")) {
                    contact.setName(contactData.get("name"));
                }
                if (contactData.containsKey("phonenumber")) {
                    String newPhone = contactData.get("phonenumber");
                    // Validate phone number format
                    if (!newPhone.matches("\\d{11}")) {
                        return ResponseEntity.badRequest().body("Неверный формат номера телефона");
                    }
                    // Check if phone exists (excluding current contact)
                    boolean phoneExists = contactsService.findAll().stream()
                            .filter(c -> !c.getId().equals(id))
                            .anyMatch(c -> c.getPhonenumber().equals(newPhone));
                    if (phoneExists) {
                        return ResponseEntity.badRequest().body("Номер телефона уже используется");
                    }
                    contact.setPhonenumber(newPhone);
                }
                if (contactData.containsKey("email")) {
                    String newEmail = contactData.get("email");
                    // Check if email exists in contacts (excluding current contact)
                    boolean emailExistsInContacts = contactsService.findAll().stream()
                            .filter(c -> !c.getId().equals(id))
                            .anyMatch(c -> c.getEmail().equals(newEmail));
                    // Check if email exists in users
                    boolean emailExistsInUsers = userService.findByEmail(newEmail) != null;
                    if (emailExistsInContacts || emailExistsInUsers) {
                        return ResponseEntity.badRequest().body("Email уже используется");
                    }
                    contact.setEmail(newEmail);
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
        // Validate phone number format
        if (!phonenumber.matches("\\d{11}")) {
            return "redirect:/profile?error=invalid_phone";
        }

        // Check if phone number exists
        boolean phoneExists = contactsService.findAll().stream()
                .anyMatch(contact -> contact.getPhonenumber().equals(phonenumber));
        if (phoneExists) {
            return "redirect:/profile?error=phone_exists";
        }

        // Check if email exists in contacts or users
        boolean emailExistsInContacts = contactsService.findAll().stream()
                .anyMatch(contact -> contact.getEmail().equals(email));
        boolean emailExistsInUsers = userService.findByEmail(email) != null;
        if (emailExistsInContacts || emailExistsInUsers) {
            return "redirect:/profile?error=email_exists";
        }

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

        List<Product> products = productService.findAllVisible();
        model.addAttribute("products", products);
        model.addAttribute("username", username);

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
            // Передать актуальную квоту
            Quota activeQuota = quotaService.getActiveQuotaForClient(client, java.time.LocalDate.now());
            model.addAttribute("activeQuota", activeQuota);
            return "productDetails";
        }
        return "redirect:/products";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam(required = false) String query, Model model, Principal principal) {
        List<Product> products;
        if (query != null && !query.isEmpty()) {
            products = productService.findAllVisible().stream()
                    .filter(product -> product.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            products = productService.findAllVisible();
        }

        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        //Long selectedPriceId = client.getSelectedPrice() != null ? client.getSelectedPrice().getId() : null;

        model.addAttribute("products", products);
        //model.addAttribute("selectedPriceId", selectedPriceId);

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
    @ResponseBody
    public ResponseEntity<Map<String, Object>> ProductaddToCart(
            @RequestParam Long productId,
            @RequestParam(required = false) Long sumId,
            @RequestParam String username,
            @RequestParam int quantity) {

        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Cart cart = cartService.findByClient(client);
        if (cart == null) {
            cart = new Cart();
            cart.setClient(client);
        }

        Product product = productService.findById(productId).orElse(null);

        if (product != null) {
            // Проверяем совместимость типов номенклатуры
            if (!cartService.canAddProductToCart(cart, product)) {
                String existingType = cartService.getCartNomenclatureType(cart) == NomenclatureType.RAILWAY ? "ЖД" : "Авто";
                String newType = product.getNomenclatureType() == NomenclatureType.RAILWAY ? "ЖД" : "Авто";
                return ResponseEntity.badRequest().body(Map.of("error", 
                    "Нельзя добавить товар типа '" + newType + "' в корзину с товарами типа '" + existingType + "'. " +
                    "Очистите корзину перед добавлением товаров другого типа."));
            }

            double requestedVolume = product.getVolume() * quantity;
            double cartVolume = cart.getCartItems().stream()
                .mapToDouble(item -> item.getProduct().getVolume() * item.getQuantity())
                .sum();
            double totalRequested = cartVolume + requestedVolume;
            Quota quota = quotaService.getActiveQuotaForClient(client, LocalDate.now());
            if (quota == null || quota.getAllowedVolume() < totalRequested || quota.getAllowedVolume() <= 0.01) {
                return ResponseEntity.badRequest().body(Map.of("error", "Недостаточно объёма по квоте для добавления товара в корзину"));
            }
            if (sumId != null) {
                Sum sum = productService.findSumById(sumId).orElse(null);
                if (sum != null) {
                    cart.addProduct(product, quantity, sum);
                } else {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid sum ID"));
                }
            } else {
                cart.addProduct(product, quantity);
            }
            cartService.save(cart);

            // Правильный подсчет заполненных машин/вагонов
            int filledTrucksCount = 0;
            
            if (!cart.getTrucks().isEmpty()) {
                // Подсчитываем количество заполненных машин/вагонов
                filledTrucksCount = (int) cart.getTrucks().stream()
                        .filter(truck -> {
                            int totalItemsInTruck = truck.getItems().stream()
                                    .mapToInt(CartTruckItem::getQuantity)
                                    .sum();
                            
                            if (product.getNomenclatureType() == NomenclatureType.RAILWAY) {
                                // Для ЖД товаров: максимум 49 пачек
                                return totalItemsInTruck >= Product.MAX_ITEMS_RAILWAY;
                            } else {
                                // Для авто товаров: проверяем по длине товаров
                                long longProductsCount = truck.getItems().stream()
                                        .filter(item -> item.getProduct().getLength() == 2.8)
                                        .count();
                                
                                int maxCapacity = longProductsCount >= 7 ? Product.MAX_ITEMS_LONG : Product.MAX_ITEMS_SHORT;
                                return totalItemsInTruck >= maxCapacity;
                            }
                        })
                        .count();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("redirect", "/products");

            if (filledTrucksCount > 0) {
                // Определяем тип номенклатуры для правильного сообщения
                String vehicleType = product.getNomenclatureType() == NomenclatureType.RAILWAY ? "вагон" : "грузовой автомобиль";
                String vehicleTypePlural = product.getNomenclatureType() == NomenclatureType.RAILWAY ? "вагонов" : "грузовых машин";
                
                String message = filledTrucksCount == 1 ?
                        "Первый " + vehicleType + " заполнен" :
                        "Количество заполненных " + vehicleTypePlural + ": " + filledTrucksCount;
                response.put("message", message);
            }

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Invalid product ID"));
    }

    @GetMapping("/orders")
    @Transactional
    public String orders(Model model, Principal principal, @RequestParam(required = false) String status, HttpServletRequest request) {
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

        // Инициализация коллекций
        for (Order order : orders) {
            Hibernate.initialize(order.getDocumentations());
        }

        model.addAttribute("orders", orders);
        model.addAttribute("username", username);
        model.addAttribute("status", status);
        // Добавить CSRF-токен в модель
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }

        return "orders";
    }


    @GetMapping("/orderDetails")
    public String orderDetails(@RequestParam Long id, Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        
        Optional<Order> orderOptional = orderService.findById(id);
        if (!orderOptional.isPresent()) {
            return "redirect:/orders";
        }
        
        Order order = orderOptional.get();
        
        // Проверяем, что заказ принадлежит текущему клиенту
        if (!order.getClient().getId().equals(client.getId())) {
            return "redirect:/orders";
        }
        
        // Определяем тип номенклатуры заказа
        boolean isRailwayOrder = order.getTchOrders().stream()
                .anyMatch(tchOrder -> tchOrder.getProduct().getNomenclatureType() == NomenclatureType.RAILWAY);
        
        // Получаем доступные адреса для редактирования
        List<Addresses> availableAddresses = cartService.getAvailableAddressesForOrder(client, order);
        
        model.addAttribute("order", order);
        model.addAttribute("isRailwayOrder", isRailwayOrder);
        model.addAttribute("availableAddresses", availableAddresses);
        model.addAttribute("allAddresses", client.getAddresses());
        
        // Найти актуальную квоту на сегодня
        LocalDate today = LocalDate.now();
        List<Quota> quotas = quotaService.findByClient(client);
        Quota activeQuota = quotas.stream()
            .filter(q -> (q.getStartDate() == null || !today.isBefore(q.getStartDate()))
                      && (q.getEndDate() == null || !today.isAfter(q.getEndDate())))
            .findFirst()
            .orElse(null);
        model.addAttribute("activeQuota", activeQuota);
        
        // Сохраняем оригинальные количества для проверки квоты
        Map<Long, Integer> originalQuantities = new HashMap<>();
        for (TCHOrder tchOrder : order.getTchOrders()) {
            originalQuantities.put(tchOrder.getId(), tchOrder.getQuantity());
        }
        model.addAttribute("originalQuantities", originalQuantities);
        
        // Рассчитываем общую сумму заказа
        double totalSum = order.getTchOrders().stream()
                .mapToDouble(tchOrder -> tchOrder.getQuantity() * tchOrder.getPrice())
                .sum();
        model.addAttribute("totalSum", totalSum);
        
        return "orderDetails";
    }

    @PostMapping("/orderDetails/updateAddress")
    @ResponseBody
    public ResponseEntity<?> updateOrderAddress(@RequestParam Long orderId, 
                                               @RequestParam Long newAddressId,
                                               Principal principal) {
        try {
            String username = principal.getName();
            User user = userService.findByUsername(username);
            Client client = user.getClient();
            
            Optional<Order> orderOptional = orderService.findById(orderId);
            if (!orderOptional.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Заказ не найден"));
            }
            
            Order order = orderOptional.get();
            
            // Проверяем, что заказ принадлежит текущему клиенту
            if (!order.getClient().getId().equals(client.getId())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Доступ запрещен"));
            }
            
            // Проверяем, что адрес принадлежит клиенту
            Optional<Addresses> addressOptional = addressesService.findById(newAddressId);
            if (!addressOptional.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Адрес не найден"));
            }
            
            Addresses newAddress = addressOptional.get();
            if (!newAddress.getClient().getId().equals(client.getId())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Адрес не принадлежит вам"));
            }
            
            // Проверяем совместимость адреса с типом товаров в заказе
            boolean isRailwayOrder = order.getTchOrders().stream()
                    .anyMatch(tchOrder -> tchOrder.getProduct().getNomenclatureType() == NomenclatureType.RAILWAY);
            
            if (isRailwayOrder && newAddress.getStation() == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, 
                    "message", "Для ЖД товаров нужен адрес со станцией"));
            }
            
            if (!isRailwayOrder && newAddress.getStation() != null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, 
                    "message", "Для авто товаров нужен адрес без станции"));
            }
            
            // Сохраняем старый адрес для уведомления
            Addresses oldAddress = order.getDeliveryAddress();
            
            // Обновляем адрес заказа
            order.setDeliveryAddress(newAddress);
            orderService.save(order);
            
            // Отправляем уведомление админам
            String clientName = client.getName();
            String orderNumber = order.getId().toString();
            String orderDate = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(order.getOrderDate());
            
            String notificationMessage = String.format(
                "Здравствуйте! Клиент %s внес изменения в заказ №%s от %s.", 
                clientName, orderNumber, orderDate
            );
            
            // Отправляем уведомление всем админам
            List<String> adminEmails = userService.findAllAdmins().stream()
                    .map(User::getEmail)
                    .filter(email -> email != null && !email.isEmpty())
                    .collect(Collectors.toList());
            
            if (!adminEmails.isEmpty()) {
                emailService.sendOrderAddressChangeNotificationAsync(order, notificationMessage, adminEmails);
            }
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Адрес заказа успешно обновлен"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Ошибка при обновлении адреса: " + e.getMessage()));
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
    public String addToCart(@RequestParam Long productId, @RequestParam Long sumId,
                            @RequestParam String username, @RequestParam int quantity) {
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

            // Сохраняем корзину (внутри save будет распределение)
            cartService.save(cart);
        }
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String cart(Model model, Principal principal) {
        try {
            String username = principal.getName();
            User user = userService.findByUsername(username);
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
            model.addAttribute("cartService", cartService);
            model.addAttribute("isRailwayCart", cartService.getCartNomenclatureType(cart) == NomenclatureType.RAILWAY);

            // Передача графика доставки с учетом типа номенклатуры
            List<Addresses> addresses = cartService.getAvailableAddressesForCart(client, cart);
            model.addAttribute("addresses", addresses);

            // Передать актуальную квоту
            Quota activeQuota = quotaService.getActiveQuotaForClient(client, java.time.LocalDate.now());
            model.addAttribute("activeQuota", activeQuota);

            return "cart";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @PostMapping("/cart/checkout")
    public String checkout(@RequestParam Long addressId, @RequestParam String deliveryDateTime,
                           @RequestParam String comment, Principal principal, Model model) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Cart cart = cartService.findByClient(client);

        if (cart == null || cart.getCartItems().isEmpty()) {
            model.addAttribute("error", "Ваша корзина пуста.");
            return "cart";
        }

        List<Addresses> addresses = cartService.getAvailableAddressesForCart(client, cart);
        if (addresses == null || addresses.isEmpty()) {
            String errorMessage = "Нет подходящих адресов доставки для выбранных товаров. ";
            if (cartService.getCartNomenclatureType(cart) == NomenclatureType.RAILWAY) {
                errorMessage += "Для ЖД товаров нужны адреса со станциями.";
            } else {
                errorMessage += "Для авто товаров нужны адреса без станций.";
            }
            model.addAttribute("error", errorMessage);
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
        model.addAttribute("comment", comment);
        model.addAttribute("cartService", cartService);
        model.addAttribute("isRailwayOrder", cartService.getCartNomenclatureType(cart) == NomenclatureType.RAILWAY);

        return "confirmOrder";
    }


    @PostMapping("/cart/confirmOrder")
    @Transactional
    public String confirmOrder(@RequestParam Long addressId, @RequestParam String deliveryDate,
                               @RequestParam String comment, 
                               @RequestParam(required = false) MultipartFile railwayDocument,
                               Principal principal, Model model) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Cart cart = cartService.findByClient(client);

        if (cart == null || cart.getCartItems().isEmpty()) {
            model.addAttribute("error", "Ваша корзина пуста.");
            return "cart";
        }

        // Проверяем необходимость документа для ЖД заказов
        NomenclatureType cartType = cartService.getCartNomenclatureType(cart);
        if (cartType == NomenclatureType.RAILWAY) {
            if (railwayDocument == null || railwayDocument.isEmpty()) {
                model.addAttribute("error", "Для ЖД заказов обязательно требуется прикрепить документ.");
                return "cart";
            }
            
            // Проверяем размер файла (до 10 МБ)
            if (railwayDocument.getSize() > 10 * 1024 * 1024) {
                model.addAttribute("error", "Размер документа не должен превышать 10 МБ.");
                return "cart";
            }
            
            // Проверяем тип файла
            String fileName = railwayDocument.getOriginalFilename();
            if (fileName != null) {
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                if (!Arrays.asList("pdf", "doc", "docx", "xls", "xlsx").contains(extension)) {
                    model.addAttribute("error", "Допустимые форматы документов: PDF, DOC, DOCX, XLS, XLSX.");
                    return "cart";
                }
            }
        }

        // Проверка квоты перед оформлением заказа
        double cartVolume = cart.getCartItems().stream()
            .mapToDouble(item -> item.getProduct().getVolume() * item.getQuantity())
            .sum();
        Quota quota = quotaService.getActiveQuotaForClient(client, LocalDate.now());
        if (quota == null || quota.getAllowedVolume() < cartVolume || quota.getAllowedVolume() <= 0.01) {
            model.addAttribute("error", "Недостаточно объёма по квоте для оформления заказа");
            return "cart";
        }

        List<CartTruck> cartTrucks = cart.getTrucks();
        if (cartTrucks.isEmpty()) {
            cartTrucks = cart.distributeItemsToTrucks();
        }

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        Date parsedDeliveryDate;
        try {
            parsedDeliveryDate = dateTimeFormat.parse(deliveryDate);
        } catch (ParseException e) {
            model.addAttribute("error", "Неверный формат даты и времени.");
            return "cart";
        }

        // Создаем список заказов
        List<Order> orders = new ArrayList<>();

        // Создаем отдельный заказ для каждой машины
        for (CartTruck cartTruck : cartTrucks) {
            Order order = new Order();
            order.setClient(client);
            order.setDeliveryAddress(addressesService.findById(addressId).orElse(null));
            order.setDeliveryDate(parsedDeliveryDate);
            order.setOrderDate(new Date());
            order.setStatus("В обработке");
            order.setComment(comment);

            // Создаем одну машину для этого заказа
            OrderTruck orderTruck = new OrderTruck(order);
            List<OrderTruck> orderTrucks = new ArrayList<>();
            orderTrucks.add(orderTruck);

            // Создаем TCHOrders для товаров в этой машине
            List<TCHOrder> tchOrders = new ArrayList<>();
            for (CartTruckItem cartTruckItem : cartTruck.getItems()) {
                TCHOrder tchOrder = new TCHOrder(
                        order,
                        cartTruckItem.getProduct(),
                        cartTruckItem.getQuantity(),
                        cartTruckItem.getSum().getSumma()
                );
                tchOrder.setVolume(cartTruckItem.getProduct().getVolume());
                tchOrder.setTruck(orderTruck);
                tchOrders.add(tchOrder);
            }

            order.setTchOrders(tchOrders);
            order.setTrucks(orderTrucks);
            
            // Сохраняем документ для ЖД заказов
            if (cartType == NomenclatureType.RAILWAY && railwayDocument != null && !railwayDocument.isEmpty()) {
                try {
                    String fileName = railwayDocument.getOriginalFilename();
                    String documentPath = "railway_documents/" + System.currentTimeMillis() + "_" + fileName;
                    
                    // Создаем директорию если не существует
                    java.nio.file.Path uploadDir = java.nio.file.Paths.get("uploads", "railway_documents");
                    if (!java.nio.file.Files.exists(uploadDir)) {
                        java.nio.file.Files.createDirectories(uploadDir);
                    }
                    
                    // Сохраняем файл
                    java.nio.file.Path filePath = uploadDir.resolve(System.currentTimeMillis() + "_" + fileName);
                    java.nio.file.Files.copy(railwayDocument.getInputStream(), filePath);
                    
                    order.setRailwayDocument(filePath.toString());
                    order.setRailwayDocumentName(fileName);
                } catch (Exception e) {
                    model.addAttribute("error", "Ошибка при сохранении документа: " + e.getMessage());
                    return "cart";
                }
            }
            
            // Сохраняем заказ
            orderService.save(order);
            orderService.addStatusHistory(order, "В обработке", user);
            
            orders.add(order);
        }

        // Отправляем одно уведомление для всех заказов
        if (!orders.isEmpty()) {
            // Используем первый заказ для уведомления, но включаем информацию о количестве машин
            Order firstOrder = orders.get(0);
            String relatedOrdersInfo = String.format("Сформировано %d заказов (по одной машине в каждом). Номера заказов: %s", 
                orders.size(),
                orders.stream()
                    .map(o -> String.format("№%d", o.getId()))
                    .collect(Collectors.joining(", "))
            );
            
            // Устанавливаем информацию о связанных заказах
            firstOrder.setRelatedOrdersInfo(relatedOrdersInfo);
            orderService.save(firstOrder);
            
            // Отправляем уведомление
            emailService.sendNewOrderNotification(firstOrder, client.getName());
        }

        // Очищаем корзину
        cart.getCartItems().clear();
        cart.getTrucks().clear();
        cartService.save(cart);

        // После оформления заказа уменьшить квоту
        quotaService.decreaseQuotaVolume(client, cartVolume);

        return "redirect:/orders";
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
    @PostMapping("/repeatOrder")
    public String repeatOrder(@RequestParam Long orderId, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Order order = orderService.findById(orderId).orElseThrow(() -> new RuntimeException("Заказ не найден"));
        Cart cart = cartService.findByClient(client);
        if (cart == null) {
            cart = new Cart(client);
            cartService.save(cart);
        }
        for (TCHOrder tchOrder : order.getTchOrders()) {
            Product product = tchOrder.getProduct();
            Sum sum = product.getSums().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Для товара не установлена цена"));

            cart.addProduct(product, tchOrder.getQuantity(), sum);
        }

        cartService.save(cart);

        return "redirect:/cart";
    }

    @PostMapping("/cart/updateQuantity")
    public ResponseEntity<Void> updateQuantity(
            @RequestParam Long productId,
            @RequestParam String username,
            @RequestParam int delta,
            @RequestParam(required = false) Long addressId) {

        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Cart cart = cartService.findByClient(client);

        if (cart != null) {
            // Находим товар в корзине с учетом текущего адреса
            CartItem item = cart.getCartItems().stream()
                    .filter(i -> i.getProduct().getId().equals(productId))
                    .findFirst()
                    .orElse(null);

            if (item != null) {
                int newQuantity = item.getQuantity() + delta;
                if (newQuantity > 0) {
                    item.setQuantity(newQuantity);
                    cartService.saveCartItem(item);
                } else {
                    cart.getCartItems().remove(item);
                    cartService.deleteCartItem(item);
                }

                // Обновляем распределение по машинам
                cart.distributeItemsToTrucks();
                cartService.save(cart);

                // Если указан адрес, обновляем цены
                if (addressId != null) {
                    cartService.updateCartItemPrices(cart, addressId);
                }

                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.notFound().build();
    }


    @GetMapping("/cart/manualDistribution")
    @ResponseBody
    public ResponseEntity<CartDistributionDTO> getCartForManualDistribution(Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Cart cart = cartService.findByClient(client);

        if (cart == null || cart.getCartItems().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CartDistributionDTO dto = cartService.prepareCartDistributionDTO(cart);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/cart/saveManualDistribution")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> saveManualDistribution(@RequestBody ManualDistributionRequest request, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Cart cart = cartService.findByClient(client);

        if (cart == null) {
            return ResponseEntity.badRequest().body("Корзина пуста");
        }

        double requestedVolume = request.getTrucks().stream()
            .flatMap(truck -> truck.getItems().stream())
            .mapToDouble(item -> {
                Product product = productService.findById(item.getProductId()).orElse(null);
                return product != null ? product.getVolume() * item.getQuantity() : 0;
            })
            .sum();
        Quota quota = quotaService.getActiveQuotaForClient(client, LocalDate.now());
        if (quota == null || quota.getAllowedVolume() < requestedVolume || quota.getAllowedVolume() <= 0.01) {
            return ResponseEntity.badRequest().body("Недостаточно объёма по квоте для распределения товаров");
        }
        cartService.applyManualDistribution(cart, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart/autoDistribute")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> autoDistribute(Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Cart cart = cartService.findByClient(client);

        if (cart == null) {
            return ResponseEntity.badRequest().body("Корзина пуста");
        }

        cartService.autoDistribute(cart);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart/clear")
    @Transactional
    public ResponseEntity<?> clearCart(Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Cart cart = cartService.findByClient(client);

        if (cart != null) {
            cart.getCartItems().clear();
            cart.getTrucks().clear();
            cartService.save(cart);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/cart/updatePrices")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartPrices(
            @RequestParam Long addressId,
            Principal principal) {

        String username = principal.getName();
        User user = userService.findByUsername(username);
        Client client = user.getClient();
        Cart cart = cartService.findByClient(client);

        if (cart == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cart not found"));
        }

        Map<String, CartService.PriceInfo> prices = cartService.updateCartItemPrices(cart, addressId);
        double total = cartService.calculateCartTotal(cart);

        Map<String, Object> response = new HashMap<>();
        response.put("total", total);
        response.put("prices", prices);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/check-phone")
    @ResponseBody
    public ResponseEntity<?> checkPhoneNumber(
            @RequestParam String phone,
            @RequestParam(required = false) Long excludeId) {
        
        // Check if phone number exists in contacts
        boolean exists = contactsService.findAll().stream()
                .filter(contact -> excludeId == null || !contact.getId().equals(excludeId))
                .anyMatch(contact -> contact.getPhonenumber().equals(phone));

        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/check-email")
    @ResponseBody
    public ResponseEntity<?> checkEmail(
            @RequestParam String email,
            @RequestParam(required = false) Long excludeId) {
        
        // Check if email exists in contacts
        boolean existsInContacts = contactsService.findAll().stream()
                .filter(contact -> excludeId == null || !contact.getId().equals(excludeId))
                .anyMatch(contact -> contact.getEmail().equals(email));

        // Check if email exists in users
        boolean existsInUsers = userService.findByEmail(email) != null;

        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", existsInContacts || existsInUsers);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/orders/updateTchOrderQuantity")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> updateTchOrderQuantity(
            @RequestBody Map<String, Object> payload,
            Principal principal) {
        Long tchOrderId = Long.valueOf(payload.get("tchOrderId").toString());
        int newQuantity = Integer.parseInt(payload.get("newQuantity").toString());

        try {
            String username = principal.getName();
            User user = userService.findByUsername(username);
            TCHOrder tchOrder = tchOrderRepository.findById(tchOrderId)
                    .orElseThrow(() -> new RuntimeException("TCHOrder not found"));

            Order order = tchOrder.getOrder();

            // Проверяем, что заказ принадлежит текущему пользователю
            if (!order.getClient().equals(user.getClient())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized");
            }

            // Проверяем допустимые статусы для редактирования
            List<String> allowedStatuses = Arrays.asList(
                    "В обработке",
                    "Принят",
                    "Оплачен. В работе",
                    "Оплачен. В ожидании",
                    "Назначена погрузка"
            );

            if (!allowedStatuses.contains(order.getStatus())) {
                return ResponseEntity.badRequest().body("Редактирование невозможно для текущего статуса заказа");
            }

            // Проверяем ограничения по количеству
            OrderTruck truck = tchOrder.getTruck();
            List<TCHOrder> truckOrders = truck.getTchOrders();

            // Определяем тип номенклатуры заказа
            NomenclatureType orderType = tchOrder.getProduct().getNomenclatureType();
            int totalItems = 0;
            int max = 16;
            String vehicleType = "машине";

            if (orderType == NomenclatureType.RAILWAY) {
                // Логика для ЖД товаров
                vehicleType = "вагоне";
                for (TCHOrder item : truckOrders) {
                    if (item.getId().equals(tchOrderId)) {
                        continue;
                    }
                    totalItems += item.getQuantity();
                }
                totalItems += newQuantity;
                max = 49; // Максимум 49 пачек для ЖД
            } else {
                // Логика для авто товаров (существующая)
                int count25 = 0;
                int count28 = 0;
                for (TCHOrder item : truckOrders) {
                    if (item.getId().equals(tchOrderId)) {
                        continue;
                    }
                    if (item.getProduct().getLength() == 2.5 || item.getProduct().getLength() == 2.4) {
                        count25 += item.getQuantity();
                    }
                    if (item.getProduct().getLength() == 2.8) {
                        count28 += item.getQuantity();
                    }
                }
                // Добавляем новое количество
                if (tchOrder.getProduct().getLength() == 2.5 || tchOrder.getProduct().getLength() == 2.4) {
                    count25 += newQuantity;
                }
                if (tchOrder.getProduct().getLength() == 2.8) {
                    count28 += newQuantity;
                }
                totalItems = count25 + count28;
                if (count28 > 0 && count25 > 0) {
                    max = (count28 > 7) ? 13 : 16;
                } else if (count28 > 0 && count25 == 0) {
                    max = 13;
                } else if (count25 > 0 && count28 == 0) {
                    max = 16;
                }
            }
            
            if (totalItems > max) {
                return ResponseEntity.badRequest().body(
                        "В " + vehicleType + " не может быть больше " + max + " пачек по текущим правилам!");
            }

            // --- Квота: учесть разницу ---
            int oldQuantity = tchOrder.getQuantity();
            double productVolume = tchOrder.getProduct().getVolume();
            double diff = (newQuantity - oldQuantity) * productVolume;
            Quota quota = quotaService.getActiveQuotaForClient(order.getClient(), java.time.LocalDate.now());
            if (diff > 0) {
                // Увеличение — уменьшить квоту
                if (quota == null || quota.getAllowedVolume() < diff || quota.getAllowedVolume() <= 0.01) {
                    return ResponseEntity.badRequest().body("Недостаточно объёма по квоте для увеличения количества");
                }
                quota.setAllowedVolume(Math.max(0, quota.getAllowedVolume() - diff));
                quotaService.save(quota);
            } else if (diff < 0) {
                // Уменьшение — вернуть в квоту
                if (quota != null) {
                    quota.setAllowedVolume(quota.getAllowedVolume() + Math.abs(diff));
                    quotaService.save(quota);
                }
            }
            // ---

            // Обновляем количество
            tchOrder.setQuantity(newQuantity);
            tchOrderRepository.save(tchOrder);
            
            // Отправляем уведомление админам об изменении количества
            String clientName = user.getClient().getName();
            String orderNumber = order.getId().toString();
            String orderDate = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(order.getOrderDate());
            String notificationMessage = String.format(
                "Здравствуйте! Клиент %s внес изменения в заказ №%s от %s.", 
                clientName, orderNumber, orderDate
            );
            
            List<String> adminEmails = userService.findAllAdmins().stream()
                    .map(User::getEmail)
                    .filter(email -> email != null && !email.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
            
            if (!adminEmails.isEmpty()) {
                emailService.sendOrderAddressChangeNotificationAsync(order, notificationMessage, adminEmails);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Количество успешно обновлено"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}

