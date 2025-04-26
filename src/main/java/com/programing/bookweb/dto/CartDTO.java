package com.programing.bookweb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {

    private List<CartItemDTO> cartItems = new ArrayList<>();

    public List<CartItemDTO> getCartItems(){
        return cartItems;
    }

    public void setCartItems(List<CartItemDTO> cartItems){
        this.cartItems = cartItems;
    }

    public double totalPrice(){
        double total = 0.0;
        for (CartItemDTO cartItem : cartItems){
            total += cartItem.getTotalPrice();
        }
        return total;
    }
}
