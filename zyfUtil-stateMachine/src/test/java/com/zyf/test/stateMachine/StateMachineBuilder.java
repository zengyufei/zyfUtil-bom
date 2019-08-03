package com.zyf.test.stateMachine;

import com.zyf.utils.stateMachine.AbstractStateMachineBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试 状态机构建器
 */
@Slf4j
public class StateMachineBuilder extends AbstractStateMachineBuilder<OrderStatus, OrderStatusChangeEvent, Order> {

    // 模拟 dao 层
    private final Map<Integer, Order> map = new HashMap<Integer, Order>() {{
        put(1, new Order(1, OrderStatus.WAIT_PAYMENT));
    }};

    @Override
    protected String getName() {
        return "测试的状态机";
    }

    @Override
    protected void initStates(StateMachineStateConfigurer<OrderStatus, OrderStatusChangeEvent> stateConfigurer) throws Exception {
        stateConfigurer
                .withStates()
                .initial(OrderStatus.WAIT_PAYMENT)
                .states(EnumSet.allOf(OrderStatus.class));
    }

    @Override
    protected void initEvents(StateMachineTransitionConfigurer<OrderStatus, OrderStatusChangeEvent> transitionConfigurer) throws Exception {
        transitionConfigurer
                // 来源状态: WAIT_PAYMENT, // 待支付
                // 触发事件：PAYED, // 支付
                // 目标状态: WAIT_DELIVER,  // 待发货
                .withExternal()
                .source(OrderStatus.WAIT_PAYMENT).target(OrderStatus.WAIT_DELIVER)
                .event(OrderStatusChangeEvent.PAYED)
                .and()

                // 来源状态: WAIT_DELIVER,  // 待发货
                // 触发事件：DELIVERY, // 发货
                // 目标状态: WAIT_RECEIVE, // 待收货
                .withExternal()
                .source(OrderStatus.WAIT_DELIVER).target(OrderStatus.WAIT_RECEIVE)
                .event(OrderStatusChangeEvent.DELIVERY)
                .and()

                // 来源状态: WAIT_RECEIVE, // 待收货
                // 触发事件：RECEIVED, // 确认收货
                // 目标状态: FINISH, // 订单结束（确认收货触发）
                .withExternal()
                .source(OrderStatus.WAIT_RECEIVE).target(OrderStatus.FINISH)
                .event(OrderStatusChangeEvent.RECEIVED)
                .and()

                // 来源状态: FINISH, // 订单结束（确认收货触发）
                // 触发事件：REFUND // 用户退货（款）
                // 目标状态: CLOSED // 订单退款（退货事件触发）
                .withExternal()
                .source(OrderStatus.FINISH).target(OrderStatus.CLOSED)
                .event(OrderStatusChangeEvent.REFUND);
    }

    /**
     * 可以不实现
     */
    @Override
    protected void listener(String stateMachineName, OrderStatus source, OrderStatus target, OrderStatusChangeEvent event, Object key, Object value) {
        if (source == OrderStatus.WAIT_PAYMENT) {
            log.info("【{}】【{}】订单创建，待支付", stateMachineName, key);
        } else if (source == OrderStatus.WAIT_DELIVER) {
            log.info("【{}】【{}】用户完成支付，待发货", stateMachineName, key);
        } else if (source == OrderStatus.WAIT_RECEIVE) {
            log.info("【{}】【{}】已发货，待用户收货", stateMachineName, key);
        } else if (source == OrderStatus.FINISH) {
            log.info("【{}】【{}】用户已收货，订单完成", stateMachineName, key);
        } else if (source == OrderStatus.CLOSED) {
            log.info("【{}】【{}】用户退货，退款", stateMachineName, key);
        }
    }

    @Override
    protected OrderStatus getCurrentState(StateMachine<OrderStatus, OrderStatusChangeEvent> stateMachine, Order order) {
        Integer id = order.getId();
        try {
            Order o = map.get(id);
            if (o != null) {
                return o.getState();
            }
        } catch (Exception e) {
            String stateMachineName = stateMachine.getId();
            log.error("{} 获取 id = {} 当前状态失败，失败原因：{} ", stateMachineName, id, e.getMessage());
            stateMachine.setStateMachineError(e);
        }
        stateMachine.setStateMachineError(new RuntimeException("获取当前状态找不到 id = " + id + "的数据。"));
        return null;
    }

    @Override
    protected void save(StateMachine<OrderStatus, OrderStatusChangeEvent> stateMachine, Order old, OrderStatus newState) {
        String stateMachineName = stateMachine.getId();
        Integer oldOrderId = old.getId();
        Order o = map.get(oldOrderId);
        if (o != null) {
            String oldOrderState = o.getState().toString();
            try {
                o.setState(newState);
                map.put(oldOrderId, o); // 模拟 dao.save()
                log.info("{} 持久化 id = {} oldState= {} newState= {} ", stateMachineName, oldOrderId, oldOrderState, newState);
            } catch (Exception e) {
                log.error("{} 持久化 id = {} newState= {} 失败，失败原因：{} ", stateMachineName, oldOrderId, newState, e.getMessage());
                stateMachine.setStateMachineError(e);
            }
        } else {
            log.error("{} 持久化找不到 id = {} 的数据。", stateMachineName, oldOrderId);
            stateMachine.setStateMachineError(new RuntimeException(stateMachineName + " 找不到 id = " + oldOrderId + "的数据。"));
        }
    }

}

enum OrderStatus {
    WAIT_PAYMENT, // 待支付
    WAIT_DELIVER,  // 待发货
    WAIT_RECEIVE, // 待收货
    FINISH, // 订单结束（确认收货触发）
    CLOSED // 订单退款（退货事件触发）
}

enum OrderStatusChangeEvent {
    PAYED, // 支付
    DELIVERY, // 发货
    RECEIVED, // 确认收货
    REFUND // 用户退货（款）
}

/**
 * 实体类
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class Order {
    private Integer id;
    private OrderStatus state;

    public Order(Integer id) {
        this.id = id;
    }
}