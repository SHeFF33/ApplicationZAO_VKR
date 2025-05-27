package ru.zaomurom.applicationzao.apicontrollers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.zaomurom.applicationzao.models.client.*;
import ru.zaomurom.applicationzao.models.order.*;
import ru.zaomurom.applicationzao.models.prices.ClientsRegion;
import ru.zaomurom.applicationzao.models.prices.Region;
import ru.zaomurom.applicationzao.models.product.*;
import ru.zaomurom.applicationzao.repositories.OrderTruckRepository;
import ru.zaomurom.applicationzao.services.*;
import ru.zaomurom.applicationzao.dto.*;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final ProductService productService;
    private final ProductImageService productImageService;
    private final DocumentationService documentationService;
    private final SumService sumService;
    private final OrderService orderService;
    private final OrderDocumentationService orderDocumentationService;
    private final OrderTruckRepository orderTruckRepository;
    private final ClientService clientService;
    private final ContactsService contactsService;
    private final AddressesService addressesService;
    private final UserService userService;
    private final DocumentTypeService documentTypeService;
    private final EmailService emailService;
    private final RegionService regionService;

    @Autowired
    public ApiController(ProductService productService,
                         ProductImageService productImageService,
                         DocumentationService documentationService,
                         SumService sumService,
                         OrderService orderService,
                         OrderDocumentationService orderDocumentationService,
                         OrderTruckRepository orderTruckRepository,
                         ClientService clientService,
                         ContactsService contactsService,
                         AddressesService addressesService,
                         UserService userService,
                         DocumentTypeService documentTypeService,
                         EmailService emailService,
                         RegionService regionService) {
        this.productService = productService;
        this.productImageService = productImageService;
        this.documentationService = documentationService;
        this.sumService = sumService;
        this.orderService = orderService;
        this.orderDocumentationService = orderDocumentationService;
        this.orderTruckRepository = orderTruckRepository;
        this.clientService = clientService;
        this.contactsService = contactsService;
        this.addressesService = addressesService;
        this.userService = userService;
        this.documentTypeService = documentTypeService;
        this.emailService = emailService;
        this.regionService = regionService;
    }
    @PostMapping("/login")
    public ResponseEntity<?> loginApi(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            // Попытка аутентификации
            request.login(loginRequest.getUsername(), loginRequest.getPassword());

            // Получаем аутентифицированного пользователя
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                User user = userService.findByUsername(auth.getName());
                if (user != null) {
                    UserDTO userDTO = convertToUserDTO(user);
                    return ResponseEntity.ok(userDTO);
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        } catch (ServletException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @GetMapping("/current-user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUser(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        if (user != null) {
            return ResponseEntity.ok(convertToUserDTO(user));
        }
        return ResponseEntity.notFound().build();
    }

    // Методы для товаров
    @GetMapping("/products")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productService.findAll();
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/products/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.findById(id);
        return product.map(p -> ResponseEntity.ok(convertToProductDTO(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/products/{id}/images")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductImageDTO>> getProductImages(@PathVariable Long id) {
        Optional<Product> product = productService.findById(id);
        if (product.isPresent()) {
            List<ProductImageDTO> imageDTOs = product.get().getImages().stream()
                    .map(this::convertToProductImageDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(imageDTOs);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/products/{id}/documentations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentationDTO>> getProductDocumentations(@PathVariable Long id) {
        Optional<Product> product = productService.findById(id);
        if (product.isPresent()) {
            List<DocumentationDTO> docDTOs = product.get().getDocumentations().stream()
                    .map(this::convertToDocumentationDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(docDTOs);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/products/{id}/prices")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SumDTO>> getProductPrices(@PathVariable Long id) {
        Optional<Product> product = productService.findById(id);
        if (product.isPresent()) {
            List<SumDTO> sumDTOs = product.get().getSums().stream()
                    .map(this::convertToSumDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(sumDTOs);
        }
        return ResponseEntity.notFound().build();
    }

    // Методы для заказов
    @GetMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<Order> orders = orderService.findAll();
        List<OrderDTO> orderDTOs = orders.stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderService.findById(id);
        return order.map(o -> ResponseEntity.ok(convertToOrderDTO(o)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/orders/client/{clientId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderDTO>> getOrdersByClientId(@PathVariable Long clientId) {
        List<Order> orders = orderService.findByClientId(clientId);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/orders/{id}/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TCHOrderDTO>> getOrderItems(@PathVariable Long id) {
        Optional<Order> order = orderService.findById(id);
        if (order.isPresent()) {
            List<TCHOrderDTO> tchOrderDTOs = order.get().getTchOrders().stream()
                    .map(this::convertToTCHOrderDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(tchOrderDTOs);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/orders/{id}/trucks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderTruckDTO>> getOrderTrucks(@PathVariable Long id) {
        Optional<Order> order = orderService.findById(id);
        if (order.isPresent()) {
            List<OrderTruckDTO> truckDTOs = order.get().getTrucks().stream()
                    .map(this::convertToOrderTruckDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(truckDTOs);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/orders/{id}/documentations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderDocumentationDTO>> getOrderDocumentations(@PathVariable Long id) {
        Optional<Order> order = orderService.findById(id);
        if (order.isPresent()) {
            List<OrderDocumentationDTO> docDTOs = order.get().getDocumentations().stream()
                    .map(this::convertToOrderDocumentationDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(docDTOs);
        }
        return ResponseEntity.notFound().build();
    }

    // Методы для клиентов
    @GetMapping("/clients")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        List<Client> clients = clientService.findAll();
        List<ClientDTO> clientDTOs = clients.stream()
                .map(this::convertToClientDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientDTOs);
    }

    @GetMapping("/clients/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
        Optional<Client> client = clientService.findById(id);
        return client.map(c -> ResponseEntity.ok(convertToClientDTO(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/clients/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClientDTO> getClientByOrderId(@PathVariable Long orderId) {
        Optional<Order> order = orderService.findById(orderId);
        if (order.isPresent() && order.get().getClient() != null) {
            return ResponseEntity.ok(convertToClientDTO(order.get().getClient()));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/clients/{id}/addresses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AddressesDTO>> getClientAddresses(@PathVariable Long id) {
        Optional<Client> client = clientService.findById(id);
        if (client.isPresent()) {
            List<AddressesDTO> addressesDTOs = client.get().getAddresses().stream()
                    .map(this::convertToAddressesDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(addressesDTOs);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/clients/{id}/contacts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ContactsDTO>> getClientContacts(@PathVariable Long id) {
        Optional<Client> client = clientService.findById(id);
        if (client.isPresent()) {
            List<ContactsDTO> contactsDTOs = client.get().getContacts().stream()
                    .map(this::convertToContactsDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(contactsDTOs);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/clients/{id}/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserDTO>> getClientUsers(@PathVariable Long id) {
        Optional<Client> client = clientService.findById(id);
        if (client.isPresent()) {
            List<UserDTO> userDTOs = client.get().getUsers().stream()
                    .map(this::convertToUserDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        }
        return ResponseEntity.notFound().build();
    }

    // Новые методы для DocumentType
    @GetMapping("/documentTypes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentTypeDTO>> getAllDocumentTypes() {
        List<DocumentType> documentTypes = documentTypeService.findAll();
        List<DocumentTypeDTO> documentTypeDTOs = documentTypes.stream()
                .map(this::convertToDocumentTypeDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(documentTypeDTOs);
    }

    @GetMapping("/documentTypes/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentTypeDTO> getDocumentTypeById(@PathVariable Long id) {
        Optional<DocumentType> documentType = documentTypeService.findById(id);
        return documentType.map(dt -> ResponseEntity.ok(convertToDocumentTypeDTO(dt)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/documentTypes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentTypeDTO> createDocumentType(@RequestBody DocumentTypeDTO documentTypeDTO) {
        DocumentType documentType = new DocumentType(documentTypeDTO.getName());
        DocumentType savedDocumentType = documentTypeService.save(documentType);
        return ResponseEntity.ok(convertToDocumentTypeDTO(savedDocumentType));
    }

    // Методы для обновления цен в заказах
    @PutMapping("/orders/{orderId}/updatePrices")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderDTO> updateOrderPrices(
            @PathVariable Long orderId,
            @RequestBody List<TCHOrderPriceUpdateDTO> priceUpdates) {

        Optional<Order> orderOptional = orderService.findById(orderId);
        if (!orderOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Order order = orderOptional.get();

        for (TCHOrderPriceUpdateDTO update : priceUpdates) {
            TCHOrder tchOrder = orderService.findTchOrderById(update.getTchOrderId());
            if (tchOrder != null) {
                tchOrder.setPrice(update.getNewPrice());
                orderService.saveTchOrder(tchOrder);
            }
        }

        // Пересчет итоговой суммы для всех машин в заказе
        for (OrderTruck truck : order.getTrucks()) {
            double total = truck.getTchOrders().stream()
                    .mapToDouble(t -> t.getPrice() * t.getQuantity() * t.getVolume())
                    .sum();
            truck.setTotal(total);
            orderTruckRepository.save(truck);
        }

        return ResponseEntity.ok(convertToOrderDTO(order));
    }

    // Методы для обновления статуса заказа
    @PutMapping("/orders/{orderId}/updateStatus")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String newStatus,
            Principal principal) {

        Optional<Order> orderOptional = orderService.findById(orderId);
        if (!orderOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Order order = orderOptional.get();
        order.setStatus(newStatus);
        Order updatedOrder = orderService.save(order);

        // Добавление записи в историю статусов
        User user = userService.findByUsername(principal.getName());
        orderService.addStatusHistory(order, newStatus, user);

        // Отправка уведомления на email
        emailService.sendOrderStatusUpdateEmailAsync(order, newStatus);

        return ResponseEntity.ok(convertToOrderDTO(updatedOrder));
    }

    @PostMapping(value = "/orders/{orderId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderDocumentationDTO> addOrderDocumentation(
            @PathVariable Long orderId,
            @RequestParam("documentTypeId") Long documentTypeId,
            @RequestParam("name") String name,
            @RequestParam("docnumber") String docnumber,
            @RequestParam("file") MultipartFile file) throws IOException {
        System.out.println("Order ID: " + orderId);
        System.out.println("Document Type ID: " + documentTypeId);
        System.out.println("Name: " + name);
        System.out.println("File: " + (file != null ? file.getOriginalFilename() : "null"));
        System.out.println("File size: " + (file != null ? file.getSize() : "null"));

        Optional<Order> orderOptional = orderService.findById(orderId);
        Optional<DocumentType> documentTypeOptional = documentTypeService.findById(documentTypeId);

        if (!orderOptional.isPresent() || !documentTypeOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        OrderDocumentation documentation = new OrderDocumentation();
        documentation.setName(name);
        documentation.setDocnumber(docnumber);
        documentation.setBytes(file.getBytes());
        documentation.setDocumentType(documentTypeOptional.get());
        documentation.setOrder(orderOptional.get());

        OrderDocumentation savedDoc = orderDocumentationService.save(documentation);
        return ResponseEntity.ok(convertToOrderDocumentationDTO(savedDoc));
    }

    @DeleteMapping("/orders/{orderId}/documents/{docId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteOrderDocumentation(
            @PathVariable Long orderId,
            @PathVariable Long docId) {

        orderDocumentationService.deleteById(docId);
        return ResponseEntity.noContent().build();
    }

    private ProductDTO convertToProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSort(product.getSort());
        dto.setTolsh(product.getTolsh());
        dto.setLength(product.getLength());
        dto.setVolume(product.getVolume());
        dto.setQuantity(product.getQuantity());
        dto.setDescription(product.getDescription());
        dto.setVisible(product.isVisible());
        return dto;
    }

    private ProductImageDTO convertToProductImageDTO(ProductImage image) {
        ProductImageDTO dto = new ProductImageDTO();
        dto.setId(image.getId());
        dto.setBytes(image.getBytes()); // Добавлен массив байт изображения
        return dto;
    }

    private DocumentationDTO convertToDocumentationDTO(Documentation doc) {
        DocumentationDTO dto = new DocumentationDTO();
        dto.setId(doc.getId());
        dto.setName(doc.getName());
        dto.setDescription(doc.getDescription());
        dto.setBytes(doc.getBytes()); // Добавлен массив байт документа
        return dto;
    }

    private SumDTO convertToSumDTO(Sum sum) {
        SumDTO dto = new SumDTO();
        dto.setId(sum.getId());
        dto.setSumma(sum.getSumma());
        dto.setPeriod(sum.getPeriod());
        if (sum.getPrice() != null) {
            dto.setPriceId(sum.getPrice().getId());
            dto.setPriceName(sum.getPrice().getName());
        }
        return dto;
    }

    private OrderDTO convertToOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        if (order.getClient() != null) {
            dto.setClientId(order.getClient().getId());
            dto.setClientName(order.getClient().getName());
        }
        if (order.getDeliveryAddress() != null) {
            dto.setDeliveryAddressId(order.getDeliveryAddress().getId());
            dto.setDeliveryAddressString(order.getDeliveryAddress().getCity() + ", " +
                    order.getDeliveryAddress().getStreet() + ", " +
                    order.getDeliveryAddress().getHome());
        }
        dto.setDeliveryDate(order.getDeliveryDate());
        dto.setOrderDate(order.getOrderDate());
        dto.setComment(order.getComment());
        dto.setStatus(order.getStatus());
        return dto;
    }

    private TCHOrderDTO convertToTCHOrderDTO(TCHOrder tchOrder) {
        TCHOrderDTO dto = new TCHOrderDTO();
        dto.setId(tchOrder.getId());
        if (tchOrder.getProduct() != null) {
            dto.setProductId(tchOrder.getProduct().getId());
            dto.setProductName(tchOrder.getProduct().getName());
        }
        dto.setQuantity(tchOrder.getQuantity());
        dto.setPrice(tchOrder.getPrice());
        dto.setVolume(tchOrder.getVolume());
        dto.setOriginalPrice(tchOrder.getOriginalPrice());
        dto.setDiscount(tchOrder.getDiscount());
        return dto;
    }

    private OrderTruckDTO convertToOrderTruckDTO(OrderTruck truck) {
        OrderTruckDTO dto = new OrderTruckDTO();
        dto.setId(truck.getId());
        dto.setTotal(truck.getTotal());
        return dto;
    }

    private OrderDocumentationDTO convertToOrderDocumentationDTO(OrderDocumentation doc) {
        OrderDocumentationDTO dto = new OrderDocumentationDTO();
        dto.setId(doc.getId());
        dto.setName(doc.getName());
        dto.setDocnumber(doc.getDocnumber());
        dto.setBytes(doc.getBytes());
        if (doc.getDocumentType() != null) {
            dto.setDocumentTypeId(doc.getDocumentType().getId());
            dto.setDocumentTypeName(doc.getDocumentType().getName());
        }
        return dto;
    }


    private ClientDTO convertToClientDTO(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setInn(client.getInn());
        dto.setKpp(client.getKpp());
        dto.setUraddress(client.getUraddress());
        dto.setFactaddress(client.getFactaddress());
        dto.setSum1(client.getSum1());
        dto.setSum2(client.getSum2());
        
        // Convert addresses
        if (client.getAddresses() != null) {
            dto.setAddresses(client.getAddresses().stream()
                    .map(this::convertToAddressesDTO)
                    .collect(Collectors.toList()));
        }
        
        // Convert contacts
        if (client.getContacts() != null) {
            dto.setContacts(client.getContacts().stream()
                    .map(this::convertToContactsDTO)
                    .collect(Collectors.toList()));
        }
        
        // Convert users
        if (client.getUsers() != null) {
            dto.setUsers(client.getUsers().stream()
                    .map(this::convertToUserDTO)
                    .collect(Collectors.toList()));
        }
        // Convert selected price
        if (client.getSelectedPrice() != null) {
            PriceDTO priceDTO = new PriceDTO();
            priceDTO.setId(client.getSelectedPrice().getId());
            priceDTO.setName(client.getSelectedPrice().getName());
            priceDTO.setVid(client.getSelectedPrice().getVid());
            dto.setSelectedPrice(priceDTO);
        }
        
        return dto;
    }

    private AddressesDTO convertToAddressesDTO(Addresses address) {
        AddressesDTO dto = new AddressesDTO();
        dto.setId(address.getId());
        dto.setPostalcode(address.getPostalcode());
        dto.setCountry(address.getCountry());
        if (address.getClientsRegion() != null && address.getClientsRegion().getRegion() != null) {
            dto.setRegionName(address.getClientsRegion().getRegion().getName());
        }
        dto.setRayon(address.getRayon());
        dto.setCity(address.getCity());
        dto.setStreet(address.getStreet());
        dto.setHome(address.getHome());
        dto.setRoomnumber(address.getRoomnumber());
        dto.setSchedule(address.getSchedule());
        if (address.getContact() != null) {
            dto.setContactDTO(convertToContactsDTO(address.getContact()));
        }
        return dto;
    }

    private ContactsDTO convertToContactsDTO(Contacts contact) {
        ContactsDTO dto = new ContactsDTO();
        dto.setId(contact.getId());
        dto.setContactType(contact.getContactType());
        dto.setName(contact.getName());
        dto.setPhonenumber(contact.getPhonenumber());
        dto.setEmail(contact.getEmail());
        return dto;
    }

    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setAdmin(user.isAdmin());
        return dto;
    }

    private DocumentTypeDTO convertToDocumentTypeDTO(DocumentType documentType) {
        DocumentTypeDTO dto = new DocumentTypeDTO();
        dto.setId(documentType.getId());
        dto.setName(documentType.getName());
        return dto;
    }

    private RegionDTO convertToRegionDTO(Region region) {
        RegionDTO dto = new RegionDTO();
        dto.setId(region.getId());
        dto.setName(region.getName());
        return dto;
    }
}
