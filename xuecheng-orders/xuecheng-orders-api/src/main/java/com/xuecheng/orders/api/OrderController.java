package com.xuecheng.orders.api;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.xuecheng.base.exception.XueChengException;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import com.xuecheng.orders.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Controller
public class OrderController {

    @Resource
    OrderService orderService;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;


    @ResponseBody
    @PostMapping("/generatepaycode")
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            XueChengException.cast("请登陆后继续选课");
        }
        PayRecordDto order = orderService.createOrder(user.getId(), addOrderDto);
        String qrCode = null;
        try {
            qrCode = new QRCodeUtil().createQRCode("http://10.16.43.244/api/orders/requestpay?payNo="+
                    order.getPayNo(), 200, 200);
        } catch (IOException e) {
            XueChengException.cast("生成二维码出错");
        }

        order.setQrcode(qrCode);

        return order;
    }

    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws IOException {
        //如果 payNo 不存在则提示重新发起支付
        XcPayRecord payRecord = orderService.getPayRecordByPayno(payNo);
        if(payRecord == null){
            XueChengException.cast("请重新点击支付获取二维码");
        }
        //构造 sdk 的客户端对象
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);//获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
        //        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        //告诉支付宝支付结果通知的地址
        // 没有公网ip G
        alipayRequest.setNotifyUrl("");//在公共参数中设置回跳和通知地址
                alipayRequest.setBizContent("{" +
                        " \"out_trade_no\":\""+payRecord.getPayNo()+"\"," +
                        " \"total_amount\":\""+payRecord.getTotalPrice()+"\"," +
                        " \"subject\":\""+payRecord.getOrderName()+"\"," +
                        " \"product_code\":\"QUICK_WAP_PAY\"" +
                        " }");//填充业务参数
        String form = "";
        try {
            //请求支付宝下单接口,发起 http 请求
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用 SDK 生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单 html 输出到页面
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();

    }
}
