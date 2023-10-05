package com.alicp.jetcache;

import org.springframework.cglib.core.ReflectUtils;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @author: zhangtong
 * @create: 2023/10/5 11:44 AM
 */
public class VirtualThreadUtil {
    public static ExecutorService createExecuteor(){
        ExecutorService executorService = null;
        try {
            Method method = ReflectUtils.findDeclaredMethod(java.util.concurrent.Executors.class, "newVirtualThreadPerTaskExecutor", new Class[]{});
            if (method != null) {
                System.out.println("use newVirtualThreadPerTaskExecutor");
                executorService = (ExecutorService) method.invoke(null);
            }
        }catch (Exception e){
            return null;
        }
        return executorService;
    }
}
