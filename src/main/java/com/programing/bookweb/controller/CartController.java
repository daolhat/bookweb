package com.programing.bookweb.controller;

import com.programing.bookweb.dto.CartDTO;
import com.programing.bookweb.dto.CartItemDTO;
import com.programing.bookweb.dto.ProductRequest;
import com.programing.bookweb.dto.UserOrder;
import com.programing.bookweb.entity.Order;
import com.programing.bookweb.entity.Product;
import com.programing.bookweb.entity.User;
import com.programing.bookweb.enums.PaymentMethod;
import com.programing.bookweb.service.ICartService;
import com.programing.bookweb.service.IOrderService;
import com.programing.bookweb.service.IProductService;
import com.programing.bookweb.service.impl.VNPayServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping("/cart")
public class CartController extends BaseController{

    private final ICartService cartService;
    private final IProductService productService;
    private final HttpSession session;
    private final IOrderService orderService;
    private final VNPayServiceImpl vnPayService;


    @GetMapping
    public String getCartPage(Model model) {
        CartDTO cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
        double totalCart = cart.totalPrice();
        model.addAttribute("totalCart", totalCart);
        return "user/cart";
    }


    @PostMapping("/add-to-cart")
    public ResponseEntity<String> addToCart(@RequestBody ProductRequest request) {
        // Kiểm tra người dùng đã đăng nhập chưa
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng");
        }

        try {
            long productId = request.getProductId();
            int quantity = request.getQuantity();

            CartItemDTO addedItem = new CartItemDTO();
            addedItem.setQuantity(quantity);
            addedItem.setProductId(productId);
            Product existingBook = productService.getProductById(productId);
            addedItem.setTitle(existingBook.getTitle());
            addedItem.setAuthor(existingBook.getAuthor());

            double discountRate = existingBook.getDiscount() / 100.0;
            double salePrice = existingBook.getPrice() * ( 1 - discountRate);

            addedItem.setPrice(existingBook.getPrice());
            addedItem.setSalePrice(salePrice);
            addedItem.setImageProduct(existingBook.getImageProduct());
            cartService.addProductToCart(session, addedItem);

            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }


    @PostMapping("/update-cart-item")
    @ResponseBody
    public ResponseEntity<String> updateCartItem(@RequestParam Long productId, @RequestParam int quantity) {
        // Kiểm tra người dùng đã đăng nhập chưa
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập để cập nhật giỏ hàng");
        }
        cartService.updateProductQuantity(session, productId, quantity);
        return ResponseEntity.ok("Cart item updated.");
    }


    @GetMapping("/remove-cart-item/{productId}")
    public String removeCartItem(@PathVariable Long productId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        cartService.removeProductFromCart(session, productId);
        return "redirect:/cart";
    }


    @GetMapping("/cart-item-count")
    @ResponseBody
    public int getCartItemCount() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return 0;
        }
        return cartService.getCart(session).getCartItems().size();
    }


    @GetMapping("/checkout")
    public String getCheckOut(Model model, RedirectAttributes redirectAttributes) {
        // Kiểm tra người dùng đã đăng nhập chưa
        User curUser = getCurrentUser();
        if (curUser == null) {
            // Nếu chưa đăng nhập, chuyển hướng đến trang đăng nhập
            redirectAttributes.addFlashAttribute("checkoutRedirect", true);
            return "redirect:/login";
        }

        CartDTO cart = cartService.getCart(session);

        if (cart.getCartItems().isEmpty()) {
            return "redirect:/cart?error=empty_cart";
        }

        for (CartItemDTO itemDTO : cart.getCartItems()){
            Product product = productService.getProductById(itemDTO.getProductId());
            if(itemDTO.getQuantity() > product.getQuantity()){
                redirectAttributes.addAttribute("error", "Vượt quá số lượng trong kho");
                return "redirect:/cart?error=insufficient_quantity&productId=" + itemDTO.getProductId();
            }
        }

        model.addAttribute("cart", cart);
        double totalCart = cart.totalPrice();
        model.addAttribute("totalCart", totalCart);

//        User curUser = getCurrentUser();
        UserOrder orderPerson = new UserOrder();
        orderPerson.setFullName(curUser.getFullName());
        orderPerson.setEmail(curUser.getEmail());
        orderPerson.setPhoneNumber(curUser.getPhoneNumber());
        orderPerson.setAddress(curUser.getAddress());
        model.addAttribute("orderPerson", orderPerson);
        // Thêm danh sách phương thức thanh toán vào model
        model.addAttribute("paymentMethods", PaymentMethod.values());

        return "user/checkout";
    }


    @PostMapping("/place-order")
    public String placeOrder(@ModelAttribute("orderPerson") UserOrder userOrder,
                             @RequestParam("paymentMethod") String paymentMethod) {
        try {
            PaymentMethod selectedPaymentMethod = PaymentMethod.valueOf(paymentMethod);
            User currentUser = getCurrentUser();
            if (selectedPaymentMethod == PaymentMethod.COD) {
                try {
                    Order order = orderService.createOrder(currentUser, cartService.getCart(session), userOrder, selectedPaymentMethod);
                    cartService.clearCart(session);
                    return "redirect:/cart/checkout/order-result?success=true&orderId=" + order.getId();
                } catch (Exception e) {
                    session.setAttribute("orderErrorMessage", e.getMessage());
                    return "redirect:/cart/checkout/order-result?success=false";
                }
            } else if (selectedPaymentMethod == PaymentMethod.ONLINE) {
                session.setAttribute("pendingOrder", userOrder);
                session.setAttribute("paymentMethod", selectedPaymentMethod);
                CartDTO cart = cartService.getCart(session);
                double amount = cart.totalPrice();
                return "redirect:/payment/create-payment?amount=" + amount + "&orderInfo=Thanh toan don hang Book.com";
            } else {
                session.setAttribute("orderErrorMessage", "Phương thức thanh toán không hợp lệ");
                return "redirect:/cart/checkout/order-result?success=false";
            }
        } catch (IllegalArgumentException e) {
            session.setAttribute("orderErrorMessage", "Phương thức thanh toán không hợp lệ");
            return "redirect:/cart/checkout/order-result?success=false";
        } catch (Exception e) {
            session.setAttribute("orderErrorMessage", e.getMessage());
            return "redirect:/cart/checkout/order-result?success=false";
        }
    }

    @GetMapping("checkout/vnpay-payment")
    public String GetMapping(HttpServletRequest request, Model model){
        int paymentStatus =vnPayService.orderReturn(request);

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");

        model.addAttribute("orderId", orderInfo);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("paymentTime", paymentTime);
        model.addAttribute("transactionId", transactionId);

        return paymentStatus == 1 ? "ordersuccess" : "orderfail";
    }

    @GetMapping("/checkout/order-result")
    public String orderResult(@RequestParam(value = "success", defaultValue = "false") boolean isSuccess,
                              @RequestParam(value = "orderId", required = false) Long orderId,
                              Model model) {
        model.addAttribute("isSuccess", isSuccess);
        if (isSuccess && orderId != null) {
            try {
                Order order = orderService.getOrderById(orderId);
                model.addAttribute("orderId", orderId);
                model.addAttribute("totalAmount", order.getTotalPrice());
                model.addAttribute("paymentMethod", order.getPaymentMethod());
            } catch (Exception e) {
                model.addAttribute("isSuccess", false);
                model.addAttribute("errorMessage", "Không tìm thấy thông tin đơn hàng");
            }
        } else if (!isSuccess) {
            String errorMessage = (String) session.getAttribute("orderErrorMessage");
            model.addAttribute("errorMessage", errorMessage);
            session.removeAttribute("orderErrorMessage");
        }
        return "user/order-result";
    }
}
