package com.zyf.utils.stateMachine;

public class StateMachineUtil {

    public static <E, O, K> boolean change(Class<? extends AbstractStateMachineBuilder> stateMachineBuilder, E event, O order, K key) {
        try {
            return stateMachineBuilder.newInstance().change(event, order, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
