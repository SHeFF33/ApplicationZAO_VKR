    package ru.zaomurom.applicationzao.controllers;

    import org.apache.poi.hssf.usermodel.HSSFWorkbook;
    import org.apache.poi.ss.usermodel.*;
    import org.apache.poi.xssf.usermodel.XSSFWorkbook;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.http.*;
import org.springframework.http.ContentDisposition;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;
    import ru.zaomurom.applicationzao.models.DocumentZAO;
    import ru.zaomurom.applicationzao.models.Station;
    import ru.zaomurom.applicationzao.models.Quota;
    import ru.zaomurom.applicationzao.models.client.*;
    import ru.zaomurom.applicationzao.models.order.*;
    import ru.zaomurom.applicationzao.models.prices.ClientsRegion;
import ru.zaomurom.applicationzao.models.prices.PricesOnRegions;
import ru.zaomurom.applicationzao.models.prices.PricesOnRegionsJD;
import ru.zaomurom.applicationzao.models.prices.Region;
    import ru.zaomurom.applicationzao.models.product.*;
    import ru.zaomurom.applicationzao.repositories.OrderTruckRepository;
    import ru.zaomurom.applicationzao.repositories.SumRepository;
    import ru.zaomurom.applicationzao.services.*;

    import java.io.IOException;
    import java.nio.charset.StandardCharsets;
    import java.security.Principal;
    import java.text.SimpleDateFormat;
    import java.time.LocalDateTime;
    import java.time.format.DateTimeFormatter;
    import java.util.*;
    import java.util.concurrent.atomic.AtomicInteger;
    import java.util.stream.Collectors;
    import java.util.Arrays;
    import java.time.LocalDate;

    import org.apache.poi.ss.usermodel.Workbook;
    import org.apache.poi.ss.usermodel.Sheet;
    import org.apache.poi.ss.usermodel.Cell;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    @Controller("adminController")
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
        @Autowired
        private PricesOnRegionsJDService pricesOnRegionsJDService;
        @Autowired
        private AddressesService addressesService;
        @Autowired
        private StationService stationService;
        
        @Autowired
        private SumRepository sumRepository;
        @Autowired
        private QuotaService quotaService;

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
            model.addAttribute("stations", stationService.findAll());
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
                @RequestParam(required = false) List<String> specialRequirements,
                @RequestParam(required = false) List<Long> regionIdsForAddresses,
                @RequestParam(required = false) List<Long> stationIds,
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

            // Добавление адресов
            if (postalcodes != null && !postalcodes.isEmpty()) {
                for (int i = 0; i < postalcodes.size(); i++) {
                    if (postalcodes.get(i) != null && !postalcodes.get(i).trim().isEmpty()) {
                        Addresses address = new Addresses();
                        address.setPostalcode(Integer.parseInt(postalcodes.get(i)));
                        address.setCountry(countries.get(i));
                        address.setRayon(rayons.get(i));
                        address.setCity(cities.get(i));
                        address.setStreet(streets.get(i));
                        address.setHome(homes.get(i));
                        address.setRoomnumber(roomnumbers.get(i));
                        address.setSchedule(schedules.get(i));
                        address.setSpecialRequirements(specialRequirements.get(i));
                        address.setClient(client);
                        
                        // Установка региона
                        if (regionIdsForAddresses != null && i < regionIdsForAddresses.size() && regionIdsForAddresses.get(i) != null) {
                            Region region = regionService.findById(regionIdsForAddresses.get(i)).orElse(null);
                            if (region != null) {
                                ClientsRegion clientsRegion = new ClientsRegion();
                                clientsRegion.setClient(client);
                                clientsRegion.setRegion(region);
                                clientsRegionService.save(clientsRegion);
                                address.setClientsRegion(clientsRegion);
                            }
                        }
                        
                        // Установка станции
                        if (stationIds != null && i < stationIds.size() && stationIds.get(i) != null) {
                            Station station = stationService.findById(stationIds.get(i)).orElse(null);
                            if (station != null) {
                                address.setStation(station);
                            }
                        }
                        
                        addressesService.save(address);
                    }
                }
            }

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
                        .distinct() // Убираем дубликаты
                        .collect(Collectors.toList());

                model.addAttribute("client", client);
                model.addAttribute("regions", allRegions);
                model.addAttribute("clientRegions", clientRegions);
                model.addAttribute("stations", stationService.findAll());
                model.addAttribute("clientStations", stationService.findAll());
                return "admin/editClient";
            }
            return "redirect:/admin/clients";
        }

        @PostMapping("/admin/editClient/{id}")
        @Transactional
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
                @RequestParam(required = false) List<Long> addressStationIds,
                @RequestParam(required = false) List<String> rayons,
                @RequestParam(required = false) List<String> cities,
                @RequestParam(required = false) List<String> streets,
                @RequestParam(required = false) List<String> homes,
                @RequestParam(required = false) List<String> roomnumbers,
                @RequestParam(required = false) List<String> schedules,
                @RequestParam(required = false) List<String> specialRequirements,
                @RequestParam(required = false) List<String> typeContacts,
                @RequestParam(required = false) List<String> contactNames,
                @RequestParam(required = false) List<String> phonenumbers,
                @RequestParam(required = false) List<String> emails,
                @RequestParam(required = false) List<String> usernames,
                @RequestParam(required = false) List<String> passwords,
                @RequestParam(required = false) List<String> isAdmins,
                @RequestParam(required = false) List<String> nameuser,
                @RequestParam(required = false) List<String> userEmails,
                @RequestParam(required = false) List<Long> contactIds,
                @RequestParam(required = false) List<Long> userIds,
                RedirectAttributes redirectAttributes) {

            logger.debug("Starting editClient method for client ID: {}", id);
            logger.debug("Received parameters:");
            logger.debug("  postalcodes: {}", postalcodes);
            logger.debug("  countries: {}", countries);
            logger.debug("  regionIds: {}", regionIds);
            logger.debug("  addressRegionIds: {}", addressRegionIds);
            logger.debug("  rayons: {}", rayons);
            logger.debug("  cities: {}", cities);
            logger.debug("  streets: {}", streets);
            logger.debug("  homes: {}", homes);
            logger.debug("  roomnumbers: {}", roomnumbers);
            logger.debug("  schedules: {}", schedules);
            logger.debug("  specialRequirements: {}", specialRequirements);
            
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

            // Обновление адресов
            if (postalcodes != null && !postalcodes.isEmpty()) {
                // Удаляем старые адреса
                addressesService.deleteByClient(client);
                
                // Добавляем новые адреса
                for (int i = 0; i < postalcodes.size(); i++) {
                    if (postalcodes.get(i) != null && !postalcodes.get(i).trim().isEmpty()) {
                        Addresses address = new Addresses();
                        address.setPostalcode(Integer.parseInt(postalcodes.get(i)));
                        address.setCountry(countries.get(i));
                        address.setRayon(rayons.get(i));
                        address.setCity(cities.get(i));
                        address.setStreet(streets.get(i));
                        address.setHome(homes.get(i));
                        address.setRoomnumber(roomnumbers.get(i));
                        address.setSchedule(schedules.get(i));
                        address.setSpecialRequirements(specialRequirements.get(i));
                        address.setClient(client);
                        
                        // Установка региона
                        if (addressRegionIds != null && i < addressRegionIds.size() && addressRegionIds.get(i) != null) {
                            Region region = regionService.findById(addressRegionIds.get(i)).orElse(null);
                            if (region != null) {
                                ClientsRegion clientsRegion = new ClientsRegion();
                                clientsRegion.setClient(client);
                                clientsRegion.setRegion(region);
                                clientsRegionService.save(clientsRegion);
                                address.setClientsRegion(clientsRegion);
                            }
                        }
                        
                        // Установка станции
                        if (addressStationIds != null && i < addressStationIds.size() && addressStationIds.get(i) != null) {
                            Station station = stationService.findById(addressStationIds.get(i)).orElse(null);
                            if (station != null) {
                                address.setStation(station);
                            }
                        }
                        
                        addressesService.save(address);
                    }
                }
            } else {
                // Если адресов нет, очищаем список
                logger.debug("No addresses provided, clearing address list");
                client.setAddresses(new ArrayList<>());
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
                // Сохраняем клиента (каскадное сохранение должно обработать адреса)
                client = clientService.save(client);
                
                logger.debug("Successfully saved client {} with {} addresses", id, client.getAddresses().size());
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

        @GetMapping("/api/checkInn")
        @ResponseBody
        public ResponseEntity<?> checkInn(
                @RequestParam String inn,
                @RequestParam(required = false) Long clientId) {

            if (inn == null || inn.isEmpty()) {
                return ResponseEntity.ok(Collections.singletonMap("valid", false));
            }

            boolean exists = clientService.findAll().stream()
                    .filter(client -> clientId == null || !client.getId().equals(clientId))
                    .anyMatch(client -> client.getInn().equals(inn));

            Map<String, Object> response = new HashMap<>();
            response.put("valid", !exists);
            if (exists) {
                response.put("message", "Клиент с таким ИНН уже существует");
            }

            return ResponseEntity.ok(response);
        }

        @GetMapping("/api/checkPhone")
        @ResponseBody
        public ResponseEntity<?> checkPhone(
                @RequestParam String phone,
                @RequestParam(required = false) Long contactId) {

            if (phone == null || phone.isEmpty()) {
                return ResponseEntity.ok(Collections.singletonMap("valid", false));
            }

            // Check if phone number exists in contacts
            boolean exists = contactsService.findAll().stream()
                    .filter(contact -> contactId == null || !contact.getId().equals(contactId))
                    .anyMatch(contact -> contact.getPhonenumber().equals(phone));

            Map<String, Object> response = new HashMap<>();
            response.put("valid", !exists);
            if (exists) {
                response.put("message", "Контакт с таким номером телефона уже существует");
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
                List<ClientsRegion> clientsRegions = clientsRegionService.findByClient(client);
                List<Region> clientRegions = clientsRegions.stream()
                        .map(ClientsRegion::getRegion)
                        .distinct() // Убираем дубликаты
                        .collect(Collectors.toList());

                model.addAttribute("client", client);
                model.addAttribute("clientRegions", clientRegions);
                model.addAttribute("stations", stationService.findAll());
                return "admin/clientDetails";
            }
            return "redirect:/admin/clients";
        }

        @PostMapping("/admin/addProduct")
        public String addProduct(@ModelAttribute Product product, @RequestParam("nomenclatureType") String nomenclatureType, RedirectAttributes redirectAttributes) {
            try {
                product.setNomenclatureType(ru.zaomurom.applicationzao.models.product.NomenclatureType.valueOf(nomenclatureType));
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
                @RequestParam("images") List<MultipartFile> images,
                RedirectAttributes redirectAttributes) throws IOException {
            Product product = productService.findById(productId).orElse(null);
            if (product != null) {
                if (!product.canAddMoreImages()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Достигнут лимит в " + Product.MAX_IMAGES + " фотографии для товара");
                    return "redirect:/admin/dashboard";
                }

                int remainingSlots = product.getRemainingImageSlots();
                List<MultipartFile> allowedImages = images.subList(0, Math.min(images.size(), remainingSlots));

                List<ProductImage> imageList = new ArrayList<>();
                if (allowedImages != null && !allowedImages.isEmpty()) {
                    for (MultipartFile image : allowedImages) {
                        ProductImage productImage = new ProductImage();
                        productImage.setBytes(image.getBytes());
                        productImage.setProduct(product);
                        imageList.add(productImage);
                    }
                    productImageService.saveAll(imageList);
                    
                    if (images.size() > remainingSlots) {
                        redirectAttributes.addFlashAttribute("warningMessage", 
                            "Добавлено только " + remainingSlots + " фото, так как достигнут лимит в " + Product.MAX_IMAGES + " фотографии");
                    } else {
                        redirectAttributes.addFlashAttribute("successMessage", "Фотографии успешно добавлены");
                    }
                }
                productService.updateProductVisibility(productId);
                return "redirect:/admin/dashboard";
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
                    // Ищем существующую цену для этого продукта и региона
                    Optional<Sum> existingSum = sumRepository.findByProductAndRegionName(product, regionName);
                    
                    Sum  sum;
                    if (existingSum.isPresent()) {
                        // Обновляем существующую цену
                        sum = existingSum.get();
                        redirectAttributes.addFlashAttribute("successMessage", "Цена успешно обновлена");
                    } else {
                        // Создаем новую цену
                        sum = new Sum();
                        sum.setProduct(product);
                        sum.setRegionName(regionName);
                        redirectAttributes.addFlashAttribute("successMessage", "Цена успешно добавлена");
                    }
                    
                    sum.setSumma(summa);
                    sum.setPeriod(java.sql.Date.valueOf(period));
                    
                    // Определяем тип номенклатуры и устанавливаем соответствующую связь с прайсами
                    boolean isRailway = NomenclatureType.RAILWAY.equals(product.getNomenclatureType());
                    if (isRailway) {
                        // Для ЖД номенклатуры ищем соответствующий ЖД прайс
                        List<PricesOnRegionsJD> jdPrices = pricesOnRegionsJDService.findByThickness(Double.valueOf(product.getTolsh()));
                        if (!jdPrices.isEmpty()) {
                            // Берем первый найденный прайс для данной толщины
                            sum.setPricesOnRegionsJD(jdPrices.get(0));
                        }
                    } else {
                        // Для Авто номенклатуры ищем соответствующий Авто прайс
                        List<PricesOnRegions> autoPrices = pricesOnRegionsService.findByThickness(Double.valueOf(product.getTolsh()));
                        if (!autoPrices.isEmpty()) {
                            // Берем первый найденный прайс для данной толщины
                            sum.setPricesOnRegions(autoPrices.get(0));
                        }
                    }
                    
                    sumService.save(sum);
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
                
                // Определяем тип номенклатуры заказа
                boolean isRailwayOrder = false;
                if (!order.getTchOrders().isEmpty()) {
                    isRailwayOrder = order.getTchOrders().get(0).getProduct().getNomenclatureType() == NomenclatureType.RAILWAY;
                }
                model.addAttribute("isRailwayOrder", isRailwayOrder);
                
                return "admin/orderDetails";
            }
            return "error";
        }

        @PostMapping("/admin/updateOrderStatus")
        public String updateOrderStatus(
                @RequestParam Long orderId,
                @RequestParam String status,
                @RequestParam(name = "adminComment", required = false) String adminComment,
                RedirectAttributes redirectAttributes,
                Principal principal) {

            Order order = orderService.findById(orderId).orElse(null);
            if (order != null) {
                String previousStatus = order.getStatus();
                order.setStatus(status);
                // Ограничим длину комментария на всякий случай
                if (adminComment != null && adminComment.length() > 3000) {
                    adminComment = adminComment.substring(0, 3000);
                }
                order.setAdminComment(adminComment);
                orderService.save(order);
                User user = userService.findByUsername(principal.getName());
                orderService.addStatusHistory(order, status, user);

                // Если статус изменён на "Аннулирован", вернуть объём в квоту клиента
                if ("Аннулирован".equalsIgnoreCase(status) && (previousStatus == null || !"Аннулирован".equalsIgnoreCase(previousStatus))) {
                    double orderVolume = order.getTchOrders().stream()
                            .mapToDouble(t -> t.getVolume() * t.getQuantity())
                            .sum();
                    if (orderVolume > 0) {
                        quotaService.increaseQuotaVolume(order.getClient(), orderVolume);
                    }
                } else if (previousStatus != null && "Аннулирован".equalsIgnoreCase(previousStatus) && !"Аннулирован".equalsIgnoreCase(status)) {
                    // Возврат из аннулированного состояния — заново списать объём из квоты
                    double orderVolume = order.getTchOrders().stream()
                            .mapToDouble(t -> t.getVolume() * t.getQuantity())
                            .sum();
                    if (orderVolume > 0) {
                        quotaService.decreaseQuotaVolumeFlexible(order.getClient(), orderVolume);
                    }
                }

                // Асинхронная отправка уведомлений
                List<String> emailAddresses = orderService.collectOrderEmailAddresses(order);
                emailService.sendOrderStatusUpdateEmailAsync(order, status, emailAddresses);

                redirectAttributes.addFlashAttribute("successMessage", "Статус заказа и комментарий успешно обновлены.");
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
                @RequestParam(required = false) Long orderId,
                @RequestParam(required = false) Long clientId,
                @RequestParam(required = false) String status,
                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endDate,
                Model model) {

            List<OrderStatusHistory> history = orderService.findStatusHistoryByFilters(orderId, clientId, status, startDate, endDate);
            model.addAttribute("history", history);
            model.addAttribute("clients", clientService.findAll());
            model.addAttribute("orderId", orderId);
            model.addAttribute("clientId", clientId);
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
            model.addAttribute("stations", stationService.findAll());
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

        @GetMapping("/admin/orders/{id}/railway-document")
        public ResponseEntity<byte[]> downloadRailwayDocument(@PathVariable Long id) {
            try {
                Order order = orderService.findById(id).orElse(null);
                if (order == null || order.getRailwayDocument() == null) {
                    return ResponseEntity.notFound().build();
                }

                java.nio.file.Path filePath = java.nio.file.Paths.get(order.getRailwayDocument());
                if (!java.nio.file.Files.exists(filePath)) {
                    return ResponseEntity.notFound().build();
                }

                byte[] fileContent = java.nio.file.Files.readAllBytes(filePath);
                String fileName = order.getRailwayDocumentName();
                String mimeType = determineMimeType(fileName);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(mimeType));
                headers.setContentDispositionFormData("attachment", fileName);

                return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
            } catch (Exception e) {
                logger.error("Error downloading railway document for order {}: {}", id, e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
                @RequestParam("nomenclatureType") String nomenclatureType,
                RedirectAttributes redirectAttributes) {

            Product product = productService.findById(id).orElseThrow();
            product.setName(name);
            product.setSort(sort);
            product.setTolsh(tolsh);
            product.setVolume(volume);
            product.setLength(length);
            product.setQuantity(quantity);
            product.setDescription(description);
            product.setNomenclatureType(ru.zaomurom.applicationzao.models.product.NomenclatureType.valueOf(nomenclatureType));
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
                @RequestParam("images") List<MultipartFile> images,
                RedirectAttributes redirectAttributes) throws IOException {
            Product product = productService.findById(productId).orElse(null);
            if (product != null) {
                if (!product.canAddMoreImages()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Достигнут лимит в " + Product.MAX_IMAGES + " фотографии для товара");
                    return "redirect:/admin/editProduct/" + productId;
                }

                int remainingSlots = product.getRemainingImageSlots();
                List<MultipartFile> allowedImages = images.subList(0, Math.min(images.size(), remainingSlots));

                List<ProductImage> imageList = new ArrayList<>();
                if (allowedImages != null && !allowedImages.isEmpty()) {
                    for (MultipartFile image : allowedImages) {
                        ProductImage productImage = new ProductImage();
                        productImage.setBytes(image.getBytes());
                        productImage.setProduct(product);
                        imageList.add(productImage);
                    }
                    productImageService.saveAll(imageList);
                    
                    if (images.size() > remainingSlots) {
                        redirectAttributes.addFlashAttribute("warningMessage", 
                            "Добавлено только " + remainingSlots + " фото, так как достигнут лимит в " + Product.MAX_IMAGES + " фотографии");
                    } else {
                        redirectAttributes.addFlashAttribute("successMessage", "Фотографии успешно добавлены");
                    }
                }
                productService.updateProductVisibility(productId);
                return "redirect:/admin/editProduct/" + productId;
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

                    // Ищем существующую цену для этого продукта и региона
                    Optional<Sum> existingSum = sumRepository.findByProductAndRegionName(product, regionName);
                    
                    Sum sum;
                    if (existingSum.isPresent()) {
                        // Обновляем существующую цену
                        sum = existingSum.get();
                        redirectAttributes.addFlashAttribute("successMessage", "Цена успешно обновлена");
                    } else {
                        // Создаем новую цену
                        sum = new Sum();
                        sum.setProduct(product);
                        sum.setRegionName(regionName);
                        redirectAttributes.addFlashAttribute("successMessage", "Цена успешно добавлена");
                    }
                    
                    sum.setSumma(summa);
                    sum.setPeriod(periodDate);
                    
                    sumService.save(sum);
                    productService.updateProductVisibility(productId);
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
            model.addAttribute("pricesOnRegionsJD", pricesOnRegionsJDService.findByFilters(regionId, thickness));
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

        @PostMapping("/admin/addPricesOnRegionsJD")
        public String addPricesOnRegionsJD(@RequestParam Long regionId, @RequestParam Double thickness,
                                         @RequestParam Double pricePerSquareMeter, RedirectAttributes redirectAttributes) {
            Region region = regionService.findById(regionId).orElse(null);
            if (region == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Регион не найден");
                return "redirect:/admin/regions";
            }
            PricesOnRegionsJD pricesOnRegionsJD = new PricesOnRegionsJD();
            pricesOnRegionsJD.setRegion(region);
            pricesOnRegionsJD.setThickness(thickness);
            pricesOnRegionsJD.setPricePerSquareMeter(pricePerSquareMeter);
            pricesOnRegionsJDService.save(pricesOnRegionsJD);
            redirectAttributes.addFlashAttribute("successMessage", "Прайс ЖД по региону успешно добавлен");
            return "redirect:/admin/regions";
        }

        @PostMapping("/admin/deletePricesOnRegionsJD/{id}")
        public String deletePricesOnRegionsJD(@PathVariable Long id, RedirectAttributes redirectAttributes) {
            try {
                pricesOnRegionsJDService.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Прайс ЖД по региону успешно удален");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении прайса ЖД по региону: " + e.getMessage());
            }
            return "redirect:/admin/regions";
        }

        @PostMapping("/admin/importPricesOnRegions")
        public String importPricesOnRegions(@RequestParam("file") MultipartFile file,
                                            RedirectAttributes redirectAttributes) {
            try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);
                int processedRegions = 0;
                int updatedPrices = 0;

                // Получаем все существующие толщины из номенклатур
                final Set<Double> existingThicknesses = productService.findAll().stream()
                        .map(Product::getTolsh)
                        .collect(Collectors.toSet());

                logger.info("Существующие толщины в номенклатурах: {}", existingThicknesses);

                // Основная обработка
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String regionName = getCellStringValue(row.getCell(0));
                    if (regionName == null || regionName.trim().isEmpty()) {
                        logger.warn("Пустое название региона в строке {}", i+1);
                        continue;
                    }

                    // Находим или создаем регион
                    Region region = regionService.findByName(regionName.trim())
                            .orElseGet(() -> {
                                Region newRegion = new Region(regionName.trim());
                                return regionService.save(newRegion);
                            });
                    processedRegions++;

                    // Обработка цен для 6 мм
                    if (existingThicknesses.contains(6.0)) {
                        if (processPriceCell(row.getCell(1), region, 6.0)) {
                            updatedPrices++;
                        }
                    }

                    // Обработка цен для 8 мм
                    if (existingThicknesses.contains(8.0)) {
                        if (processPriceCell(row.getCell(2), region, 8.0)) {
                            updatedPrices++;
                        }
                    }

                    // Обработка цен для 9-22 мм
                    if (row.getCell(3) != null && row.getCell(3).getCellType() != CellType.BLANK) {
                        try {
                            int price = (int) Math.round(row.getCell(3).getNumericCellValue());
                            for (double thickness = 9.0; thickness <= 22.0; thickness++) {
                                if (existingThicknesses.contains(thickness)) {
                                    updatePrice(region, thickness, price);
                                    updatedPrices++;
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Ошибка обработки цены (9-22 мм) в строке {}: {}", i+1, e.getMessage());
                        }
                    }
                }

                redirectAttributes.addFlashAttribute("successMessage",
                        String.format("Успешно обработано: %d регионов, %d цен",
                                processedRegions, updatedPrices));

            } catch (Exception e) {
                logger.error("Ошибка импорта", e);
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Ошибка импорта: " + e.getMessage());
            }

            return "redirect:/admin/regions";
        }

        @PostMapping("/admin/importPricesOnRegionsJD")
        public String importPricesOnRegionsJD(@RequestParam("file") MultipartFile file,
                                            RedirectAttributes redirectAttributes) {
            try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);
                int processedRegions = 0;
                int updatedPrices = 0;

                // Получаем все существующие толщины из номенклатур типа ЖД
                List<Product> allProducts = productService.findAll();
                logger.info("Всего продуктов в базе: {}", allProducts.size());
                
                List<Product> railwayProducts = allProducts.stream()
                        .filter(product -> NomenclatureType.RAILWAY.equals(product.getNomenclatureType()))
                        .collect(Collectors.toList());
                logger.info("Продуктов типа RAILWAY: {}", railwayProducts.size());
                
                final Set<Double> existingThicknesses = railwayProducts.stream()
                        .map(Product::getTolsh)
                        .collect(Collectors.toSet());

                logger.info("Существующие толщины в ЖД номенклатурах: {}", existingThicknesses);
                
                // Если нет продуктов с типом RAILWAY, используем стандартные толщины
                if (existingThicknesses.isEmpty()) {
                    logger.warn("Не найдено продуктов типа RAILWAY. Используем стандартные толщины для импорта.");
                    existingThicknesses.addAll(Arrays.asList(6.0, 8.0, 9.0, 10.0, 12.0, 15.0, 18.0, 20.0, 22.0));
                }

                // Основная обработка
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String regionName = getCellStringValue(row.getCell(0));
                    if (regionName == null || regionName.trim().isEmpty()) {
                        logger.warn("Пустое название региона в строке {}", i+1);
                        continue;
                    }

                    // Проверяем тип номенклатуры в 5-й колонке (индекс 4)
                    String nomenclatureType = getCellStringValue(row.getCell(4));
                    if (nomenclatureType == null || !"ЖД".equals(nomenclatureType.trim())) {
                        logger.warn("Пропускаем строку {} - тип номенклатуры не ЖД: {}", i+1, nomenclatureType);
                        continue;
                    }

                    // Находим или создаем регион
                    Region region = regionService.findByName(regionName.trim())
                            .orElseGet(() -> {
                                Region newRegion = new Region(regionName.trim());
                                return regionService.save(newRegion);
                            });
                    processedRegions++;

                    // Обработка цен для 6 мм
                    if (existingThicknesses.contains(6.0)) {
                        if (processPriceCellJD(row.getCell(1), region, 6.0)) {
                            updatedPrices++;
                        }
                    }

                    // Обработка цен для 8 мм
                    if (existingThicknesses.contains(8.0)) {
                        if (processPriceCellJD(row.getCell(2), region, 8.0)) {
                            updatedPrices++;
                        }
                    }

                    // Обработка цен для 9-22 мм
                    if (row.getCell(3) != null && row.getCell(3).getCellType() != CellType.BLANK) {
                        try {
                            int price = (int) Math.round(row.getCell(3).getNumericCellValue());
                            for (double thickness = 9.0; thickness <= 22.0; thickness++) {
                                if (existingThicknesses.contains(thickness)) {
                                    updatePriceJD(region, thickness, price);
                                    updatedPrices++;
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Ошибка обработки цены (9-22 мм) в строке {}: {}", i+1, e.getMessage());
                        }
                    }
                }

                redirectAttributes.addFlashAttribute("successMessage",
                        String.format("Успешно обработано ЖД прайсов: %d регионов, %d цен",
                                processedRegions, updatedPrices));

            } catch (Exception e) {
                logger.error("Ошибка импорта ЖД прайсов", e);
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Ошибка импорта ЖД прайсов: " + e.getMessage());
            }

            return "redirect:/admin/regions";
        }

        // Вспомогательный метод для получения строкового значения из ячейки
        private String getCellStringValue(Cell cell) {
            if (cell == null) {
                return null;
            }

            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    }
                    return String.valueOf((int) cell.getNumericCellValue());
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    return cell.getCellFormula();
                default:
                    return null;
            }
        }

        private boolean processPriceCell(Cell cell, Region region, double thickness) {
            if (cell == null || cell.getCellType() == CellType.BLANK) {
                return false;
            }

            try {
                int price = (int) Math.round(cell.getNumericCellValue());
                updatePrice(region, thickness, price);
                return true;
            } catch (Exception e) {
                logger.error("Ошибка обработки цены для толщины {}: {}", thickness, e.getMessage());
                return false;
            }
        }

        private void updatePrice(Region region, double thickness, double price) {
            Optional<PricesOnRegions> existingPrice = pricesOnRegionsService
                    .findByRegionAndThickness(region.getId(), thickness)
                    .stream()
                    .findFirst();

            if (existingPrice.isPresent()) {
                PricesOnRegions priceObj = existingPrice.get();
                priceObj.setPricePerSquareMeter(price);
                pricesOnRegionsService.save(priceObj);
            } else {
                PricesOnRegions newPrice = new PricesOnRegions();
                newPrice.setRegion(region);
                newPrice.setThickness(thickness);
                newPrice.setPricePerSquareMeter(price);
                pricesOnRegionsService.save(newPrice);
            }
        }

        private boolean processPriceCellJD(Cell cell, Region region, double thickness) {
            if (cell == null || cell.getCellType() == CellType.BLANK) {
                return false;
            }

            try {
                int price = (int) Math.round(cell.getNumericCellValue());
                updatePriceJD(region, thickness, price);
                return true;
            } catch (Exception e) {
                logger.error("Ошибка обработки ЖД цены для толщины {}: {}", thickness, e.getMessage());
                return false;
            }
        }

        private void updatePriceJD(Region region, double thickness, double price) {
            Optional<PricesOnRegionsJD> existingPrice = pricesOnRegionsJDService
                    .findByRegionAndThickness(region.getId(), thickness)
                    .stream()
                    .findFirst();

            if (existingPrice.isPresent()) {
                PricesOnRegionsJD priceObj = existingPrice.get();
                priceObj.setPricePerSquareMeter(price);
                pricesOnRegionsJDService.save(priceObj);
            } else {
                PricesOnRegionsJD newPrice = new PricesOnRegionsJD();
                newPrice.setRegion(region);
                newPrice.setThickness(thickness);
                newPrice.setPricePerSquareMeter(price);
                pricesOnRegionsJDService.save(newPrice);
            }
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
                @RequestParam(required = false) String specialRequirements, // Добавлено новое поле
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
                address.setSpecialRequirements(specialRequirements != null ? specialRequirements : "");
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
                @RequestParam(required = false) String specialRequirements,
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
                address.setSpecialRequirements(specialRequirements != null ? specialRequirements : "");

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

        @PostMapping("/admin/updateDocumentType")
        public String updateDocumentType(@RequestParam Long id, @RequestParam String name) {
            Optional<DocumentType> documentTypeOptional = documentTypeService.findById(id);
            if (documentTypeOptional.isPresent()) {
                DocumentType documentType = documentTypeOptional.get();
                documentType.setName(name);
                documentTypeService.save(documentType);
            }
            return "redirect:/admin/documentTypes";
        }

        @PostMapping("/admin/addStation")
        public String addStation(@RequestParam String name, RedirectAttributes redirectAttributes) {
            if (stationService.existsByName(name)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Станция с таким названием уже существует");
                return "redirect:/admin/documentTypes";
            }
            Station station = new Station(name);
            stationService.save(station);
            redirectAttributes.addFlashAttribute("successMessage", "Станция успешно добавлена");
            return "redirect:/admin/documentTypes";
        }

        @PostMapping("/admin/deleteStation")
        public String deleteStation(@RequestParam Long stationId, RedirectAttributes redirectAttributes) {
            try {
                stationService.deleteById(stationId);
                redirectAttributes.addFlashAttribute("successMessage", "Станция успешно удалена");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении станции: " + e.getMessage());
            }
            return "redirect:/admin/documentTypes";
        }

        @PostMapping("/admin/updateStation")
        public String updateStation(@RequestParam Long id, @RequestParam String name, RedirectAttributes redirectAttributes) {
            try {
                Optional<Station> existingStation = stationService.findById(id);
                if (!existingStation.isPresent()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Станция не найдена");
                    return "redirect:/admin/documentTypes";
                }

                Station station = existingStation.get();
                String newName = name.trim();
                
                // Проверяем, что новое имя не конфликтует с существующими станциями
                if (!newName.equals(station.getName()) && stationService.existsByName(newName)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Станция с таким названием уже существует");
                    return "redirect:/admin/documentTypes";
                }

                station.setName(newName);
                stationService.save(station);
                redirectAttributes.addFlashAttribute("successMessage", "Станция успешно обновлена");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении станции: " + e.getMessage());
            }
            return "redirect:/admin/documentTypes";
        }

        @PostMapping("/admin/importStations")
        public String importStations(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
            try {
                if (file.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Файл не выбран");
                    return "redirect:/admin/documentTypes";
                }

                Workbook workbook;
                if (file.getOriginalFilename().endsWith(".xlsx")) {
                    workbook = new XSSFWorkbook(file.getInputStream());
                } else {
                    workbook = new HSSFWorkbook(file.getInputStream());
                }

                Sheet sheet = workbook.getSheetAt(0);
                int addedCount = 0;
                int skippedCount = 0;

                // Начинаем с первой строки (пропускаем заголовок)
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    Cell cell = row.getCell(0); // Первая колонка - название станции
                    if (cell == null || cell.getCellType() == CellType.BLANK) continue;

                    String stationName = getCellStringValue(cell).trim();
                    if (stationName.isEmpty()) continue;

                    // Проверяем, существует ли станция с таким именем
                    if (!stationService.existsByName(stationName)) {
                        Station station = new Station(stationName);
                        stationService.save(station);
                        addedCount++;
                    } else {
                        skippedCount++;
                    }
                }

                workbook.close();
                
                String message = String.format("Импорт завершен. Добавлено: %d, пропущено: %d", addedCount, skippedCount);
                redirectAttributes.addFlashAttribute("successMessage", message);
                
            } catch (Exception e) {
                logger.error("Ошибка при импорте станций: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при импорте: " + e.getMessage());
            }
            
            return "redirect:/admin/documentTypes";
        }

        // Методы для работы с квотами
        @GetMapping("/admin/quotas")
        public String listQuotas(Model model) {
            List<Quota> quotas = quotaService.findAll();
            List<Client> clients = clientService.findAll();
            model.addAttribute("quotas", quotas);
            model.addAttribute("clients", clients);
            return "admin/quotas";
        }

        @PostMapping("/admin/quotas/add")
        public String addQuota(@RequestParam Long clientId,
                               @RequestParam String startDate,
                               @RequestParam String endDate,
                               @RequestParam double allowedVolume) {
            Client client = clientService.findById(clientId).orElseThrow();
            Quota quota = new Quota(LocalDate.parse(startDate), LocalDate.parse(endDate), client, allowedVolume);
            quotaService.save(quota);
            return "redirect:/admin/quotas";
        }

        @PostMapping("/admin/quotas/addVolume")
        public String addVolume(@RequestParam Long quotaId, @RequestParam double additionalVolume) {
            quotaService.addVolume(quotaId, additionalVolume);
            return "redirect:/admin/quotas";
        }

        @PostMapping("/admin/quotas/delete")
        public String deleteQuota(@RequestParam Long quotaId) {
            quotaService.deleteById(quotaId);
            return "redirect:/admin/quotas";
        }
    }
