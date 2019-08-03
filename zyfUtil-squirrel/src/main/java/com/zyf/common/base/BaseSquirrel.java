package com.zyf.common.base;

import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.StateMachineConfiguration;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;
import org.squirrelframework.foundation.fsm.UntypedStateMachineBuilder;
import org.squirrelframework.foundation.fsm.impl.AbstractUntypedStateMachine;

import java.util.Map;

public abstract class BaseSquirrel<S, E, O> extends AbstractUntypedStateMachine {

    public UntypedStateMachineBuilder stateMachineBuilder;

    public BaseSquirrel() {
        stateMachineBuilder = StateMachineBuilderFactory.create(getClass());
        init(stateMachineBuilder);
    }

    protected abstract void init(UntypedStateMachineBuilder stateMachineBuilder);

    public void fire(S initialState, E event, O context) {
        UntypedStateMachine untypedStateMachine = stateMachineBuilder.newUntypedStateMachine(
                initialState,
                StateMachineConfiguration.create()
                        //暂时开启debug进行日志trace
                        .enableDebugMode(true)
                        .enableAutoStart(true)
        );
        untypedStateMachine.fire(event, context);
    }
}
