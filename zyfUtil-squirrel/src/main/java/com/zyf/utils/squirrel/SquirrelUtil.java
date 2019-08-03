package com.zyf.utils.squirrel;

import com.zyf.common.base.BaseSquirrel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SquirrelUtil {

    private final static Map<String, BaseSquirrel> stateMachineBuilderMap = new ConcurrentHashMap<>();

    private SquirrelUtil() {
    }

    public static <T extends BaseSquirrel, S, E, O> void fire(Class<T> clazz, S initialState, E event, O context) {
        BaseSquirrel t = null;
        String clazzName = clazz.getName();
        if (stateMachineBuilderMap.containsKey(clazzName)) {
            t = stateMachineBuilderMap.get(clazzName);
        } else {
            try {
                t = clazz.newInstance();
                stateMachineBuilderMap.put(clazzName, t);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (t != null) {
            t.fire(initialState, event, context);
        }else{
            throw new RuntimeException("BaseSquirrel is Null.");
        }


    }

}
