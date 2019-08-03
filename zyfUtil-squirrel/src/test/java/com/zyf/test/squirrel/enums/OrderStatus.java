package com.zyf.test.squirrel.enums;

public enum OrderStatus {

    WAIT_PAYMENT, // 待支付
    WAIT_DELIVER,  // 待发货
    WAIT_RECEIVE, // 待收货
    FINISH, // 订单结束（确认收货触发）
    CLOSED // 订单退款（退货事件触发）

}
