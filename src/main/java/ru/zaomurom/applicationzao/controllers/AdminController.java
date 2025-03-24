package ru.zaomurom.applicationzao.controllers;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.zaomurom.applicationzao.models.DocumentZAO;
import ru.zaomurom.applicationzao.models.client.*;
import ru.zaomurom.applicationzao.models.order.*;
import ru.zaomurom.applicationzao.models.product.Documentation;
import ru.zaomurom.applicationzao.models.product.Product;
import ru.zaomurom.applicationzao.models.product.ProductImage;
import ru.zaomurom.applicationzao.models.product.Sum;
import ru.zaomurom.applicationzao.services.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminController {
    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private AddressesService addressesService;
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
            @RequestParam Long selectedPriceId,
            @RequestParam(required = false) List<String> postalcodes,
            @RequestParam(required = false) List<String> countries,
            @RequestParam(required = false) List<String> regions,
            @RequestParam(required = false) List<String> rayons,
            @RequestParam(required = false) List<String> cities,
            @RequestParam(required = false) List<String> streets,
            @RequestParam(required = false) List<String> homes,
            @RequestParam(required = false) List<String> roomnumbers,
            @RequestParam(required = false) List<String> typeContacts,
            @RequestParam(required = false) List<String> contactNames,
            @RequestParam(required = false) List<String> phonenumbers,
            @RequestParam(required = false) List<String> emails,
            @RequestParam(required = false) List<String> usernames,
            @RequestParam(required = false) List<String> passwords,
            @RequestParam(required = false) List<String> isAdmins,
            @RequestParam(required = false) List<String> nameuser
    ) {
        Client client = new Client();
        client.setName(name);
        client.setInn(inn);
        client.setKpp(kpp);
        client.setUraddress(uraddress);
        client.setFactaddress(factaddress);

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
                User user = new User();
                user.setUsername(usernames.get(i));
                user.setPassword(new BCryptPasswordEncoder().encode(passwords.get(i)));
                user.setAdmin(isAdmins != null && isAdmins.size() > i && Boolean.parseBoolean(isAdmins.get(i)));
                user.setName(nameuser.get(i));
                user.setClient(client);
                usersList.add(user);
            }
        }
        client.setUsers(usersList);

        clientService.save(client);

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
            @RequestParam Long selectedPriceId,
            @RequestParam(required = false) List<String> postalcodes,
            @RequestParam(required = false) List<String> countries,
            @RequestParam(required = false) List<String> regions,
            @RequestParam(required = false) List<String> rayons,
            @RequestParam(required = false) List<String> cities,
            @RequestParam(required = false) List<String> streets,
            @RequestParam(required = false) List<String> homes,
            @RequestParam(required = false) List<String> roomnumbers,
            @RequestParam(required = false) List<String> typeContacts,
            @RequestParam(required = false) List<String> contactNames,
            @RequestParam(required = false) List<String> phonenumbers,
            @RequestParam(required = false) List<String> emails,
            @RequestParam(required = false) List<String> usernames,
            @RequestParam(required = false) List<String> passwords,
            @RequestParam(required = false) List<String> isAdmins,
            @RequestParam(required = false) List<String> nameuser,
            @RequestParam(required = false) List<Long> contactIds
    ) {
        Optional<Client> optionalClient = clientService.findById(id);
        if (optionalClient.isPresent()) {
            Client client = optionalClient.get();
            client.setName(name);
            client.setInn(inn);
            client.setKpp(kpp);
            client.setUraddress(uraddress);
            client.setFactaddress(factaddress);

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

            List<User> existingUsers = client.getUsers();
            if (usernames != null && !usernames.isEmpty()) {
                for (int i = 0; i < usernames.size(); i++) {
                    User user = (i < existingUsers.size()) ? existingUsers.get(i) : new User();
                    user.setUsername(usernames.get(i));
                    user.setPassword(passwords != null && i < passwords.size() ? new BCryptPasswordEncoder().encode(passwords.get(i)) : user.getPassword());
                    user.setAdmin(isAdmins != null && isAdmins.size() > i && Boolean.parseBoolean(isAdmins.get(i)));
                    user.setName(nameuser != null && i < nameuser.size() ? nameuser.get(i) : user.getName());
                    user.setClient(client);
                    if (i >= existingUsers.size()) {
                        existingUsers.add(user);
                    }
                }
            }

            clientService.save(client);
        }
        return "redirect:/admin/clients";
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
            @RequestParam int quantity,
            @RequestParam String description
    ) {
        Product product = new Product();
        product.setName(name);
        product.setSort(sort);
        product.setTolsh(tolsh);
        product.setQuantity(quantity);
        product.setDescription(description);

        productService.save(product);

        return "redirect:/admin/products";
    }

    @PostMapping("/admin/addDocumentation")
    public String addDocumentation(
            @RequestParam Long productId,
            @RequestParam("documentation") MultipartFile documentationFile,
            @RequestParam String description
    ) throws IOException {
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
            @RequestParam("images") List<MultipartFile> images
    ) throws IOException {
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
            }
            product.getImages().addAll(imageList);
            productService.save(product);
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/addSum")
    public String addSum(
            @RequestParam Long productId,
            @RequestParam List<Double> summas,
            @RequestParam List<String> periods,
            @RequestParam List<Long> priceIds
    ) {
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
        Optional<Order> orderOptional = orderService.findById(id);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            model.addAttribute("order", order);
            model.addAttribute("tchOrders", order.getTchOrders()); // Добавьте эту строку
            model.addAttribute("documentTypes", documentTypeService.findAll());
            return "admin/orderDetails";
        } else {
            System.out.println("Order not found for id: " + id);
            return "error";
        }
    }


    @PostMapping("/admin/updateOrderStatus")
    public String updateOrderStatus(
            @RequestParam Long orderId,
            @RequestParam String status,
            RedirectAttributes redirectAttributes,
            Principal principal
    ) {
        Order order = orderService.findById(orderId).orElse(null);
        if (order != null) {
            order.setStatus(status);
            orderService.save(order);
            User user = userService.findByUsername(principal.getName());
            orderService.addStatusHistory(order, status, user);
            Client client = order.getClient();
            for (Contacts contact : client.getContacts()) {
                try {
                    emailService.sendOrderStatusUpdateEmail(contact.getEmail(), order, status);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
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

        for (int i = 0; i < prices.size(); i++) {
            TCHOrder tchOrder = orderService.findTchOrderById(tchOrderIds.get(i));
            if (tchOrder != null) {
                tchOrder.setPrice(prices.get(i));
                orderService.saveTchOrder(tchOrder);
            }
        }
        redirectAttributes.addFlashAttribute("successMessage", "Цены успешно обновлены.");
        return "redirect:/admin/orders/" + id;
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

}
