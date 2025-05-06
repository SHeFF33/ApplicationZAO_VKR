package ru.zaomurom.applicationzao.dto;

public class OrderDocumentationDTO {
    private Long id;
    private String name;
    private Long orderId;
    private Long documentTypeId;
    private String documentTypeName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getDocumentTypeId() { return documentTypeId; }
    public void setDocumentTypeId(Long documentTypeId) { this.documentTypeId = documentTypeId; }
    public String getDocumentTypeName() { return documentTypeName; }
    public void setDocumentTypeName(String documentTypeName) { this.documentTypeName = documentTypeName; }
}