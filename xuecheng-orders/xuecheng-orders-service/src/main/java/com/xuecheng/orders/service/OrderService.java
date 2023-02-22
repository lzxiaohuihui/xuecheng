package com.xuecheng.orders.service;

import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.po.XcPayRecord;

public interface OrderService {

    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    XcPayRecord getPayRecordByPayno(String payNo);
}
