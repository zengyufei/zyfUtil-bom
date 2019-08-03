package com.zyf.test.squirrel;

import cn.hutool.json.JSONUtil;
import com.zyf.test.squirrel.domain.Order;
import com.zyf.test.squirrel.enums.OrderStatus;
import com.zyf.test.squirrel.enums.OrderStatusChangeEvent;
import com.zyf.test.squirrel.fsm.StateMachineDemo;
import com.zyf.utils.squirrel.SquirrelUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

public class TestSquirrel {

    @Before
    public void setup() {
        // 模拟数据库
        StateMachineDemo.map.put(1, new Order(1, OrderStatus.WAIT_PAYMENT));
    }

    @Test
    public void testSquirrel() {
        StateMachineDemo testStateMachine = new StateMachineDemo();

        // 模拟 数据查询
        Order oldOrder = StateMachineDemo.map.get(1);

        testStateMachine.fire(oldOrder.getOrderStatus(), OrderStatusChangeEvent.PAYED, oldOrder);
        testStateMachine.fire(oldOrder.getOrderStatus(), OrderStatusChangeEvent.DELIVERY, oldOrder);

        // 模拟 数据查询
        Order newOrder = StateMachineDemo.map.get(1);
        System.out.println(JSONUtil.toJsonPrettyStr(newOrder));
    }

    @Test
    public void testSquirrelUtil() throws Exception {
        // 模拟 数据查询
        Order oldOrder = StateMachineDemo.map.get(1);

        SquirrelUtil.fire(StateMachineDemo.class, oldOrder.getOrderStatus(), OrderStatusChangeEvent.PAYED, oldOrder);
        SquirrelUtil.fire(StateMachineDemo.class, oldOrder.getOrderStatus(), OrderStatusChangeEvent.DELIVERY, oldOrder);

        // 模拟 数据查询
        Order newOrder = StateMachineDemo.map.get(1);
        System.out.println(JSONUtil.toJsonPrettyStr(newOrder));
    }

}
