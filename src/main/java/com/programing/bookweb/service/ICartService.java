package com.programing.bookweb.service;

import com.programing.bookweb.dto.CartDTO;
import com.programing.bookweb.dto.CartItemDTO;
import jakarta.servlet.http.HttpSession;

public interface ICartService {

    void addProductToCart(HttpSession session, CartItemDTO cartItem);

    void updateProductQuantity(HttpSession session, Long productId, int quantity);

    void removeProductFromCart(HttpSession session, Long productId);

    CartDTO getCart(HttpSession session);

    void clearCart(HttpSession session);


}
