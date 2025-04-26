package com.programing.bookweb.service;

import com.programing.bookweb.entity.Order;
import com.programing.bookweb.entity.OrderDetail;

import java.util.List;

public interface IOrderDetailService {

    List<OrderDetail> getAllOrderDetailByOrder(Order order);

}
