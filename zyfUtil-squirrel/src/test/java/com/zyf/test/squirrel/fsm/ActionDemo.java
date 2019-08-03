package com.zyf.test.squirrel.fsm;

import org.squirrelframework.foundation.fsm.UntypedAnonymousAction;
import org.squirrelframework.foundation.fsm.UntypedStateMachine;

public class ActionDemo extends UntypedAnonymousAction {

    @Override
    public void execute(Object from, Object to, Object event, Object context, UntypedStateMachine stateMachine) {
        System.out.println("execute");
    }

}