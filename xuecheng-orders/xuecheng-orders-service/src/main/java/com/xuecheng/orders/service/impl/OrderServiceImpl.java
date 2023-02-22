package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import com.xuecheng.orders.utils.IdWorkerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Resource
    XcOrdersMapper ordersMapper;

    @Resource
    XcOrdersGoodsMapper ordersGoodsMapper;

    @Resource
    XcPayRecordMapper payRecordMapper;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {
        // 添加商品订单
        XcOrders orders = saveXcOrders(userId, addOrderDto);
        // 添加支付交易记录
        XcPayRecord payRecord = createPayRecord(orders);

        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);

        return payRecordDto;
    }

    @Override
    public XcPayRecord getPayRecordByPayno(String payNo) {
        XcPayRecord xcPayRecord = payRecordMapper.selectOne(new
                LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
        return xcPayRecord;

    }

    @Transactional
    @Override
    public void saveAliPayStatus(PayStatusDto payStatusDto) {
        //支付结果
        String trade_status = payStatusDto.getTrade_status();
        if (trade_status.equals("TRADE_SUCCESS")) {
            //支付流水号
            String payNo = payStatusDto.getOut_trade_no();
            //查询支付流水
            XcPayRecord payRecord = getPayRecordByPayno(payNo);
            //支付金额变为分
            Float totalPrice = payRecord.getTotalPrice() * 100;
            Float total_amount = Float.parseFloat(payStatusDto.getTotal_amount()) *
                    100;
        //校验是否一致
            if (payRecord != null
                    && payStatusDto.getApp_id().equals(APP_ID)
                    && totalPrice.intValue() == total_amount.intValue()) {
                String status = payRecord.getStatus();
                if ("601001".equals(status)) {//未支付时进行处理
                    log.debug("更新支付结果,支付交易流水号:{},支付结果:{}", payNo,
                            trade_status);
                    XcPayRecord payRecord_u = new XcPayRecord();
                    payRecord_u.setStatus("601002");//支付成功
                    payRecord_u.setOutPayChannel("Alipay");
                    payRecord_u.setOutPayNo(payStatusDto.getTrade_no());//支付宝交易号
                    payRecord_u.setPaySuccessTime(LocalDateTime.now());//通知时间
                    int update1 = payRecordMapper.update(payRecord_u, new
                            LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
                    if (update1 > 0) {
                        log.info("收到支付通知,更新支付交易状态成功.付交易流水号:{},支付结果:{}", payNo, trade_status);
                    } else {
                        log.error("收到支付通知,更新支付交易状态失败.支付交易流水号:{},支付结果:{}", payNo, trade_status);
                    }
                    //关联的订单号
                    Long orderId = payRecord.getOrderId();
                    XcOrders orders = ordersMapper.selectById(orderId);
                    if (orders != null) {
                        XcOrders order_u = new XcOrders();
                        order_u.setStatus("600002");
                        int update = ordersMapper.update(order_u, new
                                LambdaQueryWrapper<XcOrders>().eq(XcOrders::getId, orderId));
                        if (update > 0) {
                            log.info("收到支付通知,更新订单状态成功.付交易流水号:{},支付结果:{},订单号:{},状态: {} ", payNo, trade_status, orderId, " 600002 ");
                        } else {
                            log.error("收到支付通知,更新订单状态失败.支付交易流水号:{},支付结果:{},订单号: {},状态: {} ", payNo, trade_status, orderId, " 600002 ");
                        }
                    }
                }
            }
        }


    }

    @Transactional
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto) {
        XcOrders orders = ordersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, addOrderDto.getOutBusinessId()));

        if (orders != null) {
            return orders;
        }

        orders = new XcOrders();
        long orderId = IdWorkerUtils.getInstance().nextId();
        orders.setId(orderId);
        orders.setTotalPrice(addOrderDto.getTotalPrice());
        orders.setCreateDate(LocalDateTime.now());
        orders.setStatus("600001");
        orders.setUserId(userId);
        orders.setOrderType(addOrderDto.getOrderType());
        orders.setOrderName(addOrderDto.getOrderName());
        orders.setOrderDetail(addOrderDto.getOrderDetail());
        orders.setOrderDescrip(addOrderDto.getOrderDescrip());
        orders.setOutBusinessId(addOrderDto.getOutBusinessId());//选课记录 id
        ordersMapper.insert(orders);

        String orderDetailJson = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoodsLists = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
        xcOrdersGoodsLists.forEach(goods -> {
            XcOrdersGoods xcOrdersGoods = new XcOrdersGoods();
            BeanUtils.copyProperties(goods, xcOrdersGoods);
            xcOrdersGoods.setOrderId(orderId);
            ordersGoodsMapper.insert(xcOrdersGoods);
        });

        return orders;


    }

    public XcPayRecord createPayRecord(XcOrders orders) {
        XcPayRecord payRecord = new XcPayRecord();
        long payNo = IdWorkerUtils.getInstance().nextId();
        payRecord.setPayNo(payNo);
        payRecord.setOrderId(orders.getId());//商品订单号
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus("601001");//未支付
        payRecord.setUserId(orders.getUserId());
        payRecordMapper.insert(payRecord);
        return payRecord;

    }
}
