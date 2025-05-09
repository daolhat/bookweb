package com.programing.bookweb.service.impl;

import com.programing.bookweb.dto.CartDTO;
import com.programing.bookweb.dto.CartItemDTO;
import com.programing.bookweb.dto.UserOrder;
import com.programing.bookweb.entity.Order;
import com.programing.bookweb.entity.OrderDetail;
import com.programing.bookweb.entity.Product;
import com.programing.bookweb.entity.User;
import com.programing.bookweb.enums.OrderStatus;
import com.programing.bookweb.enums.PaymentMethod;
import com.programing.bookweb.enums.PaymentStatus;
import com.programing.bookweb.repository.OrderDetailRepository;
import com.programing.bookweb.repository.OrderRepository;
import com.programing.bookweb.repository.ProductRepository;
import com.programing.bookweb.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;

    //lấy toàn bộ đơn hàng
    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    //huỷ một đơn hàng
    @Transactional
    @Override
    public void cancelOrder(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel an already cancelled or delivered order.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);

        for (OrderDetail orderDetail : orderDetails) {
            Product product = orderDetail.getProduct();
            product.setQuantitySold(product.getQuantitySold() - orderDetail.getQuantity());
            product.setQuantity(product.getQuantity() + orderDetail.getQuantity());
            productRepository.save(product);
        }
        orderRepository.save(order);
    }

    //đếm số lượng đơn hàng theo từng người dùng
    @Override
    public Long countOrderByUser(User user) {
        return orderRepository.countByUser(user);
    }

    //cập nhật trạng thái cho đơn hàng
    @Override
    public void setProcessingOrder(Order order) {
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
    }

    @Override
    public void setDeliveringOrder(Order order) {
        order.setStatus(OrderStatus.DELIVERING);
        orderRepository.save(order);
    }

    @Override
    public void setDeliveredOrder(Order order) {
        order.setStatus(OrderStatus.DELIVERED);
        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(order);
    }

    @Override
    public void setReceivedToOrder(Order order) {
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
    }

    @Override
    public long countOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.countByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public BigDecimal getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal result = orderRepository.sumTotalPriceByDateRange(startDate, endDate);
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    public List<Object[]> getTopSpendingUsers(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        return orderRepository.findTopSpendingUsers(startDate, endDate, PageRequest.of(0, limit));
    }

    @Override
    public Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        if (status == null) {
            return orderRepository.findAll(pageable);
        }
        return orderRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Order> getOrdersBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return orderRepository.findAll(pageable);
        }
        return orderRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }

    @Override
    public Page<Order> getOrdersByStatusAndBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (status == null || startDate == null || endDate == null || startDate.isAfter(endDate)){
            if (status != null) {
                return getOrdersByStatus(status, pageable);
            } else if (startDate != null && endDate != null && startDate.isBefore(endDate)) {
                return getOrdersBetween(startDate, endDate, pageable);
            } else {
                return orderRepository.findAll(pageable);
            }
        }
        return orderRepository.findByStatusAndCreatedAtBetween(status, startDate, endDate, pageable);
    }

    @Override
    public Page<Order> getOrderSearch(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            String searchNew = search.trim();
            return orderRepository.findByCodeOrReceiverContainingOrCustomerPhone(searchNew, pageable);
        }
        return orderRepository.findAll(pageable);
    }

    @Override
    public BigDecimal getTotalRevenue() {
        BigDecimal result = orderRepository.sumTotalPrice();
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    public void deleteOrder(Order order) {
        //Cập nhật lại số lượng sản phẩm
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
        for (OrderDetail orderDetail : orderDetails) {
            Product product = orderDetail.getProduct();
            product.setQuantitySold(product.getQuantitySold() - orderDetail.getQuantity());
            product.setQuantity(product.getQuantity() + orderDetail.getQuantity());
            productRepository.save(product);
        }

        orderRepository.deleteById(order.getId());
    }

    @Transactional
    @Override
    public Order createOrder(User user, CartDTO cart, UserOrder userOrder, PaymentMethod paymentMethod) {
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng không được rỗng hoặc null");
        }
        if (userOrder == null) {
            throw new IllegalArgumentException("Thông tin người nhận không được null");
        }
        Order order = new Order();
        order.setUser(user);
        order.setReceiver(userOrder.getFullName());
        order.setEmail(userOrder.getEmail());
        order.setCustomerPhone(userOrder.getPhoneNumber());
        order.setAddressShipping(userOrder.getAddress());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(paymentMethod);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalPrice(cart.totalPrice());

        List<CartItemDTO> cartItems = cart.getCartItems();

        List<Long> productIds = cartItems.stream()
                .map(CartItemDTO::getProductId)
                .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            throw new IllegalArgumentException("Không có sản phẩm nào trong giỏ hàng");
        }

        Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        if (productMap.size() != productIds.size()) {
            List<Long> missingProductIds = productIds.stream()
                    .filter(id -> !productMap.containsKey(id))
                    .collect(Collectors.toList());
            throw new IllegalStateException("Không tìm thấy sản phẩm với ID: " + missingProductIds);
        }
        List<OrderDetail> orderDetails = new ArrayList<>();
        List<Product> productsToUpdate = new ArrayList<>();
        for (CartItemDTO cartItem : cartItems) {
            Product product = productMap.get(cartItem.getProductId());
            if (product == null) {
                throw new IllegalStateException("Sản phẩm với ID " + cartItem.getProductId() + " không tồn tại");
            }
            double discountRate = product.getDiscount() / 100.0;
            double salePrice = product.getPrice() * (1 - discountRate);
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setProduct(product);
            orderDetail.setQuantity(cartItem.getQuantity());
            orderDetail.setPrice(salePrice);
            orderDetail.setOrder(order);
            orderDetails.add(orderDetail);
            //Cập nhật lại sô lượng của sản phẩm trong kho
            product.setQuantitySold(product.getQuantitySold() + cartItem.getQuantity());
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productsToUpdate.add(product);
        }
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setOrderDetails(orderDetails);
        Order savedOrder = orderRepository.save(order);
        productRepository.saveAll(productsToUpdate);
        return savedOrder;
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @Override
    public Long countOrder() {
        return orderRepository.count();
    }

    @Override
    public Page<Order> getAllOrdersByUserPage(User user, Pageable pageable) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
}
