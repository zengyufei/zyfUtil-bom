package com.zyf.utils.stateMachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;

/**
 * 父类状态机构建器
 */
@Slf4j
public abstract class AbstractStateMachineBuilder<S, E, O> {

    private final static String MESSAGE_PRIMARY_KEY = "AbstractStateMachineBuilder<S, E, O>_KEY";
    private final static String MESSAGE_VALUE_KEY = "AbstractStateMachineBuilder<S, E, O>_VALUE";

    protected abstract String getName();

    protected abstract void initStates(StateMachineStateConfigurer<S, E> stateConfigurer) throws Exception;

    protected abstract void initEvents(StateMachineTransitionConfigurer<S, E> transitionConfigurer) throws Exception;

    protected void listener(String stateMachineName, S source, S target, E event, Object key, Object value) {
    }

    protected void setData(Message<E> message, StateMachine<S, E> stateMachine) {
    }

    protected abstract S getCurrentState(StateMachine<S, E> stateMachine, O obj);

    protected abstract void save(StateMachine<S, E> stateMachine, O old, S newState);

    /**
     * 从持久化恢复到状态机
     */
    public boolean restore(StateMachine stateMachine, O obj) throws Exception {
        StateMachinePersister<S, E, O> persister = getPersister(stateMachine);
        persister.restore(stateMachine, obj);
        return stateMachine.hasStateMachineError();
    }

    public <K> boolean change(E event, O obj, K key) throws Exception {
        /*
        Class<?> typeArgument = ClassUtil.getTypeArgument(this.getClass(), 0);
        StateMachine<S, E> stateMachine = this.build(typeArgument.getSimpleName());
        */
        StateMachine<S, E> stateMachine = this.build(getName());
        String stateMachineName = stateMachine.getId();

        boolean errorFlag;
        try {
            // 创建流程
            stateMachine.start();

            // 从持久化恢复到状态机
            errorFlag = this.restore(stateMachine, obj);
            if (errorFlag) {
                log.error("【{}】【{}】从持久化恢复到状态机 失败，有错误", stateMachineName, key);
                return false;
            }
            log.info("【{}】【{}】从持久化恢复到状态机 状态：{}", stateMachineName, key, stateMachine.getState().getId());


            // 触发事件
            Message<E> message = MessageBuilder.withPayload(event)
                    .setHeader(MESSAGE_PRIMARY_KEY, key)
                    .setHeader(MESSAGE_VALUE_KEY, obj)
                    .build();
            boolean b1 = stateMachine.sendEvent(message);
            if (!b1) {
                log.error("【{}】【{}】触发 {} 事件 失败", stateMachineName, key, event.toString());
                return false;
            }

            errorFlag = stateMachine.hasStateMachineError();
            if (errorFlag) {
                log.error("【{}】【{}】触发 {} 事件 失败，有错误", stateMachineName, key, event.toString());
                return false;
            }
            log.info("【{}】【{}】触发 {} 事件 后的状态：{}", stateMachineName, key, event.toString(), stateMachine.getState().getId());

            // 持久化
            errorFlag = this.persist(stateMachine, obj);
            if (errorFlag) {
                log.error("【{}】【{}】持久化 失败，有错误", stateMachineName, key);
                return false;
            }
            log.info("【{}】【{}】持久化业务完毕.", stateMachineName, key);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        } finally {
            stateMachine.stop();
        }
        return false;
    }

    /**
     * 持久化状态机
     */
    private boolean persist(StateMachine stateMachine, O obj) throws Exception {
        StateMachinePersister<S, E, O> persister = getPersister(stateMachine);
        persister.persist(stateMachine, obj);
        return stateMachine.hasStateMachineError();
    }

    private StateMachinePersister<S, E, O> getPersister(StateMachine stateMachine) {
        StatePersist statePersist = new StatePersist(stateMachine);
        return new DefaultStateMachinePersister<>(statePersist);
    }

    /**
     * 构建状态机
     */
    public StateMachine<S, E> build() throws Exception {
        return build(null);
    }

    /**
     * 构建状态机
     */
    public StateMachine<S, E> build(String stateMachineName) throws Exception {
        StateMachineBuilder.Builder<S, E> builder = StateMachineBuilder.builder();

        builder.configureConfiguration()
                .withConfiguration()
                .machineId(stateMachineName);

        StateMachineStateConfigurer<S, E> stateConfigurer = builder.configureStates();
        initStates(stateConfigurer);

        StateMachineTransitionConfigurer<S, E> transitionConfigurer = builder.configureTransitions();
        initEvents(transitionConfigurer);

        StateMachine<S, E> stateMachine = builder.build();
        stateMachine.getStateMachineAccessor().doWithRegion(function -> function.addStateMachineInterceptor(new LocalStateMachineInterceptor()));


        return stateMachine;
    }

    class LocalStateMachineInterceptor extends StateMachineInterceptorAdapter<S, E> {

        @Override
        public void preStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition, StateMachine<S, E> stateMachine) {
            String stateMachineName = stateMachine.getId();
            State<S, E> transitionSource = transition.getSource();
            State<S, E> transitionTarget = transition.getTarget();
            S source = transitionSource.getId();
            S target = transitionTarget.getId();
            if (source != null && target != null) {
                E event = message.getPayload();
                MessageHeaders messageHeaders = message.getHeaders();
                Object key = messageHeaders.get(MESSAGE_PRIMARY_KEY);
                Object value = messageHeaders.get(MESSAGE_VALUE_KEY);
                listener(stateMachineName, source, target, event, key, value);
            } else {
                log.error("source or target is Null.");
            }
        }

        @Override
        public Message<E> preEvent(Message<E> message, StateMachine<S, E> stateMachine) {
            setData(message, stateMachine);
            return message;
        }

        @Override
        public Exception stateMachineError(StateMachine<S, E> stateMachine, Exception exception) {
            return exception;
        }

    }

    class StatePersist implements StateMachinePersist<S, E, O> {

        private StateMachine<S, E> stateMachine;

        StatePersist(StateMachine<S, E> stateMachine) {
            this.stateMachine = stateMachine;
        }

        @Override
        public StateMachineContext<S, E> read(O obj) {
            S s = getCurrentState(stateMachine, obj);
            if (s == null) {
                s = stateMachine.getInitialState().getId();
            }
            return new DefaultStateMachineContext<>(s, null, null, null, null, stateMachine.getId());
        }

        @Override
        public void write(StateMachineContext<S, E> stateMachineContext, O obj) {
            save(stateMachine, obj, stateMachineContext.getState());
        }

    }


}