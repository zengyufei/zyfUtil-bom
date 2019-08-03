package com.zyf.test.stateMachine;

import com.zyf.utils.stateMachine.StateMachineUtil;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestStateMachine {

    @Test
    public void testStateMachine() throws Exception {
        StateMachineBuilder stateMachineBuilder = new StateMachineBuilder();

        Order order = new Order(1);
        boolean change = stateMachineBuilder.change(OrderStatusChangeEvent.PAYED, order, order.getId());
        assertTrue(change);
    }

    @Test
    public void testStateMachineError() throws Exception {
        StateMachineBuilder stateMachineBuilder = new StateMachineBuilder();
        Order order = new Order(11);
        boolean change = stateMachineBuilder.change(OrderStatusChangeEvent.PAYED, order, order.getId());
        assertFalse(change);
    }

    @Test
    public void testStateMachineUtil() {
        Order order = new Order(1);
        boolean change = StateMachineUtil.change(StateMachineBuilder.class, OrderStatusChangeEvent.PAYED, order, order.getId());
        assertTrue(change);
    }

}
