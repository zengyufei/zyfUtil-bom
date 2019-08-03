package com.zyf.utils;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BusUtil {

    // 管理同步事件
    static EventBus syncEventBus = new EventBus();

    // 管理异步事件
    static AsyncEventBus asyncEventBus = new AsyncEventBus(Executors.newCachedThreadPool());

    /**
     * 同步方法
     */
    public static <T> void postSync(T event) {
        log.debug("注册" + event.getClass().getName());
        syncEventBus.register(event);
        log.debug("同步调用 start " + event.getClass().getName());
        syncEventBus.post(event);
        log.debug("反注册" + event.getClass().getName());
        syncEventBus.unregister(event);
    }

    /**
     * 异步方法
     * @param event
     */
    public static <T> void postAsync(T event) {
        log.debug("注册" + event.getClass().getName());
        asyncEventBus.register(event);
        log.debug("异步调用 start " + event.getClass().getName());
        asyncEventBus.post(event);
        log.debug("反注册" + event.getClass().getName());
        asyncEventBus.unregister(event);
    }

}