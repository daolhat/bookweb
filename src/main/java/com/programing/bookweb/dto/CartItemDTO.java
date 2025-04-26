package com.programing.bookweb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemDTO {

    private Long productId;
    private String imageProduct;
    private String title;
    private String author;
    private double price;
    private double salePrice;
    private int quantity;

    public double getTotalPrice(){
        return salePrice * quantity;
    }
}
