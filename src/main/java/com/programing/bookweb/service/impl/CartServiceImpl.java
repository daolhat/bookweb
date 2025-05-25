package com.programing.bookweb.service.impl;

import com.programing.bookweb.dto.CartDTO;
import com.programing.bookweb.dto.CartItemDTO;
import com.programing.bookweb.service.ICartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements ICartService {

    @Override
    public void addProductToCart(HttpSession session, CartItemDTO cartItem) {
        if (cartItem.getQuantity() <= 0){
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }
        CartDTO cart = getCart(session);
        List<CartItemDTO> cartItems = cart.getCartItems();
        Optional<CartItemDTO> existingItem = cartItems.stream()
                .filter(item -> item.getProductId().equals(cartItem.getProductId()))
                .findFirst();
        if (existingItem.isPresent()){
            CartItemDTO item = existingItem.get();
            item.setQuantity(item.getQuantity() + cartItem.getQuantity());
        } else {
            cartItems.add(cartItem);
        }
        session.setAttribute("cart", cart);
    }

    @Override
    public void updateProductQuantity(HttpSession session, Long productId, int quantity) {
        if (quantity < 0){
            throw new IllegalArgumentException("Số lượng không phù hợp");
        }
        CartDTO cart = getCart(session);
        List<CartItemDTO> cartItems = cart.getCartItems();
        Optional<CartItemDTO> existingItem = cartItems.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
        existingItem.ifPresent(item -> {
            if (quantity == 0){
                cartItems.remove(item);
            } else {
                item.setQuantity(quantity);
            }
        });
        session.setAttribute("cart", cart);
    }

    @Override
    public void removeProductFromCart(HttpSession session, Long productId) {
        CartDTO cart = getCart(session);
        List<CartItemDTO> cartItems = cart.getCartItems();
        cartItems.removeIf(item -> item.getProductId().equals(productId));
        session.setAttribute("cart", cart);
    }


    @Override
    public CartDTO getCart(HttpSession session) {
        CartDTO cart = (CartDTO) session.getAttribute("cart");
        if (cart == null){
            cart = new CartDTO();
            session.setAttribute("cart", cart);
        }
        return cart;
    }


    @Override
    public void clearCart(HttpSession session) {
        CartDTO cart = getCart(session);
        cart.getCartItems().clear();
        session.setAttribute("cart", cart);
    }

}
