package com.zyf.vo;

import cn.hutool.json.JSONUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 消息传输类。结果返回集。
 * 可用在 service 服务层或 controller 控制层
 * 快捷方式: ok 和 error
 * @author zengyufei
 * @since 1.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class Msg<T> {

    public final static Msg OK = Msg.ok().build();
    public final static Msg ERROR = Msg.error().build();

    /**
     * 成功标识：true or false
     */
    private boolean success = true;
    /**
     * 数据
     */
    private T data;
    /**
     * 信息，消息
     */
    private String message;
    /**
     * 错误代码
     */
    private int errorCode;

    public Msg(int errorCode) {
        this.errorCode = errorCode;
    }

    public Msg(String msg, T data) {
        this.message = msg;
        this.data = data;
    }

    public Msg(boolean success, String msg, T data) {
        this.success = success;
        this.message = msg;
        this.data = data;
    }

    public Msg(int errorCode, String msg, T data) {
        this.errorCode = errorCode;
        this.message = msg;
        this.data = data;
    }

    public Msg(boolean success, int errorCode, String msg, T data) {
        this.success = success;
        this.errorCode = errorCode;
        this.message = msg;
        this.data = data;
    }

    /* 快捷输出 start */
    public static BodyBuilder ok() {
        return code(true, 0);
    }

    public static Msg ok(Object data) {
        if (data instanceof Boolean) {
            return ok().build();
        } else if (data instanceof String) {
            return ok().msg(String.valueOf(data)).build();
        }
        BodyBuilder builder = ok();
        return builder.body(data);
    }

    public static Msg ok(Object... data) {
        BodyBuilder builder = ok();
        return builder.body(data);
    }

    public static String okJson(Object data) {
        return JSONUtil.toJsonStr(ok(data));
    }

    public static String okJson(Object... data) {
        return JSONUtil.toJsonStr(ok(data));
    }

    public static String errorJson(String errorMsg, Object data) {
        return JSONUtil.toJsonStr(error(errorMsg, data));
    }

    public static String errorJson(String errorMsg, Object... data) {
        return JSONUtil.toJsonStr(error(errorMsg, data));
    }

    public static Msg ok(String msg, Object data) {
        BodyBuilder builder = code(true, 0);
        return builder.msg(msg).body(data);
    }

    public static Msg ok(String msg, Object... data) {
        BodyBuilder builder = code(true, 0);
        return builder.msg(msg).body(data);
    }

    public static BodyBuilder error() {
        return code(false, 1000);
    }

    public static Msg error(Object data) {
        BodyBuilder builder = error(1000);
        return builder.body(data);
    }

    public static BodyBuilder error(int errorCode) {
        return code(false, errorCode);
    }

    public static Msg error(String msg) {
        BodyBuilder builder = error(1000);
        return builder.msg(msg).build();
    }

    public static Msg error(String msg, Object data) {
        BodyBuilder builder = error(1000);
        return builder.msg(msg).body(data);
    }

    public static Msg error(int errorCode, String msg, Object data) {
        BodyBuilder builder = error(errorCode);
        return builder.msg(msg).body(data);
    }

    public static Msg error(int errorCode, String msg) {
        BodyBuilder builder = error(errorCode);
        return builder.msg(msg).build();
    }
    /* 快捷输出 end */


    /* 构建输出 start */
    public static BodyBuilder code(boolean success, int errorCode) {
        return new DefaultBuilder(success, errorCode);
    }

    public static BodyBuilder code(boolean success) {
        return new DefaultBuilder(success);
    }
    /* 构建输出 end */

    @Getter
    private static class DefaultBuilder implements BodyBuilder {

        private boolean success;
        private int errorCode;
        private String message;

        private DefaultBuilder(boolean success) {
            this.success = success;
        }

        private DefaultBuilder(boolean success, int errorCode) {
            this.success = success;
            this.errorCode = errorCode;
        }

        public DefaultBuilder(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        @Override
        public Msg body(Object data) {
            Msg msg = new Msg();
            msg.success = this.success;
            msg.message = this.message;
            msg.errorCode = this.errorCode;
            if (data instanceof Number) {
                return new Msg(this.success, this.errorCode, this.message, data);
            }
            msg.data = data;
            if (msg.data == null) {
                msg.data = new Object();
            }
            return msg;
        }

        @Override
        public Msg body(Object... data) {
            Msg msg = new Msg();
            msg.success = this.success;
            msg.message = this.message;
            msg.errorCode = this.errorCode;
            msg.data = data;
            if (msg.data == null) {
                msg.data = new Object();
            }
            return msg;
        }

        @Override
        public BodyBuilder msg(String message) {
            this.message = message;
            return this;
        }

        @Override
        public Msg build() {
            return new Msg(this.success, this.errorCode, this.message, "");
        }

    }

    public interface BodyBuilder {

        Msg body(Object obj);

        Msg body(Object... obj);

        BodyBuilder msg(String message);

        Msg build();
    }

}
