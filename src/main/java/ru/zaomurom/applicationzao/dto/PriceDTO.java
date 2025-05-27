package ru.zaomurom.applicationzao.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PriceDTO {
    private Long id;
    private String name;
    private String vid;
}