    package ru.zaomurom.applicationzao.controllers;

    import jakarta.mail.MessagingException;
    import org.apache.poi.ss.usermodel.*;
    import org.apache.poi.xssf.usermodel.XSSFWorkbook;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.http.*;
    import org.springframework.orm.ObjectOptimisticLockingFailureException;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;
    import ru.zaomurom.applicationzao.models.DocumentZAO;
    import ru.zaomurom.applicationzao.models.client.*;
    import ru.zaomurom.applicationzao.models.order.*;
    import ru.zaomurom.applicationzao.models.prices.ClientsRegion;
    import ru.zaomurom.applicationzao.models.prices.PricesOnRegions;
    import ru.zaomurom.applicationzao.models.prices.Region;
    import ru.zaomurom.applicationzao.models.product.Documentation;
    import ru.zaomurom.applicationzao.models.product.Product;
    import ru.zaomurom.applicationzao.models.product.ProductImage;
    import ru.zaomurom.applicationzao.models.product.Sum;
    import ru.zaomurom.applicationzao.repositories.OrderTruckRepository;
    import ru.zaomurom.applicationzao.services.*;

    import java.io.ByteArrayOutputStream;
    import java.io.IOException;
    import java.nio.charset.StandardCharsets;
    import java.security.Principal;
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.time.LocalDateTime;
    import java.time.format.DateTimeFormatter;
    import java.util.*;
    import java.util.stream.Collectors;

    import org.apache.poi.ss.usermodel.Workbook;
    import org.apache.poi.ss.usermodel.Sheet;
    import org.apache.poi.ss.usermodel.Cell;
    import org.apache.poi.xssf.usermodel.XSSFWorkbook;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    @Controller
    public class AdminController {
        private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

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
        @Autowired
        private RegionService regionService;
        @Autowired
        private ClientsRegionService clientsRegionService;
        @Autowired
        private PricesOnRegionsService pricesOnRegionsService;

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
            model.addAttribute("regions", regionService.findAll());
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
                @RequestParam(required = false) List<Long> regionIds,
                @RequestParam(required = false) List<String> postalcodes,
                @RequestParam(required = false) List<String> countries,
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

            logger.debug("Received usernames: {}", usernames);
            logger.debug("Received nameuser: {}", nameuser);
            logger.debug("Received passwords: {}", passwords);
            logger.debug("Received isAdmins: {}", isAdmins);
            logger.debug("Received userEmails: {}", userEmails);

            // Проверка уникальности username
            if (usernames != null) {
                for (String username : usernames) {
                    if (userService.existsByUsername(username)) {
                        logger.warn("Username already exists: {}", username);
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
                        String email = userEmails.size() > i ? userEmails.get(i) : null;
                        if (email == null || email.isEmpty()) {
                            logger.warn("Email required for admin user at index: {}", i);
                            redirectAttributes.addFlashAttribute("errorMessage",
                                    "Для администратора необходимо указать email");
                            return "redirect:/admin/addClient";
                        }
                        if (userService.existsByEmail(email)) {
                            logger.warn("Email already exists: {}", email);
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

            client = clientService.save(client);
            logger.debug("Saved client with ID: {}", client.getId());

            // Сохраняем выбранные регионы
            if (regionIds != null && !regionIds.isEmpty()) {
                for (Long regionId : regionIds) {
                    Region region = regionService.findById(regionId).orElse(null);
                    if (region != null) {
                        ClientsRegion clientsRegion = new ClientsRegion();
                        clientsRegion.setClient(client);
                        clientsRegion.setRegion(region);
                        clientsRegionService.save(clientsRegion);
                    }
                }
            }

            List<Addresses> addressesList = new ArrayList<>();
            if (postalcodes != null && !postalcodes.isEmpty()) {
                for (int i = 0; i < postalcodes.size(); i++) {
                    Addresses address = new Addresses();
                    address.setPostalcode(Integer.parseInt(postalcodes.get(i)));
                    address.setCountry(countries.get(i));
                    Region region = regionService.findById(regionIds.get(i)).orElse(null);
                    if (region == null) {
                        logger.warn("Region not found for ID: {}", regionIds.get(i));
                        redirectAttributes.addFlashAttribute("errorMessage",
                                "Регион с ID " + regionIds.get(i) + " не найден");
                        return "redirect:/admin/addClient";
                    }
                    ClientsRegion clientsRegion = new ClientsRegion();
                    clientsRegion.setRegion(region);
                    clientsRegion.setClient(client);
                    clientsRegionService.save(clientsRegion);
                    address.setClientsRegion(clientsRegion);
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
                        logger.warn("Missing nameuser or password at index: {}", i);
                        continue;
                    }

                    User user = new User();
                    user.setUsername(usernames.get(i));
                    user.setPassword(new BCryptPasswordEncoder().encode(passwords.get(i)));
                    boolean isAdmin = isAdmins != null && isAdmins.size() > i && Boolean.parseBoolean(isAdmins.get(i));
                    user.setAdmin(isAdmin);
                    user.setName(nameuser.get(i));

                    if (isAdmin && userEmails != null && userEmails.size() > i && !userEmails.get(i).isEmpty()) {
                        user.setEmail(userEmails.get(i));
                    } else {
                        user.setEmail(null);
                    }

                    user.setClient(client);
                    usersList.add(user);
                    logger.debug("Added user: {}", user.getUsername());
                }
            }
            client.setUsers(usersList);

            try {
                clientService.save(client);
                logger.debug("Client saved successfully with {} users", usersList.size());
                redirectAttributes.addFlashAttribute("successMessage", "Клиент успешно добавлен");
                return "redirect:/admin/clients";
            } catch (Exception e) {
                logger.error("Error saving client: {}", e.getMessage(), e);
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при сохранении клиента: " + e.getMessage());
                return "redirect:/admin/addClient";
            }
        }

        @GetMapping("/admin/editClient/{id}")
        public String editClientForm(@PathVariable Long id, Model model) {
            Optional<Client> optionalClient = clientService.findById(id);
            if (optionalClient.isPresent()) {
                Client client = optionalClient.get();

                List<Region> allRegions = regionService.findAll();
                List<ClientsRegion> clientsRegions = clientsRegionService.findByClient(client);
                List<Region> clientRegions = clientsRegions.stream()
                        .map(ClientsRegion::getRegion)
                        .collect(Collectors.toList());

                model.addAttribute("client", client);
                model.addAttribute("regions", allRegions);
                model.addAttribute("clientRegions", clientRegions);
                return "admin/editClient";
            }
            return "redirect:/admin/clients";
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
                @RequestParam(required = false) List<String> postalcodes,
                @RequestParam(required = false) List<String> countries,
                @RequestParam(required = false) List<Long> regionIds,
                @RequestParam(required = false) List<Long> addressRegionIds,
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

            logger.debug("Starting editClient method for client ID: {}", id);
            
            Optional<Client> optionalClient = clientService.findById(id);
            if (!optionalClient.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Клиент не найден");
                return "redirect:/admin/clients";
            }

            Client client = optionalClient.get();

            // Обновление основной информации клиента
            client.setName(name);
            client.setInn(inn);
            client.setKpp(kpp);
            client.setUraddress(uraddress);
            client.setFactaddress(factaddress);
            client.setSum1(sum1);
            client.setSum2(sum2);

            client = clientService.save(client);

            // Обновляем связи с регионами
            if (regionIds != null) {
                logger.debug("Updating regions for client. Number of regions: {}", regionIds.size());
                clientsRegionService.deleteByClient(client);
                for (Long regionId : regionIds) {
                    Region region = regionService.findById(regionId).orElse(null);
                    if (region != null) {
                        ClientsRegion cr = new ClientsRegion();
                        cr.setClient(client);
                        cr.setRegion(region);
                        clientsRegionService.save(cr);
                    }
                }
            }

            // Обновляем адреса
            if (postalcodes != null && !postalcodes.isEmpty()) {
                logger.debug("Processing {} addresses", postalcodes.size());
                List<Addresses> existingAddresses = client.getAddresses();
                List<Addresses> updatedAddresses = new ArrayList<>();
                
                for (int i = 0; i < postalcodes.size(); i++) {
                    try {
                        Addresses address;
                        // Пытаемся найти существующий адрес
                        if (i < existingAddresses.size()) {
                            address = existingAddresses.get(i);
                        } else {
                            address = new Addresses();
                        }

                        String postalCode = postalcodes.get(i).trim();
                        if (!postalCode.matches("\\d{6}")) {
                            redirectAttributes.addFlashAttribute("errorMessage", 
                                "Почтовый индекс должен содержать 6 цифр");
                            return "redirect:/admin/editClient/" + id;
                        }
                        address.setPostalcode(Integer.parseInt(postalCode));
                        address.setCountry(countries.get(i));
                        
                        // Проверяем и устанавливаем регион
                        Long regionId = addressRegionIds.get(i);
                        logger.debug("Processing address {} with region ID: {}", i, regionId);
                        
                        ClientsRegion clientsRegion = clientsRegionService.findByClientAndRegionId(client, regionId);
                        if (clientsRegion == null) {
                            logger.error("Region not found for client {} and region ID {}", client.getId(), regionId);
                            redirectAttributes.addFlashAttribute("errorMessage", 
                                "Выбранный регион не доступен для данного клиента");
                            return "redirect:/admin/editClient/" + id;
                        }
                        
                        address.setClientsRegion(clientsRegion);
                        address.setRayon(rayons.get(i));
                        address.setCity(cities.get(i));
                        address.setStreet(streets.get(i));
                        address.setHome(homes.get(i));
                        address.setRoomnumber(roomnumbers.get(i));
                        address.setSchedule(schedules.get(i));
                        address.setClient(client);

                        // Устанавливаем контакт, если он указан
                        if (contactIds != null && i < contactIds.size()) {
                            Long contactId = contactIds.get(i);
                            if (contactId != null) {
                                Optional<Contacts> contact = contactsService.findById(contactId);
                                contact.ifPresent(address::setContact);
                            }
                        }

                        updatedAddresses.add(address);
                        logger.debug("Successfully processed address {}", i);
                        
                    } catch (Exception e) {
                        logger.error("Error processing address {}: {}", i, e.getMessage());
                        redirectAttributes.addFlashAttribute("errorMessage", 
                            "Ошибка при обработке адреса: " + e.getMessage());
                        return "redirect:/admin/editClient/" + id;
                    }
                }
                
                // Обновляем список адресов
                client.getAddresses().clear();
                client.getAddresses().addAll(updatedAddresses);
                logger.debug("Updated addresses list size: {}", updatedAddresses.size());
            }

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
                logger.debug("Successfully saved client {}", id);
                redirectAttributes.addFlashAttribute("successMessage", "Изменения клиента успешно сохранены");
                return "redirect:/admin/clientDetails/" + id;
            } catch (Exception e) {
                logger.error("Error saving client {}: {}", id, e.getMessage());
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
        public String addProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
            try {
                Product savedProduct = productService.save(product);
                redirectAttributes.addFlashAttribute("successMessage", "Товар успешно добавлен");
                return "redirect:/admin/products";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при добавлении товара: " + e.getMessage());
                return "redirect:/admin/products";
            }
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
                @RequestParam(required = false) Double summa,
                @RequestParam String period,
                @RequestParam(required = false) String regionName,
                RedirectAttributes redirectAttributes) {
            try {
                Product product = productService.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Продукт не найден"));

                if (summa == null) {
                    // Автоматический расчет цен
                    List<Sum> calculatedSums = sumService.calculateAutomaticPrices(product, java.sql.Date.valueOf(period));
                    redirectAttributes.addFlashAttribute("successMessage", "Цены успешно рассчитаны");
                } else {
                    // Ручное добавление цены
                    Sum sum = new Sum();
                    sum.setSumma(summa);
                    sum.setPeriod(java.sql.Date.valueOf(period));
                    sum.setProduct(product);
                    sum.setRegionName(regionName);
                    sumService.save(sum);
                    redirectAttributes.addFlashAttribute("successMessage", "Цена успешно добавлена");
                }
                productService.updateProductVisibility(productId);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
            }
            return "redirect:/admin/editProduct/" + productId;
        }

        @PostMapping("/admin/calculateAutomaticPrices")
        public String calculateAutomaticPrices(
                @RequestParam Long productId,
                @RequestParam String period,
                RedirectAttributes redirectAttributes) {
            try {
                Product product = productService.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Продукт не найден"));

                List<Sum> calculatedSums = sumService.calculateAutomaticPrices(
                    product, 
                    java.sql.Date.valueOf(period)
                );
                
                if (calculatedSums.isEmpty()) {
                    redirectAttributes.addFlashAttribute("warningMessage", 
                        "Не найдены цены для данной толщины (" + product.getTolsh() + ")");
                } else {
                    redirectAttributes.addFlashAttribute("successMessage", 
                        "Цены успешно рассчитаны для " + calculatedSums.size() + " регионов");
                }
                
                productService.updateProductVisibility(productId);
                
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Ошибка при расчете цен: " + e.getMessage());
            }
            
            return "redirect:/admin/editProduct/" + productId;
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
                @RequestParam(required = false) String docnumber,
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
                documentation.setDocnumber(docnumber);
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
                @RequestParam double tolsh,
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
                @RequestParam Long sumIds,
                RedirectAttributes redirectAttributes) {
            try {
                sumService.deleteById(sumIds);
                productService.updateProductVisibility(productId);
                redirectAttributes.addFlashAttribute("successMessage", "Цена успешно удалена");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Ошибка при удалении цены: " + e.getMessage());
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
                @RequestParam Double summa,
                @RequestParam String period,
                @RequestParam String regionName,
                RedirectAttributes redirectAttributes) {
            try {
                Product product = productService.findById(productId).orElse(null);
                if (product != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date periodDate = dateFormat.parse(period);

                    Sum sum = new Sum();
                    sum.setSumma(summa);
                    sum.setPeriod(periodDate);
                    sum.setProduct(product);
                    sum.setRegionName(regionName);
                    
                    sumService.save(sum);
                    productService.updateProductVisibility(productId);
                    
                    redirectAttributes.addFlashAttribute("successMessage", "Цена успешно добавлена");
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Ошибка при добавлении цены: " + e.getMessage());
            }
            return "redirect:/admin/editProduct/" + productId;
        }

        @GetMapping("/admin/regions")
        public String regions(Model model,
                              @RequestParam(required = false) Long regionId,
                              @RequestParam(required = false) Double thickness) {
            model.addAttribute("regions", regionService.findAll());
            model.addAttribute("clientsRegions", clientsRegionService.findAll());
            model.addAttribute("pricesOnRegions", pricesOnRegionsService.findByFilters(regionId, thickness));
            model.addAttribute("clients", clientService.findAll());
            model.addAttribute("selectedRegionId", regionId);
            model.addAttribute("selectedThickness", thickness);
            return "admin/regions";
        }

        @PostMapping("/admin/addRegion")
        public String addRegion(@RequestParam String name, RedirectAttributes redirectAttributes) {
            if (regionService.existsByName(name)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Регион с таким названием уже существует");
                return "redirect:/admin/regions";
            }
            Region region = new Region();
            region.setName(name);
            regionService.save(region);
            redirectAttributes.addFlashAttribute("successMessage", "Регион успешно добавлен");
            return "redirect:/admin/regions";
        }

        @PostMapping("/admin/deleteRegion/{id}")
        public String deleteRegion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
            try {
                regionService.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Регион успешно удален");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении региона: " + e.getMessage());
            }
            return "redirect:/admin/regions";
        }

        @PostMapping("/admin/addClientsRegion")
        public String addClientsRegion(@RequestParam Long clientId, @RequestParam Long regionId, RedirectAttributes attr) {
            Client client = clientService.findById(clientId).orElse(null);
            Region region = regionService.findById(regionId).orElse(null);
            if (client == null || region == null) {
                attr.addFlashAttribute("errorMessage", "Клиент или регион не найдены");
                return "redirect:/admin/regions";
            }
            ClientsRegion clientsRegion = new ClientsRegion();
            clientsRegion.setClient(client);
            clientsRegion.setRegion(region);
            clientsRegionService.save(clientsRegion);
            attr.addFlashAttribute("successMessage", "Регион клиента успешно добавлен");
            return "redirect:/admin/regions";
        }

        @PostMapping("/admin/deleteClientsRegion/{id}")
        public String deleteClientsRegion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
            try {
                clientsRegionService.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Регион клиента успешно удален");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении региона клиента: " + e.getMessage());
            }
            return "redirect:/admin/regions";
        }

        @PostMapping("/admin/addPricesOnRegions")
        public String addPricesOnRegions(@RequestParam Long regionId, @RequestParam Double thickness,
                                         @RequestParam Double pricePerSquareMeter, RedirectAttributes redirectAttributes) {
            Region region = regionService.findById(regionId).orElse(null);
            if (region == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Регион не найден");
                return "redirect:/admin/regions";
            }
            PricesOnRegions pricesOnRegions = new PricesOnRegions();
            pricesOnRegions.setRegion(region);
            pricesOnRegions.setThickness(thickness);
            pricesOnRegions.setPricePerSquareMeter(pricePerSquareMeter);
            pricesOnRegionsService.save(pricesOnRegions);
            redirectAttributes.addFlashAttribute("successMessage", "Прайс по региону успешно добавлен");
            return "redirect:/admin/regions";
        }

        @PostMapping("/admin/deletePricesOnRegions/{id}")
        public String deletePricesOnRegions(@PathVariable Long id, RedirectAttributes redirectAttributes) {
            try {
                pricesOnRegionsService.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Прайс по региону успешно удален");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении прайса по региону: " + e.getMessage());
            }
            return "redirect:/admin/regions";
        }

        @PostMapping("/admin/importPricesOnRegions")
        public String importPricesOnRegions(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Пожалуйста, выберите файл для загрузки");
                return "redirect:/admin/regions";
            }

            try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> rowIterator = sheet.iterator();

                // Пропускаем заголовок
                if (rowIterator.hasNext()) {
                    rowIterator.next();
                }

                int rowNum = 1;
                int successCount = 0;
                List<String> errorMessages = new ArrayList<>();

                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    rowNum++;

                    try {
                        // Читаем данные из строки
                        Cell idCell = row.getCell(0);
                        Cell regionIdCell = row.getCell(1);
                        Cell thicknessCell = row.getCell(2);
                        Cell priceCell = row.getCell(3);

                        // Проверяем, что ячейки не пустые
                        if (regionIdCell == null || thicknessCell == null || priceCell == null) {
                            errorMessages.add("Строка " + rowNum + ": пустые ячейки");
                            continue;
                        }

                        Long id = idCell != null && idCell.getCellType() == CellType.NUMERIC
                                ? (long) idCell.getNumericCellValue() : null;
                        Long regionId = (long) regionIdCell.getNumericCellValue();
                        Double thickness = thicknessCell.getNumericCellValue();
                        Double pricePerSquareMeter = priceCell.getNumericCellValue();

                        // Проверяем существование региона
                        Region region = regionService.findById(regionId).orElse(null);
                        if (region == null) {
                            errorMessages.add("Строка " + rowNum + ": регион с ID " + regionId + " не найден");
                            continue;
                        }

                        PricesOnRegions price;
                        if (id != null) {
                            // Загружаем существующую запись
                            Optional<PricesOnRegions> existingPrice = pricesOnRegionsService.findById(id);
                            if (existingPrice.isEmpty()) {
                                errorMessages.add("Строка " + rowNum + ": запись с ID " + id + " не найдена");
                                continue;
                            }
                            price = existingPrice.get();
                        } else {
                            // Создаем новую запись
                            price = new PricesOnRegions();
                        }

                        // Обновляем поля
                        price.setRegion(region);
                        price.setThickness(thickness);
                        price.setPricePerSquareMeter(pricePerSquareMeter);

                        // Сохраняем запись
                        try {
                            pricesOnRegionsService.save(price);
                            successCount++;
                        } catch (ObjectOptimisticLockingFailureException e) {
                            errorMessages.add("Строка " + rowNum + ": запись (ID " + id + ") была изменена другой транзакцией");
                        }
                    } catch (Exception e) {
                        errorMessages.add("Строка " + rowNum + ": " + e.getMessage());
                    }
                }

                // Формируем итоговое сообщение
                if (successCount > 0) {
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Импортировано " + successCount + " записей");
                }
                if (!errorMessages.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Ошибки при импорте: " + String.join("; ", errorMessages));
                } else if (successCount == 0) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Ни одна запись не была импортирована");
                }

            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Ошибка при чтении файла: " + e.getMessage());
            }

            return "redirect:/admin/regions";
        }
        @GetMapping("/admin/regions/data")
        @ResponseBody
        public List<Region> getAllRegions() {
            return regionService.findAll();
        }

        @PostMapping("/admin/editClient/{clientId}/addAddress")
        public ResponseEntity<?> addAddress(
                @PathVariable Long clientId,
                @RequestParam String postalcode,
                @RequestParam String country,
                @RequestParam Long regionId,
                @RequestParam(required = false) String rayon,
                @RequestParam String city,
                @RequestParam String street,
                @RequestParam String home,
                @RequestParam String roomnumber,
                @RequestParam(required = false) String schedule,
                @RequestParam(required = false) Long contactId,
                RedirectAttributes redirectAttributes) {

            logger.debug("Adding address for client ID: {}", clientId);

            Optional<Client> optionalClient = clientService.findById(clientId);
            if (!optionalClient.isPresent()) {
                logger.error("Client not found with ID: {}", clientId);
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Клиент не найден"));
            }

            Client client = optionalClient.get();

            try {
                // Проверка почтового индекса
                if (!postalcode.matches("\\d{6}")) {
                    logger.error("Invalid postal code: {}", postalcode);
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Почтовый индекс должен содержать 6 цифр"));
                }

                Addresses address = new Addresses();
                address.setPostalcode(Integer.parseInt(postalcode));
                address.setCountry(country);
                address.setRayon(rayon != null ? rayon : "");
                address.setCity(city);
                address.setStreet(street);
                address.setHome(home);
                address.setRoomnumber(roomnumber);
                address.setSchedule(schedule != null ? schedule : "");
                address.setClient(client);

                // Установка региона
                ClientsRegion clientsRegion = clientsRegionService.findByClientAndRegionId(client, regionId);
                if (clientsRegion == null) {
                    Region region = regionService.findById(regionId).orElse(null);
                    if (region == null) {
                        logger.error("Region not found with ID: {}", regionId);
                        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Регион не найден"));
                    }
                    clientsRegion = new ClientsRegion();
                    clientsRegion.setClient(client);
                    clientsRegion.setRegion(region);
                    clientsRegionService.save(clientsRegion);
                }
                address.setClientsRegion(clientsRegion);

                // Установка контакта, если указан
                if (contactId != null) {
                    Optional<Contacts> contact = contactsService.findById(contactId);
                    if (contact.isPresent()) {
                        address.setContact(contact.get());
                    } else {
                        logger.warn("Contact not found with ID: {}", contactId);
                    }
                }

                // Добавление адреса в список клиента
                client.getAddresses().add(address);
                clientService.save(client);

                logger.debug("Address added successfully for client ID: {}", clientId);
                return ResponseEntity.ok().body(Map.of("success", true));

            } catch (Exception e) {
                logger.error("Error adding address for client ID {}: {}", clientId, e.getMessage());
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Ошибка при добавлении адреса: " + e.getMessage()));
            }
        }
        @PostMapping("/admin/editClient/{clientId}/editAddress")
        public ResponseEntity<?> editAddress(
                @PathVariable Long clientId,
                @RequestParam Long id, // ID адреса
                @RequestParam String postalcode,
                @RequestParam String country,
                @RequestParam Long regionId,
                @RequestParam(required = false) String rayon,
                @RequestParam String city,
                @RequestParam String street,
                @RequestParam String home,
                @RequestParam String roomnumber,
                @RequestParam(required = false) String schedule,
                @RequestParam(required = false) Long contactId,
                RedirectAttributes redirectAttributes) {

            logger.debug("Editing address ID: {} for client ID: {}", id, clientId);

            Optional<Client> optionalClient = clientService.findById(clientId);
            if (!optionalClient.isPresent()) {
                logger.error("Client not found with ID: {}", clientId);
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Клиент не найден"));
            }

            Client client = optionalClient.get();
            Optional<Addresses> optionalAddress = client.getAddresses().stream()
                    .filter(addr -> addr.getId().equals(id))
                    .findFirst();

            if (!optionalAddress.isPresent()) {
                logger.error("Address not found with ID: {}", id);
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Адрес не найден"));
            }

            try {
                Addresses address = optionalAddress.get();

                // Проверка почтового индекса
                if (!postalcode.matches("\\d{6}")) {
                    logger.error("Invalid postal code: {}", postalcode);
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Почтовый индекс должен содержать 6 цифр"));
                }

                address.setPostalcode(Integer.parseInt(postalcode));
                address.setCountry(country);
                address.setRayon(rayon != null ? rayon : "");
                address.setCity(city);
                address.setStreet(street);
                address.setHome(home);
                address.setRoomnumber(roomnumber);
                address.setSchedule(schedule != null ? schedule : "");

                // Установка региона
                ClientsRegion clientsRegion = clientsRegionService.findByClientAndRegionId(client, regionId);
                if (clientsRegion == null) {
                    Region region = regionService.findById(regionId).orElse(null);
                    if (region == null) {
                        logger.error("Region not found with ID: {}", regionId);
                        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Регион не найден"));
                    }
                    clientsRegion = new ClientsRegion();
                    clientsRegion.setClient(client);
                    clientsRegion.setRegion(region);
                    clientsRegionService.save(clientsRegion);
                }
                address.setClientsRegion(clientsRegion);

                // Установка контакта
                if (contactId != null) {
                    Optional<Contacts> contact = contactsService.findById(contactId);
                    if (contact.isPresent()) {
                        address.setContact(contact.get());
                    } else {
                        logger.warn("Contact not found with ID: {}", contactId);
                        address.setContact(null);
                    }
                } else {
                    address.setContact(null);
                }

                clientService.save(client);

                logger.debug("Address ID: {} updated successfully for client ID: {}", id, clientId);
                return ResponseEntity.ok().body(Map.of("success", true));

            } catch (Exception e) {
                logger.error("Error editing address ID {} for client ID {}: {}", id, clientId, e.getMessage());
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Ошибка при редактировании адреса: " + e.getMessage()));
            }
        }
    }
