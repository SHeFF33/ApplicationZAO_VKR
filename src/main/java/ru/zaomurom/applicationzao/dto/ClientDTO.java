package ru.zaomurom.applicationzao.dto;

public class ClientDTO {
    private Long id;
    private String name;
    private String inn;
    private String kpp;
    private String uraddress;
    private String factaddress;
    private Double sum1;
    private Double sum2;
    private Long selectedPriceId;
    private String selectedPriceName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getInn() { return inn; }
    public void setInn(String inn) { this.inn = inn; }
    public String getKpp() { return kpp; }
    public void setKpp(String kpp) { this.kpp = kpp; }
    public String getUraddress() { return uraddress; }
    public void setUraddress(String uraddress) { this.uraddress = uraddress; }
    public String getFactaddress() { return factaddress; }
    public void setFactaddress(String factaddress) { this.factaddress = factaddress; }
    public Double getSum1() { return sum1; }
    public void setSum1(Double sum1) { this.sum1 = sum1; }
    public Double getSum2() { return sum2; }
    public void setSum2(Double sum2) { this.sum2 = sum2; }
    public Long getSelectedPriceId() { return selectedPriceId; }
    public void setSelectedPriceId(Long selectedPriceId) { this.selectedPriceId = selectedPriceId; }
    public String getSelectedPriceName() { return selectedPriceName; }
    public void setSelectedPriceName(String selectedPriceName) { this.selectedPriceName = selectedPriceName; }
}