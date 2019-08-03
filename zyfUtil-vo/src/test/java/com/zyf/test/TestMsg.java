package com.zyf.test;

import cn.hutool.json.JSONUtil;
import com.zyf.vo.Msg;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class TestMsg {

    @Test
    public void testMsg(){
        Msg msg0 = Msg.ok().build();
        Msg msg1 = Msg.ok("消息", "数据");
        Msg msg2 = Msg.ok("消息");
        Msg msg3 = Msg.ok(new Object()); // 数据
        Msg msg4 = Msg.OK;
        Msg msg5 = Msg.error(500).msg("消息").build();
        Msg msg6 = Msg.error(500, "消息");
        Msg msg7 = Msg.error(500, "消息", "数据");
        Msg msg8 = Msg.error("消息", "数据");
        Msg msg9 = Msg.error("消息");
        Msg msg10 = Msg.ERROR;
        Msg.BodyBuilder msg11 = Msg.ok();

        log.info(JSONUtil.toJsonStr(msg0));
        log.info(JSONUtil.toJsonStr(msg1));
        log.info(JSONUtil.toJsonStr(msg2));
        log.info(JSONUtil.toJsonStr(msg3));
        log.info(JSONUtil.toJsonStr(msg4));
        log.info(JSONUtil.toJsonStr(msg5));
        log.info(JSONUtil.toJsonStr(msg6));
        log.info(JSONUtil.toJsonStr(msg7));
        log.info(JSONUtil.toJsonStr(msg8));
        log.info(JSONUtil.toJsonStr(msg9));
        log.info(JSONUtil.toJsonStr(msg10));
        log.info(JSONUtil.toJsonStr(msg11));
    }

}
