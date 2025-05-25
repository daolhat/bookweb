package com.programing.bookweb.controller;

import com.programing.bookweb.dto.CartDTO;
import com.programing.bookweb.dto.CartItemDTO;
import com.programing.bookweb.dto.ProductRequest;
import com.programing.bookweb.dto.UserOrder;
import com.programing.bookweb.entity.Order;
import com.programing.bookweb.entity.Product;
import com.programing.bookweb.entity.User;
import com.programing.bookweb.enums.PaymentMethod;
import com.programing.bookweb.enums.PaymentStatus;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping("/cart")
public class CartController extends BaseController {

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
    public ResponseEntity<String> addToCart(@RequestBody ProductRequest request,
                                            RedirectAttributes redirectAttributes) {
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
            double salePrice = existingBook.getPrice() * (1 - discountRate);

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
    public ResponseEntity<String> updateCartItem(@RequestParam Long productId,
                                                 @RequestParam int quantity) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vui lòng đăng nhập để cập nhật giỏ hàng");
        }
        cartService.updateProductQuantity(session, productId, quantity);
        return ResponseEntity.ok("Cart item updated.");
    }


    @GetMapping("/remove-cart-item/{id}")
    public String removeCartItem(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        cartService.removeProductFromCart(session, id);
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
    public String getCheckOut(Model model,
                              RedirectAttributes redirectAttributes) {
        User curUser = getCurrentUser();
        if (curUser == null) {
            redirectAttributes.addFlashAttribute("checkoutRedirect", true);
            return "redirect:/login";
        }
        CartDTO cart = cartService.getCart(session);
        if (cart.getCartItems().isEmpty()) {
            return "redirect:/cart?error=empty_cart";
        }
        for (CartItemDTO itemDTO : cart.getCartItems()) {
            Product product = productService.getProductById(itemDTO.getProductId());
            if (itemDTO.getQuantity() > product.getQuantity()) {
                redirectAttributes.addAttribute("error", "Vượt quá số lượng trong kho");
                return "redirect:/cart?error=insufficient_quantity&productId=" + itemDTO.getProductId();
            }
        }
        model.addAttribute("cart", cart);
        double totalCart = cart.totalPrice();
        model.addAttribute("totalCart", totalCart);

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
    @Transactional
    public String placeOrder(@ModelAttribute("orderPerson") UserOrder userOrder,
                             @RequestParam("paymentMethod") String paymentMethod,
                             HttpServletRequest request) {
        try {
            PaymentMethod selectedPaymentMethod = PaymentMethod.valueOf(paymentMethod);
            User currentUser = getCurrentUser();
            if (selectedPaymentMethod == PaymentMethod.COD) {
                try {
                    Order order = orderService.createOrder(currentUser, cartService.getCart(session), userOrder, selectedPaymentMethod);
                    session.setAttribute("orderResult", order);
                    //cartService.clearCart(session);
                    return "redirect:/cart/checkout/order-result?success=true&orderId=" + order.getId();
                } catch (Exception e) {
                    session.setAttribute("orderErrorMessage", e.getMessage());
                    return "redirect:/cart/checkout/order-result?success=false";
                }
            } else if (selectedPaymentMethod == PaymentMethod.ONLINE) {
                try {
                    session.setAttribute("pendingOrder", userOrder);
                    session.setAttribute("paymentMethod", selectedPaymentMethod);
                    session.setAttribute("payment", paymentMethod);
                    CartDTO cart = cartService.getCart(session);
                    int amount = (int) cart.totalPrice();

                    String c = cart.toString();

                    String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                    String orderInfo = "Thanh toan don hang #" + c;
                    String vnpayUrl = vnPayService.createOrder(amount, orderInfo, baseUrl);

                    return "redirect:" + vnpayUrl;
                } catch (Exception e) {
                    session.setAttribute("vnPayErrorMessage", e.getMessage());
                    return "redirect:/cart/checkout/vnpay-payment?fail";
                }
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

    @GetMapping("/checkout/vnpay-payment")
    public String vnpayPaymentCallback(HttpServletRequest request, Model model) {
        int paymentStatus = vnPayService.orderReturn(request);

        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");

        User currentUser = getCurrentUser();
        if (currentUser != null) {
            try {
                if (paymentStatus == 1) {
                    String paymentMethod = (String) session.getAttribute("payment");
                    PaymentMethod selectedPaymentMethod = PaymentMethod.valueOf(paymentMethod);
                    UserOrder userOrder = (UserOrder) session.getAttribute("pendingOrder");
                    Order order = orderService.createOrder(currentUser, cartService.getCart(session), userOrder, selectedPaymentMethod);

                    session.removeAttribute("payment");
                    session.removeAttribute("pendingOrder");
                    session.removeAttribute("paymentMethod");

                    session.setAttribute("vnpayTransactionId", transactionId);
                    session.setAttribute("vnpayPaymentTime", paymentTime);
                    session.setAttribute("orderResult", order);

                    //cartService.clearCart(session);
                    return "redirect:/cart/checkout/order-result?success=true&orderId=" + order.getId();
                } else {
                    session.setAttribute("orderErrorMessage", "Thanh toán không thành công. Mã giao dịch: " +
                            (transactionId != null ? transactionId : "N/A"));
                    return "redirect:/cart/checkout/order-result?success=false";
                }
            } catch (Exception e) {
                e.printStackTrace();
                session.setAttribute("orderErrorMessage", "Lỗi khi xử lý thanh toán: " + e.getMessage());
                return "redirect:/cart/checkout/order-result?success=false";
            }
        } else {
            session.setAttribute("orderErrorMessage", "Vui lòng đăng nhập để hoàn tất thanh toán");
            return "redirect:/login";
        }
    }


    @GetMapping("/checkout/order-result")
    public String orderResult(@RequestParam(value = "success", defaultValue = "false") boolean isSuccess,
                              @RequestParam(value = "orderId", required = false) Long orderId,
                              Model model) {
        model.addAttribute("isSuccess", isSuccess);
        if (isSuccess && orderId != null) {
            try {
                cartService.clearCart(session);
                Order order = orderService.getOrderById(orderId);
                model.addAttribute("orderCode", order.getCode());
                model.addAttribute("orderId", order.getId());
                model.addAttribute("totalAmount", order.getTotalPrice());
                model.addAttribute("paymentMethod", order.getPaymentMethod());

                PaymentMethod selectedPaymentMethod = (PaymentMethod) session.getAttribute("paymentMethod");
                if (selectedPaymentMethod == PaymentMethod.ONLINE){
                    orderService.setPaidOrder(order);
                }
                session.removeAttribute("paymentMethod");
                if (session.getAttribute("vnpayTransactionId") != null) {
                    model.addAttribute("vnpayTransactionId", session.getAttribute("vnpayTransactionId"));
                    model.addAttribute("vnpayPaymentTime", session.getAttribute("vnpayPaymentTime"));

                    // Clear the session attributes after transferring to model
                    session.removeAttribute("vnpayTransactionId");
                    session.removeAttribute("vnpayPaymentTime");
                }
            } catch (Exception e) {
                model.addAttribute("isSuccess", false);
                model.addAttribute("errorMessage", "Không tìm thấy thông tin đơn hàng");
            }
        } else if (!isSuccess) {
            String errorMessage = (String) session.getAttribute("orderErrorMessage");
            model.addAttribute("errorMessage", errorMessage);
            session.removeAttribute("orderErrorMessage");
            Order order = orderService.getOrderById(orderId);
            try {
                //Order order = (Order) session.getAttribute("orderResult");
                orderService.deleteOrder(order);
            } catch (Exception e) {
                System.out.println("Failed to delete order: " + e.getMessage());
            }
            session.removeAttribute("orderResult");
        }
        return "user/order-result";
    }
}
