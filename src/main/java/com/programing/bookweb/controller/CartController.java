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
import com.programing.bookweb.repository.OrderRepository;
import com.programing.bookweb.service.ICartService;
import com.programing.bookweb.service.IOrderService;
import com.programing.bookweb.service.IProductService;
import com.programing.bookweb.service.impl.VNPayServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/cart")
public class CartController extends BaseController{

    private final ICartService cartService;
    private final IProductService productService;
    private final HttpSession session;
    private final IOrderService orderService;
    private final VNPayServiceImpl vnPayService;
    private final OrderRepository orderRepository;


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
        // User curUser = getCurrentUser();
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
                    cartService.clearCart(session);
                    return "redirect:/cart/checkout/order-result?success=true&orderId=" + order.getId();
                } catch (Exception e) {
                    session.setAttribute("orderErrorMessage", e.getMessage());
                    return "redirect:/cart/checkout/order-result?success=false";
                }
            } else if (selectedPaymentMethod == PaymentMethod.ONLINE) {
                try {
                    session.setAttribute("pendingOrder", userOrder);
                    session.setAttribute("paymentMethod", selectedPaymentMethod);
                    CartDTO cart = cartService.getCart(session);
                    int amount = (int) cart.totalPrice();

                    Order order = orderService.createOrder(currentUser, cartService.getCart(session), userOrder, selectedPaymentMethod);

                    String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                    String orderInfo = "Thanh toan don hang #" + order.getId();
                    String vnpayUrl = vnPayService.createOrder(amount, orderInfo, baseUrl);
                    System.out.println("vnpayUrl: " + vnpayUrl);

                    cartService.clearCart(session);
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
    public String vnpayPaymentCallback(HttpServletRequest request, Model model){
        int paymentStatus = vnPayService.orderReturn(request);

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");

        // Get the most recent order for the current user
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            try {
                // Find the user's most recent order
                Pageable pageable = PageRequest.of(0, 1);
                Page<Order> userOrders = orderService.getAllOrdersByUserPage(currentUser, pageable);

                if (!userOrders.isEmpty()) {
                    Order recentOrder = userOrders.getContent().get(0);
                    Long orderId = recentOrder.getId();

                    if (paymentStatus == 1) {
                        // Payment successful - update order payment status
                        recentOrder.setPaymentStatus(PaymentStatus.PAID);
                        orderRepository.save(recentOrder);

                        // Store VNPAY transaction details in session for display
                        session.setAttribute("vnpayTransactionId", transactionId);
                        session.setAttribute("vnpayPaymentTime", paymentTime);

                        return "redirect:/cart/checkout/order-result?success=true&orderId=" + orderId;
                    } else {
                        // Payment failed
                        List<CartItemDTO> cartItemsToRestore = new ArrayList<>();
                        // Get order details to restore cart items
                        recentOrder.getOrderDetails().forEach(orderDetail -> {
                            CartItemDTO item = new CartItemDTO();
                            Product product = orderDetail.getProduct();

                            item.setProductId(product.getId());
                            item.setQuantity(orderDetail.getQuantity());
                            item.setTitle(product.getTitle());
                            item.setAuthor(product.getAuthor());
                            item.setPrice(product.getPrice());
                            double discountRate = product.getDiscount() / 100.0;
                            double salePrice = product.getPrice() * (1 - discountRate);
                            item.setSalePrice(salePrice);
                            item.setImageProduct(product.getImageProduct());

                            cartItemsToRestore.add(item);
                        });
                        // Delete the order
                        try {
                            orderService.deleteOrder(recentOrder);
                        } catch (Exception e) {
                            System.out.println("Failed to delete order: " + e.getMessage());
                        }
                        // Restore cart items
                        CartDTO restoredCart = new CartDTO();
                        restoredCart.setCartItems(cartItemsToRestore);
                        session.setAttribute("cart", restoredCart);
                        session.setAttribute("orderErrorMessage", "Thanh toán không thành công. Mã giao dịch: " +
                                (transactionId != null ? transactionId : "N/A"));
                        return "redirect:/cart/checkout/order-result?success=false";
                    }
                } else {
                    session.setAttribute("orderErrorMessage", "Không tìm thấy thông tin đơn hàng gần đây");
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
                Order order = orderService.getOrderById(orderId);
                model.addAttribute("orderId", order.getCode());
                model.addAttribute("totalAmount", order.getTotalPrice());
                model.addAttribute("paymentMethod", order.getPaymentMethod());
                // Transfer VNPAY transaction details from session to model if they exist
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
        }
        return "user/order-result";
    }
}
