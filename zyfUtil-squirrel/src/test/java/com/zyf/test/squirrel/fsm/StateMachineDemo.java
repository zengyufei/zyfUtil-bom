package com.zyf.test.squirrel.fsm;

import com.zyf.common.base.BaseSquirrel;
import com.zyf.test.squirrel.domain.Order;
import com.zyf.test.squirrel.enums.OrderStatus;
import com.zyf.test.squirrel.enums.OrderStatusChangeEvent;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.builder.ExternalTransitionBuilder;

import java.util.HashMap;
import java.util.Map;

public class StateMachineDemo extends BaseSquirrel<OrderStatus, OrderStatusChangeEvent, Order> {

    // 模拟 dao 层
    public final static Map<Integer, Order> map = new HashMap<>();

    @Override
    protected void init(UntypedStateMachineBuilder stateMachineBuilder) {
        ActionDemo action = new ActionDemo();
        ExternalTransitionBuilder<UntypedStateMachine, Object, Object, Object> externalTransitionBuilder = stateMachineBuilder.externalTransition();

        // 来源状态: WAIT_PAYMENT, // 待支付
        // 触发事件：PAYED, // 支付
        // 目标状态: WAIT_DELIVER,  // 待发货
        externalTransitionBuilder.from(OrderStatus.WAIT_PAYMENT).to(OrderStatus.WAIT_DELIVER).on(OrderStatusChangeEvent.PAYED).perform(action);

        // 来源状态: WAIT_DELIVER,  // 待发货
        // 触发事件：DELIVERY, // 发货
        // 目标状态: WAIT_RECEIVE, // 待收货
        externalTransitionBuilder.from(OrderStatus.WAIT_DELIVER).to(OrderStatus.WAIT_RECEIVE).on(OrderStatusChangeEvent.DELIVERY).perform(action);

        // 来源状态: WAIT_RECEIVE, // 待收货
        // 触发事件：RECEIVED, // 确认收货
        // 目标状态: FINISH, // 订单结束（确认收货触发）
        externalTransitionBuilder.from(OrderStatus.WAIT_RECEIVE).to(OrderStatus.FINISH).on(OrderStatusChangeEvent.RECEIVED).perform(action);

        // 来源状态: FINISH, // 订单结束（确认收货触发）
        // 触发事件：REFUND // 用户退货（款）
        // 目标状态: CLOSED // 订单退款（退货事件触发）
        externalTransitionBuilder.from(OrderStatus.FINISH).to(OrderStatus.CLOSED).on(OrderStatusChangeEvent.REFUND).perform(action);
    }

    @Override
    protected void afterTransitionCompleted(Object fromState, Object toState, Object event, Object context) {
        if (context instanceof Order && toState instanceof OrderStatus) {
            Order stateMachineContext = (Order) context;
            OrderStatus targetStauts = (OrderStatus) toState;
            stateMachineContext.setOrderStatus(targetStauts);
            // 模拟 dao save()
            StateMachineDemo.map.put(stateMachineContext.getId(), stateMachineContext);
            System.out.println("afterTransitionCompleted from state: " + fromState + " to state: " + toState);
        } else {
            throw new RuntimeException("error2");
        }
    }


    @Override
    protected void afterTransitionCausedException(Object fromState, Object toState, Object event, Object context) {
        Throwable targeException = getLastException().getTargetException();
        if (targeException instanceof RuntimeException) {
            System.out.println("afterTransitionCausedException");
        } else {
            super.afterTransitionCausedException(fromState, toState, event, context);
        }
    }
}