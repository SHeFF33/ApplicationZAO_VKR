package ru.zaomurom.applicationzao.controllers;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.zaomurom.applicationzao.models.DocumentZAO;
import ru.zaomurom.applicationzao.models.client.*;
import ru.zaomurom.applicationzao.models.order.*;
import ru.zaomurom.applicationzao.models.product.Documentation;
import ru.zaomurom.applicationzao.models.product.Product;
import ru.zaomurom.applicationzao.models.product.ProductImage;
import ru.zaomurom.applicationzao.models.product.Sum;
import ru.zaomurom.applicationzao.repositories.OrderTruckRepository;
import ru.zaomurom.applicationzao.services.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AdminController {
    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private ContactsService contactsService;
    @Autowired
    private PriceService priceService;
    @Autowired
    private DocumentationService documentationService;
    @Autowired
    private SumService sumService;
    @Autowired
    private ProductImageService productImageService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDocumentationService orderDocumentationService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private DocumentTypeService documentTypeService;
    @Autowired
    private OrderTruckRepository orderTruckRepository;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        return "admin/dashboard";
    }


    @GetMapping("/admin/products")
    public String products(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("prices", priceService.findAll());
        return "admin/products";
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Optional<ProductImage> image = productImageService.findById(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(image.get().getBytes(), headers, HttpStatus.OK);
    }

    @GetMapping("/admin/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @GetMapping("/admin/clients")
    public String clients(Model model) {
        model.addAttribute("clients", clientService.findAll());
        return "admin/clients";
    }

    @GetMapping("/admin/addClient")
    public String addClientForm(Model model) {
        model.addAttribute("contacts", new ArrayList<>());
        model.addAttribute("prices", priceService.findAll());
        return "admin/addClient";
    }


    @PostMapping("/admin/addClient")
    public String addClient(
            @RequestParam String name,
            @RequestParam String inn,
            @RequestParam String kpp,
            @RequestParam String uraddress,
            @RequestParam String factaddress,
            @RequestParam(required = false) Double sum1,
            @RequestParam(required = false) Double sum2,
            @RequestParam Long selectedPriceId,
            @RequestParam(required = false) List<String> postalcodes,
            @RequestParam(required = false) List<String> countries,
            @RequestParam(required = false) List<String> regions,
            @RequestParam(required = false) List<String> rayons,
            @RequestParam(required = false) List<String> cities,
            @RequestParam(required = false) List<String> streets,
            @RequestParam(required = false) List<String> homes,
            @RequestParam(required = false) List<String> roomnumbers,
            @RequestParam(required = false) List<String> schedules,
            @RequestParam(required = false) List<String> typeContacts,
            @RequestParam(required = false) List<String> contactNames,
            @RequestParam(required = false) List<String> phonenumbers,
            @RequestParam(required = false) List<String> emails,
            @RequestParam(required = false) List<String> usernames,
            @RequestParam(required = false) List<String> passwords,
            @RequestParam(required = false) List<String> isAdmins,
            @RequestParam(required = false) List<String> nameuser,
            @RequestParam(required = false) List<String> userEmails,
            RedirectAttributes redirectAttributes) {

        // Проверка уникальности username
        if (usernames != null) {
            for (String username : usernames) {
                if (userService.existsByUsername(username)) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Пользователь с логином '" + username + "' уже существует");
                    return "redirect:/admin/addClient";
                }
            }
        }

        // Проверка email для администраторов
        if (isAdmins != null && userEmails != null) {
            for (int i = 0; i < isAdmins.size(); i++) {
                if (Boolean.parseBoolean(isAdmins.get(i))) {
                    String email = userEmails.get(i);
                    if (email == null || email.isEmpty()) {
                        redirectAttributes.addFlashAttribute("errorMessage",
                                "Для администратора необходимо указать email");
                        return "redirect:/admin/addClient";
                    }
                    if (userService.existsByEmail(email)) {
                        redirectAttributes.addFlashAttribute("errorMessage",
                                "Пользователь с email '" + email + "' уже существует");
                        return "redirect:/admin/addClient";
                    }
                }
            }
        }

        Client client = new Client();
        client.setName(name);
        client.setInn(inn);
        client.setKpp(kpp);
        client.setUraddress(uraddress);
        client.setFactaddress(factaddress);
        client.setSum1(sum1);
        client.setSum2(sum2);

        Price selectedPrice = priceService.findById(selectedPriceId).orElse(null);
        client.setSelectedPrice(selectedPrice);

        List<Addresses> addressesList = new ArrayList<>();
        if (postalcodes != null && !postalcodes.isEmpty()) {
            for (int i = 0; i < postalcodes.size(); i++) {
                Addresses address = new Addresses();
                address.setPostalcode(Integer.parseInt(postalcodes.get(i)));
                address.setCountry(countries.get(i));
                address.setRegion(regions.get(i));
                address.setRayon(rayons.get(i));
                address.setCity(cities.get(i));
                address.setStreet(streets.get(i));
                address.setHome(homes.get(i));
                address.setRoomnumber(roomnumbers.get(i));
                address.setSchedule(schedules != null && i < schedules.size() ? schedules.get(i) : null);
                address.setClient(client);
                addressesList.add(address);
            }
        }
        client.setAddresses(addressesList);

        // Обработка контактов (остается без изменений)
        List<Contacts> contactsList = new ArrayList<>();
        if (contactNames != null && !contactNames.isEmpty()) {
            for (int i = 0; i < contactNames.size(); i++) {
                Contacts contact = new Contacts();
                contact.setContactType(typeContacts.get(i));
                contact.setName(contactNames.get(i));
                contact.setPhonenumber(phonenumbers.get(i));
                contact.setEmail(emails.get(i));
                contact.setClient(client);
                contactsList.add(contact);
            }
        }
        client.setContacts(contactsList);

        List<User> usersList = new ArrayList<>();
        if (usernames != null && !usernames.isEmpty()) {
            for (int i = 0; i < usernames.size(); i++) {
                if ((nameuser == null || nameuser.size() <= i) ||
                        (passwords == null || passwords.size() <= i)) {
                    continue;
                }

                User user = new User();
                user.setUsername(usernames.get(i));
                user.setPassword(new BCryptPasswordEncoder().encode(passwords.get(i)));
                boolean isAdmin = isAdmins != null && isAdmins.size() > i && Boolean.parseBoolean(isAdmins.get(i));
                user.setAdmin(isAdmin);
                user.setName(nameuser.get(i));

                if (isAdmin && userEmails != null && userEmails.size() > i) {
                    user.setEmail(userEmails.get(i));
                }

                user.setClient(client);
                usersList.add(user);
            }
        }
        client.setUsers(usersList);

        clientService.save(client);
        redirectAttributes.addFlashAttribute("successMessage", "Клиент успешно добавлен");
        return "redirect:/admin/clients";
    }

    @GetMapping("/admin/editClient/{id}")
    public String editClientForm(@PathVariable Long id, Model model) {
        Optional<Client> optionalClient = clientService.findById(id);
        if (optionalClient.isPresent()) {
            Client client = optionalClient.get();
            model.addAttribute("client", client);
            model.addAttribute("prices", priceService.findAll());
            return "admin/editClient";
        } else {
            return "redirect:/admin/clients";
        }
    }
    @PostMapping("/admin/editClient/{id}")
    public String editClient(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String inn,
            @RequestParam String kpp,
            @RequestParam String uraddress,
            @RequestParam String factaddress,
            @RequestParam(required = false) Double sum1,
            @RequestParam(required = false) Double sum2,
            @RequestParam Long selectedPriceId,
            @RequestParam(required = false) List<String> postalcodes,
            @RequestParam(required = false) List<String> countries,
            @RequestParam(required = false) List<String> regions,
            @RequestParam(required = false) List<String> rayons,
            @RequestParam(required = false) List<String> cities,
            @RequestParam(required = false) List<String> streets,
            @RequestParam(required = false) List<String> homes,
            @RequestParam(required = false) List<String> roomnumbers,
            @RequestParam(required = false) List<String> schedules,
            @RequestParam(required = false) List<String> typeContacts,
            @RequestParam(required = false) List<String> contactNames,
            @RequestParam(required = false) List<String> phonenumbers,
            @RequestParam(required = false) List<String> emails,
            @RequestParam(required = false) List<String> usernames,
            @RequestParam(required = false) List<String> passwords,
            @RequestParam(required = false) List<String> isAdmins,
            @RequestParam(required = false) List<String> nameuser,
            @RequestParam(required = false) List<Long> contactIds,
            @RequestParam(required = false) List<Long> userIds,
            @RequestParam(required = false) List<String> userEmails,
            RedirectAttributes redirectAttributes) {

        Optional<Client> optionalClient = clientService.findById(id);
        if (!optionalClient.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Клиент не найден");
            return "redirect:/admin/clients";
        }

        Client client = optionalClient.get();

        // Проверка уникальности username
        if (usernames != null) {
            for (int i = 0; i < usernames.size(); i++) {
                String username = usernames.get(i);
                Long userId = (userIds != null && i < userIds.size()) ? userIds.get(i) : null;

                User existingUser = userService.findByUsername(username);
                if (existingUser != null && (userId == null || !existingUser.getId().equals(userId))) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Пользователь с логином '" + username + "' уже существует");
                    return "redirect:/admin/editClient/" + id;
                }
            }
        }

        // Проверка email для администраторов
        if (isAdmins != null && userEmails != null) {
            for (int i = 0; i < isAdmins.size(); i++) {
                if (Boolean.parseBoolean(isAdmins.get(i))) {
                    String email = userEmails.get(i);
                    if (email == null || email.isEmpty()) {
                        redirectAttributes.addFlashAttribute("errorMessage",
                                "Для администратора необходимо указать email");
                        return "redirect:/admin/editClient/" + id;
                    }

                    Long userId = (userIds != null && i < userIds.size()) ? userIds.get(i) : null;
                    User existingUser = userService.findByEmail(email);
                    if (existingUser != null && (userId == null || !existingUser.getId().equals(userId))) {
                        redirectAttributes.addFlashAttribute("errorMessage",
                                "Пользователь с email '" + email + "' уже существует");
                        return "redirect:/admin/editClient/" + id;
                    }
                }
            }
        }

        // Обновление основной информации клиента
        client.setName(name);
        client.setInn(inn);
        client.setKpp(kpp);
        client.setUraddress(uraddress);
        client.setFactaddress(factaddress);
        client.setSum1(sum1);
        client.setSum2(sum2);

        Price selectedPrice = priceService.findById(selectedPriceId).orElse(null);
        client.setSelectedPrice(selectedPrice);

        List<Addresses> existingAddresses = client.getAddresses();
        if (postalcodes != null && !postalcodes.isEmpty()) {
            for (int i = 0; i < postalcodes.size(); i++) {
                Addresses address = (i < existingAddresses.size()) ? existingAddresses.get(i) : new Addresses();
                address.setPostalcode(Integer.parseInt(postalcodes.get(i)));
                address.setCountry(countries.get(i));
                address.setRegion(regions.get(i));
                address.setRayon(rayons.get(i));
                address.setCity(cities.get(i));
                address.setStreet(streets.get(i));
                address.setHome(homes.get(i));
                address.setRoomnumber(roomnumbers.get(i));
                address.setSchedule(schedules != null && i < schedules.size() ? schedules.get(i) : null);
                address.setClient(client);

                if (contactIds != null && i < contactIds.size()) {
                    Long contactId = contactIds.get(i);
                    if (contactId != null) {
                        Contacts contact = contactsService.findById(contactId).orElse(null);
                        address.setContact(contact);
                    }
                }

                if (i >= existingAddresses.size()) {
                    existingAddresses.add(address);
                }
            }
        }

        // Обработка контактов
        List<Contacts> existingContacts = client.getContacts();
        if (contactNames != null && !contactNames.isEmpty()) {
            for (int i = 0; i < contactNames.size(); i++) {
                Contacts contact = (i < existingContacts.size()) ? existingContacts.get(i) : new Contacts();
                contact.setContactType(typeContacts.get(i));
                contact.setName(contactNames.get(i));
                contact.setPhonenumber(phonenumbers.get(i));
                contact.setEmail(emails.get(i));
                contact.setClient(client);
                if (i >= existingContacts.size()) {
                    existingContacts.add(contact);
                }
            }
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        List<User> existingUsers = client.getUsers();
        if (usernames != null && !usernames.isEmpty()) {
            for (int i = 0; i < usernames.size(); i++) {
                User user = (i < existingUsers.size()) ? existingUsers.get(i) : new User();
                user.setUsername(usernames.get(i));
                user.setName(nameuser.get(i));

                if (passwords != null && i < passwords.size() && !passwords.get(i).isEmpty()) {
                    user.setPassword(passwordEncoder.encode(passwords.get(i)));
                } else if (user.getId() == null) {
                    user.setPassword(passwordEncoder.encode("defaultPassword"));
                }

                boolean isAdmin = isAdmins != null && isAdmins.size() > i && Boolean.parseBoolean(isAdmins.get(i));
                user.setAdmin(isAdmin);

                if (isAdmin && userEmails != null && userEmails.size() > i) {
                    user.setEmail(userEmails.get(i));
                } else {
                    user.setEmail(null);
                }

                user.setClient(client);

                if (i >= existingUsers.size()) {
                    existingUsers.add(user);
                }
            }
        }

        try {
            clientService.save(client);
            redirectAttributes.addFlashAttribute("successMessage", "Изменения клиента успешно сохранены");
            return "redirect:/admin/clientDetails/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при сохранении клиента: " + e.getMessage());
            return "redirect:/admin/editClient/" + id;
        }
    }

    @GetMapping("/api/checkUsername")
    @ResponseBody
    public ResponseEntity<?> checkUsername(
            @RequestParam String username,
            @RequestParam(required = false) Long userId) {

        User existingUser = userService.findByUsername(username);
        Map<String, Object> response = new HashMap<>();

        if (existingUser != null && (userId == null || !existingUser.getId().equals(userId))) {
            response.put("available", false);
            response.put("message", "Пользователь с таким логином уже существует");
        } else {
            response.put("available", true);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/checkEmail")
    @ResponseBody
    public ResponseEntity<?> checkEmail(
            @RequestParam String email,
            @RequestParam(required = false) Long userId) {

        if (email == null || email.isEmpty()) {
            return ResponseEntity.ok(Collections.singletonMap("valid", false));
        }

        User existingUser = userService.findByEmail(email);
        Map<String, Object> response = new HashMap<>();

        if (existingUser != null && (userId == null || !existingUser.getId().equals(userId))) {
            response.put("valid", false);
            response.put("message", "Пользователь с таким email уже существует");
        } else {
            response.put("valid", true);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/deleteUser/{clientId}/{userId}")
    public String deleteUser(@PathVariable Long clientId, @PathVariable Long userId) {
        Optional<Client> optionalClient = clientService.findById(clientId);
        if (optionalClient.isPresent()) {
            Client client = optionalClient.get();
            List<User> users = client.getUsers();
            users.removeIf(user -> user.getId().equals(userId));
            clientService.save(client);
        }
        return "redirect:/admin/editClient/" + clientId;
    }


    @GetMapping("/admin/clientDetails/{id}")
    public String clientDetails(@PathVariable Long id, Model model) {
        Optional<Client> optionalClient = clientService.findById(id);
        if (optionalClient.isPresent()) {
            Client client = optionalClient.get();
            model.addAttribute("client", client);
            return "admin/clientDetails";
        } else {
            return "redirect:/admin/clients";
        }
    }

    @PostMapping("/admin/addProduct")
    public String addProduct(
            @RequestParam String name,
            @RequestParam String sort,
            @RequestParam String tolsh,
            @RequestParam double volume,
            @RequestParam Double length,
            @RequestParam int quantity,
            @RequestParam String description
    ) {
        Product product = new Product();
        product.setName(name);
        product.setSort(sort);
        product.setTolsh(tolsh);
        product.setVolume(volume);
        product.setLength(length);
        product.setQuantity(quantity);
        product.setDescription(description);

        productService.save(product);

        return "redirect:/admin/products";
    }

    @PostMapping("/admin/addDocumentation")
    public String addDocumentation(
            @RequestParam Long productId,
            @RequestParam("documentation") MultipartFile documentationFile,
            @RequestParam String description) throws IOException {
        Product product = productService.findById(productId).orElse(null);
        if (product != null) {
            Documentation documentation = new Documentation();
            documentation.setName(documentationFile.getOriginalFilename());
            documentation.setBytes(documentationFile.getBytes());
            documentation.setDescription(description);
            documentation.setProduct(product);
            documentationService.save(documentation);
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/addProductImages")
    public String addProductImages(
            @RequestParam Long productId,
            @RequestParam("images") List<MultipartFile> images) throws IOException {
        Product product = productService.findById(productId).orElse(null);
        if (product != null) {
            List<ProductImage> imageList = new ArrayList<>();
            if (images != null && !images.isEmpty()) {
                for (MultipartFile image : images) {
                    ProductImage productImage = new ProductImage();
                    productImage.setBytes(image.getBytes());
                    productImage.setProduct(product);
                    imageList.add(productImage);
                }
                productImageService.saveAll(imageList); // Сохраняем все изображения
            }
            productService.updateProductVisibility(productId);
            return "redirect:/admin/dashboard"; // Перенаправляем на страницу dashboard
        }
        return "redirect:/admin/products";
    }


    @PostMapping("/admin/addSum")
    public String addSum(
            @RequestParam Long productId,
            @RequestParam List<Double> summas,
            @RequestParam List<String> periods,
            @RequestParam List<Long> priceIds) {
        Product product = productService.findById(productId).orElse(null);
        if (product != null) {
            for (int i = 0; i < summas.size(); i++) {
                Price price = priceService.findById(priceIds.get(i)).orElse(null);
                if (price != null) {
                    Sum sum = new Sum();
                    sum.setSumma(summas.get(i));
                    sum.setPeriod(java.sql.Date.valueOf(periods.get(i)));
                    sum.setProduct(product);
                    sum.setPrice(price);
                    sumService.save(sum);
                }
            }
        }
        productService.updateProductVisibility(productId);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/orders")
    public String admorders(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endDate,
            Model model
    ) {
        List<Order> orders = orderService.findByFilters(clientId, status, startDate, endDate);
        model.addAttribute("orders", orders);
        model.addAttribute("clients", clientService.findAll());
        model.addAttribute("clientId", clientId);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate != null ? startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : null);
        model.addAttribute("endDate", endDate != null ? endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : null);
        return "admin/orders";
    }

    @GetMapping("/admin/orders/{id}")
    public String admorderDetails(Model model, @PathVariable Long id) {
        List<DocumentType> documentTypes = documentTypeService.findAll();
        Optional<Order> orderOptional = orderService.findById(id);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();

            order.getTrucks().forEach(truck -> {
                double total = truck.getTchOrders().stream()
                        .mapToDouble(t -> t.getPrice() * t.getQuantity())
                        .sum();
                truck.setTotal(total); // Добавьте поле total в OrderTruck
            });

            model.addAttribute("order", order);
            model.addAttribute("documentTypes", documentTypes);
            return "admin/orderDetails";
        }
        return "error";
    }

    @PostMapping("/admin/updateOrderStatus")
    public String updateOrderStatus(
            @RequestParam Long orderId,
            @RequestParam String status,
            RedirectAttributes redirectAttributes,
            Principal principal) {

        Order order = orderService.findById(orderId).orElse(null);
        if (order != null) {
            order.setStatus(status);
            orderService.save(order);
            User user = userService.findByUsername(principal.getName());
            orderService.addStatusHistory(order, status, user);

            // Асинхронная отправка уведомлений
            emailService.sendOrderStatusUpdateEmailAsync(order, status);

            redirectAttributes.addFlashAttribute("successMessage", "Статус заказа успешно обновлен.");
        }
        return "redirect:/admin/orders/" + orderId;
    }

    @PostMapping("/admin/addOrderDocumentation")
    public String addOrderDocumentation(
            @RequestParam Long orderId,
            @RequestParam("documentation") MultipartFile documentationFile,
            @RequestParam Long documentTypeId,
            RedirectAttributes redirectAttributes
    ) throws IOException {
        Order order = orderService.findById(orderId).orElse(null);
        DocumentType documentType = documentTypeService.findById(documentTypeId).orElse(null);
        if (order != null && documentType != null) {
            OrderDocumentation documentation = new OrderDocumentation();
            documentation.setName(documentationFile.getOriginalFilename());
            documentation.setBytes(documentationFile.getBytes());
            documentation.setDocumentType(documentType);
            documentation.setOrder(order);
            orderDocumentationService.save(documentation);
            redirectAttributes.addFlashAttribute("successMessage", "Документ успешно загружен.");
        }
        return "redirect:/admin/orders/" + orderId;
    }
    @PostMapping("/admin/deleteOrderDocumentation")
    public String deleteOrderDocumentation(
            @RequestParam Long orderId,
            @RequestParam Long docId,
            RedirectAttributes redirectAttributes
    ) {
        orderDocumentationService.deleteById(docId);
        redirectAttributes.addFlashAttribute("successMessage", "Документ успешно удален.");
        return "redirect:/admin/orders/" + orderId;
    }

    @GetMapping("/admin/orderStatusHistory")
    public String orderStatusHistory(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endDate,
            Model model) {

        List<OrderStatusHistory> history = orderService.findStatusHistoryByFilters(status, startDate, endDate);
        model.addAttribute("history", history);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate != null ? startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : null);
        model.addAttribute("endDate", endDate != null ? endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : null);

        return "admin/orderStatusHistory";
    }


    @GetMapping("/admin/orderStatusHistory/{id}")
    public String orderStatusHistory(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id).orElse(null);
        if (order != null) {
            List<OrderStatusHistory> history = orderService.getStatusHistory(order);
            model.addAttribute("history", history);
            return "admin/orderStatusHistory";
        } else {
            return "redirect:/admin/orders";
        }
    }
    @GetMapping("/admin/documentTypes")
    public String documentTypes(Model model) {
        model.addAttribute("documentTypes", documentTypeService.findAll());
        model.addAttribute("prices", priceService.findAll());
        return "admin/documentTypes";
    }

    @PostMapping("/admin/addDocumentType")
    public String addDocumentType(@RequestParam String name) {
        DocumentType documentType = new DocumentType(name);
        documentTypeService.save(documentType);
        return "redirect:/admin/documentTypes";
    }
    @PostMapping("/admin/addPrice")
    public String addPrice(@RequestParam String vid, @RequestParam String name) {
        Price price = new Price();
        price.setVid(vid);
        price.setName(name);
        priceService.save(price);
        return "redirect:/admin/documentTypes";
    }
    @GetMapping("/admin/orders/{orderId}/document/{docId}")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable Long orderId,
            @PathVariable Long docId) {
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

    @PostMapping("/admin/updateOrderPrice/{id}")
    public String updateOrderPrice(
            @PathVariable Long id,
            @RequestParam List<Double> prices,
            @RequestParam List<Long> tchOrderIds,
            RedirectAttributes redirectAttributes) {

        if (prices == null || tchOrderIds == null || prices.size() != tchOrderIds.size()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Неверные данные для обновления цен");
            return "redirect:/admin/orders/" + id;
        }

        for (int i = 0; i < tchOrderIds.size(); i++) {
            Double price = prices.get(i);
            if (price == null) {
                continue;
            }

            TCHOrder tchOrder = orderService.findTchOrderById(tchOrderIds.get(i));
            if (tchOrder != null) {
                tchOrder.setPrice(price);
                orderService.saveTchOrder(tchOrder);
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Цены успешно обновлены");
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/admin/applyDiscountToTruck/{orderId}/{truckId}")
    public String applyDiscountToTruck(
            @PathVariable Long orderId,
            @PathVariable Long truckId,
            @RequestParam Double discount,
            RedirectAttributes redirectAttributes) {

        OrderTruck truck = orderTruckRepository.findById(truckId).orElse(null);
        if (truck != null) {
            for (TCHOrder tchOrder : truck.getTchOrders()) {
                if (tchOrder.getOriginalPrice() == null) {
                    tchOrder.setOriginalPrice(tchOrder.getPrice());
                }

                // Применяем скидку к оригинальной цене
                double discountedPrice = tchOrder.getOriginalPrice() * (100 - discount) / 100;
                tchOrder.setPrice(discountedPrice);
                tchOrder.setDiscount(discount);
                orderService.saveTchOrder(tchOrder);
            }
            redirectAttributes.addFlashAttribute("successMessage",
                    "Скидка " + discount + "% успешно применена. Цены обновлены.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Не удалось найти грузовик с ID: " + truckId);
        }
        return "redirect:/admin/orders/" + orderId;
    }

    @Autowired
    private DocumentZAOService documentZAOService;

    @GetMapping("/admin/document")
    public String documents(Model model) {
        List<DocumentZAO> documents = documentZAOService.findAll();
        if (documents == null) {
            throw new RuntimeException("Документы не найдены!");
        }
        model.addAttribute("documents", documents);
        return "admin/documents";
    }

    @PostMapping("/admin/documents/add")
    public String addDocument(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam("document") MultipartFile documentFile) throws IOException {

        try {
            DocumentZAO documentZAO = new DocumentZAO();
            documentZAO.setName(name);
            documentZAO.setDescription(description);
            documentZAO.setDocumentName(documentFile.getOriginalFilename());
            documentZAO.setDocumentBytes(documentFile.getBytes());

            documentZAOService.save(documentZAO);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }

        return "redirect:/admin/document";
    }


    @GetMapping("/admin/documents/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        Optional<DocumentZAO> documentZAO = documentZAOService.findById(id);
        if (documentZAO.isPresent()) {
            DocumentZAO doc = documentZAO.get();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", doc.getDocumentName());
            return new ResponseEntity<>(doc.getDocumentBytes(), headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/admin/documents/delete/{id}")
    public String deleteDocument(@PathVariable Long id) {
        documentZAOService.deleteById(id);
        return "redirect:/admin/document";
    }

    @GetMapping("/admin/editProduct/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Optional<Product> product = productService.findById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            model.addAttribute("prices", priceService.findAll());
            return "admin/editProduct";
        }
        return "redirect:/admin/dashboard";
    }
    @PostMapping("/admin/updateProduct/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String sort,
            @RequestParam String tolsh,
            @RequestParam Double length,
            @RequestParam Double volume,
            @RequestParam int quantity,
            @RequestParam String description,
            @RequestParam(required = false, defaultValue = "false") boolean visible,
            RedirectAttributes redirectAttributes) {

        Product product = productService.findById(id).orElseThrow();
        product.setName(name);
        product.setSort(sort);
        product.setTolsh(tolsh);
        product.setVolume(volume);
        product.setLength(length);
        product.setQuantity(quantity);
        product.setDescription(description);
        if (product.isReadyForDisplay()) {
            product.setVisible(visible);
        } else {
            product.setVisible(false);
        }

        productService.save(product);
        redirectAttributes.addFlashAttribute("successMessage", "Товар успешно обновлен");
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/search")
    public String searchProducts(@RequestParam String query, Model model) {
        List<Product> products = productService.findBySearch().stream()
                .filter(product -> product.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        model.addAttribute("products", products);
        return "admin/product-grid-fragment :: productGrid";
    }
    @PostMapping("/admin/deleteProduct/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Продукт успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении продукта: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/deleteProductImages")
    public String deleteProductImages(
            @RequestParam Long productId,
            @RequestParam List<Long> imageIds,
            RedirectAttributes redirectAttributes) {
        try {
            for (Long imageId : imageIds) {
                productImageService.deleteById(imageId);
            }
            productService.updateProductVisibility(productId);
            redirectAttributes.addFlashAttribute("successMessage", "Фотографии успешно удалены");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении фотографий: " + e.getMessage());
        }
        return "redirect:/admin/editProduct/" + productId;
    }

    @PostMapping("/admin/deleteProductDocumentation")
    public String deleteProductDocumentation(
            @RequestParam Long productId,
            @RequestParam List<Long> docIds,
            RedirectAttributes redirectAttributes) {
        try {
            for (Long docId : docIds) {
                documentationService.deleteById(docId);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Документы успешно удалены");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении документов: " + e.getMessage());
        }
        return "redirect:/admin/editProduct/" + productId;
    }

    @PostMapping("/admin/deleteProductPrices")
    public String deleteProductPrices(
            @RequestParam Long productId,
            @RequestParam List<Long> sumIds,
            RedirectAttributes redirectAttributes) {
        try {
            for (Long sumId : sumIds) {
                sumService.deleteById(sumId);
            }
            productService.updateProductVisibility(productId);
            redirectAttributes.addFlashAttribute("successMessage", "Цены успешно удалены");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении цен: " + e.getMessage());
        }
        return "redirect:/admin/editProduct/" + productId;
    }

    @PostMapping("/admin/addProductImagesEditProducts")
    public String addProductImagesEditProducts(
            @RequestParam Long productId,
            @RequestParam("images") List<MultipartFile> images) throws IOException {
        Product product = productService.findById(productId).orElse(null);
        if (product != null) {
            List<ProductImage> imageList = new ArrayList<>();
            if (images != null && !images.isEmpty()) {
                for (MultipartFile image : images) {
                    ProductImage productImage = new ProductImage();
                    productImage.setBytes(image.getBytes());
                    productImage.setProduct(product);
                    imageList.add(productImage);
                }
                productImageService.saveAll(imageList); // Сохраняем все изображения
            }
            productService.updateProductVisibility(productId);
            return "redirect:/admin/dashboard"; // Перенаправляем на страницу dashboard
        }
        return "redirect:/admin/editProduct/" + productId;
    }

    @PostMapping("/admin/addDocumentationEditProducts")
    public String addDocumentationEditProducts(
            @RequestParam Long productId,
            @RequestParam("documentation") MultipartFile documentationFile,
            @RequestParam String description) throws IOException {
        Product product = productService.findById(productId).orElse(null);
        if (product != null) {
            Documentation documentation = new Documentation();
            documentation.setName(documentationFile.getOriginalFilename());
            documentation.setBytes(documentationFile.getBytes());
            documentation.setDescription(description);
            documentation.setProduct(product);
            documentationService.save(documentation);
        }
        return "redirect:/admin/editProduct/" + productId;
    }

    @PostMapping("/admin/addSumEditProducts")
    public String addSumEditProducts(
            @RequestParam Long productId,
            @RequestParam List<Double> summas,
            @RequestParam List<String> periods,
            @RequestParam List<Long> priceIds) {
        Product product = productService.findById(productId).orElse(null);
        if (product != null) {
            for (int i = 0; i < summas.size(); i++) {
                Price price = priceService.findById(priceIds.get(i)).orElse(null);
                if (price != null) {
                    Sum sum = new Sum();
                    sum.setSumma(summas.get(i));
                    sum.setPeriod(java.sql.Date.valueOf(periods.get(i)));
                    sum.setProduct(product);
                    sum.setPrice(price);
                    sumService.save(sum);
                }
            }
        }
        productService.updateProductVisibility(productId);
        return "redirect:/admin/editProduct/" + productId;
    }
}
