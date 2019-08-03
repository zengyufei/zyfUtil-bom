package com.zyf.test;

import cn.hutool.core.util.StrUtil;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.zyf.utils.BusUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class TestBus {

    private final static CountDownLatch countDownLatch = new CountDownLatch(1);

    @Test
    public void testSyncbus() throws InterruptedException {
        BusUtil.postSync(new TestEvent("admin"));
        log.info("全部结束。");
    }

    @Test
    public void testAsyncbus() throws InterruptedException {
        BusUtil.postAsync(new TestEvent("admin"));
        //等待所有的子线程结束
        countDownLatch.await(); // 异步调用时 - 防止提前退出主线程
        log.info("全部结束。");
    }

    @Slf4j
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static class TestEvent {

        String value;

        @Subscribe
        @AllowConcurrentEvents // 异步调用时 - 线程安全
        public void method(TestEvent event) {
            log.debug("监听登录成功事件 start...");
            String value = event.getValue();
            if (StrUtil.isBlank(value)) {
                log.warn("value 不能为空");
                //线程结束时，将计时器减一
                countDownLatch.countDown();
                return;
            }
            log.info("登录成功监听事件");
            log.debug("监听登录成功事件 end...");
            //线程结束时，将计时器减一
            countDownLatch.countDown();
        }
    }

}
