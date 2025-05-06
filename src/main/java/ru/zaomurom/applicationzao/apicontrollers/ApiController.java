package ru.zaomurom.applicationzao.apicontrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.zaomurom.applicationzao.models.client.*;
import ru.zaomurom.applicationzao.models.order.*;
import ru.zaomurom.applicationzao.models.product.*;
import ru.zaomurom.applicationzao.repositories.OrderTruckRepository;
import ru.zaomurom.applicationzao.services.*;
import ru.zaomurom.applicationzao.dto.*;

import java.util.List;
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
                         UserService userService) {
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

    // Методы преобразования в DTO
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
        // Не включаем байты изображения в DTO, чтобы не перегружать ответ
        return dto;
    }

    private DocumentationDTO convertToDocumentationDTO(Documentation doc) {
        DocumentationDTO dto = new DocumentationDTO();
        dto.setId(doc.getId());
        dto.setName(doc.getName());
        dto.setDescription(doc.getDescription());
        // Не включаем байты документа в DTO
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
        if (client.getSelectedPrice() != null) {
            dto.setSelectedPriceId(client.getSelectedPrice().getId());
            dto.setSelectedPriceName(client.getSelectedPrice().getName());
        }
        return dto;
    }

    private AddressesDTO convertToAddressesDTO(Addresses address) {
        AddressesDTO dto = new AddressesDTO();
        dto.setId(address.getId());
        dto.setPostalcode(address.getPostalcode());
        dto.setCountry(address.getCountry());
        dto.setRegion(address.getRegion());
        dto.setRayon(address.getRayon());
        dto.setCity(address.getCity());
        dto.setStreet(address.getStreet());
        dto.setHome(address.getHome());
        dto.setRoomnumber(address.getRoomnumber());
        dto.setSchedule(address.getSchedule());
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
}