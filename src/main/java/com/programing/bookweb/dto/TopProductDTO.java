package com.programing.bookweb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopProductDTO {

    private Long id;
    private String title;
    private double price;
    private double discount;
    private Long quantity;

}
